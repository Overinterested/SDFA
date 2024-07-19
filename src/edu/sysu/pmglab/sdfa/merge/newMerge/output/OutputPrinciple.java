package edu.sysu.pmglab.sdfa.merge.newMerge.output;

import edu.sysu.pmglab.container.File;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-07-09 01:47
 * @description
 */
public interface OutputPrinciple {
    void initHeader() throws IOException;

    void finishWrite() throws IOException;

    OutputPrinciple setOutputFile(File file);


}
