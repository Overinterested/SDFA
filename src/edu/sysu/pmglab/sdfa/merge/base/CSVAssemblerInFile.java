package edu.sysu.pmglab.sdfa.merge.base;

import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-15 21:13
 * @description
 */
public class CSVAssemblerInFile {
    HashMap<Integer, Entry<Integer, Array<UnifiedSV>>> SVIndexInFileCSVMap = new HashMap<>();

    public boolean isEmpty() {
        return SVIndexInFileCSVMap.isEmpty();
    }

    public Array<ComplexSV> collect() {
        Array<ComplexSV> categoryCSVArray = new Array<>();
        Integer[] SVIndexInFileArray = SVIndexInFileCSVMap.keySet().toArray(new Integer[0]);
        for (Integer svIndexInFile : SVIndexInFileArray) {
            Entry<Integer, Array<UnifiedSV>> tmpCSVEntry = SVIndexInFileCSVMap.get(svIndexInFile);
            Array<UnifiedSV> tmpCSV = tmpCSVEntry.getValue();
            if (tmpCSV.size() == tmpCSVEntry.getKey()) {
                ComplexSV csv = new ComplexSV();
                categoryCSVArray.add(csv.setSVs(tmpCSV));
                SVIndexInFileCSVMap.remove(svIndexInFile);
            }
        }
        return categoryCSVArray;
    }

    public void put(UnifiedSV sv) {
        int svIndexInFile = sv.getIndexOfFile();
        Entry<Integer, Array<UnifiedSV>> CSVEntry = SVIndexInFileCSVMap.get(svIndexInFile);
        if (CSVEntry == null) {
            Array<UnifiedSV> svs = new Array<>();
            svs.add(sv);
            SVIndexInFileCSVMap.put(svIndexInFile, new Entry<>(calcCSVInnerSVNumber(sv), svs));
        } else {
            ByteCodeArray specificInfoField = sv.getSpecificInfoField();
            if (specificInfoField != null) {
                specificInfoField.clear();
            }
            CSVEntry.getValue().add(sv);
        }
    }

    int calcCSVInnerSVNumber(UnifiedSV sv) {
        return sv.numOfSVs();
    }

}
