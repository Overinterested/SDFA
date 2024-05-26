package edu.sysu.pmglab.sdfa.genome.brief;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.IntArray;

/**
 * @author Wenjie Peng
 * @create 2023-04-21 15:51
 * @description
 */
public class RefRNAName {
    final int exonSize;
    final ByteCode RNAName;

    public RefRNAName(ByteCode RNAName, int exonSize) {
        this.RNAName = RNAName;
        this.exonSize = exonSize;
    }

    public ByteCode getRNAName() {
        return RNAName;
    }

    public static RefRNAName decode(IRecord record) {
        return new RefRNAName(((ByteCode) record.get(4)).asUnmodifiable(),
                IntArray.wrap(record.get(6)).size() / 2);
    }

    public static ByteCode getRegion(int mode) {
        switch (mode) {
            case 0:
                return new ByteCode(new byte[]{ByteCode.L, ByteCode.e, ByteCode.f, ByteCode.t, ByteCode.I,
                        ByteCode.n, ByteCode.t, ByteCode.e, ByteCode.r, ByteCode.g, ByteCode.e, ByteCode.n,
                        ByteCode.i, ByteCode.c});
            case 1:
                return new ByteCode(new byte[]{ByteCode.u, ByteCode.p, ByteCode.s, ByteCode.t, ByteCode.r,
                        ByteCode.e, ByteCode.a, ByteCode.m});
            case 2:
                return new ByteCode(new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.FIVE});
            case 3:
                return new ByteCode(new byte[]{ByteCode.L, ByteCode.e, ByteCode.f, ByteCode.t, ByteCode.I,
                        ByteCode.n, ByteCode.t, ByteCode.r, ByteCode.o});
            case 4:
                return new ByteCode(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n});
            case 5:
                return new ByteCode(new byte[]{ByteCode.i, ByteCode.n, ByteCode.t, ByteCode.r, ByteCode.o});
            case 6:
                return new ByteCode(new byte[]{ByteCode.R, ByteCode.i, ByteCode.g, ByteCode.h, ByteCode.t,
                        ByteCode.I, ByteCode.n, ByteCode.t, ByteCode.r, ByteCode.o});
            case 7:
                return new ByteCode(new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.THREE});
            case 8:
                return new ByteCode(new byte[]{ByteCode.D, ByteCode.o, ByteCode.w, ByteCode.n, ByteCode.s,
                        ByteCode.t, ByteCode.r, ByteCode.e, ByteCode.a, ByteCode.m});
            case 9:
                return new ByteCode(new byte[]{ByteCode.R, ByteCode.i, ByteCode.g, ByteCode.h, ByteCode.t,
                        ByteCode.I, ByteCode.n, ByteCode.t, ByteCode.e, ByteCode.r, ByteCode.g, ByteCode.e,
                        ByteCode.n, ByteCode.i, ByteCode.c});
            case 10:
                return new ByteCode(new byte[]{ByteCode.N, ByteCode.C, ByteCode.R, ByteCode.N, ByteCode.A});
            case 11:
                return new ByteCode(new byte[]{ByteCode.W, ByteCode.h, ByteCode.o, ByteCode.l, ByteCode.e,
                        ByteCode.G, ByteCode.e, ByteCode.n, ByteCode.e, ByteCode.C, ByteCode.o, ByteCode.v,
                        ByteCode.e, ByteCode.r, ByteCode.e, ByteCode.d});
            case 12:
                return new ByteCode(new byte[]{ByteCode.U, ByteCode.n, ByteCode.k, ByteCode.n, ByteCode.o,
                        ByteCode.w, ByteCode.n, ByteCode.R, ByteCode.e, ByteCode.g, ByteCode.i, ByteCode.o,
                        ByteCode.n});
            default:
                throw new UnsupportedOperationException("No such RNA element");
        }
    }

    public int numOfExons() {
        return exonSize;
    }
}