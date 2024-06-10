package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.container.File;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 11:13
 * @description
 */
public class SDF2VCF {
    boolean loadFormat;
    final File sdfFile;
    final File vcfOutputFile;

    public SDF2VCF(Object sdfFile, Object vcfOutput) {
        this.sdfFile = new File(sdfFile.toString());
        this.vcfOutputFile = new File(vcfOutput.toString());
    }

    public File convert(){
        return vcfOutputFile;
    }
}
