package edu.sysu.pmglab.sdfa.merge;

import edu.sysu.pmglab.sdfa.command.SDFAEntry;
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
        SDFAEntry.main(new String[]{"merge ", "--threads 4 ",
                "-dir /Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/cutesv_gz ",
                "-o /Users/wenjiepeng/Desktop/SDFA/merge/test"});
        new SDFAMergeManager()
                .setCollectorMode(SVCollectorMode.CHR_LEVEL)
                .setMergeMode(SVMergeMode.POS_LEVEL)
                .setOutputMode(SVMergedOutputMode.VCF_MODE)
                .setThread(4)
                .setInputDir("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/cutesv_gz")
                .setOutputDir("/Users/wenjiepeng/Desktop/SDFA/merge/test")
//                .setInputDir("/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_5md_PB")
//                .setOutputDir("/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_5md_PB/output")
                .submit(4);

        System.out.println(System.currentTimeMillis() - l);
    }
}
