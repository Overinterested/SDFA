package edu.sysu.pmglab.sdfa.annotation.toolkit.function;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 15:22
 * @description
 */
public abstract class AnnotationOutputFunction {
    public int fieldSize;
    Lock lock = new ReentrantLock();
    public IntArray rawFieldIndexArray;

    public AnnotationOutputFunction() {

    }

    public AnnotationOutputFunction(IntArray rawFieldIndexArray) {
        this.fieldSize = rawFieldIndexArray.size();
        this.rawFieldIndexArray = rawFieldIndexArray;
    }

    abstract public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords);

    public ByteCode safeGetOutputCol(Array<IRecord> relatedRefRecords) {
        lock.lock();
        ByteCode res = unsafeGetOutputCol(relatedRefRecords);
        lock.unlock();
        return res;
    }

    public AnnotationOutputFunction setRawFieldIndexArray(IntArray rawFieldIndexArray) {
        this.fieldSize = rawFieldIndexArray.size();
        this.rawFieldIndexArray = rawFieldIndexArray;
        return this;
    }
}
