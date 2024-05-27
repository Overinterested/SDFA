package edu.sysu.pmglab.sdfa.sv;

import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.merge.cmo.AbstractSVMergeOutput;

/**
 * @author Wenjie Peng
 * @create 2024-03-22 08:02
 * @description
 */
public class UnifiedSV implements Comparable<UnifiedSV> {
    int fileID;
    int length;
    SVTypeSign type;
    ByteCode ID;
    ByteCode ref;
    ByteCode alt;
    ByteCode qual;
    ByteCode filter;
    SVGenotypes genotypes;
    SVCoordinate coordinate;
    CSVLocation csvLocation;
    ByteCodeArray specificInfoField;
    Array<ByteCode> property;
    // following fields are in this: ID, REF, ALT, QUAL, FILTER


    @Override
    public int compareTo(UnifiedSV o) {
        return coordinate.compareTo(o.coordinate);
    }

    public UnifiedSV setFileID(int fileID) {
        this.fileID = fileID;
        return this;
    }

    public UnifiedSV setLength(int length) {
        if (length < -1) {
            length = -length;
        }
        this.length = length;
        return this;
    }

    public UnifiedSV setType(SVTypeSign type) {
        this.type = type;
        return this;
    }

    public UnifiedSV setCoordinate(SVCoordinate coordinate) {
        if (this.coordinate != null) {
            this.coordinate.pos = coordinate.pos;
            this.coordinate.end = coordinate.end;
            this.coordinate.chr = coordinate.chr;
            return this;
        }
        this.coordinate = coordinate;
        return this;
    }

    public UnifiedSV setGenotypes(SVGenotypes gtys) {
        this.genotypes = gtys;
        return this;
    }

    public UnifiedSV addProperty(ByteCode value) {
        if (property == null) {
            property = new Array<>();
        }
        property.add(value);
        return this;
    }

    public int getFileID() {
        return fileID;
    }

    public int getPos() {
        return coordinate.pos;
    }

    public int getEnd() {
        return coordinate.end;
    }

    public Chromosome getChr() {
        return coordinate.chr;
    }

    public SVCoordinate getCoordinate() {
        return coordinate;
    }

    public SVGenotypes getGenotypes() {
        return genotypes;
    }

    public int length() {
        return length;
    }

    public SVTypeSign type() {
        return type;
    }

    public int getTypeIndex() {
        return type.index;
    }

    public boolean isComplex() {
        return type.isComplex;
    }

    public boolean spanChromosome() {
        return type.spanChromosome;
    }

    public ByteCode getID() {
        return ID;
    }

    public UnifiedSV setID(ByteCode ID) {
        this.ID = ID;
        return this;
    }

    public ByteCode getRef() {
        return ref;
    }

    public UnifiedSV setRef(ByteCode ref) {
        this.ref = ref;
        return this;
    }

    public ByteCode getAlt() {
        return alt;
    }

    public UnifiedSV setAlt(ByteCode alt) {
        this.alt = alt;
        return this;
    }

    public ByteCode getQual() {
        return qual;
    }

    public UnifiedSV setQual(ByteCode qual) {
        this.qual = qual;
        return this;
    }

    public ByteCode getFilterField() {
        return filter;
    }

    public UnifiedSV setFilterField(ByteCode filterField) {
        this.filter = filterField;
        return this;
    }

    public ByteCodeArray getSpecificInfoField() {
        return specificInfoField;
    }

    public UnifiedSV setCSVLocation(CSVLocation csvLocation) {
        this.csvLocation = csvLocation;
        return this;
    }


    public UnifiedSV setFormatFieldSet(Array<ByteCodeArray> formatFieldArray) {
        if (genotypes == null) {
            genotypes = new SVGenotypes(new SVGenotype[0]);
        }
        genotypes.setProperties(formatFieldArray);
        return this;
    }

    public UnifiedSV setRawInfoField(ByteCodeArray info) {
        this.specificInfoField = info;
        return this;
    }

    public UnifiedSV setSpecificInfoField(BaseArray<ByteCode> specificInfoField) {
        if (specificInfoField == null) {
            this.specificInfoField = null;
            return this;
        }
        if (this.specificInfoField == null) {
            this.specificInfoField = new ByteCodeArray();
        } else {
            this.specificInfoField.clear();
        }
        this.specificInfoField.addAll(specificInfoField);
        return this;
    }

    public int getIndexOfFile() {
        return csvLocation.indexInFile;
    }

    public int numOfSVs() {
        return csvLocation.chromosomeArray == null ? 1 : csvLocation.chromosomeArray.size();
    }

