package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFReader;

import java.io.IOException;
import java.util.PriorityQueue;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 11:13
 * @description
 */
public class SDFConcat {
    //
    final File inputDir;
    final File outputDir;
    Array<SDFReader> sdfReaderArray;

    private SDFConcat(Object inputDir, Object outputDir) {
        this.inputDir = new File(inputDir.toString());
        this.outputDir = new File(outputDir.toString());
    }

    public File concat() throws IOException {
        SDFManager sdfManager = SDFManager.of(inputDir, outputDir);
        sdfManager.collectSDF();
        sdfReaderArray = sdfManager.getSdfReaderArray();
        File outputFile = outputDir.getSubFile("concat_samples.sdf");
        CCFWriter writer = CCFWriter.Builder.of(outputFile)
                .addFields(SDFFormat.SDFFields)
                .build();
        LoserTree loserTree = new LoserTree(writer);
        SDFMeta meta = new SDFMeta();
//        initMeta(meta);
//        loserTree.init();
        for (Chromosome chromosome : GlobalVCFContigConvertor.support()) {
            loserTree.limitRange(chromosome, sdfReaderArray);
            while (loserTree.popOne(sdfReaderArray)) {
                // output one
            }
        }
        writer.writeMeta(meta);
        writer.close();
        return outputFile;
    }

    static class LoserTree {
        CCFWriter writer;
        int prePopFileIndex = -1;

        public LoserTree(CCFWriter writer) {
            this.writer = writer;
        }

        PriorityQueue<Entry<Integer,IRecord>> chromosomeRecordQueue = new PriorityQueue<>(
                (v1, v2) -> {
                    IRecord o1 = v1.getValue();
                    IRecord o2 = v2.getValue();
                    int[] coordinate1 = o1.get(0);
                    int[] coordinate2 = o2.get(0);
                    int status = Integer.compare(coordinate1[1], coordinate2[1]);
                    if (status != 0) {
                        return status;
                    }
                    status = Integer.compare(coordinate1[2], coordinate2[2]);
                    return status == 0 ? Integer.compare(o1.get(1), o2.get(1)) : status;
                }
        );

        public void limitRange(Chromosome chromosome, Array<SDFReader> readerArray) throws IOException {
            for (SDFReader sdfReader : readerArray) {
                sdfReader.restart();
                if (sdfReader.limitChrBlock(chromosome)) {
                    sdfReader.close();
                }
            }
        }

        public boolean popOne(Array<SDFReader> sdfReaderArray) throws IOException {
            boolean canRead = false;
            SDFReader sdfReader = sdfReaderArray.get(prePopFileIndex);
            if (!sdfReader.getReader().isClosed()) {
                // not close
                IRecord record = sdfReader.readRecord();
                chromosomeRecordQueue.add(new Entry<>(sdfReader.getFileID(), record));
                // pop one
//                prePopFileIndex = xxx;
                return true;
            } else {
                // close
                if (!chromosomeRecordQueue.isEmpty()) {
                    Entry<Integer, IRecord> pop = chromosomeRecordQueue.poll();
                    prePopFileIndex = pop.getKey();
                    // pop one

                    return true;
                }
                return false;
            }
        }
    }

    /**
     * build subject, contig, information fields and chromosome block range for merged meta
     *
     * @param meta initial meta
     */
//    private void initMeta(SDFMeta meta) {
//        // subject
//        CallableSet<Subject> indexableSubjects = SubjectManager.getInstance().getIndexableSubjects();
//        for (Subject indexableSubject : indexableSubjects) {
//            meta.getSubjects().addSubject(indexableSubject);
//        }
//        // contig
//        Contig contig = new Contig();
//        contig.setAllChromosomes(GlobalVCFContigConvertor.support());
//        meta.setContig(contig);
//        // encode mode
//        meta.setEncodeMode(2);
//        // info fields
//        CallableSet<ByteCode> mergedInfoFieldSet = new CallableSet<>();
//        for (int i = 0; i < sdfReaderArray.size(); i++) {
//            SDFReader sdfReader = sdfReaderArray.get(i);
//            CallableSet<ByteCode> infoFieldSet = sdfReader.getMeta().getInfoFieldSet();
//            mergedInfoFieldSet.addAll(infoFieldSet);
//        }
//        meta.setInfoFieldSet(mergedInfoFieldSet);
//        // chromosome change
//        ReusableMap<Chromosome, Integer> chromosomeBlockRange = new ReusableMap<>(GlobalVCFContigConvertor.support());
//        for (int i = 0; i < sdfReaderArray.size(); i++) {
//            SDFReader sdfReader = sdfReaderArray.get(i);
//            SDFMeta tmpMeta = sdfReader.getMeta();
//            for (Chromosome chromosome : GlobalVCFContigConvertor.support()) {
//                Interval<Integer> chrBlockRange = tmpMeta.getChrBlockRange(chromosome);
//                if (chrBlockRange != null) {
//                    chromosomeBlockRange.putIfAbsent(chromosome, 0);
//                    chromosomeBlockRange.put(
//                            chromosome,
//                            chromosomeBlockRange.get(chromosome) + chrBlockRange.end() - chrBlockRange.start()
//                    );
//                }
//            }
//        }
//        HashMap<Chromosome, Interval<Integer>> chromosomeIntervalHashMap = new HashMap<>();
//        int indexOfFile = 0;
//        for (Chromosome chromosome : GlobalVCFContigConvertor.support()) {
//            int size;
//            Integer tmpSize = chromosomeBlockRange.get(chromosome);
//            if (tmpSize == null) {
//                size = 0;
//            } else {
//                size = tmpSize;
//            }
//            chromosomeIntervalHashMap.put(chromosome, new Interval<>(indexOfFile, indexOfFile += size));
//        }
//        meta.setChrBlockRange(chromosomeIntervalHashMap);
//    }
}
