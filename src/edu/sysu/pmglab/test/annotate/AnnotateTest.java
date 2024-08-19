package edu.sysu.pmglab.test.annotate;

import edu.sysu.pmglab.sdfa.command.SDFAEntry;

/**
 * @author Wenjie Peng
 * @create 2024-08-15 20:27
 * @description
 */
public class AnnotateTest {
    public static void main(String[] args) {
        SDFAEntry.main(new String[]{
                "annotate", "--annot-config",
                "/Users/wenjiepeng/Desktop/tmp/sdfa_test/sdf-toolkit/sdfa-annotate/annotation.config",
                "-dir", "/Users/wenjiepeng/Desktop/tmp/sdfa_test/sdf-toolkit/sdfa-annotate",
                "-o", "/Users/wenjiepeng/Desktop/tmp/sdfa_test/sdf_builder"
        });
    }
}
