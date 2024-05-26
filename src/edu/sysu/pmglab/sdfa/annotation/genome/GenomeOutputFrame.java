package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.sdfa.annotation.toolkit.OutputFrame;

/**
 * @author Wenjie Peng
 * @create 2024-04-19 19:32
 * @description
 */
public class GenomeOutputFrame extends OutputFrame {
    static boolean posAnnotation = true;
    static boolean endAnnotation = true;
    static boolean coverageAnnotation = true;

    public GenomeOutputFrame() {
        ByteCodeArray outputColNameArray = new ByteCodeArray();
        if (posAnnotation) {
            outputColNameArray.add(new byte[]{ByteCode.P, ByteCode.O, ByteCode.S});
        }
        if (endAnnotation) {
            outputColNameArray.add(new byte[]{ByteCode.E, ByteCode.N, ByteCode.D});
        }
        if (coverageAnnotation) {
            outputColNameArray.add(new byte[]{ByteCode.C, ByteCode.O, ByteCode.V, ByteCode.E, ByteCode.R, ByteCode.A, ByteCode.G, ByteCode.E});
        }
        this.setOutputColNameArray(outputColNameArray);
    }

    @Override
    public void check() {
    }


}
