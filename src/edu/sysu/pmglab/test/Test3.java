package edu.sysu.pmglab.test;

import edu.sysu.pmglab.sdfa.command.SDFAEntry;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-02 08:25
 * @description
 */
public class Test3 {
    public static void main(String[] args) throws IOException {
        SDFAEntry.main("--line-sort vcf2sdf -dir /Users/wenjiepeng/Downloads/CCS_CHM13_lra_svim_CHM13等5个文件 -o /Users/wenjiepeng/Downloads/CCS_CHM13_lra_svim_CHM13等5个文件 -c 0".split(" "));
    }
}
