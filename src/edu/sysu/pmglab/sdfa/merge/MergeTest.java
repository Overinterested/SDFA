package edu.sysu.pmglab.sdfa.merge;

import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.sdfa.command.SDFAEntry;
import edu.sysu.pmglab.sdfa.merge.cmo.PosSVMergeStrategy;
import edu.sysu.pmglab.sdfa.merge.cmo.SVCollectorMode;
import edu.sysu.pmglab.sdfa.merge.cmo.SVMergeMode;
import edu.sysu.pmglab.sdfa.merge.cmo.SVMergedOutputMode;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 22:43
 * @description
 */
public class MergeTest {
    public static void main(String[] args) throws IOException {
        long l = System.currentTimeMillis();
//        SDFAEntry.main(new String[]{"merge", "--threads 4", "--avg-pos",
//                "-dir /Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/cutesv_gz",
//                "-o /Users/wenjiepeng/Desktop/SDFA/merge/mean_sdfa"});
        PosSVMergeStrategy.outputMeanPosFunc = false;
        LogBackOptions.init();
        new SDFAMergeManager()
                .setLogger(LogBackOptions.getRootLogger())
                .setCollectorMode(SVCollectorMode.CHR_LEVEL)
                .setMergeMode(SVMergeMode.POS_LEVEL)
                .setOutputMode(SVMergedOutputMode.VCF_MODE)
                .setThread(4)
                .setInputDir("/Users/wenjiepeng/Desktop/PaperWriter/SV/SDFA/revise/version_1/data/LEL5_cuteSV")
                .setOutputDir("/Users/wenjiepeng/Desktop/PaperWriter/SV/SDFA/revise/version_1/data/LEL5_cuteSV/sdfa")
//                .setInputDir("/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_5md_PB")
//                .setOutputDir("/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_5md_PB/output")
                .submit(4);

        System.out.println(System.currentTimeMillis() - l);
    }
}
