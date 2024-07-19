package edu.sysu.pmglab.sdfa.merge.newMerge.order;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Wenjie Peng
 * @create 2024-07-09 02:28
 * @description
 */
public class CSVAssembleForFile {
    Lock lock = new ReentrantLock();
    HashMap<Integer, Array<UnifiedSV>> csvIndexSVArrayMap;

    public CSVAssembleForFile() {
        csvIndexSVArrayMap = new HashMap<>();
    }

    public synchronized void update(UnifiedSV sv) {
        int indexOfFile = sv.getIndexOfFile();
        Array<UnifiedSV> svs = csvIndexSVArrayMap.get(indexOfFile);
        if (svs == null) {
            svs = new Array<>();
            svs.add(sv);
            csvIndexSVArrayMap.put(indexOfFile, svs);
        } else {
            svs.add(sv);
        }
    }

    public BaseArray<ComplexSV> collect() {
        BaseArray<ComplexSV> collect = new Array<>();
        for (Integer indexOfFile : csvIndexSVArrayMap.keySet()) {
            Array<UnifiedSV> svs = csvIndexSVArrayMap.get(indexOfFile);
            int numOfInnerSV = svs.get(0).numOfSVs();
            if (numOfInnerSV == svs.size()) {
                collect.add(new ComplexSV().setSVs(svs));
                csvIndexSVArrayMap.remove(indexOfFile);
            }
        }
        if (collect.isEmpty()) {
            return null;
        }
        return collect;
    }

}
