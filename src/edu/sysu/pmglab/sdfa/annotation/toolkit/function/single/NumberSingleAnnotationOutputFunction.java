package edu.sysu.pmglab.sdfa.annotation.toolkit.function.single;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.FloatArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.easytools.ValueUtils;

/**
 * @author Wenjie Peng
 * @create 2023-12-03 15:50
 * @description
 */
public abstract class NumberSingleAnnotationOutputFunction extends SingleAnnotationOutputFunction {
    static int defaultDecimal = 3;
    static FloatArray fieldArray = new FloatArray();

    public NumberSingleAnnotationOutputFunction() {

    }

    public NumberSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
        super(rawFieldIndexArray);
    }

    @Override
    public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords, int rawFieldIndex) {
        fieldArray.clear();
        int size = relatedRefRecords.size();
        for (int i = 0; i < size; i++) {
            ByteCode field = relatedRefRecords.get(i).get(rawFieldIndex);
            try {
                fieldArray.add(field.toFloat());
            } catch (NumberFormatException e) {
                // default 0
                fieldArray.add(0f);
            }
        }
        return new ByteCode(ValueUtils.Value2Text.double2bytes(calc(fieldArray), defaultDecimal));
    }

    abstract public double calc(FloatArray fieldArray);

    public static class MaxNumberSingleAnnotationOutputFunction extends NumberSingleAnnotationOutputFunction {
        public MaxNumberSingleAnnotationOutputFunction(){

        }
        public MaxNumberSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
            super(rawFieldIndexArray);
        }

        @Override
        public double calc(FloatArray fieldArray) {
            return fieldArray.max();
        }
    }

    public static class MinNumberSingleAnnotationOutputFunction extends NumberSingleAnnotationOutputFunction {
        public MinNumberSingleAnnotationOutputFunction(){

        }
        public MinNumberSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
            super(rawFieldIndexArray);
        }

        @Override
        public double calc(FloatArray fieldArray) {
            return fieldArray.min();
        }
    }

    public static class MeanNumberSingleAnnotationOutputFunction extends NumberSingleAnnotationOutputFunction {
        public MeanNumberSingleAnnotationOutputFunction(){

        }
        public MeanNumberSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
            super(rawFieldIndexArray);
        }

        @Override
        public double calc(FloatArray fieldArray) {
            return fieldArray.mean();
        }
    }

    public static class SumNumberSingleAnnotationOutputFunction extends NumberSingleAnnotationOutputFunction {
        public SumNumberSingleAnnotationOutputFunction(){

        }
        public SumNumberSingleAnnotationOutputFunction(IntArray rawFieldIndexArray) {
            super(rawFieldIndexArray);
        }

        @Override
        public double calc(FloatArray fieldArray) {
            return fieldArray.sum();
        }
    }

}
