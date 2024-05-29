package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-04-03 06:28
 * @description
 */
public class SDFConcatProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger(SDFConcatProgram.class);
    protected SDFConcatProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {

    }

}
