package edu.sysu.pmglab.sdfa.annotation;

import edu.sysu.pmglab.container.File;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-18 01:01
 * @description
 */
public class AnnotateTest {
    public static void main(String[] args) throws IOException {
        new SDFAAnnotator(
                new File("/Users/wenjiepeng/Desktop/SDFA/annotation/sniffles"),
                new File("/Users/wenjiepeng/Desktop/SDFA/annotation/annotation.config"),
                new File("/Users/wenjiepeng/Desktop/SDFA/annotation/sniffles"),
                1
        ).submit();
    }
}
