package edu.sysu.pmglab.sdfa.nagf;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.sdfa.genome.RefGene;
import edu.sysu.pmglab.sdfa.genome.RefRNA;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-19 07:21
 * @description
 */
public class NumericGeneFeature {
    RefGene refGene;
    private static int subjectSize;
    Array<UnifiedSV> relatedSVRecord;
    private static float AFCutOff = -1f;
    static NumericTranscriptFeature SVLevel = new NumericTranscriptFeature();
    private static Array<NumericTranscriptFeature> subjectOfRNALevelNAGFArray;
    private static final byte NONE_GENOTYPE = SVGenotype.of(".", ".").getBegCode();
    private static final byte HOMOZYGOUS_GENOTYPE = SVGenotype.of("0", "0").getBegCode();

    private NumericGeneFeature() {
        relatedSVRecord = new Array<>();
    }

    public Array<UnifiedSV> getRelatedSVRecord() {
        return relatedSVRecord;
    }

    public boolean containRelatedSV() {
        return relatedSVRecord != null && !relatedSVRecord.isEmpty();
    }

    public boolean buildGeneLevelNAGF() {
        initNAGF();
        boolean hasNAGF = false;
        Array<RefRNA> rnaList = refGene.getRNAList();
        Array<NumericTranscriptFeature> subjectOfGeneLevelNAGF = new Array<>(subjectSize);
        for (int i = 0; i < subjectSize; i++) {
            subjectOfGeneLevelNAGF.add(new NumericTranscriptFeature());
        }
        for (RefRNA refRNA : rnaList) {
            boolean itemFlag = buildRNALevelNAGF(refRNA);
            if (!itemFlag) {
                continue;
            }
            hasNAGF = true;
            for (int i = 0; i < subjectOfGeneLevelNAGF.getCapacity(); i++) {
                subjectOfGeneLevelNAGF.get(i).mergeInGene(subjectOfRNALevelNAGFArray.get(i));
            }
        }
        boolean filtered = false;
        if (hasNAGF) {
            for (int i = 0; i < subjectOfRNALevelNAGFArray.size(); i++) {
                NumericTranscriptFeature tmp = subjectOfGeneLevelNAGF.get(i);
                filtered = filtered | NumericTranscriptFeature.filter(tmp);
                subjectOfRNALevelNAGFArray.set(i, tmp);
            }
        }
        return hasNAGF && filtered;
    }

