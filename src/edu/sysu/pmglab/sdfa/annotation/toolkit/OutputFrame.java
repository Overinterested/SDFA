package edu.sysu.pmglab.sdfa.annotation.toolkit;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.AnnotationOutputFunction;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.multi.ConcatMultiAnnotationOutputFunction;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.single.ConcatSingleAnnotationOutputFunction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * @author Wenjie Peng
 * @create 2024-04-03 23:47
 * @description
 */
public class OutputFrame {
    boolean loadIndex;
    boolean parsed = false;
    Array<IntArray> rawIndexOfOutputCol;
    CallableSet<ByteCode> rawColNameArray;
    Array<ByteCodeArray> rawNamesInEachOutputCol;
    ByteCodeArray outputColNameArray = new ByteCodeArray();
    Array<AnnotationOutputFunction> outputColFunctionArray;


    public OutputFrame setRawNamesInEachOutputCol(Array<ByteCodeArray> rawNamesInEachOutputCol) {
        this.rawNamesInEachOutputCol = rawNamesInEachOutputCol;
        return this;
    }

    public OutputFrame setOutputColFunctionArray(BaseArray<Class<? extends AnnotationOutputFunction>> outputColFunctionArray) {
        if (outputColFunctionArray == null || outputColFunctionArray.isEmpty()) {
            return this;
        }
        this.outputColFunctionArray = new Array<>();
        for (Class<? extends AnnotationOutputFunction> aClass : outputColFunctionArray) {
            try {
                Constructor<? extends AnnotationOutputFunction> constructor = aClass.getConstructor();
                this.outputColFunctionArray.add(constructor.newInstance());
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public OutputFrame setRawIndexOfOutputCol(IntArray colIndex) {
        loadIndex = true;
        rawIndexOfOutputCol = new Array<>(colIndex.size());
        for (int index : colIndex) {
            rawIndexOfOutputCol.add(IntArray.wrap(new int[]{index}));
        }
        parsed = true;
        return this;
    }

    public OutputFrame setOutputColNameArray(ByteCodeArray outputColNameArray) {
        this.outputColNameArray = outputColNameArray;
        return this;
    }

    public void check() {
        if (outputColNameArray == null || outputColNameArray.isEmpty()) {
            int rawColSize = rawColNameArray.size();
            rawIndexOfOutputCol = new Array<>(rawColSize - 3);
            outputColFunctionArray = new Array<>(rawColSize - 3);
            outputColNameArray = new ByteCodeArray(rawColSize - 3);
            for (int i = 3; i < rawColSize; i++) {
                IntArray wrap = IntArray.wrap(new int[i]);
                rawIndexOfOutputCol.add(wrap);
                outputColNameArray.add(rawColNameArray.getByIndex(i));
                outputColFunctionArray.add(new ConcatSingleAnnotationOutputFunction().setRawFieldIndexArray(wrap));
            }
            return;
        }
        //region fill and check the raw index related to output frame
        if (!loadIndex) {
            // transfer raw name array of output to raw index of output
            rawIndexOfOutputCol = new Array<>(rawNamesInEachOutputCol.size());
            for (ByteCodeArray eachCol : rawNamesInEachOutputCol) {
                int currSize = eachCol.size();
                int[] indexArrayInCol = new int[currSize];
                for (int i = 0; i < currSize; i++) {
                    indexArrayInCol[i] = rawColNameArray.indexOfValue(eachCol.get(i));
                    if (indexArrayInCol[i] == -1) {
                        throw new UnsupportedOperationException(eachCol.get(i) + " can't be mapped to raw file column names" +
                                ByteCodeArray.wrap(rawColNameArray.values().toArray(new ByteCode[0])));
                    }
                }
                rawIndexOfOutputCol.add(IntArray.wrap(indexArrayInCol));
            }
        } else {
            int rawColSize = rawColNameArray.size();
            for (IntArray eachCol : rawIndexOfOutputCol) {
                for (int indexInEachCol : eachCol) {
                    if (indexInEachCol >= rawColSize) {
                        throw new UnsupportedOperationException(indexInEachCol + " can't mapped to raw file columns indexes[0," +
                                (rawColSize - 1) + "]");
                    }
                }
            }
        }
        //endregion
        // check whether size equal
        if (rawIndexOfOutputCol.size() != outputColNameArray.size()) {
            throw new UnsupportedOperationException("The number of output columns does not match the input");
        }
        // check function
        if (outputColFunctionArray == null || outputColFunctionArray.isEmpty()) {
            outputColFunctionArray = new Array<>();
            for (IntArray col : rawIndexOfOutputCol) {
                if (col.size() > 1) {
                    outputColFunctionArray.add(new ConcatMultiAnnotationOutputFunction().setRawFieldIndexArray(col));
                } else {
                    outputColFunctionArray.add(new ConcatSingleAnnotationOutputFunction().setRawFieldIndexArray(col));
                }
            }
        } else {
            if (outputColFunctionArray.size() != outputColNameArray.size()) {
                throw new UnsupportedOperationException("The number of output function does not match the output names");
            }
            for (int i = 0; i < outputColFunctionArray.size(); i++) {
                AnnotationOutputFunction annotationOutputFunction = outputColFunctionArray.get(i);
                if (annotationOutputFunction.rawFieldIndexArray == null || annotationOutputFunction.rawFieldIndexArray.isEmpty()) {
                    IntArray rawFieldIndexArray = new IntArray();
                    ByteCodeArray columns = rawNamesInEachOutputCol.get(i);
                    for (ByteCode column : columns) {
                        rawFieldIndexArray.add(rawColNameArray.indexOfValue(column));
                    }
                    annotationOutputFunction.setRawFieldIndexArray(rawFieldIndexArray);
                }
            }

        }
    }

    public ByteCodeArray getOutputColNameArray() {
        return outputColNameArray;
    }

    public ByteCode outputCol(Array<IRecord> relatedRecord, int colIndex) {
        return outputColFunctionArray.get(colIndex).unsafeGetOutputCol(relatedRecord);
    }

    public void initRawColNameArray(CallableSet<ByteCode> rawColNameArray) {
        this.rawColNameArray = rawColNameArray;
    }

    public Array<ByteCodeArray> getRawNamesInEachOutputCol() {
        return rawNamesInEachOutputCol;
    }
}
