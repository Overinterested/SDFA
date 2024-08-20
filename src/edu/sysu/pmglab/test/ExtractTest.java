package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.easytools.annotation.KeyMapFieldType;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-03-27 22:35
 * @description
 */
public class ExtractTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String vcf = "/Users/wenjiepeng/projects/sdfa_latest/test/resource/build/HG002_HiFi_aligned_GRCh38_winnowmap.sniffles.vcf";
        ByteCode output = new ByteCode("/Users/wenjiepeng/projects/sdfa_latest/test/resource/build/output");
        new VCF2SDF(vcf,output).setEncodeMode(0).setFileID(1).setFilterEncode(new KeyMapFieldType()).convert();
    }
}
