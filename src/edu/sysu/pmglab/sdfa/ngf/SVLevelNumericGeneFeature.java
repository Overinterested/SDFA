package edu.sysu.pmglab.sdfa.ngf;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.genome.RefGene;
import edu.sysu.pmglab.sdfa.genome.RefRNA;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-08 05:57
 * @description
 */
public class SVLevelNumericGeneFeature {
    File outputFile;
    SDFReader sdfReader;
    FeatureType featureLevel = FeatureType.GENE_LEVEL;
    boolean containCoverage = true;
    RefGeneManager geneResourceManager;
    Array<SVRelatedGeneIndexRecord> SVQueue = new Array<>();
    Array<NumericGeneFeature> numericGeneFeatureArray = new Array<>();

    public SVLevelNumericGeneFeature() {

    }

    public void execute() throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(outputFile, FileStream.DEFAULT_WRITER);
        initHeader(fs);
        for (Chromosome chromosome : SDFGlobalContig.support()) {
            loadGenes(chromosome);
            if (numericGeneFeatureArray.isEmpty()) {
                continue;
            }
            loadSVAndRelatedGenes(chromosome);
            for (SVRelatedGeneIndexRecord svRelatedGeneIndexRecord : SVQueue) {
                boolean containLine = false;
                Array<NumericGeneFeature> relatedGeneArray = svRelatedGeneIndexRecord.getNumericGeneFeatureArray();
                cache.writeSafety(svRelatedGeneIndexRecord.sv.getChr().getName());
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(svRelatedGeneIndexRecord.sv.getIndexOfFile()));
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(svRelatedGeneIndexRecord.sv.getPos()));
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(svRelatedGeneIndexRecord.sv.getEnd()));
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(svRelatedGeneIndexRecord.sv.getLength()));
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(svRelatedGeneIndexRecord.sv.getSVTypeName());
                cache.writeSafety(ByteCode.TAB);
                for (NumericGeneFeature numericGeneFeature : relatedGeneArray) {
                    numericGeneFeature.putSV(svRelatedGeneIndexRecord.sv);
                    if (featureLevel.equals(FeatureType.RNA_LEVEL)) {
                        for (RefRNA refRNA : numericGeneFeature.refGene.getRNAList()) {
                            boolean exist = numericGeneFeature.buildRNALevelNGFForOneSV(refRNA);
                            if (exist) {
                                containLine = true;
                                numericGeneFeature.writeRNALevelForOne(refRNA, cache, containCoverage);
                                cache.writeSafety(ByteCode.TAB);
                                /** TODO consider the complex SV
                                 * if(isComplex){
                                 *      sv.addProperty(cache.toUnmodifiedByteCode());
                                 *      csvAssemble.putSV(sv);
                                 *      cache.reset();
                                 * }else{
                                 *      cache.writeSafety(ByteCode.NEWLINE);
                                 *      fs.write(cache);
                                 *      cache.reset();
                                 * }
                                 */
                                fs.write(cache);
                                cache.reset();
                            }
                        }
                    } else if (featureLevel.equals(FeatureType.GENE_LEVEL)) {
                        boolean exist = numericGeneFeature.buildGeneLevelNGFForOneSV();
                        if (exist) {
                            containLine = true;
                            numericGeneFeature.writeGeneLevelForOne(cache, containCoverage);
                            /** TODO consider the complex SV
                             * if(isComplex){
                             *      sv.addProperty(cache.toUnmodifiedByteCode());
                             *      csvAssemble.putSV(sv);
                             *      cache.reset();
                             * }else{
                             *      cache.writeSafety(ByteCode.NEWLINE);
                             *      fs.write(cache);
                             *      cache.reset();
                             * }
                             */
                            cache.writeSafety(ByteCode.TAB);
                            fs.write(cache);
                            cache.reset();
                        }
                    }
                }
                cache.reset();
                if (containLine) {
                    fs.write(ByteCode.NEWLINE);
                }
            }
        }
        cache.close();
        fs.close();
    }

    public void loadGenes(Chromosome chromosome) {
        numericGeneFeatureArray.clear();
        AbstractResourceManager resource = GlobalResourceManager.getInstance().getResourceByIndex(0);
        if (resource instanceof RefGeneManager) {
            this.geneResourceManager = (RefGeneManager) resource;
        }
        Array<RefGene> refGeneOfChromosome = geneResourceManager.getFullGeneOfChr(chromosome);
        if (refGeneOfChromosome == null) {
            return;
        }
        for (RefGene refGene : refGeneOfChromosome) {
            numericGeneFeatureArray.add(NumericGeneFeature.load(refGene));
        }
    }

    public void loadSVAndRelatedGenes(Chromosome chromosome) throws IOException {
        UnifiedSV sv;
        SVQueue.clear();
        sdfReader.redirectSVFeaturesAndAnnotationFeatureWithGty();
        sdfReader.limitChrBlock(chromosome);
        while ((sv = sdfReader.read()) != null) {
            if (sv.getLength() > 1000000 || (sv.getEnd() - sv.getPos()) > 1000000) {
                continue;
            }
            ByteCode geneIndexArrayEncode = sv.getProperty().get(0);
            if (!geneIndexArrayEncode.equals(ByteCode.EMPTY)) {
                int[] relatedGeneIndexArray = (int[]) FieldType.int32Array.decode(geneIndexArrayEncode);
                SVQueue.add(SVRelatedGeneIndexRecord.of(sv, relatedGeneIndexArray, numericGeneFeatureArray));
            }
        }
    }

    public SVLevelNumericGeneFeature setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public SVLevelNumericGeneFeature setSDFReader(SDFReader reader) {
        this.sdfReader = reader;
        return this;
    }

    public SVLevelNumericGeneFeature setGeneResourceManager(RefGeneManager geneResourceManager) {
        this.geneResourceManager = geneResourceManager;
        return this;
    }

    private void initHeader(FileStream fs) throws IOException {
        fs.write(new byte[]{ByteCode.NUMBER_SIGN, ByteCode.C, ByteCode.H, ByteCode.R});
        fs.write(ByteCode.TAB);
        fs.write(new byte[]{ByteCode.I, ByteCode.D});
        fs.write(ByteCode.TAB);
        fs.write(new byte[]{ByteCode.P, ByteCode.O, ByteCode.S});
        fs.write(ByteCode.TAB);
        fs.write(new byte[]{ByteCode.E, ByteCode.N, ByteCode.D});
        fs.write(ByteCode.TAB);
        fs.write(new byte[]{ByteCode.L, ByteCode.E, ByteCode.N});
        fs.write(ByteCode.TAB);
        fs.write(new byte[]{ByteCode.T, ByteCode.Y, ByteCode.P, ByteCode.E});
        fs.write(ByteCode.TAB);
        if (featureLevel.equals(FeatureType.RNA_LEVEL)) {
            fs.write(new byte[]{ByteCode.R, ByteCode.N, ByteCode.A, ByteCode.N, ByteCode.UNDERLINE, ByteCode.G, ByteCode.F, ByteCode.s});
        } else {
            fs.write(new byte[]{ByteCode.G, ByteCode.E, ByteCode.N, ByteCode.E, ByteCode.UNDERLINE, ByteCode.N, ByteCode.G, ByteCode.F, ByteCode.s});
        }
        fs.write(ByteCode.NEWLINE);
    }

    public SVLevelNumericGeneFeature containCoverage(boolean containCoverage) {
        this.containCoverage = containCoverage;
        return this;
    }

    public SVLevelNumericGeneFeature setFeatureLevel(FeatureType featureLevel) {
        this.featureLevel = featureLevel;
        return this;
    }
}
