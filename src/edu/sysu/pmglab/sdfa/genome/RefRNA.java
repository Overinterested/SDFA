package edu.sysu.pmglab.sdfa.genome;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Interval;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.container.intervaltree.IntervalObject;
import edu.sysu.pmglab.container.intervaltree.IntervalTree;
import edu.sysu.pmglab.sdfa.annotation.genome.RNAAnnot;
import edu.sysu.pmglab.sdfa.genome.transcript.*;
import edu.sysu.pmglab.sdfa.nagf.NumericTranscriptFeature;
import edu.sysu.pmglab.sdfa.sv.SVCoordinate;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2023-04-21 13:34
 * @description
 */
public class RefRNA implements Comparable<RefRNA> {
    // 0: '+' 1: '-'
    byte strand;
    ByteCode RNAName;
    int startPos;
    int endPos;
    int cdsStartPos;
    int cdsEndPos;
    short RNAIndex;
    int exonNum;
    private int cdsStartExonIndex;
    private int cdsEndExonIndex;
    boolean nonCoding;
    int[] exonPosList;
    NumericTranscriptFeatureCalculator ngfCalc;

    public static int upstreamDis = 1000;
    public static int downstreamDis = 1000;
    public static int PROMOTER_DISTANCE = 200;
    public static final int DUP_TYPE = SVTypeSign.get("DUP").getIndex();
    public static final int CNV_TYPE = SVTypeSign.get("CNV").getIndex();
    public static final int INV_TYPE = SVTypeSign.get("INV").getIndex();
    public static final int INS_TYPE = SVTypeSign.get("INS").getIndex();
    public static final int INV_DUP_TYPE = SVTypeSign.get("DUP:INV").getIndex();

    private RefRNA() {
        cdsStartExonIndex = -1;
        cdsEndExonIndex = -1;
    }

    public static RefRNA build(byte strand, int startPos, int endPos, int cdsStartPos, int cdsEndPos, IntArray exonStart, IntArray exonEnd, ByteCode mRNAName) {
        RefRNA refRNA = new RefRNA();
        refRNA.strand = strand;
        refRNA.startPos = startPos;
        refRNA.endPos = endPos;
        refRNA.cdsStartPos = cdsStartPos;
        refRNA.cdsEndPos = cdsEndPos;
        refRNA.exonPosList = new int[2 * exonStart.size()];
        refRNA.nonCoding = cdsStartPos == cdsEndPos || cdsStartPos == -1;
        refRNA.exonNum = exonEnd.size();
        for (int i = 0; i < exonStart.size(); i++) {
            refRNA.cdsStartExonIndex = refRNA.cdsStartExonIndex == -1 ? (exonEnd.get(i) >= cdsStartPos ? i : -1) : refRNA.cdsStartExonIndex;
            refRNA.cdsEndExonIndex = refRNA.cdsEndExonIndex == -1 ? (exonEnd.get(i) >= cdsEndPos ? i : -1) : refRNA.cdsEndExonIndex;
            refRNA.exonPosList[2 * i] = exonStart.get(i);
            refRNA.exonPosList[2 * i + 1] = exonEnd.get(i);
        }
        refRNA.RNAName = mRNAName;
        return refRNA;
    }

    public static RefRNA build(byte strand, int startPos, int endPos, int cdsStartPos, int cdsEndPos, int[] exonStart, int[] exonEnd, String mRNAName) {
        RefRNA refRNA = new RefRNA();
        refRNA.strand = strand;
        refRNA.startPos = startPos;
        refRNA.endPos = endPos;
        refRNA.cdsStartPos = cdsStartPos;
        refRNA.cdsEndPos = cdsEndPos;
        refRNA.exonPosList = new int[2 * exonStart.length];
        refRNA.nonCoding = cdsStartPos == cdsEndPos;
        for (int i = 0; i < exonStart.length; i++) {
            refRNA.cdsStartExonIndex = refRNA.cdsStartExonIndex == -1 ? (exonEnd[i] >= cdsStartPos ? i : -1) : refRNA.cdsStartExonIndex;
            refRNA.cdsEndExonIndex = refRNA.cdsEndExonIndex == -1 ? (exonEnd[i] >= cdsEndPos ? i : -1) : refRNA.cdsEndExonIndex;
            refRNA.exonPosList[2 * i] = exonStart[i];
            refRNA.exonPosList[2 * i + 1] = exonEnd[i];
        }
        refRNA.RNAName = new ByteCode(mRNAName);
        return refRNA;
    }

    public static RefRNA build(byte strand, int startPos, int endPos, int cdsStartPos, int cdsEndPos, short RNAIndex, int exonNum, int cdsStartExonIndex, int cdsEndExonIndex, boolean nonCoding, int[] exonPosList) {
        RefRNA refRNA = new RefRNA();
        refRNA.strand = strand;
        refRNA.startPos = startPos;
        refRNA.endPos = endPos;
        refRNA.cdsStartPos = cdsStartPos;
        refRNA.cdsEndPos = cdsEndPos;
        refRNA.RNAIndex = RNAIndex;
        refRNA.exonNum = exonNum;
        refRNA.cdsStartExonIndex = cdsStartExonIndex;
        refRNA.cdsEndExonIndex = cdsEndExonIndex;
        refRNA.nonCoding = nonCoding;
        refRNA.exonPosList = exonPosList;
        return refRNA;
    }

    public static RefRNA loadRNA(IRecord record) {
        RefRNA res = new RefRNA();
        res.RNAIndex = (short) (int) record.get(1);
        res.RNAName = ((ByteCode) record.get(4)).asUnmodifiable();
        IntArray prePos = IntArray.wrap(record.get(5));
        res.startPos = prePos.get(0);
        res.endPos = prePos.get(1);
        res.cdsStartPos = prePos.get(2);
        res.cdsEndPos = prePos.get(3);
        res.cdsStartExonIndex = prePos.get(4);
        res.cdsEndExonIndex = prePos.get(5);
        res.strand = (byte) prePos.get(6).intValue();
        res.exonPosList = IntArray.wrap(record.get(6)).toBaseArray();
        res.exonNum = res.exonPosList.length / 2;
        res.nonCoding = res.cdsStartPos == res.cdsEndPos;
        return res;
    }

