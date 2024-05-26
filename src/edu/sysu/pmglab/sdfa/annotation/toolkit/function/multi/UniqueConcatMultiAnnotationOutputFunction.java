package edu.sysu.pmglab.sdfa.annotation.toolkit.function.multi;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.AnnotationOutputFunction;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 17:07
 * @description
 */
public class UniqueConcatMultiAnnotationOutputFunction extends AnnotationOutputFunction {
    static byte separator = ByteCode.COMMA;
    static VolumeByteStream cache = new VolumeByteStream();
    static CallableSet<ByteCode> uniqueFields = new CallableSet<>();

    @Override
    public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords) {
        uniqueFields.clear();
        for (IRecord record : relatedRefRecords) {
            cache.reset();
            for (int i = 0; i < fieldSize; i++) {
                cache.writeSafety((ByteCode) record.get(rawFieldIndexArray.get(i)));
                if (i != fieldSize - 1) {
                    cache.writeSafety(ByteCode.COMMA);
                }
            }
            uniqueFields.add(cache.toByteCode().asUnmodifiable());
        }
        cache.reset();
        for (ByteCode unique : uniqueFields) {
            cache.writeSafety(unique);
            cache.writeSafety(ByteCode.SEMICOLON);
        }
        return cache.toByteCode().asUnmodifiable();
    }
}
