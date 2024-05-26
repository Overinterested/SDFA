package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.sdfa.toolkit.Contig;

/**
 * @author Wenjie Peng
 * @create 2024-04-02 02:43
 * @description
 */
public class Genome2AnnotateResource {
    String resource;
    String version;
    final File genomeFile;
    Contig contig = new Contig();
    CallableSet<ByteCode> strandMap = new CallableSet<>();
    CallableSet<ByteCode> gtfResourceMap = new CallableSet<>();
    static CallableSet<ByteCode> geneFeatureSet = new CallableSet<>();
    ReusableMap<ByteCode, ByteCode> attributeSet = new ReusableMap<>();
    private static CallableSet<ByteCode> featureMap = new CallableSet<>();
    public Genome2AnnotateResource(File genomeFile) {
        this.genomeFile = genomeFile;
    }

    public Genome2AnnotateResource setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public Genome2AnnotateResource setVersion(String version) {
        this.version = version;
        return this;
    }

}
