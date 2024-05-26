package edu.sysu.pmglab.sdfa.sv;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.array.*;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;

/**
 * @author Wenjie Peng
 * @create 2024-03-22 11:37
 * @description
 */
public class SVGenotypes {
    SVGenotype[] genotypes;
    Array<ByteCodeArray> property;
    static CallableSet<ByteCode> tag;
    static DefaultValueWrapper<SVGenotype> defaultGenotypeWrapper = new DefaultValueWrapper<>(SVGenotype.noneGenotye);

    public SVGenotypes(SVGenotype[] genotypes) {
        this.genotypes = genotypes;
        property = null;
    }


    public int numOfGenotypes() {
        return this.genotypes.length;
    }


    public static SVGenotypes decode(ShortArray byteCode) {
        if (byteCode.size() == 0) {
            return null;
        }
        SVGenotype[] svGenotypes = new SVGenotype[byteCode.size()];
        int index = 0;
        for (short i : byteCode) {
            svGenotypes[index++] = SVGenotype.of(i);
        }
        return new SVGenotypes(svGenotypes);
    }

    public Object getProperty(ByteCode key) {
        assert property != null;
        return property.get(tag.indexOfValue(key));
    }

    public void fillGenotypes(SVGenotype[] svGenotypes, ByteCode gtysByteCode) {

    }

    public short[] encodeGTs() {
        if (genotypes == null || genotypes.length == 0) {
            return new short[0];
        }
        short[] res = new short[genotypes.length];
        for (int i = 0; i < genotypes.length; i++) {
            res[i] = genotypes[i].getEncoder();
        }
        return res;
    }

    public static SVGenotypes decodeGTs(short[] encode) {
        if (encode == null || encode.length == 0) {
            return new SVGenotypes(null);
        }
        SVGenotype[] genotypes = new SVGenotype[encode.length];
        for (int i = 0; i < encode.length; i++) {
            genotypes[i] = SVGenotype.of(encode[i]);
        }
        return new SVGenotypes(genotypes);
    }

    public ByteCode encodeProperties() {
        if (property == null || property.isEmpty()) {
            return ByteCode.EMPTY;
        }
        BaseArrayEncoder encoder = ByteCodeArray.getEncoder(property.size() * property.get(0).size());
        for (int i = 0; i < property.size(); i++) {
            ByteCodeArray tmpFormat = property.get(i);
            while (!tmpFormat.isEmpty()) {
                encoder.add(tmpFormat.popFirst());
            }
        }
        return encoder.flush().toByteCode();
    }

    public static Array<ByteCodeArray> decodeProperties(ByteCode src) {
        if (src.equals(ByteCode.EMPTY)) {
            return null;
        }
        ByteCodeArray sampleProperties = (ByteCodeArray) ArrayType.decode(src);
        int size = sampleProperties.size();
        Array<ByteCodeArray> properties = new Array<>(size);
        for (int i = 0; i < size; i++) {
            properties.add((ByteCodeArray) ArrayType.decode(sampleProperties.get(i)));
        }
        return properties;
    }

    public SVGenotypes setProperty(Array<ByteCodeArray> property) {
        this.property = property;
        return this;
    }

    public SVGenotype getGenotype(int index) {
        return genotypes[index];
    }

    public SVGenotype[] getGenotypes() {
        return genotypes;
    }

    public static SVGenotype getByDefaultGenotype(SVGenotype svGenotype) {
        return defaultGenotypeWrapper.getValue(svGenotype);
    }


    public Array<ByteCodeArray> getProperty() {
        return property;
    }

    public void reset() {
        if (property != null) {
            property.clear();
        }
    }
}
