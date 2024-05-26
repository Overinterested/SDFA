package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.genome.RefGene;
import edu.sysu.pmglab.sdfa.genome.RefRNA;
import edu.sysu.pmglab.sdfa.toolkit.Contig;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-04-08 23:53
 * @description
 */
public class KggSeqGeneResource2SDFAResource {
    boolean load;
    ByteCode version;
    ByteCode resource;
    final File geneFile;
    final File outputFile;
    Contig contig = new Contig();
    public static int repeatRNAName = 0;
    HashMap<ByteCode, RefGene> geneNameMap = new HashMap<>();
    HashMap<ByteCode, HashMap<Chromosome, RefGene>> geneNameChromosomeMap = new HashMap<>();

    public KggSeqGeneResource2SDFAResource(File geneFile, File outputFile) {
        this.geneFile = geneFile;
        this.outputFile = outputFile;
    }

    public static KggSeqGeneResource2SDFAResource of(Object input, Object output) {
        return new KggSeqGeneResource2SDFAResource(new File(input.toString()), new File(output.toString()));
    }

    public void convert() throws IOException {
        FileStream fs = new FileStream(geneFile);
        VolumeByteStream cache = new VolumeByteStream();
        //region parse header
        ByteCodeArray header = new ByteCodeArray();
        while (fs.readLine(cache) != -1) {
            if (cache.startWith(new byte[]{ByteCode.NUMBER_SIGN})) {
                header.add(cache.toUnmodifiableByteCode());
                cache.reset();
                continue;
            }
            break;
        }
        //endregion
        //region init parse parameters
        int RNAStart;
        int RNAEnd;
        int cdsStart;
        int cdsEnd;
        byte strand;
        ByteCode RNAName;
        ByteCode geneName;
        IntArray exonStartArray;
        IntArray exonEndArray;
        //endregion
        //region parse lines
        do {
            BaseArray<ByteCode> lineSplit = cache.toByteCode().split(ByteCode.TAB);
            RNAName = lineSplit.get(1).asUnmodifiable();
            Chromosome chromosome = contig.get(lineSplit.get(2));
            strand = lineSplit.get(3).startsWith(ByteCode.ADD) ? (byte) 0 : (byte) 1;
            RNAStart = lineSplit.get(4).toInt();
            RNAEnd = lineSplit.get(5).toInt();
            cdsStart = lineSplit.get(6).toInt();
            cdsEnd = lineSplit.get(7).toInt();
            exonStartArray = IntArray.wrap(parseArray(lineSplit.get(9)));
            exonEndArray = IntArray.wrap(parseArray(lineSplit.get(10)));
            geneName = lineSplit.get(12).asUnmodifiable();
            RefGene refGene;
            HashMap<Chromosome, RefGene> chromosomeRefGeneHashMap = geneNameChromosomeMap.get(geneName);
            if (chromosomeRefGeneHashMap == null){
                HashMap<Chromosome, RefGene> chrGeneMap = new HashMap<>();
                RefGene tmp = new RefGene(geneName);
                chrGeneMap.put(chromosome, tmp);
                tmp.setChr(chromosome);
                refGene = tmp;
                geneNameChromosomeMap.put(geneName, chrGeneMap);
            }else {
                refGene = chromosomeRefGeneHashMap.get(chromosome);
                if (refGene == null){
                    refGene = new RefGene(geneName);
                    refGene.setChr(chromosome);
                    chromosomeRefGeneHashMap.put(chromosome, refGene);
                }
            }
            RefRNA refRNA = RefRNA.build(strand, RNAStart, RNAEnd, cdsStart, cdsEnd, exonStartArray, exonEndArray, RNAName);
            refGene.updateRNA(refRNA);
        } while (fs.readLine(cache) != -1);
        //endregion
        fs.close();
        cache.close();
        Array<RefGene> refGenes = new Array<>(true);
        for (HashMap<Chromosome, RefGene> value : geneNameChromosomeMap.values()) {
            if (value == null){
                continue;
            }
            refGenes.addAll(value.values());
        }
        refGenes.sort((Comparator.comparingInt((RefGene o) -> o.getChr().getIndex()).thenComparing(o -> o)));
        toCCF(refGenes);
    }

