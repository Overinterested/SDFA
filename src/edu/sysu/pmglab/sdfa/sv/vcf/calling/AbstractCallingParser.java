package edu.sysu.pmglab.sdfa.sv.vcf.calling;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.*;
import edu.sysu.pmglab.sdfa.sv.vcf.ReusableSVArray;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;
import edu.sysu.pmglab.sdfa.toolkit.Contig;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 21:15
 * @description
 */
public abstract class AbstractCallingParser {
    Contig contig;
    ReusableSVArray unifiedSVArray;
    IntArray ignoreInfoIndex = new IntArray();
    public static final ByteCode CHR_BYTECODE = new ByteCode(new byte[]{ByteCode.C, ByteCode.H, ByteCode.R});
    public static final ByteCode CHR2_BYTECODE = new ByteCode(new byte[]{edu.sysu.pmglab.container.ByteCode.C, edu.sysu.pmglab.container.ByteCode.H, edu.sysu.pmglab.container.ByteCode.R, edu.sysu.pmglab.container.ByteCode.TWO});
    public static final ByteCode POS_BYTECODE = new ByteCode(new byte[]{edu.sysu.pmglab.container.ByteCode.P, edu.sysu.pmglab.container.ByteCode.O, edu.sysu.pmglab.container.ByteCode.S});
    public static final ByteCode POS2_BYTECODE = new ByteCode(new byte[]{edu.sysu.pmglab.container.ByteCode.P, ByteCode.O, ByteCode.S, ByteCode.TWO});
    public static final ByteCode SVLEN_BYTECODE = new ByteCode(new byte[]{ByteCode.S, ByteCode.V, ByteCode.L, ByteCode.E, ByteCode.N});
    public static final ByteCode SVLEN2_BYTECODE = new ByteCode(new byte[]{ByteCode.S, ByteCode.V, ByteCode.L, ByteCode.E, ByteCode.N, ByteCode.TWO});
    public static final ByteCode END_BYTECODE = new ByteCode(new byte[]{ByteCode.E, ByteCode.N, ByteCode.D});
    public static final ByteCode END2_BYTECODE = new ByteCode(new byte[]{ByteCode.E, ByteCode.N, ByteCode.D, ByteCode.TWO});
    public static final ByteCode SVTYPE_BYTECODE = new ByteCode(new byte[]{ByteCode.S, ByteCode.V, ByteCode.T, ByteCode.Y, ByteCode.P, ByteCode.E});
    public static final ByteCode SVTYPE2_BYTECODE = new ByteCode(new byte[]{ByteCode.S, ByteCode.V, ByteCode.T, ByteCode.Y, ByteCode.P, ByteCode.E, ByteCode.TWO});
    public static final int BND_TYPE_INDEX = SVTypeSign.get("BND").getIndex();
    private static final ByteCodeArray EMPTY_BYTECODE_ARRAY = new ByteCodeArray(new ByteCode[0]);
    private static final ByteCodeArray ONE_EMPTY_BYTECODE_ARRAY = new ByteCodeArray(new ByteCode[0]);

    abstract public void parse(int indexOfFile, Chromosome chromosome, int pos,
                               ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                               Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes);

    abstract public void parse(int indexOfFile, Chromosome chromosome, int pos, ByteCodeArray encodeNoneFieldArray,
                               ReusableMap<ByteCode, ByteCode> infoField, SVGenotypes svGenotypes);

    public void loadOne(SVTypeSign type, int len, int pos, int end, Chromosome chr,
                        ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                        Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes, CSVLocation location) {
        Array<ByteCodeArray> vcfFormatFieldArray;
        if (VCF2SDF.dropFormat) {
            vcfFormatFieldArray = new Array<>();
        } else {
            vcfFormatFieldArray = new Array<>(vcfFormatFields.size());
            for (VCFFormatField vcfFormatField : vcfFormatFields) {
                vcfFormatFieldArray.add(vcfFormatField.getProperties());
            }
        }
        unifiedSVArray.getOneUnifiedSV()
                .setType(type)
                .setLength(len)
                .setID(encodeNoneFieldArray.get(0))
                .setRef(encodeNoneFieldArray.get(1))
                .setAlt(encodeNoneFieldArray.get(2))
                .setQual(encodeNoneFieldArray.get(3))
                .setFilterField(encodeNoneFieldArray.get(4))
                .setCoordinate(new SVCoordinate(pos, end, chr))
                .setGenotypes(new SVGenotypes(genotypes))
                .setFormatFieldSet(vcfFormatFieldArray)
                .setSpecificInfoField(asUnmodifiedInfoField(infoField.values()))
                .setCSVLocation(location);
        infoField.clear();
    }

