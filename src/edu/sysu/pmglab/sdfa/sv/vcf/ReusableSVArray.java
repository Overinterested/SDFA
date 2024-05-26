package edu.sysu.pmglab.sdfa.sv.vcf;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.sdfa.SDFEncode;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 04:22
 * @description
 */
public class ReusableSVArray {
    int decomposedSize;
    int verifiedSVSize;
    SDFEncode encodeSV;
    int totalSVSizeInVCF = 0;
    Array<IRecord> encodeSVArray = new Array<>();
    static IRecord record = IRecord.getInstance(SDFFormat.SDFFields);
    Array<UnifiedSV> unifiedSVArray = new Array<>(4, true);

    public ReusableSVArray() {
    }

    public UnifiedSV getOneUnifiedSV() {
        // get new
        UnifiedSV sv = new UnifiedSV();
        unifiedSVArray.add(sv);
        decomposedSize++;
        return sv;
    }

    public void addTotalSVSizeInVCF() {
        totalSVSizeInVCF++;
    }

    public void addVerifiedSVSize() {
        for (UnifiedSV sv : unifiedSVArray) {
            encodeSVArray.add(encodeSV.encode(record.clone(), sv));
        }
        unifiedSVArray.clear();
        verifiedSVSize++;
    }

    public UnifiedSV getLastUnifiedSV() {
        return unifiedSVArray.get(unifiedSVArray.size() - 1);
    }

    public BaseArray<UnifiedSV> getLastUnifiedSVs() {
        return unifiedSVArray;
    }

    public void reset() {
        decomposedSize = 0;
        verifiedSVSize = 0;
        totalSVSizeInVCF = 0;
        encodeSVArray.clear();
        unifiedSVArray.clear();
    }

    public void rollBack() {
        decomposedSize -= unifiedSVArray.size();
        unifiedSVArray.clear();
    }

    public Array<IRecord> getLoadEncodeSVArray() {
        if (decomposedSize == 0) {
            return null;
        }
        return encodeSVArray.get(0, decomposedSize - 1);
    }

    public ReusableSVArray setEncodeSV(SDFEncode encodeSV) {
        this.encodeSV = encodeSV;
        return this;
    }
    public Array<IRecord> getEncodeSVArrayByClear(){
        return new Array<>(encodeSVArray.popFirst(encodeSVArray.size()));
    }
}
