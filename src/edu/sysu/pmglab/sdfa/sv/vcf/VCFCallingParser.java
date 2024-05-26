package edu.sysu.pmglab.sdfa.sv.vcf;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.vcf.calling.AbstractCallingParser;
import edu.sysu.pmglab.sdfa.sv.vcf.calling.VCFCallingParserFactory;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.SVLevelFilterManager;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;

/**
 * @author Wenjie Peng
 * @create 2024-03-22 09:23
 * @description
 */
public class VCFCallingParser {
    boolean closed = false;
    ReusableSVArray unifiedArray;
    AbstractCallingParser SVParser;

    public VCFCallingParser() {
        unifiedArray = new ReusableSVArray();
    }

    public void init(File vcfFile) {
        File filePath = new File(vcfFile.toString());
        if (filePath.exists() && filePath.isFile()) {
            setSVParser(VCFCallingParserFactory.of(filePath));
            SVParser.setUnifiedSVArray(unifiedArray);
        } else {
            throw new UnsupportedOperationException("Please check " + vcfFile + " path!");
        }
    }

    /**
     * @param indexOfFile
     * @param chromosome
     * @param pos
     * @param encodeNoneFieldArray a five-encoding fields array, consisting of ID, REF, ALT, QUAL, FILTER
     * @param infoField
     * @param vcfFormatFields
     * @param genotypes
     */
    public void parse(int indexOfFile, Chromosome chromosome, int pos,
                      ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                      Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {
        SVParser.parse(indexOfFile, chromosome, pos, encodeNoneFieldArray,
                infoField, vcfFormatFields, genotypes);
    }

    public void filter(SVFilterManager filter) {
        if (filter == null || !filter.filterSV()) {
            unifiedArray.addVerifiedSVSize();
            unifiedArray.addTotalSVSizeInVCF();
            return;
        }
        boolean filterRes;
        SVLevelFilterManager svFilterManager = filter.getSvFilterManager();
        UnifiedSV unifiedSV = unifiedArray.getLastUnifiedSV();
        int numOfSVs = unifiedSV.numOfSVs();
        if (numOfSVs == 1) {
            if (VCF2SDF.dropFormat) {
                unifiedSV.clearGTProperties();
            }
            filterRes = svFilterManager.filter(unifiedSV);
        } else {
            BaseArray<UnifiedSV> lastUnifiedSVs = unifiedArray.getLastUnifiedSVs();
            if (VCF2SDF.dropFormat) {
                for (UnifiedSV item : lastUnifiedSVs) {
                    item.clearGTProperties();
                }
            }
            ComplexSV csv = ComplexSV.of(lastUnifiedSVs);
            filterRes = svFilterManager.filter(csv);
        }
        if (filterRes) {
            // filter pass
            unifiedArray.addVerifiedSVSize();
        } else {
            unifiedArray.rollBack();
        }
        unifiedArray.addTotalSVSizeInVCF();
    }

    public Array<IRecord> getLoadUnifiedSVArray() {
        if (VCF2SDF.lineExtractAndSort){
            return unifiedArray.getEncodeSVArrayByClear();
        }
        return unifiedArray.getLoadEncodeSVArray();
    }

    public void reset() {
        closed = false;
        SVParser = null;
        unifiedArray.reset();
    }

    public VCFCallingParser setSVParser(AbstractCallingParser SVParser) {
        this.SVParser = SVParser;
        return this;
    }

    public void addTotalSVSizeInVCF() {
        unifiedArray.addTotalSVSizeInVCF();
    }

}
