package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.GeneFeatureAnnotationType;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.nagf.FeatureType;
import edu.sysu.pmglab.sdfa.nagf.NumericGeneFeature;
import edu.sysu.pmglab.sdfa.nagf.PopulationLevelNumericGeneFeature;
import edu.sysu.pmglab.sdfa.sv.idividual.SubjectManager;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-13 08:42
 * @description
 */
public class PopulationTestForMulti {
    public static void main(String[] args) throws IOException, InterruptedException {
        // load reference genome
        RefGeneManager of = RefGeneManager.of("/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/resource/hg19_refGene.ccf");
        GlobalResourceManager.getInstance().putResource(of);
        of.setAnnotationLevel(GeneFeatureAnnotationType.HGVS_GENE_LEVEL);
        of.load();
        // parse 46 samples
        File vcfDir = new File("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/cutesv2_output");
        File outputDir = new File("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/cutesv_ngf_46");
        SDFManager.of(vcfDir, outputDir).collectSDF();
        // init parameters
        SDFGlobalContig.Builder.getInstance().build();
        // annotate and output6t7
        BriefSVAnnotationManager.getInstance().initChromosomes();
        BriefSVAnnotationManager.getInstance().load(SDFManager.getInstance().getSdfReaderArray(), 1);
        of.annotateAll(0);
        BriefSVAnnotationManager.getInstance().toWriteMode();
        BriefSVAnnotationManager.getInstance().writeOut(new File("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/ngf_46"));
        // ngf
        // init parameters
        of.setAnnotationLevel(GeneFeatureAnnotationType.NAGF_GENE_LEVEL);
        of.load();
        NumericGeneFeature.initSubjectSize(SubjectManager.getInstance().numOfAllSubjects());
        PopulationLevelNumericGeneFeature populationLevelNumericGeneFeature = new PopulationLevelNumericGeneFeature(new File("/Users/wenjiepeng/Desktop/tmp/gene.txt"))
                .setFeatureLevel(FeatureType.GENE_LEVEL)
                .addSDFReaders(SDFManager.getInstance().getAnnotatedSDFArray())
                .setGeneResourceManager(of);
        populationLevelNumericGeneFeature.execute();
    }
}
