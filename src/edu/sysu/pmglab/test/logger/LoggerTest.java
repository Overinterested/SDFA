package edu.sysu.pmglab.test.logger;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-04-25 00:30
 * @description
 */
public class LoggerTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    public static void main(String[] args) {
        LogBackOptions.init();
        LogBackOptions.addConsoleAppender("1",
                level -> level.isGreaterOrEqual(Level.INFO)
        );
        LogBackOptions.addConsoleAppender("123123",level -> level.isGreaterOrEqual(Level.INFO));
        LogBackOptions.addFileAppender("/Users/wenjiepeng/Desktop/SDFA/test/extract/test.log",level -> level.isGreaterOrEqual(Level.TRACE));
        logger.info("11111");
        logger.info("11111");
        logger.trace("22222");

    }
}
