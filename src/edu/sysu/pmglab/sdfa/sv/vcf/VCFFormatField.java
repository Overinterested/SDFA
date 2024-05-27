package edu.sysu.pmglab.sdfa.sv.vcf;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.vcf.exception.SVGenotypeParseException;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.gty.GenotypeFilterManager;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;


/**
 * @author Wenjie Peng
 * @create 2024-03-23 00:55
 * @description
 */
public class VCFFormatField {
    SVGenotype svGenotype;
    ByteCodeArray properties;
    public static boolean loadProperties = true;

    public VCFFormatField() {
        properties = new ByteCodeArray();
    }

    private VCFFormatField(SVGenotype svGenotype, ByteCodeArray properties) {
        this.svGenotype = svGenotype;
        this.properties = properties;
    }


    /**
     * parse a subject gty into a VCFFormatField subject
     *
     * @param gtyByteCode           a subject genotype field
     * @param genotypeFilterManager a genotype filter function manager
     * @param vcfFormatField        update vcfFormatField subject
     */
    public static void parseOne(ByteCode gtyByteCode, GenotypeFilterManager genotypeFilterManager,
                                VCFFormatField vcfFormatField) {
        vcfFormatField.properties.clear();
        if (genotypeFilterManager == null){
            // no gty and
        }
        gtyByteCode.split(ByteCode.COLON, vcfFormatField.properties);
        SVGenotype gty;
        try {
            gty = SVGenotype.of(vcfFormatField.properties.popFirst());
        } catch (SVGenotypeParseException e) {
            gty = SVGenotype.noneGenotye;
        }
        vcfFormatField.svGenotype = gty;
        if (genotypeFilterManager != null && !gty.equals(SVGenotype.noneGenotye)) {
            genotypeFilterManager.filter(vcfFormatField);
        }
        if (VCF2SDF.dropFormat){
            vcfFormatField.properties.clear();
        }
    }

    public SVGenotype getSVGenotype() {
        return svGenotype;
    }

    public ByteCode getProperty(int keyIndex) {
        return properties.get(keyIndex);
    }

    public ByteCodeArray getProperties() {
        return properties;
    }

    public ByteCode mergeTags() {
        if (!loadProperties) {
            return ByteCode.EMPTY;
        }
        return new ByteCodeArray(properties).encode().toByteCode();
    }

    public static void setLoad(boolean load) {
        VCFFormatField.loadProperties = load;
    }

    public void filterGenotype() {
        svGenotype = SVGenotype.noneGenotye;
    }
}
