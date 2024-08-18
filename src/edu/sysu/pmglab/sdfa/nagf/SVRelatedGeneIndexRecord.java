package edu.sysu.pmglab.sdfa.nagf;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ShortArray;
import edu.sysu.pmglab.sdfa.annotation.genome.GeneAnnotFeature;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-20 22:39
 * @description
 */
public class SVRelatedGeneIndexRecord implements Comparable<SVRelatedGeneIndexRecord> {
    UnifiedSV sv;
    ShortArray RNAIndexArray = new ShortArray();
    ShortArray geneIndexArray = new ShortArray();
    ShortArray geneIndexRelatedRNASize = new ShortArray();
    Array<NumericGeneFeature> numericGeneFeatureArray;

    private SVRelatedGeneIndexRecord() {

    }
    private SVRelatedGeneIndexRecord(UnifiedSV sv, Array<NumericGeneFeature> numericGeneFeatureArray){
        this.sv = sv;
        this.numericGeneFeatureArray = numericGeneFeatureArray;
    }
    public static SVRelatedGeneIndexRecord of(UnifiedSV sv, int[] relatedGeneIndexArray, Array<NumericGeneFeature> allGenes){
        Array<NumericGeneFeature> numericGeneFeatures = new Array<>();
        for (int geneIndex : relatedGeneIndexArray) {
            numericGeneFeatures.add(allGenes.get(geneIndex));
        }
        return new SVRelatedGeneIndexRecord(sv, numericGeneFeatures);
    }
    public static SVRelatedGeneIndexRecord of(GeneAnnotFeature geneAnnotFeature) {
        SVRelatedGeneIndexRecord res = new SVRelatedGeneIndexRecord();
        int size = geneAnnotFeature.getSize();
        res.geneIndexArray.add(geneAnnotFeature.getGeneIndex(0));
        res.RNAIndexArray.add(geneAnnotFeature.getRNAIndex(0));
        short count = 1;
        short lastRecordGeneIndexInArray = 0;
        for (int i = 1; i < size; i++) {
            short currGeneIndex = geneAnnotFeature.getGeneIndex(i);
            if (currGeneIndex != res.geneIndexArray.get(lastRecordGeneIndexInArray)) {
                res.geneIndexArray.add(currGeneIndex);
                lastRecordGeneIndexInArray++;
                res.geneIndexRelatedRNASize.add(count);
                count = 1;
            }
            count++;
            res.RNAIndexArray.add(geneAnnotFeature.getRNAIndex(i));
        }
        return res;
    }

    public short getFirstGeneIndex() {
        return geneIndexArray.isEmpty() ? -1 : geneIndexArray.get(0);
    }

    public ShortArray popGeneRelatedRNAIndexArray(short geneIndex) {
        if (geneIndex == geneIndexArray.get(0)) {
            geneIndexArray.popFirst();
            Short relatedRNASize = geneIndexRelatedRNASize.popFirst();
            ShortArray res = new ShortArray(relatedRNASize);
            res.addAll(RNAIndexArray.popFirst(relatedRNASize));
            return res;
        }
        return null;
    }

    @Override
    public int compareTo(SVRelatedGeneIndexRecord o) {
        int size1 = geneIndexArray.size();
        int size2 = o.geneIndexArray.size();
        int min = Math.min(size1, size2);
        for (int i = 0; i < min; i++) {
            int compare = Integer.compare(geneIndexArray.get(i), o.geneIndexArray.get(i));
            if (compare != 0) {
                return compare;
            }
        }
        return Integer.compare(size1,size2);
    }
    public boolean isEmpty(){
        return geneIndexArray.isEmpty();
    }

    public short popFirstGeneIndex(){
        return geneIndexArray.popFirst();
    }
    public short popFirstGeneIndexRelatedRNASize(){
        return geneIndexRelatedRNASize.popFirst();
    }
    public BaseArray<Short> popFirstGeneIndexRelatedRNAIndexArray(){
        return RNAIndexArray.popFirst(geneIndexRelatedRNASize.popFirst());
    }

    public ShortArray getGeneIndexArray(){
        return geneIndexArray;
    }

    public Array<NumericGeneFeature> getNumericGeneFeatureArray() {
        return numericGeneFeatureArray;
    }
}
