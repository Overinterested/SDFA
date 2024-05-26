package edu.sysu.pmglab.sdfa.sv;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.StringArray;

import java.util.Collections;
import java.util.Set;

/**
 * @author Wenjie Peng
 * @create 2024-03-22 12:09
 * @description
 */
public class SVTypeSign {
    final int index;
    final String name;
    final boolean isComplex;
    final boolean spanChromosome;
    private static final CallableSet<SVTypeSign> svTypes = new CallableSet<>();
    private static final ReusableMap<ByteCode, SVTypeSign> subCsvTypes = new ReusableMap<>();
    public static final SVTypeSign unknown = new SVTypeSign("unknown", false, false, -1);

    public static SVTypeSign get(Object svType) {
        if (svType instanceof SVTypeSign) {
            return (SVTypeSign) svType;
        } else {
            SVTypeSign returns = svTypes.get(svType);
            return returns == null ? unknown : returns;
        }
    }

    public static SVTypeSign getByName(ByteCode svName) {
        SVTypeSign returns = svTypes.get(svName);
        return returns == null ? unknown : returns;
    }

    public static SVTypeSign getByIndex(int svTypeIndex) {
        SVTypeSign byIndex = svTypes.getByIndex(svTypeIndex);
        return byIndex == null ? unknown : byIndex;
    }

    public static SVTypeSign add(String svTypeName) {
        return add(svTypeName, false, false);
    }

    public static SVTypeSign add(String svTypeName, boolean isComplex, boolean spanChromosome) {
        if (svTypeName.equals("unknown")) {
            return unknown;
        } else if (svTypes.containsKey(svTypeName)) {
            return get(svTypeName);
        } else {
            synchronized (svTypes) {
                SVTypeSign svType = new SVTypeSign(svTypeName, isComplex, spanChromosome, svTypes.size());
                svTypes.put(new ByteCode(svType.getName()), svType);
                svTypes.put(svType.getName(), svType);
                svTypes.put(svType.getIndex(), svType);
                return svType;
            }
        }
    }

    public SVTypeSign alias(String... alternativeNames) {
        if (alternativeNames == null) {
            return this;
        } else if (this == unknown) {
            throw new UnsupportedOperationException();
        } else {
            synchronized (svTypes) {
                String[] var3 = alternativeNames;
                int var4 = alternativeNames.length;
                for (int var5 = 0; var5 < var4; ++var5) {
                    String alternativeName = var3[var5];
                    if (alternativeName != null) {
                        svTypes.put(alternativeName, this);
                        svTypes.put(new ByteCode(alternativeName), this);
                    }
                }

                return this;
            }
        }
    }

