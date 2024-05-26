package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;

/**
 * @author Wenjie Peng
 * @create 2024-03-30 06:57
 * @description
 */
public class AnnotationResourceFeature implements Comparable<AnnotationResourceFeature> {
    Chromosome chr;
    int pos;
    int end;
    ByteCode encodeInfo;

    public AnnotationResourceFeature(Chromosome chr, int pos, int end, BaseArray<ByteCode> info) {
        this.chr = chr;
        this.pos = pos;
        this.end = end;
        this.encodeInfo = info.get(3, info.size() - 3).encode().toUnmodifiableByteCode();
    }

    @Override
    public int compareTo(AnnotationResourceFeature o) {
        int status = Chromosome.compare(chr, o.chr);
        if (status != 0) {
            return status;
        }
        status = Integer.compare(pos, o.pos);
        return status == 0 ? Integer.compare(end, o.end) : status;
    }

    public void toIntervalResource(IRecord record) {
        record.set(0, chr.getIndex());
        record.set(1, pos);
        record.set(2, end);
        ByteCodeArray decode = (ByteCodeArray) ArrayType.decode(encodeInfo);
        for (int i = 3; i < record.size(); i++) {
            record.set(i, decode.get(i - 3));
        }
    }

    public void toSVDatabaseResource(IRecord record) {
        record.set(0, chr.getIndex());
        record.set(1, pos);
        record.set(2, end);
        ByteCodeArray decode = (ByteCodeArray) ArrayType.decode(encodeInfo);
        record.set(3, decode.get(0).toInt());
        record.set(4, SVTypeSign.get(1).getIndex());
        for (int i = 5; i < record.size(); i++) {
            record.set(i, decode.get(i - 5));
        }
    }
    public void clear(){
        encodeInfo = null;
    }
}
