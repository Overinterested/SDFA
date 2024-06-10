package edu.sysu.pmglab.sdfa.ngf;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.genome.RefGene;
import edu.sysu.pmglab.sdfa.genome.RefRNA;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.idividual.Subject;
import edu.sysu.pmglab.sdfa.sv.idividual.SubjectManager;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Wenjie Peng
 * @create 2024-04-08 06:26
 * @description
 */
public class PopulationLevelNumericGeneFeature {
    final File outputFile;
    FeatureType featureLevel;
    boolean outputCoverage = true;
    Array<SDFReader> sdfReaderArray;
    RefGeneManager geneResourceManager;
    Array<NumericGeneFeature> geneFeatureArray = new Array<>(true);


    public PopulationLevelNumericGeneFeature(File outputFile) {
        this.outputFile = outputFile;
        this.sdfReaderArray = new Array<>();
    }

    public void execute() throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(outputFile, FileStream.DEFAULT_WRITER);
        initHeader(fs);
        CallableSet<Chromosome> contigSupport = SDFGlobalContig.support();
        ProcessBar bar = new ProcessBar(contigSupport.size())
                .setHeader("Speed for calculating chromosome ngf")
                .setUnit("chr")
                .start();
        for (Chromosome chromosome : contigSupport) {
            loadGenes(chromosome);
            if (geneFeatureArray.isEmpty()) {
                bar.addProcessed(1);
                continue;
            }
            loadSVAndCollectRelatedGene(chromosome);
            while (!geneFeatureArray.isEmpty()) {
                NumericGeneFeature numericGeneFeature = geneFeatureArray.popFirst();
                if (numericGeneFeature.containRelatedSV()) {
                    if (featureLevel.equals(FeatureType.RNA_LEVEL)) {
                        RefGene refGene = numericGeneFeature.refGene;
                        for (RefRNA refRNA : refGene.getRNAList()) {
                            boolean overlap = numericGeneFeature.buildRNALevelNGF(refRNA);
                            if (!overlap) {
                                numericGeneFeature.clearAll();
                                continue;
                            }
                            boolean flag = numericGeneFeature.writeRNALevel(refRNA, cache, outputCoverage);
                            if (flag) {
                                cache.writeSafety(ByteCode.NEWLINE);
                                fs.write(cache);
                            }
                            cache.reset();
                        }
                    } else if (featureLevel.equals(FeatureType.GENE_LEVEL)) {
                        boolean hasNGF = numericGeneFeature.buildGeneLevelNGF();
                        if (!hasNGF) {
                            numericGeneFeature.clearAll();
                            continue;
                        }
                        if (numericGeneFeature.writeGeneLevel(cache, outputCoverage)) {
                            cache.writeSafety(ByteCode.NEWLINE);
                            fs.write(cache);
                        }
                        cache.reset();
                    }
                }
                numericGeneFeature.clearAll();
            }
            bar.addProcessed(1);
        }
        bar.setFinish();
        cache.close();
        fs.close();
    }

    private void initHeader(FileStream fs) throws IOException {
        fs.write(new byte[]{ByteCode.NUMBER_SIGN, ByteCode.C, ByteCode.H, ByteCode.R, ByteCode.TAB});
        fs.write(new byte[]{ByteCode.G, ByteCode.E, ByteCode.N, ByteCode.E, ByteCode.TAB});
        fs.write("GenePos\t");
        fs.write("GeneEnd\t");
        CallableSet<Subject> indexableSubjects = SubjectManager.getInstance().getIndexableSubjects();
        for (int i = 0; i < indexableSubjects.size(); i++) {
            fs.write(indexableSubjects.getByIndex(i).getName());
            if (i != indexableSubjects.size() - 1) {
                fs.write(ByteCode.TAB);
            } else {
                fs.write(ByteCode.NEWLINE);
            }
        }
    }

    public void loadGenes(Chromosome chromosome) {
        geneFeatureArray.clear();
        AbstractResourceManager resource = GlobalResourceManager.getInstance().getResourceByIndex(0);
        if (resource instanceof RefGeneManager) {
            this.geneResourceManager = (RefGeneManager) resource;
        }
        Array<RefGene> refGeneOfChromosome = geneResourceManager.getFullGeneOfChr(chromosome);
        if (refGeneOfChromosome == null) {
            return;
        }
        for (RefGene refGene : refGeneOfChromosome) {
            geneFeatureArray.add(NumericGeneFeature.load(refGene));
        }
    }

    public void loadSVAndCollectRelatedGene(Chromosome chromosome) throws IOException {
        for (SDFReader sdfReader : sdfReaderArray) {
            sdfReader.redirectSVFeaturesAndAnnotationFeatureWithGty();
            boolean successLimit = sdfReader.limitChrBlock(chromosome);
            if (!successLimit) {
                return;
            }
            UnifiedSV sv;
            while ((sv = sdfReader.read()) != null) {
                if (sv.getLength() > 1000000) {
                    continue;
                }
                ByteCode geneIndexArrayEncode = sv.getProperty().get(0);
                if (!geneIndexArrayEncode.equals(ByteCode.EMPTY)) {
                    int[] relatedGeneIndexArray = (int[]) FieldType.int32Array.decode(geneIndexArrayEncode);
                    for (int refGeneIndex : relatedGeneIndexArray) {
                        geneFeatureArray.get(refGeneIndex).putSV(sv);
                    }
                }
                sv.setProperty(null);
            }
        }
    }

    public PopulationLevelNumericGeneFeature setFeatureLevel(FeatureType featureLevel) {
        this.featureLevel = featureLevel;
        return this;
    }

    public PopulationLevelNumericGeneFeature outputCoverage(boolean outputCoverage) {
        this.outputCoverage = outputCoverage;
        return this;
    }

    public PopulationLevelNumericGeneFeature addSDFReaders(Collection<SDFReader> sdfReaderCollection) {
        this.sdfReaderArray.addAll(sdfReaderCollection);
        return this;
    }

    public PopulationLevelNumericGeneFeature addSDFReader(SDFReader reader) {
        this.sdfReaderArray.add(reader);
        return this;
    }

    public PopulationLevelNumericGeneFeature setGeneResourceManager(RefGeneManager geneResourceManager) {
        this.geneResourceManager = geneResourceManager;
        return this;
    }
}
