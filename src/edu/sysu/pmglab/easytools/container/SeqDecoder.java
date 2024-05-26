package edu.sysu.pmglab.easytools.container;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.IntArray;

/**
 * @author Wenjie Peng
 * @create 2024-04-03 00:31
 * @description
 */
public class SeqDecoder {
    int size;
    IntArray seqEncodeArray;

    private SeqDecoder(int size, IntArray seqEncodeArray) {
        this.size = size;
        this.seqEncodeArray = seqEncodeArray;
    }

    public static SeqDecoder of(ByteCode src) {
        IntArray decoder = IntArray.wrap((int[]) FieldType.int32Array.decode(src));
        int size = decoder.get(0);
        return new SeqDecoder(size, decoder.get(1, decoder.size() - 1));
    }

    public ByteCode toByteCode() {
        VolumeByteStream cache = new VolumeByteStream(size);
        int fullSize = size >>> 4;
        int restSize = size % 16;
        for (int i = 0; i < fullSize; i++) {
            int encodeValue = seqEncodeArray.get(i);
            for (int j = 0; j < 16; j++) {
                int tmp = encodeValue >> (30 - 2 * j);
                cache.write(encodeToASCII((byte) (tmp & 0b11)));
            }
        }
        if (restSize == 0) {
            return cache.toByteCode();
        }
        int last = seqEncodeArray.popLast();
        for (int i = 0; i < restSize; i++) {
            int tmp = last >>> (2 * restSize - 2 * i - 2);
            cache.write(encodeToASCII((byte) (tmp & 0b11)));
        }
        return cache.toByteCode();
    }

    private byte encodeToASCII(byte encodeByte) {
        switch (encodeByte) {
            case 0:
                return 65;
            case 1:
                return 84;
            case 2:
                return 67;
            case 3:
                return 71;
            default:
                throw new UnsupportedOperationException(encodeByte + " can't be parsed to ATCG");
        }
    }

    public static void main(String[] args) {
        SeqEncoder seqEncoder = new SeqEncoder();
        String seqs = "ATATGCTGATGCTGTGTACGTACATCTGTACGTACATCGCTGTATGCTGTGTACGTACATCGTACATGCTGTGTACGTACATCGTACATC";
        byte[] bytes = seqs.getBytes();
        seqEncoder.addSeqs(bytes);
        SeqDecoder of = SeqDecoder.of(seqEncoder.encode());
        ByteCode byteCode = of.toByteCode();
        String string = byteCode.toString();
        System.out.println(seqs);
        System.out.println(string);
    }
}
