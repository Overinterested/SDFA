package edu.sysu.pmglab.easytools.annotation;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.BaseArrayEncoder;
import edu.sysu.pmglab.container.array.ByteCodeArray;

/**
 * @author Wenjie Peng
 * @create 2024-05-04 23:09
 * @description
 */
public class KeyMapFieldType implements FieldType {
    CallableSet<ByteCode> keys = new CallableSet<>();
    final VolumeByteStream cache = new VolumeByteStream();
    @Override
    public ByteCode encode(Object o) {
        if (o instanceof ByteCode) {
            cache.reset();
            ByteCode src = (ByteCode) o;
            int index = keys.indexOfValue(src);
            if (index == -1) {
                keys.add(src.asUnmodifiable());
                index = keys.size() - 1;
            }
            cache.writeSafety(index);
            return cache.toUnmodifiableByteCode();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object decode(ByteCode byteCode) {
        int index = byteCode.toInt();
        return keys.getByIndex(index);
    }

    public void loadKeys(ByteCode src) {
        ByteCodeArray keys = (ByteCodeArray) ArrayType.decode(src);
        keys.addAll(keys.toArray());
    }

    public ByteCode popKeys(){
        BaseArrayEncoder encoder = ByteCodeArray.getEncoder(keys.size());
        for (ByteCode key : keys) {
            encoder.add(key);
        }
        return encoder.flush().toByteCode();
    }

}
