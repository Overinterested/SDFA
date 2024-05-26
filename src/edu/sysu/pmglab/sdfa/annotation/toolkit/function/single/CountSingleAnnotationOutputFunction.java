package edu.sysu.pmglab.sdfa.annotation.toolkit.function.single;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.easytools.ValueUtils;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 15:37
 * @description
 */
public class CountSingleAnnotationOutputFunction extends SingleAnnotationOutputFunction{


    public CountSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
        super(rawFieldIndexArray);
    }

    @Override
    public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords, int rawFieldIndex) {
        return new ByteCode(ValueUtils.Value2Text.int2bytes(relatedRefRecords.size()));
    }
}
