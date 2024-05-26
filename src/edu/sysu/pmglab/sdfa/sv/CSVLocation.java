package edu.sysu.pmglab.sdfa.sv;

import edu.sysu.pmglab.container.array.ByteArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;

/**
 * @author Wenjie Peng
 * @create 2024-03-23 02:45
 * @description
 */
public class CSVLocation {
    final int indexInFile;
    IntArray chromosomeArray;

    public CSVLocation(int indexInFile) {
        this.indexInFile = indexInFile;
    }

    public CSVLocation(int indexInFile, Chromosome... chromosomes) {
        this.indexInFile = indexInFile;
        chromosomeArray = new IntArray();
        for (Chromosome chromosome : chromosomes) {
            chromosomeArray.add(chromosome.getIndex());
        }
    }
    public CSVLocation(int indexInFile, IntArray chromosomeIndexArray) {
        this.indexInFile = indexInFile;
        this.chromosomeArray = chromosomeIndexArray;
    }
    public int indexInFile() {
        return indexInFile;
    }

    public IntArray getChromosomeArray() {
        return chromosomeArray;
    }

    public int indexOfRawSV() {
        if (chromosomeArray == null) {
            return -1;
        }
        int index = 0;
        for (int chrIndex : chromosomeArray) {
            if (chrIndex == -1) {
                break;
            }
            index++;
        }
        return index;
    }

    public int getIndexInFile() {
        return indexInFile;
    }

    public int[] encodeCSVChrIndex() {
        if (chromosomeArray == null) {
            return new int[0];
        }
        return chromosomeArray.toBaseArray();
    }

    public CSVLocation setChromosomeArray(IntArray chromosomeArray) {
        this.chromosomeArray = chromosomeArray;
        return this;
    }

    public static IntArray decodeCSVChrIndex(int[] encode) {
        if (encode == null || encode.length == 0) {
            return null;
        }
        return IntArray.wrap(encode);
    }

    public int numOfSVs() {
        return chromosomeArray == null ? 1 : chromosomeArray.size();
    }

    public void reset(){
        chromosomeArray.clear();;
    }
}
