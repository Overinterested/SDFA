package edu.sysu.pmglab.sdfa.annotation.toolkit.function.single;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 15:40
 * @description
 */
public class ConcatSingleAnnotationOutputFunction extends SingleAnnotationOutputFunction {
    static VolumeByteStream cache = new VolumeByteStream();

    public ConcatSingleAnnotationOutputFunction() {

    }

    public ConcatSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
        super(rawFieldIndexArray);
    }

    @Override
    public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords, int rawFieldIndex) {
        cache.reset();
        int size = relatedRefRecords.size();
        for (int i = 0; i < size; i++) {
            try {
                cache.writeSafety((ByteCode) relatedRefRecords.get(i).get(rawFieldIndex));
            } catch (ClassCastException e) {
                cache.writeSafety(relatedRefRecords.get(i).get(rawFieldIndex).toString());
            }
            cache.writeSafety(ByteCode.SEMICOLON);
        }
        return cache.toByteCode().asUnmodifiable();
    }
}
