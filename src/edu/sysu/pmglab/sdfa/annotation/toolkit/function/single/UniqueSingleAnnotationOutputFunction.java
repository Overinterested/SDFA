package edu.sysu.pmglab.sdfa.annotation.toolkit.function.single;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 15:46
 * @description
 */
public class UniqueSingleAnnotationOutputFunction extends SingleAnnotationOutputFunction {
    static VolumeByteStream cache = new VolumeByteStream();
    static CallableSet<ByteCode> uniqueFields = new CallableSet<>();

    public UniqueSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
        super(rawFieldIndexArray);
    }

    @Override
    public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords, int rawFieldIndex) {
        uniqueFields.clear();
        for (IRecord record : relatedRefRecords) {
            uniqueFields.add(record.get(rawFieldIndex));
        }
        for (ByteCode annotation : uniqueFields) {
            cache.writeSafety(annotation);
            cache.writeSafety(ByteCode.SEMICOLON);
        }
        return cache.toByteCode();
    }
}
