package edu.sysu.pmglab.sdfa.sv.vcf.calling;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.CSVLocation;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;

/**
 * @author Wenjie Peng
 * @create 2024-03-25 11:26
 * @description
 */
public class NanovarCallingParser extends AbstractCallingParser {
    public static final byte[] INSTAG = new byte[]{ByteCode.EQUAL, 62};
    public static final byte[] SVLENINSTAG = "SVLEN=>".getBytes();
    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos,
                      ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                      Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {
        ByteCode len1ByteCode = infoField.get(SVLEN_BYTECODE);
        int len1 = transferInt(len1ByteCode);
        int end1 = transferInt(infoField.get(END_BYTECODE));
        SVTypeSign svTypeSign1 = SVTypeSign.get(infoField.get(SVTYPE_BYTECODE));
        if (svTypeSign1.isComplex()) {
            if (len1 == -1){
                len1 = wrapToInt(len1ByteCode.subByteCode(1));
            }
            Entry<ByteCode, ByteCode> entry = UnifiedSV.parseBNDAlt(encodeNoneFieldArray.get(2));
            if (entry == null) {
                loadOne(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                        vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
                return;
            }
            loadTwo(indexOfFile, chromosome, wrapToChr(entry.getKey(), chromosome), svTypeSign1, svTypeSign1,
                    pos, wrapToInt(entry.getValue()), end1, -1, -1, -1,
                    encodeNoneFieldArray, infoField, vcfFormatFields, genotypes);
        }
        loadOne(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
    }
}
