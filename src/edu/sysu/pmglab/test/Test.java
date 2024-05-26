package edu.sysu.pmglab.test;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.intervaltree.IntervalObject;
import edu.sysu.pmglab.container.intervaltree.IntervalTree;
import edu.sysu.pmglab.gbc.genome.Chromosome;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-29 09:51
 * @description
 */
public class Test {
    public static void main(String[] args) throws IOException {
        // C1GALT1， TMSB15B， SPANXN2
        HashMap<Chromosome, IntervalTree<String, Integer>> coordinateTree = new HashMap<>();
        IntervalTree<String, Integer> tree1 = new IntervalTree<>();
        tree1.addInterval(15860041, 15860555, "CHR1:SPEN");
        IntervalTree<String, Integer> tree2 = new IntervalTree<>();
        tree2.addInterval(103557277, 103557278, "CHR5:NUDT12");
        IntervalTree<String, Integer> tree3 = new IntervalTree<>();
        tree3.addInterval(10698257, 10698348, "CHR6:PAK1IP1");
        IntervalTree<String, Integer> tree4 = new IntervalTree<>();
        tree4.addInterval(7244619, 7244620, "CHR7:C1GALT1");
        IntervalTree<String, Integer> tree5 = new IntervalTree<>();
        tree5.addInterval(39183337, 39183337, "CHR17:CACNB1");
        IntervalTree<String, Integer> tree6 = new IntervalTree<>();
        tree6.addInterval(5626926, 5627059, "CHR19:SAFB");
        IntervalTree<String, Integer> tree7 = new IntervalTree<>();
        tree7.addInterval(123644285, 123644286, "CHRX:SAFB");
        coordinateTree.put(Chromosome.get("chr1"), tree1);
        coordinateTree.put(Chromosome.get("chr5"), tree2);
        coordinateTree.put(Chromosome.get("chr6"), tree3);
        coordinateTree.put(Chromosome.get("chr7"), tree4);
        coordinateTree.put(Chromosome.get("chr17"), tree5);
        coordinateTree.put(Chromosome.get("chr19"), tree6);
        coordinateTree.put(Chromosome.get("X"), tree7);
        CCFReader reader = new CCFReader("/Users/wenjiepeng/Desktop/SV/data/private/dataset/gnomAD/gnomAD.ccf");
        IRecord record = reader.getRecord();
        CallableSet<ByteCode> infoMap = new CallableSet<>((ByteCodeArray) ArrayType.decode(reader.getMeta().get("INFO").values().get(0)));
        int endIndex = infoMap.indexOfValue(new ByteCode("END"));
        int typeIndex = infoMap.indexOfValue(new ByteCode("SVTYPE"));
        int afIndex = infoMap.indexOfValue(new ByteCode("AF"));
        while (reader.read(record)) {
            Chromosome chromosome = record.get(0);
            if (coordinateTree.containsKey(chromosome)){
                IntervalTree<String, Integer> tmp = coordinateTree.get(chromosome);
                int pos = record.get(1);
                ByteCodeArray info = ByteCodeArray.wrap((ByteCode[]) record.get(7));
                int end = info.get(endIndex).toInt();
                ByteCode type = info.get(typeIndex);
                ByteCode AF = info.get(afIndex);
                ByteCode freqHomRef = info.get(18);
                ByteCode freqHet = info.get(19);
                ByteCode freqHomAlt = info.get(20);
                if (type.equals(new ByteCode("CPX"))){
                    continue;
                }
                BaseArray<IntervalObject<String, Integer>> overlapsIntervals = tmp.getOverlapsIntervals(pos, end);
                if (!overlapsIntervals.isEmpty()){
                    int b = 1;
                }
            }
        }
    }
}
