package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.merge.SDFAMergeManager;
import edu.sysu.pmglab.sdfa.merge.base.SimpleSVMergeQueue;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.toolkit.GlobalVCFContigConvertor;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 09:32
 * @description
 */
public abstract class AbstractSVMergeStrategy {
    static Chromosome chromosome;
    static int mergedSVSize = 0;
    static int capacity = 10000;
    Lock lock = new ReentrantLock();
    /*
        index represents the type of SV, for instance:
        copyOfCollectionSimpleSVQueue[0] represent the SV queue whose type encoded as 0
     */
    static Array<SimpleSVMergeQueue> copyOfCollectionSimpleSVQueue = new Array<>();
    static HashMap<Integer, Array<ComplexSV>> typeCategoryCSVMap = new HashMap<>();
    public static final Array<MergedSV> mergedSVArray = new Array<>(capacity, true);

    static {
        for (int i = 0; i < capacity; i++) {
            mergedSVArray.add(new MergedSV());
        }
        for (SVTypeSign type : SVTypeSign.support()) {
            copyOfCollectionSimpleSVQueue.add(new SimpleSVMergeQueue());
        }
    }

    public static Array<MergedSV> getMergedSVArray() {
        return mergedSVArray.get(0, mergedSVSize);
    }

    /**
     * get complete SVs(unified SVs and complex SVs) from collector
     */
    public void acceptAllCompleteSVsFromCollection() {
        chromosome = GlobalVCFContigConvertor.getGlobalChromosome(AbstractSVCollector.getChrIndex() - 1);
        Array<SimpleSVMergeQueue> collectedSimpleSVs = AbstractSVCollector.simpleSVTypeSVMergeQueueMap;
        for (int i = 0; i < collectedSimpleSVs.size(); i++) {
            BaseArray<UnifiedSV> toBeMerged = collectedSimpleSVs.get(i).popAll();
            if (!toBeMerged.isEmpty()) {
                copyOfCollectionSimpleSVQueue.get(i).addToBeMergedSVArray(toBeMerged);
            }
        }
        Array<ComplexSV> csvCompleteArray = AbstractSVCollector.csvCompleteArray;
        while (!AbstractSVCollector.csvCompleteArray.isEmpty()) {
            ComplexSV categoryCSV = csvCompleteArray.popFirst();
            typeCategoryCSVMap.computeIfAbsent(categoryCSV.getType().getIndex(), o -> new Array<>()).add(categoryCSV);
        }
    }

    abstract public Array<UnifiedSV> popFirstMergedSimpleSV(SimpleSVMergeQueue specificTypeSimpleSVMergeQueue);

    abstract public Array<ComplexSV> popFirstCanBeMergedCSVArray(Array<ComplexSV> specificTypeCSVArray);

    abstract public MergedSV mergeSimpleSVArray(Array<UnifiedSV> simpleSVArray);

    abstract public MergedSV mergeCSVArray(Array<ComplexSV> csvArray);

    abstract public ByteCode getMergeMethodName();

    public void merge() {
        for (int i = 0; i < copyOfCollectionSimpleSVQueue.size(); i++) {
            SimpleSVMergeQueue tmpSimpleSVQueue = copyOfCollectionSimpleSVQueue.get(i);
            while (!tmpSimpleSVQueue.isEmpty()) {
                Array<UnifiedSV> canBeMergedSimpleSVArray = popFirstMergedSimpleSV(tmpSimpleSVQueue);
                mergeSimpleSVArray(canBeMergedSimpleSVArray);
            }
            tmpSimpleSVQueue.unsorted();
        }
        for (int csvType : typeCategoryCSVMap.keySet()) {
            while (true) {
                Array<ComplexSV> canBeMergedCSVArray = popFirstCanBeMergedCSVArray(typeCategoryCSVMap.get(csvType));
                if (canBeMergedCSVArray.isEmpty()) {
                    break;
                }
                mergeCSVArray(canBeMergedCSVArray);
            }
        }
    }

    public MergedSV getTmpMergeSV() {
        if (SDFAMergeManager.isIsParallel()) {
            lock.lock();
            if (mergedSVSize >= capacity) {
                capacity *= 2;
                for (int i = mergedSVSize; i < capacity; i++) {
                    mergedSVArray.add(new MergedSV());
                }
            }
            MergedSV res = mergedSVArray.get(mergedSVSize++);
            lock.unlock();
            return res;
        } else {
            if (mergedSVSize >= capacity) {
                capacity *= 2;
                for (int i = mergedSVSize; i < capacity; i++) {
                    mergedSVArray.add(new MergedSV());
                }
            }
            return mergedSVArray.get(mergedSVSize++);
        }
    }

    public void acceptMergedSVArray(Array<MergedSV> tmpMergedArray) {
        int currMergedSize = tmpMergedArray.size();
        lock.lock();
        if (currMergedSize + mergedSVSize - 1 < capacity) {
            for (int i = 0; i < currMergedSize; i++) {
                mergedSVArray.set(mergedSVSize++, mergedSVArray.get(i));
            }
        } else {
            int tmp = mergedSVSize;
            for (; mergedSVSize < currMergedSize; mergedSVSize++) {
                mergedSVArray.set(mergedSVSize, tmpMergedArray.get(mergedSVSize - tmp));
            }
            for (int i = mergedSVSize - tmp; i < currMergedSize; i++) {
                mergedSVArray.add(tmpMergedArray.get(i));
            }
            capacity = mergedSVArray.getCapacity();
        }
        lock.unlock();
    }

    public Array<MergedSV> submitOutput() {
        if (mergedSVSize == 0) {
            return null;
        }
        int size = mergedSVSize;
        mergedSVSize = 0;
        return mergedSVArray.get(0, size);
    }

    public static class MergedSV {
        UnifiedSV sv;
        VolumeByteStream cache;
        Array<Object> infoFeatureArray;

        public MergedSV() {
            infoFeatureArray = new Array<>(6);
            for (int i = 0; i < 6; i++) {
                infoFeatureArray.add(null);
            }
            cache = new VolumeByteStream();
        }

        public MergedSV(UnifiedSV sv, VolumeByteStream cache, Array<Object> infoFeatureArray) {
            this.sv = sv;
            this.cache = cache;
            this.infoFeatureArray = infoFeatureArray;
        }

        public Chromosome getChr() {
            return sv.getChr();
        }
    }

    public static Chromosome getChromosome() {
        return chromosome;
    }
}
