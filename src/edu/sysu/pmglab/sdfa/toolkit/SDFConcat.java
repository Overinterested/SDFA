package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.CCFTable;
import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.easytools.container.GlobalTemporaryContainer;
import edu.sysu.pmglab.executor.Workflow;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.SDFViewer;
import edu.sysu.pmglab.sdfa.sv.idividual.SubjectManager;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wenjie Peng
 * @create 2024-05-29 01:05
 * @description
 */
public class SDFConcat {
    int thread;
    Logger logger;
    File outputPath;
    boolean silent = true;
    Array<SDFReader> allSDFFile;
    private final File outputDir;
    private final int concatTime;
    Array<File> deleteFileArray = new Array<>();
    final AtomicInteger concatCount = new AtomicInteger();
    final AtomicInteger concatRound = new AtomicInteger();

    private SDFConcat(File outputPath, Array<SDFReader> allSDFFile) {
        this.thread = 1;
        this.allSDFFile = allSDFFile;
        this.outputPath = outputPath;
        this.outputDir = outputPath.getParentFile();
        this.concatTime = (int) (Math.log(allSDFFile.size()) / Math.log(2)) + 1;
    }

    public static SDFConcat of(String concatSDFDir, String outputDir) throws IOException {
        return of(new File(concatSDFDir), new File(outputDir));
    }

    public static SDFConcat of(File concatSDFDir, File outputDir) throws IOException {
        SDFManager.of(concatSDFDir, outputDir).globalSubject(false).globalContig(true).collectSDF();
        Array<SDFReader> sdfReaderArray = SDFManager.getInstance().getSdfReaderArray();
        SDFReader sdfReader = sdfReaderArray.get(0);
        sdfReader.restart();
        SubjectManager.getInstance().register(sdfReader);
        sdfReader.close();
        sdfReader.getMeta().clear();
        CCFTable.clear(sdfReader.getReader());
        if (sdfReaderArray.isEmpty()) {
            throw new UnsupportedEncodingException("No sdf files in " + outputDir + ".");
        }
        if (sdfReaderArray.size() == 1) {
            throw new UnsupportedEncodingException("Only 1 file and no need to concat.");
        }
        SDFGlobalContig.getInstance();
        return new SDFConcat(outputDir.getSubFile("concatResult.sdf"), sdfReaderArray);
    }

