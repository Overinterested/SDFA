package edu.sysu.pmglab.sdfa.sv.bed;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;

public class BEDFile {
    File bedFile;
    File outputDir;
    boolean indexLoad = false;
    IntArray loadInfoIndex = new IntArray();
    Array<BriefSVAnnotationFeature> briefSVAnnotationFeatureArray;
    static IntArray coordinateIndex = new IntArray();

    static {
        coordinateIndex.add(0);
        coordinateIndex.add(1);
        coordinateIndex.add(2);
    }
    public static BEDFile of(File bedFile){
        return new BEDFile();
    }
    public void load() {
        indexLoad = false;

    }

    public BEDFile setChromosomeColIndex(int index) {
        coordinateIndex.set(0, index);
        return this;
    }

    public BEDFile setPosColIndex(int index) {
        coordinateIndex.set(1, index);
        return this;
    }

    public BEDFile setEndColIndex(int index) {
        coordinateIndex.set(2, index);
        return this;
    }
}
