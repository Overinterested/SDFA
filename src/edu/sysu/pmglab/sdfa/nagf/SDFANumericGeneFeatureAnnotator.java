package edu.sysu.pmglab.sdfa.nagf;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.annotation.preprocess.GTF2AnnotateResource;
import edu.sysu.pmglab.sdfa.annotation.preprocess.GFF2AnnotateResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * @author Wenjie Peng
 * @create 2024-04-08 02:55
 * @description
 */
public class SDFANumericGeneFeatureAnnotator {
    File inputDir;
    File outputDir;
    File genomeCCFFile;
    FeatureType featureType;
    final ResearchType researchType;
    Array<SDFReader> sdfReaderArray = new Array<>();
    private static final Logger logger = LoggerFactory.getLogger("SDFA NGF - Command Line");

    private SDFANumericGeneFeatureAnnotator(FeatureType featureType, ResearchType researchType) {
        this.featureType = featureType;
        this.researchType = researchType;
    }

    public static SDFANumericGeneFeatureAnnotator of(FeatureType featureType, ResearchType researchType) {
        return new SDFANumericGeneFeatureAnnotator(featureType, researchType);
    }

    public SDFANumericGeneFeatureAnnotator setSVDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public SDFANumericGeneFeatureAnnotator setGenomeFile(File genomeFile) {
        this.genomeCCFFile = genomeFile;
        return this;
    }

    public void convert() throws IOException {
        load();
        if (researchType.equals(ResearchType.SV_LEVEL)) {
            new SVLevelNumericGeneFeature()
                    .setFeatureLevel(featureType)
                    .setSDFReader(sdfReaderArray.get(0))
                    .setOutputFile(outputDir)
                    .setGeneResourceManager((RefGeneManager) GlobalResourceManager.getInstance().getResourceByIndex(0))
                    .execute();
        } else if (researchType.equals(ResearchType.SAMPLE_LEVEL)) {
            new PopulationLevelNumericGeneFeature(new File("/Users/wenjiepeng/Desktop/tmp/gene.txt"))
                    .setFeatureLevel(FeatureType.GENE_LEVEL)
                    .addSDFReader(new SDFReader("/Users/wenjiepeng/Desktop/tmp/Pacbio_winnowmap_NanoSV_NA12778.15x.vcf.gz.sdfa"))
                    .setGeneResourceManager((RefGeneManager) GlobalResourceManager.getInstance().getResourceByIndex(0))
                    .execute();
        }

    }

    /**
     * preload the reader array of sdf files and the reader of genome file
     *
     * @throws IOException return IO Exception
     */
    private void load() throws IOException {
        Array<File> sdfFileArray = FileTool.getFilesFromDir(inputDir, ".sdf");
        for (File file : sdfFileArray) {
            SDFReader sdfReader;
            try {
                sdfReader = new SDFReader(file);
                sdfReaderArray.add(sdfReader);
                sdfReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Array<File> vcfFileArray = FileTool.getFilesFromDir(inputDir, "vcf", "vcf.gz", "vcf.bgz");

        if (genomeCCFFile.withExtension("ccf")) {
            return;
        }
        if (genomeCCFFile.withExtension("gff", "gff.gz", "gff.gbz")) {
            new GFF2AnnotateResource(genomeCCFFile, outputDir.getSubFile(genomeCCFFile.getName()).addExtension(".ccf"))
                    .setResource(genomeCCFFile.getName())
                    .convert();
        } else if (genomeCCFFile.withExtension("gtf", "gtf.gz", "gtf.bgz")) {
            new GTF2AnnotateResource(genomeCCFFile, outputDir.getSubFile(genomeCCFFile.getName()).addExtension(".ccf"))
                    .setResource(genomeCCFFile.getName())
                    .convert();
        } else {
            throw new UnsupportedOperationException("The input genome file does not meet the requirements(.gtf, .gff)");
        }

    }
}
