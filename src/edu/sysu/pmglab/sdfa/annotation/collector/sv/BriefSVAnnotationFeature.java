package edu.sysu.pmglab.sdfa.annotation.collector.sv;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.annotation.Future;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2024-03-31 07:59
 * @description
 */
@Future("Start and End can drop.")
public class BriefSVAnnotationFeature {
    int fileID;
    int start;
    int end;
    int length;
    int line;
    SVTypeSign svTypeSign;
    ByteCodeArray encodeAnnotationArray;

    public BriefSVAnnotationFeature(int fileID, int start, int end, int length, int line) {
        this.fileID = fileID;
        this.start = start;
        this.end = end;
        this.length = length;
        this.line = line;
    }

    public void setSvTypeSign(int typeIndex) {
        this.svTypeSign = SVTypeSign.getByIndex(typeIndex);
    }

    public void initAnnotationFeature(int resourceSize) {
        ByteCode[] emptyFeatures = new ByteCode[resourceSize];
        Arrays.fill(emptyFeatures, ByteCode.EMPTY);
        encodeAnnotationArray = new ByteCodeArray(emptyFeatures);
    }

    public void setEncodeAnnotation(int resourceIndex, ByteCode encodeAnnotation) {
        encodeAnnotationArray.set(resourceIndex, encodeAnnotation);
    }

    public ByteCodeArray getEncodeAnnotationArray() {
        return encodeAnnotationArray;
    }

    public boolean isEmptyAnnot() {
        return encodeAnnotationArray == null || encodeAnnotationArray.isEmpty();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getFileID() {
        return fileID;
    }

    public int compareToLine(BriefSVAnnotationFeature o) {
        return Integer.compare(line, o.line);
    }

    public boolean isComplex(){
        return false;
    }
}