    public void loadOneLatest(SVTypeSign type, int len, int pos, int end, Chromosome chr,
                              ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                              SVGenotypes svGenotypes, CSVLocation location) {
        unifiedSVArray.getOneUnifiedSV()
                .setType(type)
                .setLength(len)
                .setID(encodeNoneFieldArray.get(0))
                .setRef(encodeNoneFieldArray.get(1))
                .setAlt(encodeNoneFieldArray.get(2))
                .setQual(encodeNoneFieldArray.get(3))
                .setFilterField(encodeNoneFieldArray.get(4))
                .setCoordinate(new SVCoordinate(pos, end, chr))
                .setGenotypes(svGenotypes)
                .setSpecificInfoField(asUnmodifiedInfoField(infoField.values()))
                .setCSVLocation(location);
        infoField.clear();
    }

    public void loadEncodeOne(SVTypeSign type, int len, int pos, int end, Chromosome chr,
                              ByteCodeArray encodeNoneFieldArray, ByteCodeArray infoField,
                              Array<ByteCodeArray> vcfFormatFieldArray, SVGenotype[] genotypes, CSVLocation location) {
        UnifiedSV unifiedSV = unifiedSVArray.getOneUnifiedSV()
                .setType(type)
                .setLength(len)
                .setCoordinate(new SVCoordinate(pos, end, chr))
                .setGenotypes(new SVGenotypes(genotypes))
                .setFormatFieldSet(vcfFormatFieldArray)
                .setSpecificInfoField(infoField)
                .setCSVLocation(location);
        if (encodeNoneFieldArray != null) {
            unifiedSV.setID(encodeNoneFieldArray.get(0))
                    .setRef(encodeNoneFieldArray.get(1))
                    .setAlt(encodeNoneFieldArray.get(2))
                    .setQual(encodeNoneFieldArray.get(3))
                    .setFilterField(encodeNoneFieldArray.get(4));
        }
    }

    public void loadEncodeOneLatest(SVTypeSign type, int len, int pos, int end, Chromosome chr,
                                    ByteCodeArray encodeNoneFieldArray, ByteCodeArray infoField,
                                    SVGenotypes svGenotypes, CSVLocation location) {
        UnifiedSV unifiedSV = unifiedSVArray.getOneUnifiedSV()
                .setType(type)
                .setLength(len)
                .setCoordinate(new SVCoordinate(pos, end, chr))
                .setGenotypes(svGenotypes)
                .setSpecificInfoField(infoField)
                .setCSVLocation(location);
        if (encodeNoneFieldArray != null) {
            unifiedSV.setID(encodeNoneFieldArray.get(0))
                    .setRef(encodeNoneFieldArray.get(1))
                    .setAlt(encodeNoneFieldArray.get(2))
                    .setQual(encodeNoneFieldArray.get(3))
                    .setFilterField(encodeNoneFieldArray.get(4));
        }
    }

    public void loadTwoLatest(int indexOfFile, Chromosome chr1, Chromosome chr2, SVTypeSign type1,
                              SVTypeSign type2, int pos1, int pos2, int end1, int end2, int len1, int len2,
                              ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                              SVGenotypes svGenotypes) {
        int compareStatus = compareTo(chr1, chr2, pos1, pos2, end1, end2, len1, len2);
        ByteCodeArray unmodifiedInfoField = asUnmodifiedInfoField(infoField.values());
        if (compareStatus < 0) {
            loadEncodeOneLatest(type1, len1, pos1, end1, chr1, encodeNoneFieldArray, unmodifiedInfoField,
                    svGenotypes, new CSVLocation(indexOfFile, Chromosome.unknown, chr2));
            loadEncodeOneLatest(type2, len2, pos2, end2, chr2, encodeNoneFieldArray, unmodifiedInfoField,
                    svGenotypes, new CSVLocation(indexOfFile, chr1, Chromosome.unknown));

        } else {
            loadEncodeOneLatest(type2, len2, pos2, end2, chr2, encodeNoneFieldArray, unmodifiedInfoField,
                    svGenotypes, new CSVLocation(indexOfFile, Chromosome.unknown, chr1));
            loadEncodeOneLatest(type1, len1, pos1, end1, chr1, encodeNoneFieldArray, unmodifiedInfoField,
                    svGenotypes, new CSVLocation(indexOfFile, chr2, Chromosome.unknown));
        }
    }

