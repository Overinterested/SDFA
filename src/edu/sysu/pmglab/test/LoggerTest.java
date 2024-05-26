package edu.sysu.pmglab.test;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-05-18 22:23
 * @description
 */
public class LoggerTest {
    private static final Logger logger = LoggerFactory.getLogger("1");
    public static void main(String[] args) {
        LogBackOptions.init();
        LogBackOptions.setLevel(Level.INFO);
        LogBackOptions.addFileAppender(
                "/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/1.txt",
                level -> level.isGreaterOrEqual(Level.ALL)
        );
        logger.trace("xxx");
    }
}