    public int getLength() {
        return length;
    }

    public Array<ByteCodeArray> getFormatField() {
        return genotypes.properties;
    }

    public CSVLocation getCsvLocation() {
        return csvLocation;
    }

    public int[] getEncodeCSVChrIndex() {
        return csvLocation.encodeCSVChrIndex();
    }

    public Array<ByteCode> getProperty() {
        return property;
    }

    public UnifiedSV setProperty(Array<ByteCode> property) {
        this.property = property;
        return this;
    }

    public static Entry<ByteCode, ByteCode> parseBNDAlt(ByteCode alt) {
        int sep1 = -1;
        int sep2 = -1;
        boolean find1 = false;
        for (int i = 0; i < alt.length(); i++) {
            byte tmp = alt.byteAt(i);
            // match '[' or ']'
            if (tmp == 91 || tmp == 93) {
                if (find1) {
                    sep2 = i;
                    break;
                } else {
                    sep1 = i;
                    find1 = true;
                }
            }
        }
        if (sep2 == -1) {
            return null;
        }
        BaseArray<ByteCode> split = alt.subByteCode(sep1 + 1, sep2).split(ByteCode.COLON);
        return new Entry<>(split.get(0), split.get(1));
    }

    public String getSVTypeName() {
        return type.getName();
    }

    public ByteCode toCCFOutput(VolumeByteStream cache, int ID) {
        cache.writeSafety(coordinate.chr.getName());
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate.pos));
        cache.writeSafety(ByteCode.TAB);
        // ID
        if (this.ID == null) {
            cache.writeSafety(ValueUtils.Value2Text.int2bytes(AbstractSVMergeOutput.SVCount.intValue()));
            AbstractSVMergeOutput.SVCount.incrementAndGet();
        } else {
            cache.writeSafety(this.ID);
        }
        cache.writeSafety(ByteCode.TAB);
        // ref
        if (ref == null) {
            cache.writeSafety(ByteCode.N);
        } else {
            cache.writeSafety(ref);
        }
        cache.writeSafety(ByteCode.TAB);
        // alt
        if (alt == null) {
            cache.writeSafety(new byte[]{60});
            cache.writeSafety(getSVTypeName());
            cache.writeSafety(new byte[]{62});
        } else {
            cache.writeSafety(alt);
        }
        cache.writeSafety(ByteCode.TAB);
        // qual
        if (qual == null) {
            cache.writeSafety(ByteCode.PERIOD);
        } else {
            cache.writeSafety(qual);
        }
        cache.writeSafety(ByteCode.TAB);
        // filter
        if (filter == null) {
            cache.writeSafety(ByteCode.PERIOD);
        } else {
            cache.writeSafety(filter);
        }
        cache.writeSafety(ByteCode.TAB);

        // INFO
        if (specificInfoField == null || specificInfoField.isEmpty()) {
            cache.writeSafety(ByteCode.PERIOD);
            cache.writeSafety(ByteCode.TAB);
        } else {
            for (ByteCode infoField : specificInfoField) {
                cache.writeSafety(infoField);
                cache.writeSafety(ByteCode.SEMICOLON);
            }
            cache.writeSafety(ByteCode.TAB);
        }
        // FORMAT
        cache.writeSafety(new byte[]{ByteCode.G, ByteCode.T});
        cache.writeSafety(ByteCode.TAB);
        // GENOTYPES
        if (genotypes == null) {
            cache.writeSafety(ByteCode.PERIOD);
        } else {
            SVGenotype[] sampleGT = genotypes.getGenotypes();
            int sampleSize = 0;
            if (sampleGT == null || (sampleSize = sampleGT.length) == 0) {
                cache.writeSafety(ByteCode.PERIOD);
            }
            for (int i = 0; i < sampleSize; i++) {
                cache.writeSafety(SVGenotypes.getByDefaultGenotype(sampleGT[i]).getPhasedString());
                if (i != sampleSize - 1) {
                    cache.writeSafety(ByteCode.TAB);
                }
            }
        }
        ByteCode res = cache.toByteCode();
        cache.reset();
        return res;
    }

    public void reset() {
        genotypes.genotypes = null;
        csvLocation = null;
        if (specificInfoField != null) {
            specificInfoField.clear();
        }
        if (property != null) {
            property.clear();
        }
        ID = null;
        ref = null;
        alt = null;
        qual = null;
        filter = null;
    }

    public void clearProperty() {
        property = null;
    }

    public void clearGTProperties() {
        genotypes.properties = null;
    }
}
