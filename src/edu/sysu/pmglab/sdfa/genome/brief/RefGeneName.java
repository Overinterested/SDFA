package edu.sysu.pmglab.sdfa.genome.brief;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;

/**
 * @author Wenjie Peng
 * @create 2023-04-21 15:22
 * @description
 */
public class RefGeneName {
    final short RNANum;
    final ByteCode geneName;
    Array<RefRNAName> RNANames;

    public RefGeneName(ByteCode geneName, short RNANum) {
        this.RNANum = RNANum;
        this.geneName = geneName;
        RNANames = new Array<>(RNANum);
    }

    public void updateRNAName(ByteCode RNAName, int exonSize) {
        RNANames.add(new RefRNAName(RNAName, exonSize));
    }

    public void updateRNAName(RefRNAName refRNAName){
        RNANames.add(refRNAName);
    }

    public static RefGeneName decode(IRecord record) {
        return new RefGeneName(((ByteCode) record.get(4)).asUnmodifiable(), (short) (int) record.get(2));
    }
    public RefRNAName getRNAName(int RNAIndex) {
        return RNANames.get(RNAIndex);
    }

    public ByteCode getGeneName() {
        return geneName;
    }

    public Array<RefRNAName> getRNANames() {
        return RNANames;
    }

    public short numOfRNA() {
        return RNANum;
    }
}
