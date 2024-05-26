package edu.sysu.pmglab.easytools.container;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.IntArray;

/**
 * @author Wenjie Peng
 * @create 2024-04-02 23:37
 * @description
 */
public class SeqEncoder implements FieldType {
    int size;
    private int curr;
    int currIntSize = 0;
    IntArray encode = new IntArray();

    public void addSeq(byte byteOfSeq) {
        if (currIntSize == 16) {
            encode.add(curr);
            curr = 0;
            currIntSize = 0;
        }
        encodeOne(byteOfSeq);
    }

    public void addSeqs(byte[] bytesOfSeq) {
        for (byte seq : bytesOfSeq) {
            addSeq(seq);
        }
    }

    private void encodeOne(byte byteOfSeq) {
        switch (byteOfSeq) {
            case (byte) 65:
            case (byte) 97:
                // A,a
                curr = curr << 2;
                break;
            case (byte) 84:
            case (byte) 116:
                // T,t
                curr = curr << 2 | 1;
                break;
            case (byte) 67:
            case (byte) 99:
                // C,c
                curr = curr << 2 | 2;
                break;
            case (byte) 71:
            case (byte) 103:
                // G,g
                curr = curr << 2 | 3;
                break;
            default:
                throw new UnsupportedOperationException(String.valueOf(byteOfSeq) + " can't be read.");
        }
        size++;
        currIntSize++;
    }

    public int[] covert() {
        if (size != 0) {
            int encodeSize = encode.size();
            int[] res;
            if (currIntSize != 0) {
                res = new int[encodeSize + 2];
            } else {
                res = new int[encodeSize + 1];
            }
            res[0] = size;
            for (int i = 0; i < encodeSize; i++) {
                res[i + 1] = encode.get(i);
            }
            encode.clear();
            if (currIntSize != 0) {
                res[encodeSize + 1] = curr;
            }
            return res;
        }
        return new int[0];
    }

    public ByteCode encode() {
        int[] covert = covert();
        return FieldType.int32Array.encode(covert);
    }

    public void clear() {
        size = 0;
        curr = 0;
        currIntSize = 0;
        encode.clear();
    }

    @Override
    public ByteCode encode(Object o) {
        if (o instanceof byte[]) {
            addSeqs((byte[]) o);
            return encode();
        } else if (o instanceof ByteCode) {
            ByteCode src = (ByteCode) o;
            int length = src.length();
            if (length == 0){
                return ByteCode.EMPTY;
            }else {
                for (int i = 0; i < length; i++) {
                    addSeq(src.byteAt(i));
                }
            }
            return encode();
        } else {
            throw new UnsupportedOperationException(o.getClass() + " is not byte[] and can't be encoded by seq encode");
        }
    }

    @Override
    public Object decode(ByteCode byteCode) {
        return SeqDecoder.of(byteCode).toByteCode();
    }
}
