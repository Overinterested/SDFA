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
    private static Array<NumericTranscriptFeature> subjectOfRNALevelNGFArray;
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

    public boolean buildGeneLevelNGF() {
        initNGF();
        boolean hasNGF = false;
        Array<RefRNA> rnaList = refGene.getRNAList();
        Array<NumericTranscriptFeature> subjectOfGeneLevelNGF = new Array<>(subjectSize);
        for (int i = 0; i < subjectSize; i++) {
            subjectOfGeneLevelNGF.add(new NumericTranscriptFeature());
        }
        for (RefRNA refRNA : rnaList) {
            boolean itemFlag = buildRNALevelNGF(refRNA);
            if (!itemFlag) {
                continue;
            }
            hasNGF = true;
            for (int i = 0; i < subjectOfGeneLevelNGF.getCapacity(); i++) {
                subjectOfGeneLevelNGF.get(i).mergeInGene(subjectOfRNALevelNGFArray.get(i));
            }
        }
        boolean filtered = false;
        if (hasNGF) {
            for (int i = 0; i < subjectOfRNALevelNGFArray.size(); i++) {
                NumericTranscriptFeature tmp = subjectOfGeneLevelNGF.get(i);
                filtered = filtered | NumericTranscriptFeature.filter(tmp);
                subjectOfRNALevelNGFArray.set(i, tmp);
            }
        }
        return hasNGF && filtered;
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
        int nonNGFCount = 0;
        for (int i = 0; i < subjectSize; i++) {
            NumericTranscriptFeature item = subjectOfRNALevelNGFArray.get(i);
            if (!item.parsed) {
                cache.writeSafety(ByteCode.PERIOD);
            } else {
                nonNGFCount++;
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
        boolean AFFlag = (nonNGFCount / (float) subjectSize) < AFCutOff;
        if (AFFlag) {
            cache.reset();
            return false;
        }
        return true;
    }

    public boolean buildRNALevelNGFForOneSV(RefRNA refRNA) {
        initOneNGF();
        UnifiedSV sv = relatedSVRecord.get(0);
        if (sv.getPos() >= refRNA.getEndPos() + RefRNA.downstreamDis) {
            return false;
        }
        int end = sv.getEnd() == -1 ? sv.getPos() + 1 : sv.getEnd();
        if (end <= refRNA.getStartPos() - RefRNA.upstreamDis) {
            return false;
        }
        SVLevel.mergeInRNA(refRNA.calcNGF(sv));
        return true;
    }

    public boolean buildGeneLevelNGFForOneSV() {
        boolean geneLevelExist = false;
        NumericTranscriptFeature geneLevelOfNGF = new NumericTranscriptFeature();
        for (RefRNA refRNA : refGene.getRNAList()) {
            boolean changed = buildRNALevelNGFForOneSV(refRNA);
            if (changed) {
                geneLevelExist = true;
                geneLevelOfNGF.mergeInGene(SVLevel);
            }
        }
        if (geneLevelExist) {
            SVLevel = geneLevelOfNGF;
            geneLevelExist = NumericTranscriptFeature.filter(SVLevel);
        }
        return geneLevelExist;
    }

    public boolean buildRNALevelNGF(RefRNA refRNA) {
        initNGF();
        boolean initNGF = false;
        for (UnifiedSV sv : relatedSVRecord) {
            if (sv.getPos() >= refRNA.getEndPos() + RefRNA.downstreamDis) {
                continue;
            }
            int end = sv.getEnd() == -1 ? sv.getPos() + 1 : sv.getEnd();
            if (end <= refRNA.getStartPos() - RefRNA.upstreamDis) {
                continue;
            }
            initNGF = true;
            NumericTranscriptFeature tmpNGFRes = refRNA.calcNGF(sv);
            int fileID = sv.getFileID();
            SVGenotype[] genotypes = sv.getGenotypes().getGenotypes();
            if (genotypes == null || genotypes.length == 0) {
                subjectOfRNALevelNGFArray.get(fileID).mergeInRNA(tmpNGFRes);
                continue;
            }
            for (int i = 0; i < genotypes.length; i++) {
                byte begCode = genotypes[i].getBegCode();
                if (begCode == NONE_GENOTYPE || begCode == HOMOZYGOUS_GENOTYPE) {
                    continue;
                }
                subjectOfRNALevelNGFArray.get(fileID + i).mergeInRNA(tmpNGFRes);
            }
        }
        //region cycle the memory of used RNA
        if (initNGF) {
            refRNA.dropNGF();
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
        subjectOfRNALevelNGFArray = new Array<>(subjectSize);
        for (int i = 0; i < subjectSize; i++) {
            subjectOfRNALevelNGFArray.add(new NumericTranscriptFeature());
        }
    }

    private void initNGF() {
        relatedSVRecord.sort(UnifiedSV::compareTo);
        for (NumericTranscriptFeature subjectNGF : subjectOfRNALevelNGFArray) {
            subjectNGF.reset();
        }
    }

    private void initOneNGF() {
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
