package edu.sysu.pmglab.sdfa;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.annotation.Future;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;
import edu.sysu.pmglab.sdfa.sv.SVGenotypes;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.toolkit.RefAndAltFieldType;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;

@Future(value = "Transfer it as Enum class",
        reason = "There are only several encode modes and can be reusable.")
public class SDFEncode {
    final int encodeMode;
    final Array<FieldType> extendedEncodeMethods = new Array<>(7);
    private static final ByteCodeArray emptyByteCodeArray = new ByteCodeArray(new ByteCode[0]);
    private static final DefaultValueWrapper<ByteCode> wrapper = DefaultValueWrapper.emptyBytecodeWrapper;

    public SDFEncode(int encodeMode) {
        this.encodeMode = encodeMode;
        for (int i = 0; i < 7; i++) {
            extendedEncodeMethods.add(null);
        }
        switch (encodeMode) {
            case 0:
                break;
            case 1:
            case 2:
            case 3:
                RefAndAltFieldType.mode = 1;
                RefAndAltFieldType refAndAltFieldType = new RefAndAltFieldType();
                extendedEncodeMethods.set(2, refAndAltFieldType);
                extendedEncodeMethods.set(3, refAndAltFieldType);
                VCF2SDF.dropFormat = encodeMode == 3;
                VCF2SDF.multiInfoForCSV = encodeMode == 1;
                break;
        }
    }

    public IRecord encode(IRecord record, UnifiedSV sv) {
        return encode$0(record, sv);
    }

    private IRecord encode$0(IRecord record, UnifiedSV sv) {
        // location
        SVGenotypes genotypes = sv.getGenotypes();
        record.set(0, sv.getCoordinate().encode());
        record.set(1, sv.getLength());
        record.set(2, sv.getTypeIndex());
        // gty
        record.set(3, genotypes.encodeGTs());
        //  gty format
        if (extendedEncodeMethods.get(0) == null) {
            if (VCF2SDF.dropFormat) {
                record.set(4, ByteCode.EMPTY);
            } else {
                ByteCodeArray properties = genotypes.encodeProperties();
                record.set(4, properties.popFirst(properties.size()));
            }
        } else {
            record.set(4, extendedEncodeMethods.get(0).encode(genotypes.getProperties()));
        }
        // ID, Ref, Alt, Qual, Filter
        if (extendedEncodeMethods.get(1) == null) {
            ByteCode value = wrapper.getValue(sv.getID());
            if (VCF2SDF.lineExtractAndSort) {
                record.set(5, value);
            } else {
                record.set(5, value.asUnmodifiable());
            }
        } else {
            record.set(5, extendedEncodeMethods.get(1).encode(wrapper.getValue(sv.getID())));
        }
        // Ref
        if (extendedEncodeMethods.get(2) == null) {
            ByteCode value = wrapper.getValue(sv.getRef());
            if (VCF2SDF.lineExtractAndSort) {
                record.set(6, value);
            } else {
                record.set(6, value.asUnmodifiable());
            }
        } else {
            record.set(6, extendedEncodeMethods.get(2).encode(wrapper.getValue(sv.getRef())));
        }
        // Alt
        if (extendedEncodeMethods.get(3) == null) {
            ByteCode value = wrapper.getValue(sv.getAlt());
            if (VCF2SDF.lineExtractAndSort) {
                record.set(7, value);
            } else {
                record.set(7, value.asUnmodifiable());
            }
        } else {
            record.set(7, extendedEncodeMethods.get(3).encode(wrapper.getValue(sv.getAlt())));
        }
        // Qual
        if (extendedEncodeMethods.get(4) == null) {
            ByteCode value = wrapper.getValue(sv.getQual());
            if (VCF2SDF.lineExtractAndSort) {
                record.set(8, value);
            } else {
                record.set(8, value.asUnmodifiable());
            }
        } else {
            record.set(8, extendedEncodeMethods.get(4).encode(wrapper.getValue(sv.getQual())));
        }
        // Filter
        if (extendedEncodeMethods.get(5) == null) {
            ByteCode value = wrapper.getValue(sv.getFilterField());
            if (VCF2SDF.lineExtractAndSort) {
                record.set(9, value);
            } else {
                record.set(9, value.asUnmodifiable());
            }
        } else {
            record.set(9, extendedEncodeMethods.get(5).encode(wrapper.getValue(sv.getFilterField())));
        }
        // INFO
        if (extendedEncodeMethods.get(6) == null) {
            record.set(10, wrapWithEmpty(sv.getSpecificInfoField()));
        } else {
            record.set(10, extendedEncodeMethods.get(6).encode(wrapWithEmpty(sv.getSpecificInfoField())));
        }
        // csv location
        record.set(11, sv.getIndexOfFile());
        record.set(12, sv.getEncodeCSVChrIndex());
        if (record.size() > 13) {
            Array<ByteCode> property = sv.getProperty();
            for (int i = 13; i < record.size(); i++) {
                record.set(i, property.get(i - 13));
            }
        }
        return record;
    }

    private static ByteCodeArray wrapWithEmpty(ByteCodeArray tmp) {
        if (tmp == null || VCF2SDF.dropInfoField || tmp.isEmpty()) {
            return emptyByteCodeArray;
        }
        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i) == null) {
                tmp.set(i, ByteCode.EMPTY);
            }
        }
        return new ByteCodeArray(tmp.popFirst(tmp.size()));
    }

    /**
     * note the first GT field has been extracted
     *
     * @param encodeFormat encode method
     * @return this
     */
    public SDFEncode setFormatEncode(FieldType encodeFormat) {
        extendedEncodeMethods.set(0, encodeFormat);
        return this;
    }

    public SDFEncode setIDEncode(FieldType encodeID) {
        extendedEncodeMethods.set(1, encodeID);
        return this;
    }

    public SDFEncode setRefEncode(FieldType encodeRef) {
        extendedEncodeMethods.set(2, encodeRef);
        return this;
    }

    public SDFEncode setAltEncode(FieldType encodeAlt) {
        extendedEncodeMethods.set(3, encodeAlt);
        return this;
    }

    public SDFEncode setQualEncode(FieldType encodeQual) {
        extendedEncodeMethods.set(4, encodeQual);
        return this;
    }

    public SDFEncode setFilterEncode(FieldType encodeFilter) {
        extendedEncodeMethods.set(5, encodeFilter);
        return this;
    }

    public SDFEncode setInfoEncode(FieldType encodeInfoValues) {
        extendedEncodeMethods.set(6, encodeInfoValues);
        return this;
    }

    public Array<FieldType> getExtendedEncodeMethods() {
        return extendedEncodeMethods;
    }
}
