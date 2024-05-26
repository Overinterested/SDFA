package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.genome.RefRNA;

/**
 * @author Wenjie Peng
 * @create 2023-05-05 10:08
 * @description
 */
public class GeneAnnotRecord {
    Chromosome chr;
    short geneIndex;
    short RNACount;
    int geneStartPos;
    int geneEndPos;
    public Array<RNAAnnotRecord> RNAList;

    private GeneAnnotRecord() {

    }

    public static GeneAnnotRecord getInstance(IRecord record) {
        GeneAnnotRecord geneAnnotRecord = new GeneAnnotRecord();
        geneAnnotRecord.chr = record.get(0);
        geneAnnotRecord.geneIndex = (short) record.get(1);
        geneAnnotRecord.RNACount = (short) ((int) record.get(2));
        IntArray prePos = IntArray.wrap(record.get(5));
        geneAnnotRecord.geneStartPos = prePos.get(0);
        geneAnnotRecord.geneEndPos = prePos.get(1);
        geneAnnotRecord.RNAList = new Array<>(geneAnnotRecord.RNACount);
        return geneAnnotRecord;
    }

    public void appendRNA(RNAAnnotRecord refRNA) {
        this.RNAList.add(refRNA);
    }

    public void clear() {
        for (RNAAnnotRecord rnaItem : RNAList) {
            rnaItem.clear();
        }
    }
}

class RNAAnnotRecord {
    byte strand;
    short RNAIndex;
    int startPos;
    int endPos;
    int cdsStartPos;
    int cdsEndPos;
    int cdsStartExonIndex;
    int cdsEndExonIndex;
    int exonNum;
    boolean nonCoding;
    int[] exonPosList;
    private RefRNA refRNA;

    private RNAAnnotRecord() {
    }

    public static RNAAnnotRecord getInstance(IRecord record) {
        RNAAnnotRecord rnaAnnotRecord = new RNAAnnotRecord();
        rnaAnnotRecord.RNAIndex = record.get(1);
        IntArray prePos = IntArray.wrap(record.get(5));
        rnaAnnotRecord.startPos = prePos.get(0);
        rnaAnnotRecord.endPos = prePos.get(1);
        rnaAnnotRecord.cdsStartPos = prePos.get(2);
        rnaAnnotRecord.cdsEndPos = prePos.get(3);
        rnaAnnotRecord.cdsStartExonIndex = prePos.get(4);
        rnaAnnotRecord.cdsEndExonIndex = prePos.get(5);
        rnaAnnotRecord.strand = (byte) prePos.get(6).intValue();
        rnaAnnotRecord.exonPosList = record.get(6);

        rnaAnnotRecord.exonNum = rnaAnnotRecord.exonPosList.length / 2;
        return rnaAnnotRecord;
    }

    public RefRNA toRefRNA() {
        if (refRNA == null) {
            refRNA = RefRNA.build(strand, startPos, endPos, cdsStartPos, cdsEndPos, RNAIndex, exonNum, cdsStartExonIndex, cdsEndExonIndex, nonCoding, exonPosList);
        }
        return refRNA;
    }

    public RNAAnnot annot(int pos, int upstreamDis, int downstreamDis, int geneIndex) {
        if (refRNA == null) {
            refRNA = RefRNA.build(strand, startPos, endPos, cdsStartPos, cdsEndPos, RNAIndex, exonNum, cdsStartExonIndex, cdsEndExonIndex, nonCoding, exonPosList);
        }
        return refRNA.rnaAnnotLatest(pos, upstreamDis, downstreamDis, geneIndex);
    }

    public void clear() {
        refRNA = null;
    }
}
