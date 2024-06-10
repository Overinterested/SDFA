package edu.sysu.pmglab.test.concat;

import edu.sysu.pmglab.sdfa.toolkit.SDFConcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-06-08 03:33
 * @description
 */
public class ConcatUkbb {
    private static Logger logger = LoggerFactory.getLogger(ConcatUkbb.class);
    public static void main(String[] args) throws IOException {
        SDFConcat.of(
                "/Users/wenjiepeng/Desktop/SV/data/ukbb/concat/chr1_test",
                "/Users/wenjiepeng/Desktop/SV/data/ukbb/concat/chr1_concat"
                )
                .silent(false)
                .setLogger(logger)
                .submit();
    }
}