    private int[] parseArray(ByteCode src) {
        BaseArray<ByteCode> split = src.split(ByteCode.COMMA);
        int[] res = new int[split.size() - 1];
        for (int i = 0; i < split.size() - 1; i++) {
            res[i] = split.get(i).toInt();
        }
        return res;
    }

    private void toCCF(Array<RefGene> refGenes) throws IOException {
        IntArray emptyIntArray = IntArray.wrap(new int[0]);
        CCFWriter writer = CCFWriter.Builder.of(outputFile)
                .addField("chrIndex", FieldType.varInt32)
                .addField("indexOfChr", FieldType.varInt32)
                .addField("numOfSubElement", FieldType.varInt32)
                .addField("type", FieldType.varInt32)
                .addField("name", FieldType.bytecode)
                .addField("preposition", FieldType.int32Array)
                .addField("exonArray", FieldType.int32Array)
                .build();
        int indexOfRNAInGene;
        short indexOfGeneInChr = 0;
        IRecord record = writer.getRecord();
        CallableSet<Chromosome> allChromosomes = contig.getAllChromosomes();
        int[] numOfDifferentChromosomeRecords = new int[allChromosomes.size()];
        Chromosome curr = allChromosomes.getByIndex(0);
        for (RefGene refGene : refGenes) {
            if (!refGene.getChr().equals(curr)) {
                indexOfGeneInChr = 0;
                curr = refGene.getChr();
            }
            record.set(0, curr.getIndex());
            record.set(1, indexOfGeneInChr++);
            record.set(2, refGene.numOfSubRNA());
            // gene symbol
            record.set(3, (byte) 0);
            record.set(4, refGene.getGeneName());
            IntArray geneStartAndEnd = new IntArray(2);
            geneStartAndEnd.add(refGene.getGeneStartPos());
            geneStartAndEnd.add(refGene.getGeneEndPos());
            record.set(5, geneStartAndEnd);
            record.set(6, emptyIntArray);
            numOfDifferentChromosomeRecords[curr.getIndex()]++;
            writer.write(record);
            refGene.sortRNA();
            indexOfRNAInGene = 0;
            for (RefRNA refRNA : refGene.getRNAList()) {
                record.clear();
                record.set(0, curr.getIndex());
                record.set(1, (short) indexOfRNAInGene++);
                record.set(2, refRNA.getExonNum());
                record.set(3, (byte) 1);
                record.set(4, refRNA.getRNAName());
                IntArray prePos = new IntArray(7);
                prePos.add(refRNA.getStartPos());
                prePos.add(refRNA.getEndPos());
                prePos.add(refRNA.getCdsStartPos());
                prePos.add(refRNA.getCdsEndPos());
                prePos.add(refRNA.getCdsStartExonIndex());
                prePos.add(refRNA.getCdsEndExonIndex());
                prePos.add((int) refRNA.getStrand());
                record.set(5, prePos);
                IntArray exonList = new IntArray(refRNA.getExonPosList());
                record.set(6, exonList);
                numOfDifferentChromosomeRecords[curr.getIndex()]++;
                writer.write(record);
            }
        }
        AnnotationResourceMeta meta = new AnnotationResourceMeta();
        ContigBlockContainer contigContainer = ContigBlockContainer.parse(contig, numOfDifferentChromosomeRecords);
        meta.setChromosomeBlockContainer(contigContainer);
        writer.writeMeta(meta.write());
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        KggSeqGeneResource2SDFAResource.of(
                "/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/resource/kggseqv1.1_hg38_refGene.txt.gz",
                "/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/resource/hg38_refGene.ccf"
        ).convert();
    }
}
