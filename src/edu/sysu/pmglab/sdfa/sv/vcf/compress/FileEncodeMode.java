package edu.sysu.pmglab.sdfa.sv.vcf.compress;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;

import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 02:39
 * @description
 */
public enum FileEncodeMode {
    FLAG_TYPE("Flag", FieldType.bool),
    FLOAT_TYPE("Float", FieldType.float32),
    DOUBLE_TYPE("Double", FieldType.float64),
    STRING_TYPE("String", FieldType.bytecode),
    INTEGER_TYPE("Integer", FieldType.varInt32),
    BYTECODE_TYPE("ByteCode", FieldType.bytecode);

    final ByteCode stringType;
    final FieldType encodeType;
    static final HashMap<String, FieldType> stringFieldConverter = new HashMap<>();
    static final DefaultValueWrapper<FieldType> defaultFieldType = new DefaultValueWrapper<>(FieldType.bytecode);

    static {
        stringFieldConverter.put("Flag", FieldType.bool);
        stringFieldConverter.put("Float", FieldType.float32);
        stringFieldConverter.put("Double", FieldType.float64);
        stringFieldConverter.put("String", FieldType.bytecode);
        stringFieldConverter.put("Integer", FieldType.varInt32);
        stringFieldConverter.put("ByteCode", FieldType.bytecode);
    }

    FileEncodeMode(String stringType, FieldType encodeType) {
        this.encodeType = encodeType;
        this.stringType = new ByteCode(stringType).asUnmodifiable();
    }

    public FieldType getEncodeType() {
        return encodeType;
    }

    public ByteCode getStringType() {
        return stringType;
    }

    public static FieldType getFieldType(Object object) {
        if (object instanceof FileEncodeMode) {
            return ((FileEncodeMode) object).encodeType;
        } else {
            return defaultFieldType.getValue(stringFieldConverter.get(object.toString()));
        }
    }
}
