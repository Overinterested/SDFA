package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.sdfa.toolkit.Contig;

/**
 * @author Wenjie Peng
 * @create 2024-04-08 23:54
 * @description
 */
public abstract class AbstractGeneFeatureResourceConvertor {
    ByteCode version;
    ByteCode resource;
//    final File geneFile;
    Contig contig = new Contig();
    CallableSet<ByteCode> strandMap = new CallableSet<>();
    CallableSet<ByteCode> gtfResourceMap = new CallableSet<>();
    static CallableSet<ByteCode> featureMap = new CallableSet<>();
    static CallableSet<ByteCode> geneFeatureSet = new CallableSet<>();
    ReusableMap<ByteCode, ByteCode> attributeSet = new ReusableMap<>();
}