    public void loadTwo(int indexOfFile, Chromosome chr1, Chromosome chr2, SVTypeSign type1,
                        SVTypeSign type2, int pos1, int pos2, int end1, int end2, int len1, int len2,
                        ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                        Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {
        int compareStatus = compareTo(chr1, chr2, pos1, pos2, end1, end2, len1, len2);
        Array<ByteCodeArray> encodeVcfFormatFields = getEncodeFormatFieldArray(vcfFormatFields);
        ByteCodeArray unmodifiedInfoField = asUnmodifiedInfoField(infoField.values());
        if (compareStatus < 0) {
            loadEncodeOne(type1, len1, pos1, end1, chr1, encodeNoneFieldArray, unmodifiedInfoField,
                    encodeVcfFormatFields, genotypes, new CSVLocation(indexOfFile, Chromosome.unknown, chr2));
            if (VCF2SDF.multiInfoForCSV) {
                loadEncodeOne(type2, len2, pos2, end2, chr2, encodeNoneFieldArray, unmodifiedInfoField,
                        encodeVcfFormatFields, genotypes, new CSVLocation(indexOfFile, chr1, Chromosome.unknown));
            } else {
                loadEncodeOne(type2, len2, pos2, end2, chr2, null, null,
                        null, genotypes, new CSVLocation(indexOfFile, chr1, Chromosome.unknown));
            }
        } else {
            loadEncodeOne(type2, len2, pos2, end2, chr2, encodeNoneFieldArray, unmodifiedInfoField,
                    encodeVcfFormatFields, genotypes, new CSVLocation(indexOfFile, Chromosome.unknown, chr1));
            if (VCF2SDF.multiInfoForCSV) {
                loadEncodeOne(type1, len1, pos1, end1, chr1, encodeNoneFieldArray, unmodifiedInfoField,
                        encodeVcfFormatFields, genotypes, new CSVLocation(indexOfFile, chr2, Chromosome.unknown));
            } else {
                loadEncodeOne(type1, len1, pos1, end1, chr1, null, null,
                        null, genotypes, new CSVLocation(indexOfFile, chr2, Chromosome.unknown));
            }
        }
    }


    public final int compareTo(Chromosome chr1, Chromosome chr2,
                               int pos1, int pos2,
                               int end1, int end2,
                               int len1, int len2) {
        int status = Chromosome.compare(chr1, chr2);
        if (status == 0) {
            status = Integer.compare(pos1, pos2);
            if (status == 0) {
                status = Integer.compare(end1, end2);
                if (status == 0) {
                    status = Integer.compare(len1, len2);
                }
            }
        }
        return status;
    }


    public AbstractCallingParser setUnifiedSVArray(ReusableSVArray unifiedSVArray) {
        this.unifiedSVArray = unifiedSVArray;
        return this;
    }

    public AbstractCallingParser setContig(Contig contig) {
        this.contig = contig;
        return this;
    }

    public int wrapToInt(ByteCode value) {
        if (value == null) {
            return -1;
        }
        try {
            return value.toInt();
        } catch (ClassCastException | NumberFormatException e) {
            return -1;
        }
    }

    public Chromosome wrapToChr(ByteCode value, Chromosome chr) {
        if (value == null) {
            return chr;
        }
        return contig.get(value);
    }

    public int transferInt(ByteCode src) {
        if (src == null) {
            return -1;
        }
        try {
            return src.toInt();
        } catch (ClassCastException | NumberFormatException e) {
            return -1;
        }
    }

    Array<ByteCodeArray> getEncodeFormatFieldArray(Array<VCFFormatField> vcfFormatFields) {
        Array<ByteCodeArray> vcfFormatFieldArray;
        if (VCF2SDF.dropFormat || vcfFormatFields == null) {
            vcfFormatFieldArray = new Array<>();
        } else {
            vcfFormatFieldArray = new Array<>(vcfFormatFields.size());
            for (VCFFormatField vcfFormatField : vcfFormatFields) {
                vcfFormatFieldArray.add(vcfFormatField.getProperties());
            }
        }
        return vcfFormatFieldArray;
    }

    ByteCodeArray asUnmodifiedInfoField(BaseArray<ByteCode> infoFields) {
        if (infoFields == null) {
            return ONE_EMPTY_BYTECODE_ARRAY;
        }
        ByteCodeArray res = new ByteCodeArray(infoFields.size());
        int count = 0;
        int maxIndex = ignoreInfoIndex.size() - 1;
        for (int i = 0; i < infoFields.size(); i++) {
            int tmp = Math.min(count, maxIndex);
            if (i == ignoreInfoIndex.get(tmp)) {
                count++;
                continue;
            }
            ByteCode info = infoFields.get(i);
            if (info == null || info.length() == 0) {
                res.add(ByteCode.EMPTY);
                continue;
            }
            if (VCF2SDF.lineExtractAndSort) {
                res.add(info);
            } else {
                res.add(info.asUnmodifiable());
            }
        }
        return res;
    }

    public IntArray getIgnoreInfoIndex() {
        return ignoreInfoIndex;
    }

    public void addIgnoreInfoIndex(int index) {
        ignoreInfoIndex.add(index);
    }
}
