package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.GeneFeatureAnnotationType;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.toolkit.GlobalVCFContigConvertor;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-29 01:34
 * @description
 */
public class NGFTest {
    private static final Logger logger = LoggerFactory.getLogger(NGFTest.class);
    public static void main(String[] args) throws IOException {
        SDFManager.of(new File("/Users/wenjiepeng/Desktop/SV/tmp/annotation"),new File("/Users/wenjiepeng/Desktop/SV/tmp/annotation"))
                .setLogger(logger)
                .collectSDF();
        //region sv level
        GlobalResourceManager.getInstance().putResource(
                new RefGeneManager(
                        new File("/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/resource/hg19_regGene.ccf"),
                        "genome"
                ).setAnnotationLevel(GeneFeatureAnnotationType.NGF_RNA_LEVEL)
        );

        SDFReader sdfReader = null;
        sdfReader.close();
        GlobalVCFContigConvertor.Builder.getInstance().addVCFContig(sdfReader.getContig());
        GlobalVCFContigConvertor.Builder.getInstance().build();
        BriefSVAnnotationManager.getInstance().load(Array.wrap(new SDFReader[]{sdfReader}),1);
        //endregion
    }
}