    private SDFReader concat(SDFReader k, SDFReader v) throws IOException {
        k.restart();
        v.restart();
        k.getMeta().getSubjects().clear();
        v.getMeta().getSubjects().clear();
        CCFReader kReader = k.getReader();
        CCFReader vReader = v.getReader();
        IRecord kRecord = kReader.getRecord();
        IRecord vRecord = vReader.getRecord();
        CallableSet<Chromosome> allContig = SDFGlobalContig.support();
        int[] globalContigRange = new int[allContig.size()];
        CCFWriter writer = CCFWriter.Builder.of(outputDir.getSubFile(concatCount.get() + ".sdf"))
                .addFields(kReader.getAllFields())
                .build();
        concatCount.incrementAndGet();
        for (int i = 0; i < allContig.size(); i++) {
            int writeCount = 0;
            Chromosome chromosome = allContig.getByIndex(i);
            boolean kLimit = k.limitChrBlock(chromosome);
            boolean vLimit = v.limitChrBlock(chromosome);
            if (kLimit || vLimit) {
                if (kLimit && vLimit) {
                    //region both contain records of current contig
                    boolean kRead = kReader.read(kRecord);
                    boolean vRead = vReader.read(vRecord);
                    if (kRead && vRead) {
                        int compare;
                        int[] kCoordinate;
                        int[] vCoordinate;
                        while (true) {
                            kCoordinate = kRecord.get(0);
                            vCoordinate = vRecord.get(0);
                            kCoordinate[0] = i;
                            kRecord.set(0, kCoordinate);
                            vCoordinate[0] = i;
                            vRecord.set(0, vCoordinate);
                            compare = Integer.compare(kCoordinate[1], vCoordinate[1]);
                            compare = compare == 0 ? Integer.compare(kCoordinate[2], vCoordinate[2]) : compare;
                            if (compare < 0) {
                                writer.write(kRecord);
                                writeCount++;
                                kRead = kReader.read(kRecord);
                                if (!kRead) {
                                    while (vReader.read(vRecord)) {
                                        writer.write(vRecord);
                                        writeCount++;
                                    }
                                    break;
                                }
                            } else {
                                writer.write(vRecord);
                                writeCount++;
                                vRead = vReader.read(vRecord);
                                if (!vRead) {
                                    while (kReader.read(kRecord)) {
                                        writer.write(kRecord);
                                        writeCount++;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    //endregion
                } else {
                    //region only one contain records of current contig
                    CCFReader tmp = kLimit ? kReader : vReader;
                    IRecord record = tmp.getRecord();
                    while (tmp.read(record)) {
                        int[] coordinate = record.get(0);
                        coordinate[0] = i;
                        record.set(0, coordinate);
                        writer.write(record);
                        writeCount++;
                    }
                    //endregion
                }
                globalContigRange[i] = writeCount;
            }
        }
        SDFMeta meta = k.getMeta();
        ContigBlockContainer contigBlockContainer = new ContigBlockContainer();
        for (Chromosome chromosome : allContig) {
            contigBlockContainer.getChromosomeByName(new ByteCode(chromosome.getName()));
        }
        meta.setContigBlockContainer(contigBlockContainer).initChrBlockRange(globalContigRange);
        if (concatRound.get() >= concatTime - 1) {
            meta.setSubjects(new Subjects(writer.getOutputFile()).addAll(SubjectManager.getInstance().getIndexableSubjects()));
        }
        writer.writeMeta(meta.write());
        writer.close();
        CCFTable.clear(k);
        CCFTable.clear(v);
        GlobalTemporaryContainer.remove(k.getFilePath());
        GlobalTemporaryContainer.remove(v.getFilePath());
        SDFReader res = new SDFReader(writer.getOutputFile());
        res.close();
        return res;
    }

    public void submit() throws IOException {
        Workflow workflow = new Workflow(thread);
        ProcessBar bar = null;
        if (!silent && logger != null) {
            logger.info("The concat task will spend " + concatTime + " rounds to concatenate all into one.");
        }
        do {
            if (allSDFFile.size() >= 2) {
                bar = new ProcessBar(allSDFFile.size() / 2)
                        .setHeader("Start round " + concatRound.get() + " concat")
                        .setUnit(" times")
                        .start();
                concatRound.incrementAndGet();
            }
            while (allSDFFile.size() >= 2) {
                SDFReader k = allSDFFile.popFirst();
                SDFReader v = allSDFFile.popFirst();
                ProcessBar finalBar = bar;
                workflow.addTasks((status, context) ->
                        {
                            updateArray(concat(k, v));
                            if (!silent && logger != null) {
                                finalBar.addProcessed(1);
                            }
                        }
                );
            }
            workflow.execute();
            workflow.clearTasks();
            if (bar != null) {
                bar.setFinish();
            }
        } while (allSDFFile.size() != 1);
        if (bar != null) {
            bar.setFinish();
        }
        File file = deleteFileArray.popLast();
        boolean successRename = file.renameTo(outputPath);
        if (!successRename) {
            logger.warn("Concat result file fails to rename, which stores in " + file + " .");
        }
        for (File deleteFile : deleteFileArray) {
            boolean delete = deleteFile.delete();
            if (!delete) {
                logger.warn("The intermediate file fails to be deleted. Please go to " + deleteFile.getParentFile() + " to delete it");
                break;
            }
        }
    }

    private synchronized void updateArray(SDFReader reader) {
        allSDFFile.add(reader);
        deleteFileArray.add(reader.getFilePath());
    }

    public SDFConcat threads(int thread) {
        this.thread = thread;
        return this;
    }

    public SDFConcat setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public static void main(String[] args) throws IOException {
//        Logger logger = LoggerFactory.getLogger("test");
//        SDFConcat.of(
//                new File("/Users/wenjiepeng/Desktop/tmp/concat"),
//                new File("/Users/wenjiepeng/Desktop/tmp/concat/")
//        ).silent(false).setLogger(logger).submit();
//        SDFAEntry.main("concat -dir /Users/wenjiepeng/Desktop/tmp -o /Users/wenjiepeng/Desktop/tmp --threads 1".split(" "));
        SDFViewer.view("/Users/wenjiepeng/Desktop/tmp/concat/concatResult.sdf");
    }

    public SDFConcat silent(boolean silent) {
        this.silent = silent;
        return this;
    }
}
