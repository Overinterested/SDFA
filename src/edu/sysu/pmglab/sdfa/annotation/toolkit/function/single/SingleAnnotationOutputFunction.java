package edu.sysu.pmglab.sdfa.annotation.toolkit.function.single;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.AnnotationOutputFunction;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 15:31
 * @description
 */
public abstract class SingleAnnotationOutputFunction extends AnnotationOutputFunction {
    public SingleAnnotationOutputFunction(){

    }
    public SingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
        super(rawFieldIndexArray);
    }

    @Override
    public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords) {
        return unsafeGetOutputCol(relatedRefRecords, rawFieldIndexArray.get(0));
    }

    abstract public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords, int rawFieldIndex);
}
