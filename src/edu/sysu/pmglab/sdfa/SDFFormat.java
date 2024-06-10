package edu.sysu.pmglab.sdfa;

import edu.sysu.pmglab.ccf.CCFFieldGroupMetas;
import edu.sysu.pmglab.ccf.CCFFormat;
import edu.sysu.pmglab.ccf.type.FieldType;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 04:21
 * @description
 */
public class SDFFormat extends CCFFormat {
    /**
     * SVF 版本: CCF  Version + SVF Version
     */
    public final static String EXTENSION = ".sdf";

    /**
     * 默认压缩级别, 压缩级别直接影响压缩比
     */
    public final static int DEFAULT_COMPRESSION_LEVEL = 3;

    /**
     * 默认块大小
     */
    public static final int DEFAULT_VARIANT_NUM = 16384;

    /**
     * 默认块大小 32 MB
     */
    public static final int DEFAULT_BLOCK_MEMORY = 33554432;

    /**
     * 构造器方法
     *
     * @param compressionLevel 压缩器的压缩级别
     * @param maxVariantNum    每个块最多包含的位点数
     */
    public SDFFormat(int compressionLevel, int maxVariantNum) {
        super(compressionLevel, maxVariantNum, DEFAULT_BLOCK_MEMORY);
    }


    @Override
    public String toString() {
        return "[compressionLevel=" + this.compressionLevel + ", maxVariantNum=" + this.maxRecordNum + "]";
    }

    /**
     * SVF 强制字段 (不允许命名)
     */
    public static final CCFFieldGroupMetas SDFFields = new CCFFieldGroupMetas()
            // 0
            .addField("Location::Coordinate", FieldType.int32Array)
            // 1
            .addField("Location::SVLength", FieldType.varInt32)
            // 2
            .addField("Location::TypeSign", FieldType.varInt32)
            // 3
            .addField("Format::Genotypes", FieldType.int16Array)
            // 4
            .addField("Format::Other", FieldType.bytecodeArray)
            // 5
            .addField("Field::ID", FieldType.bytecode)
            // 6
            .addField("Field::Ref", FieldType.bytecode)
            // 7
            .addField("Field::Alt", FieldType.bytecode)
            // 8
            .addField("Field::Qual", FieldType.bytecode)
            // 9
            .addField("Field::Filter", FieldType.bytecode)
            // 10
            .addField("Field::InfoField", FieldType.bytecodeArray)
            // 11
            .addField("CSVLocation::IndexOfFile", FieldType.varInt32)
            // 12
            .addField("CSVLocation::ChrIndexArray", FieldType.int32Array);
    /**
     * 字段名检查
     *
     * @param fieldName 字段名
     * @return 是否通过检查
     */
    public static boolean fieldNameChecker(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) {
            return false;
        }
        return !SDFFields.containsField(fieldName);
    }
}
