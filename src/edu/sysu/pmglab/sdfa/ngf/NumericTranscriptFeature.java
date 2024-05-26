package edu.sysu.pmglab.sdfa.ngf;

import edu.sysu.pmglab.container.VolumeByteStream;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2024-03-19 07:22
 * @description
 */
public class NumericTranscriptFeature {
    byte rnaFeature;
    boolean parsed = false;
    byte[] coverage = new byte[5];
    // TODO: upstream, downstream, UTR3, UTR5
    static byte filterForCoding = -1;
    static double[] weights = new double[7];
    static byte[] coverageCutOff = new byte[5];
    static byte[] geneFeatureLevel = new byte[7];
    static StringBuilder outputBuilder = new StringBuilder();

    static {
        for (byte i = 0; i < 7; i++) {
            geneFeatureLevel[i] = (byte) (6 - i);
            weights[i] = Math.pow(2, geneFeatureLevel[i]);
        }
        for (int i = 0; i < 5; i++) {
            coverageCutOff[i] = -1;
        }
    }

    public NumericTranscriptFeature() {
        parsed = false;
    }

    public NumericTranscriptFeature(byte rnaFeature, byte[] coverage) {
        this.rnaFeature = rnaFeature;
        this.coverage = coverage;
    }

    public NumericTranscriptFeature(byte rnaFeature, double exonCoverage, double promoterCoverage,
                                    double utrCoverage, double introCoverage, double nearbyCoverage) {
        this.parsed = true;
        this.rnaFeature = rnaFeature;
        int count = 0;
        //region set coverage of rna element
        // exon
        if (exonCoverage >= 1) {
            coverage[count++] = 100;
        } else {
            coverage[count++] = (byte) ((int) (exonCoverage * 100));
        }
        // promoter
        if (promoterCoverage >= 1) {
            coverage[count++] = 100;
        } else {
            coverage[count++] = (byte) ((int) (promoterCoverage * 100));
        }
        // utr
        if (utrCoverage >= 1) {
            coverage[count++] = 100;
        } else {
            coverage[count++] = (byte) ((int) (utrCoverage * 100));
        }
        // intro
        if (introCoverage >= 1) {
            coverage[count++] = 100;
        } else {
            coverage[count++] = (byte) ((int) (introCoverage * 100));
        }
        // nearby
        if (nearbyCoverage >= 1) {
            coverage[count] = 100;
        } else {
            coverage[count] = (byte) ((int) (nearbyCoverage * 100));
        }
        //endregion
    }

    public void mergeInRNA(NumericTranscriptFeature rnaQuantificationAnnotation) {
        parsed = true;
        rnaFeature |= rnaQuantificationAnnotation.rnaFeature;
        for (int i = 0; i < coverage.length; i++) {
            coverage[i] += rnaQuantificationAnnotation.coverage[i];
        }
    }

    /**
     * Merge this and outer ngf into one and get the maximum overlap coverage of each element
     *
     * @param rnaQuantificationAnnotation the merged ngf
     */
    public void mergeInGene(NumericTranscriptFeature rnaQuantificationAnnotation) {
        parsed = rnaQuantificationAnnotation.parsed | parsed;
        rnaFeature |= rnaQuantificationAnnotation.rnaFeature;
        for (int i = 0; i < coverage.length; i++) {
            coverage[i] = (byte) Math.max(rnaQuantificationAnnotation.coverage[i], coverage[i]);
        }
    }

    public void reset() {
        this.rnaFeature = 0;
        Arrays.fill(coverage, (byte) 0);
        this.parsed = false;
    }

    public void writeCoverage(VolumeByteStream cache) {
        outputBuilder.delete(0, outputBuilder.length());
        outputBuilder.append("[");
        for (int i = 0; i < 5; i++) {
            outputBuilder.append(coverage[i]);
            if (i != 4) {
                outputBuilder.append(",");
            }
        }
        outputBuilder.append("]");
        cache.writeSafety(outputBuilder.toString());
    }

    public static boolean filter(NumericTranscriptFeature rnaFeature) {
        byte[] coverage = rnaFeature.coverage;
        switch (filterForCoding) {
            case -1:
                break;
            case 0:
                // filter for noncoding RNA
                if (rnaFeature.rnaFeature > 0) {
                    return false;
                }
                break;
            case 1:
                // filter for coding
                if (rnaFeature.rnaFeature < 0) {
                    return false;
                }
                break;
        }
        for (int i = 0; i < 5; i++) {
            if (coverage[i] < coverageCutOff[i]) {
                rnaFeature.reset();
                return false;
            }
        }
        return true;
    }

    public static void setCoverageCutOff(byte[] coverageCutOff) {
        System.arraycopy(coverageCutOff, 0, NumericTranscriptFeature.coverageCutOff, 0, 5);
    }

    public static void setFilterForCoding(byte filterForCoding) {
        NumericTranscriptFeature.filterForCoding = filterForCoding;
    }

    public byte getRnaFeature() {
        // Check if the sign bit (MSB) is set
        boolean isNegative = (rnaFeature & 0x80) != 0;
        // Extract the numeric value (7 bits)
        byte value = (byte) (rnaFeature & 0x7F);
        // Adjust the value based on the sign bit
        if (isNegative) {
            value = (byte) -value;
        }
        return value;
    }
}
