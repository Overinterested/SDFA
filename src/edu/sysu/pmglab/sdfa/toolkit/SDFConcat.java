package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.executor.Workflow;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFReader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Wenjie Peng
 * @create 2024-05-29 01:05
 * @description
 */
public class SDFConcat {
    int thread;
    File outputPath;
    Array<SDFReader> allSDFFile;
    private final File outputDir;
    private final int concatTime;
    Lock putQueue = new ReentrantLock();
    Lock updateQueue = new ReentrantLock();
    final AtomicInteger concatCount = new AtomicInteger();
    Array<Entry<SDFReader, SDFReader>> tobeConcatenateQueue = new Array<>();

    private SDFConcat(File outputPath, Array<SDFReader> allSDFFile) {
        this.allSDFFile = allSDFFile;
        this.outputPath = outputPath;
        this.outputDir = outputPath.getParentFile();
        this.concatTime = (int) (Math.log(allSDFFile.size()) / Math.log(2)) + 1;
    }

    public static SDFConcat of(File concatSDFDir, File outputDir) throws IOException {
        SDFManager.of(concatSDFDir, outputDir).globalContig(true).collectSDF();
        Array<SDFReader> sdfReaderArray = SDFManager.getInstance().getSdfReaderArray();
        if (sdfReaderArray == null) {
            throw new UnsupportedEncodingException("No sdf files in " + outputDir + ".");
        }
        if (sdfReaderArray.size() == 1) {
            throw new UnsupportedEncodingException("Only 1 file and no need to concat.");
        }
        GlobalVCFContigConvertor.getInstance();
        return new SDFConcat(outputDir.getSubFile("concatResult.sdf"), sdfReaderArray);
    }

    private SDFReader concat(Entry<SDFReader, SDFReader> concatEntry) throws IOException {
        SDFReader k = concatEntry.getKey();
        SDFReader v = concatEntry.getValue();
        CCFReader kReader = k.getReader();
        CCFReader vReader = v.getReader();
        IRecord kRecord = kReader.getRecord();
        IRecord vRecord = vReader.getRecord();
        CallableSet<Chromosome> allContig = GlobalVCFContigConvertor.support();
        int[] globalContigRange = new int[allContig.size()];
        CCFWriter writer = CCFWriter.Builder.of(outputDir.getSubFile(concatCount.get() + ".sdf"))
                .addFields(kReader.getAllFields())
                .build();
        boolean endRead;
        for (int i = 0; i < allContig.size(); i++) {
            Chromosome chromosome = allContig.getByIndex(i);
            boolean kLimit = k.limitChrBlock(chromosome);
            boolean vLimit = v.limitChrBlock(chromosome);
            if (kLimit || vLimit) {
                if (kLimit && vLimit) {
                    //region: both contain records of current contig
                    endRead = false;
                    int count = 0;
                    while (!endRead) {
                        boolean kRead = kReader.read(kRecord);
                        boolean vRead = vReader.read(vRecord);
                        if (kRead && vRead) {
                            //region successful read the two
                            int[] kCoordinate = kRecord.get(0);
                            int[] vCoordinate = vRecord.get(0);
                            kCoordinate[0] = i;
                            kRecord.set(0, kCoordinate);
                            vCoordinate[0] = i;
                            vRecord.set(0, vCoordinate);
                            int compare = Integer.compare(kCoordinate[1], vCoordinate[1]);
                            compare = compare == 0 ? Integer.compare(kCoordinate[2], vCoordinate[2]) : compare;
                            if (compare < 0) {
                                writer.write(kRecord);
                                writer.write(vRecord);
                                count += 2;
                            } else {
                                writer.write(vRecord);
                                writer.write(kRecord);
                                count += 2;
                            }
                            //endregion
                        } else {
                            //region only read one
                            CCFReader tmp = kRead ? kReader : vReader;
                            while (tmp.read(kRecord)) {
                                int[] kCoordinate = kRecord.get(0);
                                kRecord.set(0, kCoordinate);
                                writer.write(kRecord);
                                count++;
                            }
                            endRead = true;
                            //endregion
                        }
                    }
                    globalContigRange[i] = count;
                    //endregion
                } else {
                    //region only one contain records of current contig
                    int count = 0;
                    CCFReader tmp = kLimit ? kReader : vReader;
                    IRecord record = tmp.getRecord();
                    while (tmp.read(record)) {
                        int[] coordinate = record.get(0);
                        coordinate[0] = i;
                        record.set(0, coordinate);
                        writer.write(record);
                        count++;
                    }
                    globalContigRange[i] = count;
                    //endregion
                }
            }
        }
        SDFMeta meta = k.getMeta();
        ContigBlockContainer contigBlockContainer = new ContigBlockContainer();
        for (Chromosome chromosome : allContig) {
            contigBlockContainer.getChromosomeByName(new ByteCode(chromosome.getName()));
        }
        meta.setContigBlockContainer(contigBlockContainer).initChrBlockRange(globalContigRange);
        writer.writeMeta(meta.write());
        writer.close();
        return new SDFReader(writer.getOutputFile());
    }

    public void submit() throws IOException {
        Workflow workflow = new Workflow(thread);
        //TODO while
        while (!(allSDFFile.size() <= 1)) {
            tobeConcatenateQueue.add(new Entry<>(allSDFFile.popFirst(), allSDFFile.popFirst()));
        }
    }

    public SDFConcat threads(int thread) {
        this.thread = thread;
        return this;
    }
}
