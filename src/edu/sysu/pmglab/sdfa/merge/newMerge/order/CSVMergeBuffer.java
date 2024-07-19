package edu.sysu.pmglab.sdfa.merge.newMerge.order;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.sdfa.merge.newMerge.principle.SVMergePrinciple;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;

/**
 * @author Wenjie Peng
 * @create 2024-07-09 02:18
 * @description
 */
public class CSVMergeBuffer {
    boolean sorted = false;
    Array<ComplexSV> complexSVArray;
    private static SVMergePrinciple mergePrinciple;

    public CSVMergeBuffer() {
        complexSVArray = new Array<>();
    }

    public void addAll(BaseArray<ComplexSV> csvArray) {
        complexSVArray.addAll(csvArray);
        sorted = false;
    }

    public BaseArray<ComplexSV> popFirstMergedSVs() {
        int size = complexSVArray.size();
        if (size == 0) {
            return null;
        }
        if (!sorted) {
            sorted = true;
            complexSVArray.sort(ComplexSV::compareTo);
        }
        if (size == 1) {
            return complexSVArray.popFirst(1);
        }
        ComplexSV complexSV = complexSVArray.get(0);
        for (int i = 1; i < size; i++) {
            if (!mergePrinciple.merge(complexSVArray.get(i), complexSV)) {
                return complexSVArray.popFirst(i);
            }
        }
        return complexSVArray.popFirst(size);
    }
}
