package edu.sysu.pmglab.sdfa.annotation.toolkit.function;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.multi.ConcatMultiAnnotationOutputFunction;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.multi.UniqueConcatMultiAnnotationOutputFunction;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.single.ConcatSingleAnnotationOutputFunction;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.single.CountSingleAnnotationOutputFunction;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.single.NumberSingleAnnotationOutputFunction;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.single.UniqueSingleAnnotationOutputFunction;

import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2023-12-04 16:56
 * @description
 */
public class AnnotationFunctionSummary {
    public static HashMap<String, Class<? extends AnnotationOutputFunction>> functionSummary = new HashMap<>();

    static {
        functionSummary.put("concat", ConcatSingleAnnotationOutputFunction.class);
        functionSummary.put("concatN", ConcatMultiAnnotationOutputFunction.class);
        functionSummary.put("concatUnique", UniqueConcatMultiAnnotationOutputFunction.class);
        functionSummary.put("min", NumberSingleAnnotationOutputFunction.MinNumberSingleAnnotationOutputFunction.class);
        functionSummary.put("max", NumberSingleAnnotationOutputFunction.MaxNumberSingleAnnotationOutputFunction.class);
        functionSummary.put("mean", NumberSingleAnnotationOutputFunction.MeanNumberSingleAnnotationOutputFunction.class);
        functionSummary.put("sum", NumberSingleAnnotationOutputFunction.SumNumberSingleAnnotationOutputFunction.class);
        functionSummary.put("unique", UniqueSingleAnnotationOutputFunction.class);
        functionSummary.put("count", CountSingleAnnotationOutputFunction.class);
    }

    public static Class<? extends AnnotationOutputFunction> getFunction(Object opt) {
        String lowerCase = opt.toString();
        return functionSummary.get(lowerCase);
    }


    public static void add(Object function) {
        if (function instanceof Class) {
            Class<?> fClass = (Class<?>) function;
            if (AnnotationOutputFunction.class.isAssignableFrom(fClass)) {
                Class<? extends AnnotationOutputFunction> classInstance = (Class<? extends AnnotationOutputFunction>) function;
                String className = classInstance.getSimpleName();
                if (className.length() == 0){
                    throw new UnsupportedOperationException("Only implementation classes are supported!");
                }
                if (functionSummary.containsKey(className)) {
                    throw new UnsupportedOperationException(className + " has existed!");
                } else {
                    functionSummary.put(className, classInstance);
                }
            }
        } else if (function instanceof AnnotationOutputFunction) {
            AnnotationOutputFunction f = (AnnotationOutputFunction) function;
            String className = f.getClass().getSimpleName();
            if (className.length() == 0){
                throw new UnsupportedOperationException("Only implementation classes are supported!");
            }
            if (functionSummary.containsKey(className)) {
                throw new UnsupportedOperationException(className + " has existed!");
            } else {
                functionSummary.put(className, f.getClass());
            }
        }
    }

    public static void main(String[] args) {
        add(new A());
        add(new AnnotationOutputFunction() {
            @Override
            public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords) {
                return null;
            }
        });
        add(new AnnotationOutputFunction() {
            @Override
            public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords) {
                return null;
            }
        }.getClass());

    }
    static class A extends AnnotationOutputFunction{

        @Override
        public ByteCode unsafeGetOutputCol(Array<IRecord> relatedRefRecords) {
            return null;
        }
    }
}
