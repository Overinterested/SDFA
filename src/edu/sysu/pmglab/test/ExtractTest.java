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
        String vcf = "/Users/wenjiepeng/Desktop/tmp/extract/ONT_CHM13_ngmlr_pbsv_CHM13.vcf";
        ByteCode output = new ByteCode("/Users/wenjiepeng/Desktop/tmp/extract/1.sdf");
        new VCF2SDF(vcf,output).setEncodeMode(2).setFileID(1).setFilterEncode(new KeyMapFieldType()).convert();
    }
}
