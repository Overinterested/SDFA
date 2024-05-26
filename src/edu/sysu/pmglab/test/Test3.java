package edu.sysu.pmglab.test;

import edu.sysu.pmglab.sdfa.SDFReader;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-02 08:25
 * @description
 */
public class Test3 {
    public static void main(String[] args) throws IOException {
        SDFReader reader = new SDFReader("/Users/wenjiepeng/Desktop/tmp/Pacbio_winnowmap_NanoSV_NA12778.15x.vcf.gz.sdfa");
        reader.redirectCoordinate();
        reader.redirectSVFeature();
        reader.redirectSVFeaturesAndAnnotationFeature();
        reader.redirectAllFeatures();
    }
}
