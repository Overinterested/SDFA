package edu.sysu.pmglab.easytools.container;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.Interval;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.toolkit.Contig;

import java.util.Comparator;

/**
 * @author Wenjie Peng
 * @create 2024-04-09 02:16
 * @description store the chromosome names and ranges in file
 */
public class ContigBlockContainer {
    Contig contig;
    ReusableMap<Chromosome, Interval<Integer>> chrRangeBlock = new ReusableMap<>();

    public ContigBlockContainer(){
        contig = new Contig();
    }
    public void putChromosomeRange(Chromosome chr, int start, int end) {
        chrRangeBlock.put(chr, new Interval<>(start, end));
    }

    public ByteCode encode() {
        ByteCodeArray encodeArray = new ByteCodeArray();
        for (Chromosome chromosome : chrRangeBlock.keySet()) {
            Interval<Integer> interval = chrRangeBlock.get(chromosome);
            if (interval != null) {
                encodeArray.add(chromosome.getName());
                encodeArray.add(ValueUtils.ValueEncoder.encodeInt(interval.start()));
                encodeArray.add(ValueUtils.ValueEncoder.encodeInt(interval.end()));
            }
        }
        ByteCode res = encodeArray.encode().toUnmodifiableByteCode();
        encodeArray.close();
        return res;
    }

    public static ContigBlockContainer decode(ByteCode src) {
        if (src == null || src.length() == 0) {
            return null;
        }
        ContigBlockContainer res = new ContigBlockContainer();
        ByteCodeArray decode = (ByteCodeArray) ArrayType.decode(src);
        for (int i = 0; i < decode.size(); i++) {
            ByteCode chrName = decode.get(i++).asUnmodifiable();
            Chromosome chromosome = res.contig.get(chrName);
            int start = ValueUtils.ValueDecoder.decodeInt(decode.get(i++));
            int end = ValueUtils.ValueDecoder.decodeInt(decode.get(i));
            res.chrRangeBlock.put(chromosome, new Interval<>(start, end));
        }
        return res;
    }

    public static ContigBlockContainer parse(ReusableMap<Chromosome, Integer> chromosomeCountMap) {
        if (chromosomeCountMap == null || chromosomeCountMap.isEmpty()) {
            return null;
        }
        ContigBlockContainer res = new ContigBlockContainer();
        Array<Chromosome> chromosomes = new Array<>(chromosomeCountMap.keySet());
        chromosomes.sort((Comparator.comparingInt(Chromosome::getIndex)));
        int index = 0;
        for (Chromosome chromosome : chromosomes) {
            int end = index + chromosomeCountMap.get(chromosome);
            res.putChromosomeRange(chromosome, index, end);
            index = end;
        }
        return res;
    }

    public static ContigBlockContainer parse(Contig contig, int[] chromosomeCountArray) {
        ContigBlockContainer res = new ContigBlockContainer();
        int indexOfFile = 0;
        int indexOfChr = 0;
        CallableSet<Chromosome> allChromosomes = contig.getAllChromosomes();
        for (Chromosome chromosome : allChromosomes) {
            int end = indexOfFile + chromosomeCountArray[indexOfChr++];
            res.putChromosomeRange(chromosome, indexOfFile, end);
            indexOfFile = end;
        }
        res.contig = contig;
        return res;
    }

    public CallableSet<Chromosome> getAllChromosomes(){
        return contig.getAllChromosomes();
    }
    public Chromosome getChromosomeByIndex(int chrIndex){
        return contig.getByIndex(chrIndex);
    }
    public Chromosome getChromosomeByName(ByteCode chrName){
        return contig.get(chrName);
    }

    public Interval<Integer> getChromosomeRange(Chromosome chromosome){
        return chrRangeBlock.get(chromosome);
    }

    public ContigBlockContainer setContig(Contig contig) {
        this.contig = contig;
        return this;
    }
    public ContigBlockContainer asUnmodified(){
        ContigBlockContainer res = new ContigBlockContainer();
        res.contig = contig.asUnmodified();
        res.chrRangeBlock = new ReusableMap<>();
        res.chrRangeBlock.putAll(chrRangeBlock);
        return res;
    }

    public Contig getContig() {
        return contig;
    }


}
