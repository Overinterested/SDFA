package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.base.AnnotFeature;
import edu.sysu.pmglab.sdfa.annotation.base.AnnotManager;
import edu.sysu.pmglab.sdfa.genome.RefGenome;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2023-05-02 19:45
 * @description
 */
public class GeneAnnotManager extends AnnotManager {

    private static GeneAnnotManager instance;
    HashMap<Chromosome, Array<GeneAnnotRecord>> refAnnotByChr;

    public RefGenome refGenome;
//    public GenomeAnnotOutput genomeAnnotOutput;

    public GeneAnnotManager() {
        refAnnotByChr = new HashMap<>();
        for (Chromosome chromosome : Chromosome.support()) {
            refAnnotByChr.put(chromosome, new Array<>());
        }
//        genomeAnnotOutput = new GenomeAnnotOutput();
    }

//    @Override
//    public void loadRefRecord() throws IOException {
//        GeneAnnotTool.getInstance().parseItself(this);
//    }

    @Override
    public boolean annotChrForInterval(Chromosome chr, int annotIndex) {
        Array<GeneAnnotRecord> geneAnnotRecords = refAnnotByChr.get(chr);
        if (geneAnnotRecords == null || geneAnnotRecords.size() == 0) {
            return true;
        }

//        return GeneAnnotTool.getInstance().annotChr0(chr, annotIndex, this);
        return false;
    }



    @Override
    public AnnotFeature decode(ByteCode src) {
        return null;
    }


    // FIXME: here we can load geneAnnot without getChr because of meta
    public void updateRecord(GeneAnnotRecord geneAnnotRecord) {
        refAnnotByChr.get(geneAnnotRecord.chr).add(geneAnnotRecord);
    }

    @Override
    public int getTagMapIndex() {
        return super.getTagMapIndex();
    }

    @Override
    public AnnotManager setTagMapIndex(int tagMapIndex) {
        super.setTagMapIndex(tagMapIndex);
        return this;
    }

    @Override
    public void initOutput() {
//        if (genomeAnnotOutput == null) {
//            genomeAnnotOutput = new GenomeAnnotOutput();
//        }
    }


//    @Override
//    public GenomeAnnotOutput getOutput() {
//        return genomeAnnotOutput;
//    }

//    @Override
//    public AnnotManager setOutput(IAnnotOutput output) {
//        return this;
//    }

    @Override
    public void loadRefRecord() throws IOException {

    }

    @Override
    public AnnotManager setAnnotFile(String file) {
        super.setAnnotFile(file);
        return this;
    }

    @Override
    public AnnotManager setAnnotFile(File file) {
//        genomeAnnotOutput.setRefGenome(Objects.requireNonNull(RefGenome.parseName(file.getAbsolutePath())));
        super.setAnnotFile(file);
        return this;
    }

    public int getRefGeneRecordsCount() {
        int res = 0;
        for (Chromosome chromosome : Chromosome.support()) {
            res += refAnnotByChr.get(chromosome).size();
        }
        return res;
    }

    public int getRefRNARecordsCount() {
        int res = 0;
        for (Chromosome chromosome : Chromosome.support()) {
            Array<GeneAnnotRecord> geneAnnotRecords = refAnnotByChr.get(chromosome);
            for (int i = 0; i < geneAnnotRecords.size(); i++) {
                GeneAnnotRecord geneAnnotRecord = geneAnnotRecords.get(i);
                res += geneAnnotRecord.RNACount;
            }
        }
        return res;
    }

}
