package edu.sysu.pmglab.sdfa.framework.collector;

/**
 * @author Wenjie Peng
 * @create 2024-03-30 00:44
 * @description
 */
public abstract class SVQueue {
    abstract public void reset();

    abstract public SVQueue popAll();
}