    public static void clear() {
        synchronized (svTypes) {
            svTypes.clear();
        }
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        return this.name;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            SVTypeSign that = (SVTypeSign) o;
            return this.name.equals(that.name);
        } else {
            return false;
        }
    }

    public static Set<SVTypeSign> support() {
        return Collections.unmodifiableSet(svTypes);
    }

    private SVTypeSign(String name, boolean isComplex, boolean spanChromosome, int index) {
        this.name = name;
        this.index = index;
        this.isComplex = isComplex;
        this.spanChromosome = spanChromosome;
    }

    public BaseArray<String> getNames() {
        BaseArray<Object> keys = svTypes.getKeys().get(this);
        StringArray names = new StringArray(keys.size());
        for (Object key : keys) {
            if (key instanceof String) {
                names.add((String) key);
            }
        }

        return BaseArray.unmodifiableArray(names);
    }

    static {
        add("BND", true, true).alias("BND", "Bnd");
        add("CNV").alias("CNV", "cnv", "mcnv", "MCNV");
        add("DEL").alias("del", "DEL", "Deletion", "Delete", "delete", "deletion");
        add("DUP").alias("dup", "DUP", "Duplication", "duplication", "Duplication/Triplication");
        add("INS").alias("ins", "INS", "Insertion", "Insert", "insert", "insertion");
        add("DUP/INS").alias("dup/ins");
        add("INV").alias("inv", "INV", "Inversion", "Inverse", "inverse", "inversion");
        add("MEI").alias("mei", "mobile element insertion", "mobile element insert",
                "mobile_element_insertion", "INS:ME", "INS:MEI");
        /**
         * LINE: Long Interspersed Nuclear Element 1 is a self-replicating and self-diffused DNA sequence that can be inserted into genomic DNA to produce structural variations. LINE transposons are also active and can transpose in different regions of the genome.
         * SVA: sine-r /VNTR/Alu is a complex transposon composed of two different transposons - SINE and Alu. SVA transposons are the most active in the human genome and can translocate in different regions of the genome, causing structural variation.
         * ALU: ALU sequences are a class of short interval repeats consisting of about 300 bp, belonging to SINEs(Short interval Repeats). ALU transposons can be inserted into genomic DNA, causing structural changes in the genome.
         */
        add("INS:LINE1").alias("line1", "INS:ME:LINE1", "line");
        add("INS:ALU").alias("alu", "INS:ME:ALU");
        add("INS:SVA").alias("sva", "INS:ME:SVA");
        add("DEL:LINE1").alias("DEL_LINE1");
        add("DEL:ALU").alias("DEL_ALU");
        add("DEL:SVA").alias("DEL_SVA");
        /**
         * CNV transfer:
         * 1. MCNV
         * 2. copy number gain
         * 3. copy number loss
         */
        add("MCNV");
        add("copy number loss").alias("copy_number_loss");
        add("copy number gain").alias("copy_number_gain", "Copy-Number Gain");
        /**
         * duplication transfer:
         * 1. DUP:TANDEM is an adjacent repeat, where genomic regions are repeated and arranged next to each other
         * 2. DUP:INT is an interval repeat, where regions of the genome are repeated but with other sequences inserted in between
         * 3. DUP:INV is an inverted duplication
         */
        add("DUP:TANDEM", false, false).alias("tDUP", "tandem_duplication", "tandem duplication");
        add("DUP:INT", false, false).alias("interval_duplication", "interval duplication");
        add("DUP:INV", false, false).alias("INVDUP", "INV/INVDUP", "DUPINV", "inverted_duplication", "inverted duplication");
        add("TRA", true, true).alias("deletion_insertion", "delins", "DELINS", "TRA",
                "TRA:delins", "tra", "traslocation", "Reciprocal translocation",
                "reciprocal translocation", "reciprocal_translocation");
        add("DEL/INV").alias("del/inv");
        add("CPX", true, true);
        add("CTX", true, true);
        // CSV::DEL
        add(new CSVSubType("DEL").fullType.toString(), true, false);
        subCsvTypes.put(new ByteCode("DEL").asUnmodifiable(), svTypes.get("CSV::DEL"));
        // CSV::INS
        add(new CSVSubType("INS").fullType.toString(), true, false);
        subCsvTypes.put(new ByteCode("INS").asUnmodifiable(), svTypes.get("CSV::INS"));
        // CSV::INV
        add(new CSVSubType("INV").fullType.toString(), true, false);
        subCsvTypes.put(new ByteCode("INV").asUnmodifiable(), svTypes.get("CSV::INV"));
        // CSV::DUP
        add(new CSVSubType("DUP").fullType.toString(), true, false);
        subCsvTypes.put(new ByteCode("DUP").asUnmodifiable(), svTypes.get("CSV::DUP"));
        // CSV::tDUP
        add(new CSVSubType("tDUP").fullType.toString(), true, false);
        subCsvTypes.put(new ByteCode("tDUP").asUnmodifiable(), svTypes.get("CSV::tDUP"));

        add("Triplication", false, false).alias("triplication");
        add("Amplification", false, false);

    }

    public boolean isComplex() {
        return isComplex;
    }

    public boolean spanChromosome() {
        return spanChromosome;
    }

    public static SVTypeSign decode(int encode) {
        return getByIndex(encode);
    }

    public static SVTypeSign getCSVSubType(ByteCode CSVSubType) {
        return subCsvTypes.get(CSVSubType);
    }

    static class CSVSubType {
        final ByteCode subType;
        final ByteCode fullType;

        public CSVSubType(String subType) {
            this(new ByteCode(subType));
        }

        public CSVSubType(ByteCode subType) {
            this.subType = subType;
            VolumeByteStream cache = new VolumeByteStream();
            cache.writeSafety(new byte[]{ByteCode.C, ByteCode.S, ByteCode.V, ByteCode.COLON, ByteCode.COLON});
            cache.writeSafety(subType);
            this.fullType = cache.toByteCode().asUnmodifiable();
            cache.close();
        }
    }
}