    public Interval<Integer> getExons(int exonIndex) {
        if (exonIndex >= exonNum) {
            return null;
        }
        return new Interval<>(exonPosList[exonIndex * 2], exonPosList[exonIndex * 2 + 1]);
    }

    public int getExonStart(int exonIndex) {
        if (exonIndex >= exonNum) {
            return -1;
        }
        return exonPosList[exonIndex * 2];
    }

    public int getExonEnd(int exonIndex) {
        if (exonIndex >= exonNum) {
            return -1;
        }
        return exonPosList[exonIndex * 2 + 1];
    }

    public RNAAnnot rnaAnnotLatest(int pos, int upDistance, int downDistance, int geneIndex) {
        return singlePosLocation0(pos, upDistance, downDistance, geneIndex);
    }

    public RNAAnnot singlePosLocation0(int pos, int upDistance, int downDistance, int geneIndex) {
        int index = Arrays.binarySearch(exonPosList, pos);
        int relativeCdsDis = 0;
        switch (this.strand) {
            case 0:
                // this strand is + strand
                if (index == -1) {// in the left of RNA start
                    if (pos < startPos - upDistance) {
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intergenic"), ".", breakPointID);
                        return LeftIntergenic.convert(geneIndex, RNAIndex);
                    } else if (nonCoding) {
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("ncRNA"), mRNAName + ":(" + refExonList.size() + "Exons):ncRNA", breakPointID);
                        return NCRNA.convert(geneIndex, RNAIndex, pos - startPos + 1);
                    } else {
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Upstream"), mRNAName + ":c." + (pos - this.cdsStartPos) + ".>N:(" + this.refExonList.size() + "Exons):upstream", breakPointID);
                        return Upstream.convert(geneIndex, RNAIndex, startPos - pos + 1);
                    }
                } else if (index == -exonPosList.length - 1 || index == exonPosList.length - 1) {// in the right of RNA end
                    if (pos >= endPos + downDistance) {
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intergenic"), ".", breakPointID);
                        return RightIntergenic.convert(geneIndex, RNAIndex);
                    } else if (nonCoding) {
                        return NCRNA.convert(geneIndex, RNAIndex, pos - startPos + 1);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("ncRNA"), mRNAName + ":(" + refExonList.size() + "Exons):ncRNA", breakPointID);
                    } else {
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Downstream"), mRNAName + ":c.*" + (pos - this.cdsEndPos) + ".>N:(" + this.refExonList.size() + "Exons):downstream", breakPointID);
                        return Downstream.convert(geneIndex, RNAIndex, pos - endPos + 1);
                    }
                } else {// between RNA start and RNA end
                    if (nonCoding) {
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("ncRNA"), mRNAName + ":(" + refExonList.size() + "Exons):ncRNA", breakPointID);
                        return NCRNA.convert(geneIndex, RNAIndex, pos - startPos + 1);
                    } else {
                        if (index < 0) {// in the element interval
                            if (index % 2 == 0) {// in Exon interval
                                int inExonPos = -index / 2 - 1;
                                if (pos < cdsStartPos) {// in UTR5 interval
                                    for (int i = inExonPos; i < cdsStartExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex);
                                    relativeCdsDis -= pos - getExonStart(inExonPos);
//                                    relativeCdsDis += cdsStartPos - refExonList.get(cdsStartExonIndex).start;
//                                    relativeCdsDis -= pos - refExonList.get(inExonPos).start;

                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("UTR5"), mRNAName + ":c." + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):UTR5", breakPointID);
                                    return UTR5.convert(geneIndex, RNAIndex, inExonPos + 1, relativeCdsDis, pos - getExonStart(inExonPos) + 1);
//                                    return UTR5.createInstance(geneIndex, RNAIndex, inExonPos + 1, relativeCdsDis, pos - refExonList.get(inExonPos).start + 1);
                                } else if (pos >= cdsEndPos) {// in UTR3 interval
                                    for (int i = cdsEndExonIndex; i < inExonPos; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
                                    }
                                    relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
                                    relativeCdsDis += pos - getExonStart(inExonPos) + 1;
//                                    relativeCdsDis -= cdsEndPos - refExonList.get(cdsEndExonIndex).start;
//                                    relativeCdsDis += pos - refExonList.get(inExonPos).start + 1;
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("UTR3"), mRNAName + ":c.*" + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):UTR5", breakPointID);
                                    return UTR3.convert(geneIndex, RNAIndex, inExonPos + 1, relativeCdsDis, pos - getExonStart(inExonPos) + 1);
//                                    return UTR3.createInstance(geneIndex, RNAIndex, inExonPos + 1, relativeCdsDis, pos - refExonList.get(inExonPos).start + 1);
                                } else {// in exon interval
                                    for (int i = cdsStartExonIndex; i < inExonPos; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
                                    }
                                    relativeCdsDis -= cdsStartPos - getExonStart(cdsStartExonIndex);
                                    relativeCdsDis += pos - getExonStart(inExonPos) + 1;
//                                    relativeCdsDis -= cdsStartPos - refExonList.get(cdsStartExonIndex).start;
//                                    relativeCdsDis += pos - refExonList.get(inExonPos).start + 1;
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Exon"), mRNAName + ":c." + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):Exon" + (inExonPos + 1), breakPointID);
                                    return Exon.convert(geneIndex, RNAIndex, inExonPos + 1, relativeCdsDis, pos - getExonStart(inExonPos) + 1);
//                                    return Exon.createInstance(geneIndex, RNAIndex, inExonPos + 1, relativeCdsDis, pos - refExonList.get(inExonPos).start + 1);
                                }

                            } else {// in intro interval
                                int leftExonIndex = -index / 2 - 1;
                                if (leftExonIndex >= cdsStartExonIndex && leftExonIndex < cdsEndExonIndex) {//in cds intro interval
                                    for (int i = cdsStartExonIndex; i <= leftExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsStartPos - getExonStart(cdsStartExonIndex);
//                                    relativeCdsDis -= cdsStartPos - this.refExonList.get(cdsStartExonIndex).start;
                                    // calculate which exon is near

                                    // cause the end is not included
                                    int leftExonDis = pos - getExonEnd(leftExonIndex) + 1;
//                                    int leftExonDis = pos - refExonList.get(leftExonIndex).end + 1;
                                    int rightExonDis = getExonStart(leftExonIndex + 1) - pos;
//                                    int rightExonDis = refExonList.get(leftExonIndex + 1).start - pos;
                                    if (rightExonDis < leftExonDis) {
                                        return Intro.convert(geneIndex, RNAIndex, leftExonIndex + 1, ++relativeCdsDis, -rightExonDis);
                                    } else {// if equal, choose left
                                        return Intro.convert(geneIndex, RNAIndex, leftExonIndex + 1, relativeCdsDis, leftExonDis);
                                    }
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":c." + (rightExonDis < leftExonDis ? ((relativeCdsDis++) + "-" + rightExonDis) : (relativeCdsDis + "+" + leftExonDis)) + ".>N(" + refExonList.size() + "Exons):Intro" + (leftExonIndex + 1), breakPointID);
                                } else {// not in cds intro interval
                                    if (pos < cdsStartPos) {// in the leftIntro interval
                                        for (int i = leftExonIndex + 1; i < cdsStartExonIndex; i++) {
                                            relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                            relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                        }
                                        relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex);
//                                        relativeCdsDis += cdsStartPos - refExonList.get(cdsStartExonIndex).start;
                                        int leftExonDis = pos - getExonEnd(leftExonIndex) + 1;
//                                        int leftExonDis = pos - refExonList.get(leftExonIndex).end + 1;
                                        int rightExonDis = getExonStart(leftExonIndex + 1) - pos;
//                                        int rightExonDis = refExonList.get(leftExonIndex + 1).start - pos;
                                        if (rightExonDis < leftExonDis) {
                                            return LeftIntro.convert(geneIndex, RNAIndex, leftExonIndex + 1, relativeCdsDis, -rightExonDis);
                                        } else {
                                            return LeftIntro.convert(geneIndex, RNAIndex, leftExonIndex + 1, ++relativeCdsDis, leftExonDis);
                                        }
                                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":c." + (rightExonDis < leftExonDis ? ((relativeCdsDis++) + "-" + rightExonDis) : (relativeCdsDis + "+" + leftExonDis)) + ".>N(" + refExonList.size() + "Exons):Intro" + (leftExonIndex + 1), breakPointID);
                                    }
                                    if (pos > cdsEndPos) {// rightIntro
                                        for (int i = cdsEndExonIndex; i < leftExonIndex + 1; i++) {
                                            relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                            relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                        }
                                        relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
//                                        relativeCdsDis -= cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                        int leftExonDis = pos - getExonEnd(leftExonIndex) + 1;
//                                        int leftExonDis = pos - refExonList.get(leftExonIndex).end + 1;
                                        int rightExonDis = getExonStart(leftExonIndex + 1) - pos;
//                                        int rightExonDis = refExonList.get(leftExonIndex + 1).start - pos;
                                        if (rightExonDis < leftExonDis) {
                                            // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":c.*" + relativeCdsDis + "-" + rightExonDis + ".>N(" + refExonList.size() + "Exons):Intro" + (leftExonIndex + 1), breakPointID);
                                            return RightIntro.convert(geneIndex, RNAIndex, leftExonIndex + 1, ++relativeCdsDis, -rightExonDis);
                                        } else {
//                                        svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":c.*" + relativeCdsDis + "+" + leftExonDis + ".>N(" + refExonList.size() + "Exons):Intro" + (leftExonIndex + 1), breakPointID);
                                            return RightIntro.convert(geneIndex, RNAIndex, leftExonIndex + 1, relativeCdsDis, leftExonDis);
                                        }

                                    }
                                }
                            }
                        } else {// index at boundary
                            if (index % 2 == 0) {//at exon left boundary
                                int inExonIndex = index / 2;
                                if (pos < cdsStartPos) {// in the UTR5 exon which pos is left boundary
                                    for (int i = inExonIndex; i < cdsStartExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex);
//                                    relativeCdsDis += cdsStartPos - refExonList.get(cdsStartExonIndex).start;
                                    return UTR5.convert(geneIndex, RNAIndex, inExonIndex + 1, relativeCdsDis, 1);
                                } else if (pos >= cdsEndPos) {// in the UTR3 exon which pos is left boundary
                                    for (int i = cdsEndExonIndex; i < inExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis -= cdsEndPos - this.refExonList.get(cdsEndExonIndex).start;
                                    relativeCdsDis++;
                                    return UTR3.convert(geneIndex, RNAIndex, inExonIndex + 1, relativeCdsDis, 1);
                                } else {// in the cds exon which pos is left boundary
                                    for (int i = cdsStartExonIndex; i < inExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsStartPos - getExonStart(cdsStartExonIndex);
//                                    relativeCdsDis -= cdsStartPos - this.refExonList.get(cdsStartExonIndex).start;
                                    return Exon.convert(geneIndex, RNAIndex, inExonIndex + 1, ++relativeCdsDis, 1);
                                }
                            } else {// intro(index % 2 != 0):at right boundary of exon -> intro start
                                int inExonIndex = index / 2;
                                if (pos < cdsStartPos) {// in the left intro: means left cds intro
                                    for (int i = inExonIndex + 1; i < cdsStartExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex) + 1;
//                                    relativeCdsDis += cdsStartPos - this.refExonList.get(cdsStartExonIndex).start + 1;
                                    // from the left exon's last base, so add 1
                                    relativeCdsDis++;
                                    return LeftIntro.convert(geneIndex, RNAIndex, inExonIndex + 1, relativeCdsDis, 1);
                                } else if (pos > cdsEndPos) {// in the right intro: means right cds intro
                                    for (int i = cdsEndExonIndex; i <= inExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis -= cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    return RightIntro.convert(geneIndex, RNAIndex, inExonIndex + 1, ++relativeCdsDis, -1);
                                } else {// in the cds intro
                                    for (int i = cdsStartExonIndex; i <= inExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsStartPos - getExonStart(cdsStartExonIndex);
//                                    relativeCdsDis -= cdsStartPos - refExonList.get(cdsStartExonIndex).start;
                                    return Intro.convert(geneIndex, RNAIndex, inExonIndex + 1, ++relativeCdsDis, 1);
                                }
                            }
                        }
                    }
                }
            case 1:
                // "-" strand
                if (index == -1) {//exons' left -> Intergenic or Downstream
                    if (pos < startPos - downDistance) {// intergenic
                        // svAnnot.annotUpdate(new RightIntergenic(chrIndex, geneIndex, RNAIndex), breakPointID);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intergenic"), ".", breakPointID);
                        return RightIntergenic.convert(geneIndex, RNAIndex);
                    } else if (nonCoding) {// ncRNA
                        // svAnnot.annotUpdate(new NCRNA(chrIndex, geneIndex, RNAIndex), breakPointID);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("ncRNA"), mRNAName + ":(" + this.refExonList.size() + "Exons):ncRNA", breakPointID);
                        // cause it's - strand, so here endPos means RNA start
                        return NCRNA.convert(geneIndex, RNAIndex, -(pos - endPos));
                    } else {// downstream
                        // svAnnot.annotUpdate(new Downstream(chrIndex, geneIndex, RNAIndex, (short) -(pos - this.cdsStartPos)), breakPointID);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Downstream"), mRNAName + ":c." + (pos - this.cdsStartPos) + ".>N:(" + this.refExonList.size() + "Exons):downstream", breakPointID);
                        return Downstream.convert(geneIndex, RNAIndex, cdsStartPos - pos + 1);
                    }
                } else if (index == -exonPosList.length - 1 || index == exonPosList.length - 1) {//exons' right -> intergenic or Upstream
                    if (pos >= endPos + upDistance) {// intergenic
                        // svAnnot.annotUpdate(new RightIntergenic(chrIndex, geneIndex, RNAIndex), breakPointID);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intergenic"), ".", breakPointID);
                        return LeftIntergenic.convert(geneIndex, RNAIndex);
                    } else if (nonCoding) {// ncRNA
                        // svAnnot.annotUpdate(new NCRNA(chrIndex, geneIndex, RNAIndex), breakPointID);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("ncRNA"), mRNAName + ":(" + this.refExonList.size() + "Exons):ncRNA", breakPointID);
                        // cause it's - strand, so here endPos means RNA start
                        return NCRNA.convert(geneIndex, RNAIndex, pos - endPos + 1);
                    } else {// upstream
                        // svAnnot.annotUpdate(new Upstream(chrIndex, geneIndex, RNAIndex, (short) -(pos - cdsEndPos)), breakPointID);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Upstream"), mRNAName + ":c." + (pos - this.cdsEndPos) + ".>N:(" + this.refExonList.size() + "Exons):upstream", breakPointID);
                        return Upstream.convert(geneIndex, RNAIndex, pos - cdsEndPos + 1);
                    }
                } else {
                    if (nonCoding) { // ncRNA
                        // svAnnot.annotUpdate(new NCRNA(chrIndex, geneIndex, RNAIndex), breakPointID);
                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("ncRNA"), mRNAName + ":(" + this.refExonList.size() + "Exons):ncRNA", breakPointID);
                        return NCRNA.convert(geneIndex, RNAIndex, -(pos - endPos));
                    } else {// cRNA
                        if (index < 0) {// in element interval
                            if (index % 2 == 0) {// in exon interval
                                int inExonIndex = -index / 2 - 1;
                                if (pos < cdsStartPos) {// UTR3 exon interval
                                    for (int i = inExonIndex; i < cdsStartExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex);
//                                    relativeCdsDis += cdsStartPos - refExonList.get(cdsStartExonIndex).start;
//                                    relativeCdsDis -= pos - refExonList.get(inExonIndex).start;
                                    // svAnnot.annotUpdate(new UTR3(chrIndex, geneIndex, RNAIndex, -relativeCdsDis), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("UTR3"), mRNAName + ":c.*" + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):UTR3", breakPointID);
                                    return UTR3.convert(geneIndex, RNAIndex, exonNum - inExonIndex, relativeCdsDis, -(pos - getExonStart(inExonIndex) + 1));
//                                    return UTR3.createInstance(geneIndex, RNAIndex, this.refExonList.size() - inExonIndex, relativeCdsDis, -(pos - refExonList.get(inExonIndex).start + 1));
                                } else if (pos > cdsEndPos) { // UTR5 exon interval
                                    for (int i = cdsEndExonIndex; i < inExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis -= cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    relativeCdsDis += pos - getExonStart(inExonIndex) + 1;
//                                    relativeCdsDis += pos - refExonList.get(inExonIndex).start + 1;
                                    // svAnnot.annotUpdate(new UTR5(chrIndex, geneIndex, RNAIndex, -relativeCdsDis), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("UTR5"), mRNAName + ":c." + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):UTR5", breakPointID);
                                    return UTR5.convert(geneIndex, RNAIndex, exonNum - inExonIndex, relativeCdsDis, -(pos - getExonStart(inExonIndex) + 1));
//                                    return UTR5.createInstance(geneIndex, RNAIndex, exonNum - inExonIndex, relativeCdsDis, -(pos - refExonList.get(inExonIndex).start + 1));
                                } else { // cds exon interval
                                    for (int i = inExonIndex; i < cdsEndExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis += cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    relativeCdsDis -= pos - getExonStart(inExonIndex);
//                                    relativeCdsDis -= pos - refExonList.get(inExonIndex).start;
                                    // svAnnot.annotUpdate(new Exon(chrIndex, geneIndex, RNAIndex, -relativeCdsDis, (byte) (refExonList.size() - inExonIndex)), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Exon"), mRNAName + ":" + "c." + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):Exon" + (refExonList.size() - inExonIndex), breakPointID);
                                    return Exon.convert(geneIndex, RNAIndex, exonNum - inExonIndex, relativeCdsDis, getExonEnd(inExonIndex) - pos + 1);
//                                    return Exon.createInstance(geneIndex, RNAIndex, exonNum - inExonIndex, relativeCdsDis, refExonList.get(inExonIndex).end - pos + 1);
                                }
                            } else {// in intro interval

                                int leftExonIndex = -index / 2 - 1;
                                if (pos <= cdsStartPos) { // intro: in the left of cds -> UTR3 intro
//                                    svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c.*" + (cdsStartPos - pos) + ".>N:(" + this.exonList.size() + "Exons):Intro" + (this.exonList.size() - leftExonIndex - 1), breakPointID);
                                    for (int i = leftExonIndex + 1; i < cdsStartExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    // TODO : next line not +1 : need to test
                                    relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex) + 1;
//                                    relativeCdsDis += cdsStartPos - refExonList.get(cdsStartExonIndex).start + 1;
                                    int leftExonDis = pos - getExonEnd(leftExonIndex) + 1;
//                                    int leftExonDis = pos - refExonList.get(leftExonIndex).end + 1;
                                    int rightExonDis = getExonStart(leftExonIndex + 1) - pos;
//                                    int rightExonDis = refExonList.get(leftExonIndex + 1).start - pos;
                                    if (leftExonDis <= rightExonDis) {
                                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c.*" + relativeCdsDis + "-" + leftExonDis + ".>N:(" + this.refExonList.size() + "Exons):Intro" + (this.refExonList.size() - leftExonIndex - 1), breakPointID);
                                        // svAnnot.annotUpdate(new RightIntro(chrIndex, geneIndex, RNAIndex, -relativeCdsDis, (byte) (this.refExonList.size() - leftExonIndex - 1), (short) leftExonDis, false), breakPointID);
                                        return RightIntro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex - 1, ++relativeCdsDis, -leftExonDis);
                                    }
                                    // svAnnot.annotUpdate(new RightIntro(chrIndex, geneIndex, RNAIndex, -relativeCdsDis, (byte) (this.refExonList.size() - leftExonIndex - 1), (short) rightExonDis, true), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c.*" + relativeCdsDis + "+" + rightExonDis + ".>N:(" + this.refExonList.size() + "Exons):Intro" + (this.refExonList.size() - leftExonIndex - 1), breakPointID);
                                    return RightIntro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex - 1, relativeCdsDis, rightExonDis);
                                } else if (pos >= cdsEndPos) {// intro: in the right of cds
//                                    svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c." + (cdsEndPos - pos) + ".>N:(" + this.exonList.size() + "Exons):Intro" + (this.exonList.size() - leftExonIndex - 1), breakPointID);
                                    for (int i = cdsEndExonIndex; i < leftExonIndex + 1; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis -= cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    int leftExonDis = pos - getExonEnd(leftExonIndex) + 1;
//                                    int leftExonDis = pos - refExonList.get(leftExonIndex).end + 1;
                                    int rightExonDis = getExonStart(leftExonIndex + 1) - pos;
//                                    int rightExonDis = refExonList.get(leftExonIndex + 1).start - pos;
                                    if (leftExonDis <= rightExonDis) {
                                        // svAnnot.annotUpdate(new LeftIntro(chrIndex, geneIndex, RNAIndex, -relativeCdsDis, (byte) (this.refExonList.size() - leftExonIndex - 1), (short) leftExonDis, false), breakPointID);
                                        return LeftIntro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex - 1, relativeCdsDis, -leftExonDis);
                                    }

                                    // svAnnot.annotUpdate(new LeftIntro(chrIndex, geneIndex, chrIndex, -relativeCdsDis, (byte) (this.refExonList.size() - leftExonIndex - 1), (short) relativeCdsDis, false), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c." + relativeCdsDis + "+" + (pos - this.refExonList.get(leftExonIndex).start) + ".>N:(" + this.refExonList.size() + "Exons):Intro" + (this.refExonList.size() - leftExonIndex - 1), breakPointID);
                                    return LeftIntro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex - 1, ++relativeCdsDis, rightExonDis);
                                } else {
                                    // intro : in cds
                                    for (int i = leftExonIndex + 1; i < cdsEndExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis += cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    int leftExonDis = pos - getExonEnd(leftExonIndex) + 1;
//                                    int leftExonDis = pos - refExonList.get(leftExonIndex).end + 1;
                                    int rightExonDis = getExonStart(leftExonIndex + 1) - pos;
//                                    int rightExonDis = refExonList.get(leftExonIndex + 1).start - pos;
                                    if (leftExonIndex <= rightExonDis) {
                                        // svAnnot.annotUpdate(new Intro(chrIndex, geneIndex, RNAIndex, -relativeCdsDis, (short) (this.refExonList.size() - leftExonIndex - 1), (short) leftExonDis, false), breakPointID);
                                        // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c." + relativeCdsDis + "-" + leftExonDis + ".>N:(" + this.refExonList.size() + "Exons):Intro" + (this.refExonList.size() - leftExonIndex - 1), breakPointID);
                                        return Intro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex - 1, ++relativeCdsDis, -leftExonDis);
                                    }
                                    // svAnnot.annotUpdate(new Intro(chrIndex, geneIndex, RNAIndex, -relativeCdsDis, (byte) (this.refExonList.size() - leftExonIndex), (short) relativeCdsDis, true), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c." + relativeCdsDis + "+" + rightExonDis + ".>N:(" + this.refExonList.size() + "Exons):Intro" + (this.refExonList.size() - leftExonIndex - 1), breakPointID);
                                    return Intro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex - 1, relativeCdsDis, rightExonDis);

                                }
                            }
                        } else {// at boundary
                            if (index % 2 == 0) {//exon
                                int exonIndex = index / 2;
                                if (pos < cdsStartPos) {// UTR3
                                    for (int i = exonIndex; i < cdsStartExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex);
//                                    relativeCdsDis += cdsStartPos - refExonList.get(cdsStartExonIndex).start;
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("UTR3"), mRNAName + ":" + "c.*" + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):UTR3", breakPointID);
                                    // svAnnot.annotUpdate(new UTR3(chrIndex, geneIndex, RNAIndex, relativeCdsDis), breakPointID);
                                    return UTR3.convert(geneIndex, RNAIndex, exonNum - exonIndex, relativeCdsDis, getExonEnd(exonIndex) - pos);
//                                    return UTR3.createInstance(geneIndex, RNAIndex, exonNum - exonIndex, relativeCdsDis, refExonList.get(exonIndex).end - pos);
                                } else if (pos >= cdsEndPos) { // UTR5
                                    for (int i = cdsEndExonIndex; i < exonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis -= cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    relativeCdsDis++;
                                    // svAnnot.annotUpdate(new UTR5(chrIndex, geneIndex, RNAIndex, relativeCdsDis), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("UTR3"), mRNAName + ":" + "c." + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):UTR5", breakPointID);
                                    return UTR5.convert(geneIndex, RNAIndex, exonNum - exonIndex, relativeCdsDis, getExonEnd(exonIndex) - pos);
//                                    return UTR5.createInstance(geneIndex, RNAIndex, exonNum - exonIndex, relativeCdsDis, refExonList.get(exonIndex).end - pos);
                                } else {// exon in cds
                                    for (int i = exonIndex; i < cdsEndExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis += cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    // svAnnot.annotUpdate(new Exon(chrIndex, geneIndex, RNAIndex, relativeCdsDis, (short) (refExonList.size() - exonIndex)), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Exon"), mRNAName + ":" + "c." + relativeCdsDis + ".>N(" + this.refExonList.size() + "Exons):Exon" + (refExonList.size() - exonIndex), breakPointID);
                                    return Exon.convert(geneIndex, RNAIndex, exonNum - exonIndex, relativeCdsDis, getExonEnd(exonIndex) - pos);
//                                    return Exon.createInstance(geneIndex, RNAIndex, exonNum - exonIndex, relativeCdsDis, refExonList.get(exonIndex).end - pos);
                                }
                            } else {// intro
                                int leftExonIndex = index / 2;
                                // TODO : next line not +1 : need to test
                                // TODO : logical mind is wrong?
                                if (pos < cdsStartPos) { // intro: in the right of cds -> UTR3 intro
                                    for (int i = leftExonIndex + 1; i < cdsStartExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsStartPos - getExonStart(cdsStartExonIndex) + 1;
//                                    relativeCdsDis += cdsStartPos - refExonList.get(cdsStartExonIndex).start + 1;
                                    // svAnnot.annotUpdate(new RightIntro(chrIndex, geneIndex, RNAIndex, relativeCdsDis, (byte) (refExonList.size() - exonIndex - 1), (short) -1, false), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c.*" + relativeCdsDis + "-1.>N(" + this.refExonList.size() + "Exons):Exon" + (refExonList.size() - exonIndex - 1), breakPointID);
                                    return RightIntro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex, ++relativeCdsDis, -1);
                                } else if (pos >= cdsEndPos) {// intro: in the right of cds
                                    for (int i = cdsEndExonIndex; i < leftExonIndex + 1; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += refExonList.get(i).end - refExonList.get(i).start;
                                    }
                                    relativeCdsDis -= cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis -= cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    // svAnnot.annotUpdate(new LeftIntro(chrIndex, geneIndex, RNAIndex, relativeCdsDis, (short) (refExonList.size() - leftExonIndex - 1), (short) -1, true), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c." + relativeCdsDis + "-1.>N(" + this.refExonList.size() + "Exons):Exon" + (refExonList.size() - exonIndex - 1), breakPointID);
                                    return LeftIntro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex, relativeCdsDis, -1);
                                } else {// in true intro
                                    for (int i = leftExonIndex + 1; i < cdsEndExonIndex; i++) {
                                        relativeCdsDis += getExonEnd(i) - getExonStart(i);
//                                        relativeCdsDis += this.refExonList.get(i).end - this.refExonList.get(i).start;
                                    }
                                    relativeCdsDis += cdsEndPos - getExonStart(cdsEndExonIndex);
//                                    relativeCdsDis += cdsEndPos - refExonList.get(cdsEndExonIndex).start;
                                    // svAnnot.annotUpdate(new Intro(chrIndex, geneIndex, RNAIndex, relativeCdsDis, (byte) (refExonList.size() - leftExonIndex - 1), (short) -1, false), breakPointID);
                                    // svAnnot.SVAnnotationUpdate(RNAREGION.get("Intro"), mRNAName + ":" + "c." + relativeCdsDis + "-1" + ".>N:(" + this.refExonList.size() + "Exons):Intro" + (this.refExonList.size() - exonIndex - 1), breakPointID);
                                    return Intro.convert(geneIndex, RNAIndex, exonNum - leftExonIndex, ++relativeCdsDis, -1);
                                }
                            }
                        }
                    }
                }
            default:
                // svAnnot.annotUpdate(new UnknownRegion(), breakPointID);
                return UnknownRegion.convert(geneIndex, RNAIndex);
        }
    }

    public ByteCode getRNAName() {
        return RNAName;
    }

    @Override
    public int compareTo(RefRNA o) {
        int status = Integer.compare(startPos, o.startPos);
        return status == 0 ? Integer.compare(endPos, o.endPos) : status;
    }

    public NumericTranscriptFeature calcNGF(UnifiedSV sv) {
        if (ngfCalc == null) {
            ngfCalc = new NumericTranscriptFeatureCalculator(this);
        }
        return ngfCalc.parseQuantificationAnnotation(sv.getCoordinate(), sv.getLength(), sv.type());
    }

    static class NumericTranscriptFeatureCalculator {
        // ngf config
        boolean nonCoding;
        int startPos;
        int endPos;
        private Interval<Integer> promoter;
        int exonsLength;
        private IntervalTree<String, Integer> exonIntervalTree;
        int introLength;
        private IntervalTree<String, Integer> introIntervalTree;
        int UTR5Length;
        private IntervalTree<String, Integer> UTR5IntervalTree;
        int UTR3Length;
        private IntervalTree<String, Integer> UTR3IntervalTree;
        int nearbyLength = downstreamDis + upstreamDis;
        private IntervalTree<String, Integer> nearbyIntervalTree;

        public NumericTranscriptFeatureCalculator(RefRNA refRNA) {
            this.startPos = refRNA.startPos;
            this.endPos = refRNA.endPos;
            nonCoding = refRNA.cdsStartPos == refRNA.cdsEndPos;
            introIntervalTree = new IntervalTree<>();
            UTR5IntervalTree = new IntervalTree<>();
            UTR3IntervalTree = new IntervalTree<>();
            nearbyIntervalTree = new IntervalTree<>();
            refRNA.nonCoding = refRNA.cdsStartPos == refRNA.cdsEndPos || refRNA.cdsStartPos == -1;
            exonIntervalTree = new IntervalTree<>();
            for (int i = 0; i < refRNA.exonNum; i++) {
                exonIntervalTree.addInterval(refRNA.exonPosList[2 * i], refRNA.exonPosList[2 * i + 1], null);
                exonsLength += refRNA.exonPosList[2 * i + 1] - refRNA.exonPosList[2 * i];
                if (i < refRNA.exonNum - 1) {
                    introIntervalTree.addInterval(refRNA.exonPosList[2 * i + 1], refRNA.exonPosList[2 * i + 2], null);
                    introLength += refRNA.exonPosList[2 * i + 2] - refRNA.exonPosList[2 * i + 1];
                }
            }
            if (refRNA.strand == 0) {
                // + strand
                promoter = new Interval<>(refRNA.startPos - PROMOTER_DISTANCE, refRNA.startPos);
                nearbyIntervalTree.addInterval(refRNA.startPos - upstreamDis, refRNA.startPos, null);
                nearbyIntervalTree.addInterval(refRNA.endPos, refRNA.endPos + downstreamDis, null);
                if (!refRNA.nonCoding) {
                    for (int i = 0; i < refRNA.cdsStartExonIndex; i++) {
                        UTR5Length += refRNA.exonPosList[2 * i + 1] - refRNA.exonPosList[2 * i];
                        UTR5IntervalTree.addInterval(refRNA.exonPosList[2 * i], refRNA.exonPosList[2 * i + 1], null);
                    }
                    if (refRNA.cdsStartPos != refRNA.exonPosList[2 * refRNA.cdsStartExonIndex]) {
                        UTR5Length += refRNA.cdsStartPos - refRNA.exonPosList[2 * refRNA.cdsStartExonIndex];
                        UTR5IntervalTree.addInterval(refRNA.exonPosList[2 * refRNA.cdsStartExonIndex], refRNA.cdsStartPos, null);
                    }
                    for (int i = refRNA.cdsEndExonIndex + 1; i < refRNA.exonNum; i++) {
                        UTR3Length += refRNA.exonPosList[2 * i + 1] - refRNA.exonPosList[2 * i];
                        UTR3IntervalTree.addInterval(refRNA.exonPosList[2 * i], refRNA.exonPosList[2 * i + 1], null);
                    }
                    if (refRNA.cdsEndPos != refRNA.exonPosList[2 * refRNA.cdsEndExonIndex + 1]) {
                        UTR3Length += refRNA.exonPosList[2 * refRNA.cdsEndExonIndex + 1] - refRNA.cdsEndPos;
                        UTR3IntervalTree.addInterval(refRNA.cdsEndPos, refRNA.exonPosList[2 * refRNA.cdsEndExonIndex + 1], null);
                    }
                }
            } else {
                // - strand
                promoter = new Interval<>(refRNA.endPos, refRNA.endPos + PROMOTER_DISTANCE);
                nearbyIntervalTree.addInterval(refRNA.startPos - downstreamDis, refRNA.startPos, null);
                nearbyIntervalTree.addInterval(refRNA.endPos, refRNA.endPos + upstreamDis, null);
                if (!refRNA.nonCoding) {
                    for (int i = 0; i < refRNA.cdsStartExonIndex; i++) {
                        UTR3Length += refRNA.exonPosList[2 * i + 1] - refRNA.exonPosList[2 * i];
                        UTR3IntervalTree.addInterval(refRNA.exonPosList[2 * i], refRNA.exonPosList[2 * i + 1], null);
                    }
                    if (refRNA.cdsStartPos != refRNA.exonPosList[2 * refRNA.cdsStartExonIndex]) {
                        UTR3Length += refRNA.cdsStartPos - refRNA.exonPosList[2 * refRNA.cdsStartExonIndex];
                        UTR3IntervalTree.addInterval(refRNA.exonPosList[2 * refRNA.cdsStartExonIndex], refRNA.cdsStartPos, null);
                    }
                    for (int i = refRNA.cdsEndExonIndex + 1; i < refRNA.exonNum; i++) {
                        UTR5Length += refRNA.exonPosList[2 * i + 1] - refRNA.exonPosList[2 * i];
                        UTR5IntervalTree.addInterval(refRNA.exonPosList[2 * i], refRNA.exonPosList[2 * i + 1], null);
                    }
                    if (refRNA.cdsEndPos != refRNA.exonPosList[2 * refRNA.cdsEndExonIndex + 1]) {
                        UTR5Length += refRNA.exonPosList[2 * refRNA.cdsEndExonIndex + 1] - refRNA.cdsEndPos;
                        UTR5IntervalTree.addInterval(refRNA.cdsEndPos, refRNA.exonPosList[2 * refRNA.cdsEndExonIndex + 1], null);
                    }
                }
            }
        }


        public NumericTranscriptFeature parseQuantificationAnnotation(SVCoordinate coordinate, int SVLength, SVTypeSign type) {
            int SVStart = coordinate.getPos();
            int SVEnd = coordinate.getEnd();
            byte rnaFeature = (byte) 0;
            // check
            if (SVStart <= startPos - upstreamDis && SVEnd >= endPos + downstreamDis) {
                byte[] tmpCoverage = new byte[5];
                if (type.getIndex() == DUP_TYPE || type.getIndex() == CNV_TYPE) {
                    rnaFeature |= 0b00000010;
                } else if (type.getIndex() == INV_TYPE) {
                    rnaFeature |= 0b00000001;
                } else if (type.getIndex() == INV_DUP_TYPE) {
                    rnaFeature |= 0b00000011;
                } else {
                    rnaFeature |= 0b01111100;
                    Arrays.fill(tmpCoverage, (byte) 100);
                }
                return new NumericTranscriptFeature(rnaFeature, tmpCoverage);
            }
            boolean isInsert = type.getIndex() == INS_TYPE || (SVEnd - SVStart) <= 1;
            // 1. exon
            BaseArray<IntervalObject<String, Integer>> exonRelatedIntervals = exonIntervalTree.getOverlapsIntervals(SVStart, SVEnd);
            int overlapWithExon = 0;
            if (!exonRelatedIntervals.isEmpty()) {
                while (!exonRelatedIntervals.isEmpty()) {
                    Interval<Integer> overlaps = exonRelatedIntervals.popFirst().overlaps(SVStart, SVEnd);
                    if (!overlaps.nullity()) {
                        rnaFeature |= 0b01000000;
                        if (isInsert) {
                            overlapWithExon += SVLength;
                        } else {
                            overlapWithExon += (overlaps.end() - overlaps.start());
                        }
                    }
                }
            }
            // 2. Promoter
            int overlapWithPromoter = 0;
            Interval<Integer> overlaps = promoter.overlaps(SVStart, SVEnd);
            if (!overlaps.nullity()) {
                // exist overlap
                if (isInsert) {
                    overlapWithPromoter += SVLength;
                } else {
                    overlapWithPromoter += (overlaps.end() - overlaps.start());
                }
                rnaFeature |= 0b00100000;
            }
            // 3. UTR
            int overlapWithUTR = 0;
            if (!nonCoding) {
                BaseArray<IntervalObject<String, Integer>> UTR5RelatedIntervals = UTR5IntervalTree.getOverlapsIntervals(SVStart, SVEnd);
                BaseArray<IntervalObject<String, Integer>> UTR3RelatedIntervals = UTR3IntervalTree.getOverlapsIntervals(SVStart, SVEnd);
                if (!UTR5RelatedIntervals.isEmpty() || !UTR3RelatedIntervals.isEmpty()) {
                    for (IntervalObject<String, Integer> item : UTR5RelatedIntervals) {
                        Interval<Integer> overlaps1 = item.overlaps(SVStart, SVEnd);
                        if (!overlaps1.nullity()) {
                            rnaFeature |= 0b00010000;
                            if (isInsert) {
                                overlapWithUTR += SVLength;
                            } else {
                                overlapWithUTR += (overlaps1.end() - overlaps1.start());
                            }
                        }
                    }
                    for (IntervalObject<String, Integer> item : UTR3RelatedIntervals) {
                        Interval<Integer> overlaps1 = item.overlaps(SVStart, SVEnd);
                        if (!overlaps1.nullity()) {
                            rnaFeature |= 0b00010000;
                            if (isInsert) {
                                overlapWithUTR += SVLength;
                                continue;
                            }
                            overlapWithUTR += (overlaps1.end() - overlaps1.start());
                        }
                    }
                }
            }
            // 4. Intro
            int overlapWithIntro = 0;
            BaseArray<IntervalObject<String, Integer>> introRelatedIntervals = introIntervalTree.getOverlapsIntervals(SVStart, SVEnd);
            if (!introRelatedIntervals.isEmpty()) {
                while (!introRelatedIntervals.isEmpty()) {
                    Interval<Integer> overlaps1 = introRelatedIntervals.popFirst().overlaps(SVStart, SVEnd);
                    if (!overlaps1.nullity()) {
                        rnaFeature |= 0b00001000;
                        if (isInsert) {
                            overlapWithIntro += SVLength;
                            continue;
                        }
                        overlapWithIntro += (overlaps1.end() - overlaps1.start());
                    }
                }
            }
            // 5. nearby
            int overlapWithNearby = 0;
            BaseArray<IntervalObject<String, Integer>> nearbyRelatedIntervals = nearbyIntervalTree.getOverlapsIntervals(SVStart, SVEnd);
            if (!nearbyRelatedIntervals.isEmpty()) {
                for (IntervalObject<String, Integer> item : nearbyRelatedIntervals) {
                    Interval<Integer> overlaps1 = item.overlaps(SVStart, SVEnd);
                    if (!overlaps1.nullity()) {
                        rnaFeature |= 0b00000100;
                        if (isInsert) {
                            overlapWithNearby += SVLength;
                            continue;
                        }
                        overlapWithNearby += (overlaps1.end() - overlaps1.start());
                    }
                }
            }
            if (nonCoding) {
                rnaFeature = (byte) -rnaFeature;
            }
            return new NumericTranscriptFeature(rnaFeature,
                    (double) overlapWithExon / exonsLength,
                    (double) overlapWithPromoter / PROMOTER_DISTANCE,
                    (double) overlapWithUTR / (UTR5Length + UTR3Length),
                    (double) overlapWithIntro / introLength,
                    (double) overlapWithNearby / nearbyLength);
        }

        public void clearAll() {
            if (exonIntervalTree != null) {
                exonIntervalTree.clear();
            }
            if (introIntervalTree != null) {
                introIntervalTree.clear();
            }
        }

    }

    public RefRNA setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }

    public byte getStrand() {
        return strand;
    }

    public int getStartPos() {
        return startPos;
    }

    public RefRNA setStartPos(int startPos) {
        this.startPos = startPos;
        return this;
    }

    public int getEndPos() {
        return endPos;
    }

    public RefRNA setEndPos(int endPos) {
        this.endPos = endPos;
        return this;
    }

    public int getCdsStartPos() {
        return cdsStartPos;
    }

    public int getCdsEndPos() {
        return cdsEndPos;
    }

    public short getRNAIndex() {
        return RNAIndex;
    }

    public int getExonNum() {
        return exonNum;
    }

    public int getCdsStartExonIndex() {
        return cdsStartExonIndex;
    }

    public RefRNA setCdsStartExonIndex(int cdsStartExonIndex) {
        this.cdsStartExonIndex = cdsStartExonIndex;
        return this;
    }

    public int getCdsEndExonIndex() {
        return cdsEndExonIndex;
    }

    public RefRNA setCdsEndExonIndex(int cdsEndExonIndex) {
        this.cdsEndExonIndex = cdsEndExonIndex;
        return this;
    }

    public RefRNA setNonCoding(boolean nonCoding) {
        this.nonCoding = nonCoding;
        return this;
    }

    public int[] getExonPosList() {
        return exonPosList;
    }

    public RefRNA setExonPosList(int[] exonPosList) {
        this.exonPosList = exonPosList;
        return this;
    }

    public void dropNGF() {
        ngfCalc = null;
    }
}