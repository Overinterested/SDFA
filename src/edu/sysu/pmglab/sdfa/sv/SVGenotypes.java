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
    Array<ByteCodeArray> properties;
    static CallableSet<ByteCode> tag;
    static ByteCodeArray emptyArray = new ByteCodeArray(new ByteCode[0]);
    static DefaultValueWrapper<SVGenotype> defaultGenotypeWrapper = new DefaultValueWrapper<>(SVGenotype.noneGenotye);

    public SVGenotypes(SVGenotype[] genotypes) {
        this.genotypes = genotypes;
        properties = null;
    }

    public static SVGenotypes initSubjects(int numOfSubjects) {
        return new SVGenotypes(new SVGenotype[numOfSubjects]);
    }

    public void initFormatField(int numOfFormatFields) {
        if (genotypes != null && numOfFormatFields > 0) {
            int numOfSubjects = genotypes.length;
            properties = new Array<>(numOfFormatFields);
            for (int i = 0; i < numOfFormatFields; i++) {
                properties.add(new ByteCodeArray(numOfSubjects));
            }
        }
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
        assert properties != null;
        return properties.get(tag.indexOfValue(key));
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

    public ByteCodeArray encodeProperties() {
        if (properties == null || properties.isEmpty()) {
            return emptyArray;
        }
        ByteCodeArray encodeProperties = new ByteCodeArray(properties.size());
        for (int i = 0; i < properties.size(); i++) {
            ByteCodeArray tmpFormatField = properties.get(i);
            if (tmpFormatField == null || tmpFormatField.isEmpty()) {
                encodeProperties.add(ByteCode.EMPTY);
            } else {
                encodeProperties.add(properties.get(i).encode().toByteCode());
            }
        }
        return encodeProperties;
    }

    public static Array<ByteCodeArray> decodeProperties(ByteCode[] src) {
        if (src.length == 0) {
            return null;
        }
        ByteCodeArray wrap = ByteCodeArray.wrap(src);
        int formatSize = wrap.size();
        Array<ByteCodeArray> properties = new Array<>(formatSize);
        for (int i = 0; i < formatSize; i++) {
            properties.add((ByteCodeArray) ArrayType.decode(wrap.get(i)));
        }
        return properties;
    }

    public SVGenotypes setProperties(Array<ByteCodeArray> properties) {
        this.properties = properties;
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


    public Array<ByteCodeArray> getProperties() {
        return properties;
    }

    public void reset() {
        if (properties != null) {
            properties.clear();
        }
    }

    public void addGtyAndProperty(int indexOfSubject, SVGenotype genotype, BaseArray<ByteCode> property) {
        genotypes[indexOfSubject] = genotype;
        if (property != null) {
            for (int i = 0; i < property.size(); i++) {
                properties.get(i).add(property.get(i));
            }
        }
    }

    public void clearProperties() {
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                properties.get(i).clear();
            }
        }
    }
}
