package edu.sysu.pmglab.sdfa.sv;

import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.toolkit.Contig;

/**
 * @author Wenjie Peng
 * @create 2024-03-22 08:39
 * @description
 */
public class SVCoordinate implements Comparable<SVCoordinate> {
    int pos;
    int end;
    Chromosome chr;

    public SVCoordinate(int pos, int end, Chromosome chromosome) {
        this.pos = pos;
        this.end = end;
        this.chr = chromosome;
    }

    public SVCoordinate(Chromosome chr, int pos) {
        this.chr = chr;
        this.pos = pos;
        this.end = -1;
    }

    public Chromosome getChr() {
        return chr;
    }

    public int getPos() {
        return pos;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public int compareTo(SVCoordinate o) {
        int status = Integer.compare(chr.getIndex(), o.chr.getIndex());
        if (status == 0) {
            status = Integer.compare(pos, o.pos);
            return status == 0 ? Integer.compare(end, o.end) : status;
        }
        return status;
    }

    public int[] encode() {
        return new int[]{chr.getIndex(), pos, end};
    }

    public static SVCoordinate decode(int[] encode) {
        return new SVCoordinate(encode[1], encode[2], Chromosome.get(encode[0]));
    }

    public static SVCoordinate decode(int[] encode, Contig contig) {
        return new SVCoordinate(encode[1], encode[2], contig.getByIndex(encode[0]));
    }


    public SVCoordinate setChr(Chromosome chr) {
        this.chr = chr;
        return this;
    }

    public static class CoordinateInterval implements Comparable<CoordinateInterval> {
        int chr;
        int pos;
        int end;

        public CoordinateInterval(int chr, int pos, int end) {
            this.chr = chr;
            this.pos = pos;
            this.end = end;
        }

        @Override
        public int compareTo(CoordinateInterval o) {
            int status = Integer.compare(chr, o.chr);
            if (status != 0) {
                return status;
            }
            status = Integer.compare(pos, o.pos);
            return status == 0 ? Integer.compare(end, o.end) : status;
        }

        public static CoordinateInterval decode(int[] encodeSVCoordinate) {
            return new CoordinateInterval(encodeSVCoordinate[0], encodeSVCoordinate[1], encodeSVCoordinate[2]);
        }

        public int getChr() {
            return chr;
        }

        public int getPos() {
            return pos;
        }
    }
}