    public boolean writeGeneLevel(VolumeByteStream cache, boolean containCoverage) {
        cache.writeSafety(refGene.getChr().getName());
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(refGene.getGeneName());
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(refGene.getGeneStartPos()));
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(refGene.getGeneEndPos()));
        cache.writeSafety(ByteCode.TAB);
        return write(cache, containCoverage);
    }

    public void writeRNALevelForOne(RefRNA refRNA, VolumeByteStream cache, boolean containCoverage) {
        cache.writeSafety(refRNA.getRNAName());
        cache.writeSafety(ByteCode.COLON);
        if (SVLevel.getRnaFeatureValue() < 0) {
            cache.writeSafety(ByteCode.MINUS);
        }
        cache.writeSafety(ValueUtils.Value2Text.byte2bytes(SVLevel.getAbsRnaFeature()));
        if (containCoverage) {
            cache.writeSafety(ByteCode.COLON);
            SVLevel.writeCoverage(cache);
        }
    }

    public void writeGeneLevelForOne(VolumeByteStream cache, boolean containCoverage) {
        cache.writeSafety(refGene.getGeneName());
        cache.writeSafety(ByteCode.COLON);
        if (SVLevel.getRnaFeatureValue() < 0) {
            cache.writeSafety(ByteCode.MINUS);
        }
        cache.writeSafety(ValueUtils.Value2Text.byte2bytes(SVLevel.getAbsRnaFeature()));
        if (containCoverage) {
            cache.writeSafety(ByteCode.COLON);
            SVLevel.writeCoverage(cache);
        }
    }

    public boolean writeRNALevel(RefRNA refRNA, VolumeByteStream cache, boolean containCoverage) {
        cache.writeSafety(refGene.getChr().getName());
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(refGene.getGeneName());
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(refRNA.getRNAName());
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(refRNA.getStartPos()));
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(refRNA.getEndPos()));
        cache.writeSafety(ByteCode.TAB);
        return write(cache, containCoverage);
    }

    private boolean write(VolumeByteStream cache, boolean containCoverage) {
        int nonNAGFCount = 0;
        for (int i = 0; i < subjectSize; i++) {
            NumericTranscriptFeature item = subjectOfRNALevelNAGFArray.get(i);
            if (!item.parsed) {
                cache.writeSafety(ByteCode.PERIOD);
            } else {
                nonNAGFCount++;
                if (item.getRnaFeatureValue() < 0) {
                    cache.writeSafety(ByteCode.MINUS);
                }
                cache.writeSafety(ValueUtils.Value2Text.byte2bytes(item.getAbsRnaFeature()));
                if (containCoverage) {
                    item.writeCoverage(cache);
                }
            }
            if (i != subjectSize - 1) {
                cache.writeSafety(ByteCode.TAB);
            }
        }
        boolean AFFlag = (nonNAGFCount / (float) subjectSize) < AFCutOff;
        if (AFFlag) {
            cache.reset();
            return false;
        }
        return true;
    }

    public boolean buildRNALevelNAGFForOneSV(RefRNA refRNA) {
        initOneNAGF();
        UnifiedSV sv = relatedSVRecord.get(0);
        if (sv.getPos() >= refRNA.getEndPos() + RefRNA.downstreamDis) {
            return false;
        }
        int end = sv.getEnd() == -1 ? sv.getPos() + 1 : sv.getEnd();
        if (end <= refRNA.getStartPos() - RefRNA.upstreamDis) {
            return false;
        }
        SVLevel.mergeInRNA(refRNA.calcNAGF(sv));
        return true;
    }

    public boolean buildGeneLevelNAGFForOneSV() {
        boolean geneLevelExist = false;
        NumericTranscriptFeature geneLevelOfNAGF = new NumericTranscriptFeature();
        for (RefRNA refRNA : refGene.getRNAList()) {
            boolean changed = buildRNALevelNAGFForOneSV(refRNA);
            if (changed) {
                geneLevelExist = true;
                geneLevelOfNAGF.mergeInGene(SVLevel);
            }
        }
        if (geneLevelExist) {
            SVLevel = geneLevelOfNAGF;
            geneLevelExist = NumericTranscriptFeature.filter(SVLevel);
        }
        return geneLevelExist;
    }

    public boolean buildRNALevelNAGF(RefRNA refRNA) {
        initNAGF();
        boolean initNAGF = false;
        for (UnifiedSV sv : relatedSVRecord) {
            if (sv.getPos() >= refRNA.getEndPos() + RefRNA.downstreamDis) {
                continue;
            }
            int end = sv.getEnd() == -1 ? sv.getPos() + 1 : sv.getEnd();
            if (end <= refRNA.getStartPos() - RefRNA.upstreamDis) {
                continue;
            }
            initNAGF = true;
            NumericTranscriptFeature tmpNAGFRes = refRNA.calcNAGF(sv);
            int fileID = sv.getFileID();
            SVGenotype[] genotypes = sv.getGenotypes().getGenotypes();
            if (genotypes == null || genotypes.length == 0) {
                subjectOfRNALevelNAGFArray.get(fileID).mergeInRNA(tmpNAGFRes);
                continue;
            }
            for (int i = 0; i < genotypes.length; i++) {
                byte begCode = genotypes[i].getBegCode();
                if (begCode == NONE_GENOTYPE || begCode == HOMOZYGOUS_GENOTYPE) {
                    continue;
                }
                subjectOfRNALevelNAGFArray.get(fileID + i).mergeInRNA(tmpNAGFRes);
            }
        }
        //region cycle the memory of used RNA
        if (initNAGF) {
            refRNA.dropNAGF();
            return true;
        }
        return false;
        //endregion
    }

    public static NumericGeneFeature load(RefGene refGene) {
        NumericGeneFeature res = new NumericGeneFeature();
        res.refGene = refGene;
        return res;
    }

    public void putSV(UnifiedSV relatedSV) {
        this.relatedSVRecord.add(relatedSV);
    }

    public static void initSubjectSize(int subjectSize) {
        NumericGeneFeature.subjectSize = subjectSize;
        subjectOfRNALevelNAGFArray = new Array<>(subjectSize);
        for (int i = 0; i < subjectSize; i++) {
            subjectOfRNALevelNAGFArray.add(new NumericTranscriptFeature());
        }
    }

    private void initNAGF() {
        relatedSVRecord.sort(UnifiedSV::compareTo);
        for (NumericTranscriptFeature subjectNAGF : subjectOfRNALevelNAGFArray) {
            subjectNAGF.reset();
        }
    }

    private void initOneNAGF() {
        SVLevel.reset();
    }

    public void clearAll() {
        refGene = null;
        relatedSVRecord.clear();
    }

    public static void setAFCutOff(float AFCutOff) {
        NumericGeneFeature.AFCutOff = AFCutOff;
    }
}
