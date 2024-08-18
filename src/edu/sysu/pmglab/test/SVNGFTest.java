package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.GeneFeatureAnnotationType;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.nagf.FeatureType;
import edu.sysu.pmglab.sdfa.nagf.NumericGeneFeature;
import edu.sysu.pmglab.sdfa.nagf.SVLevelNumericGeneFeature;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-11 23:33
 * @description
 */
public class SVNGFTest {
    public static void main(String[] args) throws IOException {
        SDFManager.of(
                new File("/Users/wenjiepeng/Desktop/SV/tmp"),
                new File("/Users/wenjiepeng/Desktop/SV/tmp/newTest")
        ).collectSDF();
        // load reference genome
        RefGeneManager of = RefGeneManager.of("/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/resource/hg19_regGene.ccf");
        GlobalResourceManager.getInstance().putResource(of);
        // load SV to brief coordinate
        Array<SDFReader> sdfReaderArray = SDFManager.getInstance().getSdfReaderArray();
        File outputDir = new File("/Users/wenjiepeng/Desktop/tmp1");
        outputDir.mkdirs();
        for (SDFReader sdfReader : sdfReaderArray) {
            sdfReader.setFileID(0);
            SDFGlobalContig.reset();
            SDFGlobalContig.Builder.getInstance().addVCFContig(sdfReader.getContig()).build();
            Array<SDFReader> array = Array.wrap(new SDFReader[]{sdfReader});
            BriefSVAnnotationManager.getInstance().clear();
            BriefSVAnnotationManager.getInstance().initChromosomes().load(array, 1);
            of.setAnnotationLevel(GeneFeatureAnnotationType.HGVS_GENE_LEVEL).load().annotateAll(0);
            BriefSVAnnotationManager.getInstance().toWriteMode();
            of.setAnnotationLevel(GeneFeatureAnnotationType.NGF_GENE_LEVEL).load();
            BriefSVAnnotationManager.getInstance().writeOut(outputDir);
            NumericGeneFeature.initSubjectSize(1);
            File subFile = outputDir.getSubFile(sdfReader.getFilePath().changeExtension("sdfa", "sdf").getName());
            SVLevelNumericGeneFeature svLevelNumericGeneFeature = new SVLevelNumericGeneFeature()
                    .setFeatureLevel(FeatureType.RNA_LEVEL)
                    .setOutputFile(new File("/Users/wenjiepeng/Desktop/tmp/" + subFile.getName() + ".ngf.txt"))
                    .setSDFReader(new SDFReader(subFile))
                    .setGeneResourceManager(of);
            svLevelNumericGeneFeature.execute();
        }

        SDFReader sdfReader = new SDFReader("/Users/wenjiepeng/Desktop/SV/tmp/未命名文件夹/HG002_HiFi_aligned_GRCh38_winnowmap.cuteSV2.vcf.sdf");
        sdfReader.close();
        SDFGlobalContig.Builder.getInstance().addVCFContig(sdfReader.getContig()).build();
        Array<SDFReader> array = Array.wrap(new SDFReader[]{sdfReader});
        BriefSVAnnotationManager.getInstance().initChromosomes().load(array, 1);
        of.setAnnotationLevel(GeneFeatureAnnotationType.HGVS_GENE_LEVEL).load().annotateAll(0);
        BriefSVAnnotationManager.getInstance().toWriteMode();
        of.setAnnotationLevel(GeneFeatureAnnotationType.NGF_GENE_LEVEL).load();
        BriefSVAnnotationManager.getInstance().writeOut(new File("/Users/wenjiepeng/Desktop/tmp"));
        NumericGeneFeature.initSubjectSize(1);
        SVLevelNumericGeneFeature svLevelNumericGeneFeature = new SVLevelNumericGeneFeature()
                .setFeatureLevel(FeatureType.RNA_LEVEL)
                .setOutputFile(new File("/Users/wenjiepeng/Desktop/tmp/SVLevel_NGF_1.txt"))
                .setSDFReader(new SDFReader("/Users/wenjiepeng/Desktop/tmp/HG002_HiFi_aligned_GRCh38_winnowmap.cuteSV2.vcf.sdfa"))
                .setGeneResourceManager(of);
        svLevelNumericGeneFeature.execute();
    }
}
