package edu.sysu.pmglab.sdfa.annotation.base;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.IntArray;

/**
 * @author Wenjie Peng
 * @create 2023-03-09 16:44
 * @description
 */
public abstract class AnnotFeature {

    /**
     * 将对象编码为字节码，并返回字节码对象
     *
     * @return 返回编码后的字节码对象
     */
    abstract public ByteCode encode();


    /**
     * 判断是否为空
     *
     * @return 如果为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * 获取注释个数的方法
     *
     * @return 返回个数，目前返回0
     */
    public int getSize() {
        return 0;
    }

    public IntArray getLineIndexes() {
        return null;
    }

}
