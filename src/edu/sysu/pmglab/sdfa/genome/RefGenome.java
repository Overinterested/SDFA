package edu.sysu.pmglab.sdfa.genome;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.genome.brief.RefGeneName;
import edu.sysu.pmglab.sdfa.genome.brief.RefRNAName;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2023-04-21 15:18
 * @description
 */
public class RefGenome {
    private static RefGenome instance = new RefGenome();
    HashMap<Chromosome, Array<RefGene>> detailRefGenome = new HashMap<>();
    HashMap<Chromosome, Array<RefGeneName>> briefRefGenomeName = new HashMap<>();

    private RefGenome() {
        init();
    }

    public void init() {
        for (Chromosome chromosome : Chromosome.support()) {
            detailRefGenome.put(chromosome, new Array<>());
            briefRefGenomeName.put(chromosome, new Array<>());
        }
    }

    public RefGeneName updateGene(Chromosome chromosome, ByteCode geneName, short RNANum) {
        RefGeneName refGeneName = new RefGeneName(geneName, RNANum);
        briefRefGenomeName.get(chromosome).add(refGeneName);
        return refGeneName;
    }

    public static RefGenome parseGenomeName(String file) {
        try {
            CCFReader reader = new CCFReader(file);
            Array<CCFFieldMeta> ccfMetas = new Array<>((Collection<CCFFieldMeta>) reader.getAllFields());
            Array<CCFFieldMeta> loadMetas = new Array<>();
            loadMetas.add(ccfMetas.get(0));
            loadMetas.add(ccfMetas.get(2));
            loadMetas.add(ccfMetas.get(4));
            loadMetas.add(ccfMetas.get(6));
            reader = new CCFReader(file, loadMetas);
            RefGenome refGenome = new RefGenome();
            IRecord record = reader.getRecord();
            RefGeneName refGeneName;
            while (reader.read(record)) {
                refGeneName = new RefGeneName(((ByteCode) record.get(2)).asUnmodifiable(), (short) (int) record.get(1));
                refGenome.getBriefRefGenomeName().get((Chromosome) record.get(0)).add(refGeneName);
                short rnaNum = refGeneName.numOfRNA();
                for (int i = 0; i < rnaNum; i++) {
                    reader.read(record);
                    refGeneName.getRNANames().add(
                            new RefRNAName(((ByteCode) record.get(2)).asUnmodifiable(), IntArray.wrap(record.get(3)).size() / 2)
                    );
                }
            }
            reader.close();
            return refGenome;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RefGenome parseWholeGenome(String file) {
        try {
            CCFReader reader = new CCFReader(file);
            RefGenome refGenome = new RefGenome();
            IRecord record = reader.getRecord();
            while (reader.read(record)) {
                RefGene refGene = new RefGene().loadGene(record);
                refGenome.detailRefGenome.get((Chromosome) record.get(0)).add(refGene);
                for (int i = 0; i < refGene.numOfSubRNA(); i++) {
                    reader.read(record);
                    refGene.getRNAList().add(RefRNA.loadRNA(record));
                }
            }
            reader.close();
            return refGenome;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public HashMap<Chromosome, Array<RefGeneName>> getBriefRefGenomeName() {
        return briefRefGenomeName;
    }
}
