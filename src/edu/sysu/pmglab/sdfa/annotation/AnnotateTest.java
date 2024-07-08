package edu.sysu.pmglab.sdfa.annotation;

import edu.sysu.pmglab.container.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-18 01:01
 * @description
 */
public class AnnotateTest {
    private static final Logger logger = LoggerFactory.getLogger(AnnotateTest.class);
    public static void main(String[] args) throws IOException {
        new SDFAAnnotator(
                new File("/Users/wenjiepeng/Desktop/tmp/sdf"),
                new File("/Users/wenjiepeng/Desktop/SDFA/annotation/multi_resource/frame.config"),
                new File("/Users/wenjiepeng/Desktop/tmp/sdf/output"),
                1
        ).setLogger(logger).submit();
    }
}
