package edu.sysu.pmglab.sdfa.annotation.toolkit.function.multi;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.AnnotationOutputFunction;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 16:22
 * @description
 */
public class ConcatMultiAnnotationOutputFunction extends AnnotationOutputFunction {
    static byte separator = ByteCode.COMMA;
    static VolumeByteStream cache = new VolumeByteStream();

    public ConcatMultiAnnotationOutputFunction() {

    }

    public ConcatMultiAnnotationOutputFunction(IntArray rawFieldIndexArray) {
        super(rawFieldIndexArray);
    }

    @Override
    public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords) {
        cache.reset();
        for (int i = 0; i < relatedRefRecords.size(); i++) {
            for (int j = 0; j < fieldSize; j++) {
                try {
                    cache.writeSafety((ByteCode) relatedRefRecords.get(i).get(rawFieldIndexArray.get(j)));
                } catch (Exception e) {
                    cache.writeSafety((ByteCode) relatedRefRecords.get(i).get(rawFieldIndexArray.get(j)));

                }
                if (j != fieldSize - 1) {
                    cache.writeSafety(separator);
                }
            }
            cache.writeSafety(ByteCode.SEMICOLON);
        }
        return cache.toByteCode().asUnmodifiable();
    }

    public static void setSeparator(byte separator) {
        ConcatMultiAnnotationOutputFunction.separator = separator;
    }
}
