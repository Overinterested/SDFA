package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.GeneFeatureAnnotationType;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.nagf.FeatureType;
import edu.sysu.pmglab.sdfa.nagf.NumericGeneFeature;
import edu.sysu.pmglab.sdfa.nagf.PopulationLevelNumericGeneFeature;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-10 01:42
 * @description
 */
public class PopulationTest {
    public static void main(String[] args) throws IOException {
        RefGeneManager of = RefGeneManager.of("/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/resource/hg19_regGene.ccf");
        GlobalResourceManager.getInstance().putResource(of);
        SDFReader sdfReader = new SDFReader("/Users/wenjiepeng/Desktop/SV/data/public/sdf/Pacbio_winnowmap_NanoSV_NA12778.15x.vcf.gz.sdf");
        sdfReader.close();
        SDFGlobalContig.Builder.getInstance().addVCFContig(sdfReader.getContig());
        SDFGlobalContig.Builder.getInstance().build();
        SDFManager.of(
                new File("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/托尔斯泰"),
                new File("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/ngf_46"))
                .collectSDF();
        BriefSVAnnotationManager.getInstance().initChromosomes();
        BriefSVAnnotationManager.getInstance().load(SDFManager.getInstance().getSdfReaderArray(), 1);
        of.setAnnotationLevel(GeneFeatureAnnotationType.HGVS_RNA_LEVEL);
        of.load();
        of.annotateAll(0);
        BriefSVAnnotationManager.getInstance().toWriteMode();
        of.setAnnotationLevel(GeneFeatureAnnotationType.NAGF_GENE_LEVEL);
        of.load();
        BriefSVAnnotationManager.getInstance().writeOut(new File("/Users/wenjiepeng/Desktop/tmp"));
        NumericGeneFeature.initSubjectSize(1);
        PopulationLevelNumericGeneFeature populationLevelNumericGeneFeature = new PopulationLevelNumericGeneFeature(new File("/Users/wenjiepeng/Desktop/tmp/gene.txt"))
                .setFeatureLevel(FeatureType.GENE_LEVEL)
                .addSDFReader(new SDFReader("/Users/wenjiepeng/Desktop/tmp/Pacbio_winnowmap_NanoSV_NA12778.15x.vcf.gz.sdfa"))
                .setGeneResourceManager(of);
        populationLevelNumericGeneFeature.execute();
    }
}
