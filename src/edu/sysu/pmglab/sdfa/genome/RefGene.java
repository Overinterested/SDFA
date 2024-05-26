package edu.sysu.pmglab.sdfa.genome;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;

import java.util.HashSet;

/**
 * @author Wenjie Peng
 * @create 2023-04-21 13:34
 * @description
 */
public class RefGene implements Comparable<RefGene> {
    Chromosome chr;
    ByteCode geneName;
    int numOfRNA;
    int geneStartPos;
    int geneEndPos;
    short geneIndex;
    Array<RefRNA> RNAList;
    HashSet<ByteCode> RNANameSet = new HashSet<>();
    public RefGene() {
        RNAList = new Array<>();
    }

    public RefGene(ByteCode geneName) {
        this.geneName = geneName;
        RNAList = new Array<>();
    }

    public static RefGene loadGene(IRecord record) {
        RefGene refGene = new RefGene();
        refGene.chr = Chromosome.get(record.get(0));
        refGene.geneIndex = (short) (int)record.get(1);
        refGene.numOfRNA = (int) record.get(2);
        refGene.geneName = ((ByteCode) record.get(4)).asUnmodifiable();
        refGene.RNAList = new Array<>(refGene.numOfRNA);
        IntArray startEndPos = IntArray.wrap(record.get(5));
        refGene.geneStartPos = startEndPos.get(0);
        refGene.geneEndPos = startEndPos.get(1);
        return refGene;
    }

    public void updateRNA(RefRNA refRNA) {
        // consider multiply RNA
        if (RNANameSet.contains(refRNA.getRNAName())) {
            boolean flag = false;
            int count = 1;
            while (!flag) {
                refRNA.RNAName = new ByteCode(refRNA.RNAName.toString() + "." + count);
                if (!RNANameSet.contains(refRNA.getRNAName())) {
                    flag = true;
                }
                count++;
            }
        }
        if (numOfRNA == 0) {
            geneStartPos = refRNA.startPos;
        } else {
            geneStartPos = Math.min(geneStartPos, refRNA.startPos);
        }
        geneEndPos = Math.max(geneEndPos, refRNA.endPos);
        numOfRNA++;
        RNANameSet.add(refRNA.getRNAName());
        RNAList.add(refRNA);
    }

    @Override
    public int compareTo(RefGene o) {
        int status = Integer.compare(geneStartPos, o.geneStartPos);
        return status == 0 ? Integer.compare(geneEndPos, o.geneEndPos) : status;
    }

    public RefGene setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public ByteCode getGeneName() {
        return geneName;
    }

    public Chromosome getChr() {
        return chr;
    }

    public RefGene setChr(Chromosome chr) {
        this.chr = chr;
        return this;
    }

    public short getGeneIndex() {
        return geneIndex;
    }

    public int numOfSubRNA() {
        return numOfRNA;
    }

    public int getGeneStartPos() {
        return geneStartPos;
    }

    public int getGeneEndPos() {
        return geneEndPos;
    }

    public Array<RefRNA> getRNAList() {
        return RNAList;
    }
    public void sortRNA() {
        RNAList.sort(RefRNA::compareTo);
        short count = 0;
        for (RefRNA refRNA : RNAList) {
            refRNA.setRNAIndex(count++);
        }
        RNANameSet.clear();
    }

    public void putRNA(RefRNA refRNA){
        if (RNAList == null){
            RNAList = new Array<>(numOfRNA);
        }
        RNAList.add(refRNA);
    }
}
