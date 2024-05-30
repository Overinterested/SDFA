package edu.sysu.pmglab.sdfa;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.easytools.annotation.Future;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;
import edu.sysu.pmglab.sdfa.sv.*;
import edu.sysu.pmglab.sdfa.toolkit.Contig;
import edu.sysu.pmglab.sdfa.toolkit.SDFRefAltFieldType;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 04:21
 * @description
 */
public class SDFDecode {
    final int decodeMode;
    private Contig contig;
    private IntArray loadFieldArray;
    final static SDFRefAltFieldType altDecode = new SDFRefAltFieldType();

    public SDFDecode(int decodeMode) {
        this.decodeMode = decodeMode;
    }

    public BriefSVAnnotationFeature decodeForBrief(int fileID, int line, IRecord record) {
        SVCoordinate coordinate = SVCoordinate.decode(record.get(0));
        return new BriefSVAnnotationFeature(fileID, coordinate.getPos(), coordinate.getEnd(), record.get(1), line);
    }

    public UnifiedSV decode(IRecord record) {
        if (loadFieldArray != null && !loadFieldArray.isEmpty()) {
            return decodePart(decodeMode, record, new UnifiedSV(), loadFieldArray, contig);
        }
        return decode(decodeMode, record, new UnifiedSV(), contig);
    }

    public UnifiedSV decode(IRecord record, UnifiedSV sv) {
        if (loadFieldArray != null && !loadFieldArray.isEmpty()) {
            return decodePart(decodeMode, record, sv, loadFieldArray, contig);
        }
        return decode(decodeMode, record, sv, contig);
    }

    private static UnifiedSV decode(int decodeMode, IRecord record, Contig contig) {
        return decode(decodeMode, record, new UnifiedSV(), contig);
    }

    private static UnifiedSV decode(int decodeMode, IRecord record,
                                    UnifiedSV sv, Contig contig) {
        switch (decodeMode) {
            case 0:
                return decode$0(record, sv);
            case 1:
                return decode$1(record, sv);
            case 2:
                return decode$2(record, sv);
            default:
                throw new UnsupportedOperationException("Encode mode is not defined.");
        }
    }

    @Future("Add decodeMode for part decoder")
    private static UnifiedSV decodePart(int decodeMode, IRecord record, UnifiedSV sv,
                                        IntArray indexOfLoadFields, Contig contig) {
        int size = indexOfLoadFields.size();
        for (int i = 0; i < size; i++) {
            int rawIndex = indexOfLoadFields.get(i);
            switch (rawIndex) {
                case 0:
                    sv.setCoordinate(SVCoordinate.decode(record.get(i), contig));
                    break;
                case 1:
                    sv.setLength(record.get(i));
                    break;
                case 2:
                    sv.setType(SVTypeSign.getByIndex(record.get(i)));
                    break;
                case 3:
                    sv.setGenotypes(SVGenotypes.decodeGTs(record.get(i)));
                    break;
                case 4:
                    // format: encode
                    SVGenotypes genotypes = sv.getGenotypes();
                    if (genotypes == null) {
                        sv.setGenotypes(new SVGenotypes(new SVGenotype[0]));
                    }
                    sv.getGenotypes().setProperties(SVGenotypes.decodeProperties(record.get(i)));
                    break;
                case 5:
                    sv.setID(record.get(i));
                    break;
                case 6:
                    // ref: encode
                    sv.setRef(record.get(i));
                    break;
                case 7:
                    // alt: encode
                    sv.setAlt(record.get(i));
                    break;
                case 8:
                    sv.setQual(record.get(i));
                    break;
                case 9:
                    sv.setFilterField(record.get(i));
                    break;
                case 10:
                    // encode
                    if (record.get(i) == null) {
                        break;
                    }
                    sv.setSpecificInfoField(ByteCodeArray.wrap((ByteCode[]) record.get(i)));
                    break;
                case 11:
                    if (record.get(i) == null) {
                        break;
                    }
                    sv.setCSVLocation(new CSVLocation(record.get(i)));
                    break;
                case 12:
                    if (record.get(i) == null) {
                        break;
                    }
                    sv.getCsvLocation().setChromosomeArray(CSVLocation.decodeCSVChrIndex(record.get(i)));
                    break;
                default:
                    if (record.get(i) == null) {
                        break;
                    }
                    if (sv.getProperty() == null) {
                        sv.setProperty(new Array<>());
                    }
                    sv.addProperty(record.get(i));
            }
        }
        return sv;
    }

    private static UnifiedSV decode$0(IRecord record, UnifiedSV sv) {
        int size = record.size();
        sv.setCoordinate(SVCoordinate.decode(record.get(0)));
        sv.setLength(record.get(1));
        sv.setType(SVTypeSign.getByIndex(record.get(2)));
        sv.setGenotypes(SVGenotypes.decodeGTs(record.get(3)));
        sv.getGenotypes().setProperties(SVGenotypes.decodeProperties(record.get(4)));
        sv.setID(record.get(5));
        sv.setRef((ByteCode) altDecode.decode(record.get(6)));
        sv.setAlt((ByteCode) altDecode.decode(record.get(7)));
        sv.setQual(record.get(8));
        sv.setFilterField(record.get(9));
        sv.setSpecificInfoField(ByteCodeArray.wrap((ByteCode[]) record.get(10)));
        sv.setCSVLocation(new CSVLocation(record.get(11)));
        sv.getCsvLocation().setChromosomeArray(CSVLocation.decodeCSVChrIndex(record.get(12)));
        for (int i = 13; i < size; i++) {
            sv.addProperty(record.get(i));
        }
        return sv;
    }

    private static UnifiedSV decode$1(IRecord record, UnifiedSV sv) {
        return sv;
    }

    private static UnifiedSV decode$2(IRecord record, UnifiedSV sv) {
        return sv;
    }

    public SDFDecode setContig(Contig contig) {
        this.contig = contig;
        return this;
    }

    public void setLoadFieldArray(IntArray loadFieldArray) {
        this.loadFieldArray = loadFieldArray;
    }
}
