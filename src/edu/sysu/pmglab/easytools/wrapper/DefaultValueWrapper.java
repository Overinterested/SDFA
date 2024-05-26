package edu.sysu.pmglab.easytools.wrapper;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.sdfa.sv.vcf.calling.AbstractCallingParser;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author Wenjie Peng
 * @create 2024-03-25 00:42
 * @description
 */
public class DefaultValueWrapper<T> {
    final T defaultValue;
    public static DefaultValueWrapper<ByteCode> emptyBytecodeWrapper = new DefaultValueWrapper<>(ByteCode.EMPTY);
    public static DefaultValueWrapper<ByteCode> periodBytecodeWrapper = new DefaultValueWrapper<>(new ByteCode(new byte[]{ByteCode.PERIOD}));

    public DefaultValueWrapper(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T getValue(T value) {
        return value == null ? defaultValue : value;
    }

    public <A> T getByDefault(A raw, Function<A, T> function) {
        try {
            return function.apply(raw);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T> T[] getWrappedArray(Collection<T> array, T defaultValue){
        DefaultValueWrapper<T> wrapper = new DefaultValueWrapper<>(defaultValue);
        Array<T> res = new Array<>();
        for (T t : array) {
            res.add(wrapper.getValue(t));
        }
        return res.toArray();
    }
}
