package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.easytools.container.SeqDecoder;
import edu.sysu.pmglab.easytools.container.SeqEncoder;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;

/**
 * @author Wenjie Peng
 * @create 2024-04-02 22:09
 * @description
 */
public class SDFRefAltFieldType implements FieldType {
    public static int mode;
    SeqEncoder seqEncoder = new SeqEncoder();
    private static final byte[] TYPE_ALT = new byte[]{0};
    private static final byte[] SEQUENCE_ALT = new byte[]{1};
    private static final byte[] RAW_ALT = new byte[]{2};
    private final VolumeByteStream cache = new VolumeByteStream();

    static {
        FieldType.add("alt", new SDFRefAltFieldType());
    }


    @Override
    public ByteCode encode(Object o) {
        cache.reset();
        ByteCode altByteCode = (ByteCode) o;
        if (mode == 1) {
            altByteCode = multiEncode(altByteCode, cache);
            if (VCF2SDF.lineExtractAndSort){
                return altByteCode;
            }else {
                return altByteCode.asUnmodifiable();
            }
        }
        if (VCF2SDF.lineExtractAndSort){
            return altByteCode;
        }else {
            return altByteCode.asUnmodifiable();
        }
    }

    @Override
    public Object decode(ByteCode byteCode) {
        VolumeByteStream cache = new VolumeByteStream();
        if (mode == 0) {
            return byteCode;
        } else {
            byte mode = byteCode.byteAt(0);
            switch (mode) {
                case 0:
                    ByteCode byteCode1 = byteCode.subByteCode(1);
                    int decode = (int) ValueUtils.ValueDecoder.decode(byteCode1);
                    cache.write(new byte[]{60});
                    cache.writeSafety(SVTypeSign.getByIndex(decode).getName());
                    cache.writeSafety(new byte[]{62});
                    break;
                case 1:
                    cache.writeSafety(SeqDecoder.of(byteCode.subByteCode(1)).toByteCode());
                    break;
                default:
                    cache.writeSafety(byteCode.subByteCode(1));
            }
        }
        ByteCode res = cache.toByteCode().asUnmodifiable();
        cache.close();
        return res;
    }


    private ByteCode multiEncode(ByteCode alt, VolumeByteStream cache) {
        if (alt.length() <= 4) {
            cache.writeSafety(RAW_ALT);
            cache.writeSafety(alt);
            return cache.toByteCode();
        }
        if (alt.byteAt(0) == 60) {
            int endIndex = -1;
            for (int i = 0; i < alt.length(); i++) {
                if (alt.byteAt(i) == 62) {
                    endIndex = i;
                    break;
                }
            }
            if (endIndex == -1) {
                cache.writeSafety(RAW_ALT);
                cache.writeSafety(alt.asUnmodifiable());
                return cache.toByteCode();
            }
            ByteCode type = alt.subByteCode(1, endIndex);
            SVTypeSign svTypeSign = SVTypeSign.get(type);
            if (svTypeSign.equals(SVTypeSign.unknown)) {
                cache.writeSafety(RAW_ALT);
                cache.writeSafety(alt);
            } else {
                cache.writeSafety(TYPE_ALT);
                cache.writeSafety(ValueUtils.ValueEncoder.encodeInt(svTypeSign.getIndex()));
            }
        } else {
            boolean raw = false;
            seqEncoder.clear();
            for (int i = 0; i < alt.length(); i++) {
                byte tmp = alt.byteAt(i);
                if (tmp == 91 || tmp == 93) {
                    raw = true;
                    break;
                } else {
                    try {
                        seqEncoder.addSeq(tmp);
                    } catch (UnsupportedOperationException e) {
                        raw = true;
                        break;
                    }
                }
            }
            if (raw) {
                cache.writeSafety(RAW_ALT);
                cache.writeSafety(alt);
            } else {
                cache.writeSafety(SEQUENCE_ALT);
                cache.writeSafety(seqEncoder.encode());
            }
            return cache.toByteCode();
        }
        return cache.toByteCode();
    }

}
