package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.CCFTable;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.container.GlobalTemporaryContainer;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.executor.Pipeline;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.sv.idividual.SubjectManager;
import edu.sysu.pmglab.sdfa.sv.vcf.ReusableVCFPool;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wenjie Peng
 * @create 2024-04-12 01:41
 * @description
 */
public class SDFManager {
    Logger logger;
    File outputDir;
    int encodeMode = 2;
    boolean silent = false;
    boolean storeMeta = true;
    boolean globalContig = true;
    boolean globalSubject = true;
    final Array<File> sdfFileArray;
    final Array<File> vcfFileArray;
    private static SDFManager instance;
    Array<SDFReader> sdfReaderArray = new Array<>();
    Array<SDFReader> annotatedSDFArray = new Array<>();

    private SDFManager(File inputDir, File outputDir) {
        this.outputDir = outputDir;
        this.sdfFileArray = FileTool.getFilesFromDir(inputDir, "sdf");
        this.vcfFileArray = FileTool.getFilesFromDir(inputDir, "vcf", "vcf.gz", "vcf.bgz");
    }

    public static SDFManager of(File inputDir, File outputDir) {
        FileTool.checkDirWithCreate(outputDir);
        if (instance == null) {
            return (instance = new SDFManager(inputDir, outputDir));
        }
        throw new UnsupportedOperationException("SDFManager has been initial and can use `SDFManager.reset()` to create a new one.");
    }

    public void collectSDF() throws IOException {
        int fileID = 0;
        ProcessBar bar = null;
        if (!silent) {
            if (logger != null) {
                logger.info("Totally collect " + (sdfFileArray.size() + vcfFileArray.size()) + " SV files: "
                        + sdfFileArray.size() + " for sdf files, "
                        + vcfFileArray.size() + " for vcf files.");

                int size = sdfFileArray.size() + vcfFileArray.size();
                bar = new ProcessBar(size)
                        .setHeader("Load SV Files Speed")
                        .setUnit("files")
                        .start();
            }
        }
        for (File file : sdfFileArray) {
            SDFReader sdfReader = new SDFReader(file).setFileID(fileID++);
            if (storeMeta) {
                GlobalTemporaryContainer.add(file, sdfReader.getMeta());
            }
            if (globalContig) {
                GlobalVCFContigConvertor.Builder.getInstance().addVCFContig(sdfReader.getContig());
            }
            if (globalSubject) {
                SubjectManager.getInstance().register(sdfReader);
            }
            put(sdfReader);
            if (bar != null) {
                bar.addProcessed(1);
            }
        }
        for (File file : vcfFileArray) {
            File sdfFile = new VCF2SDF(file, outputDir.getSubFile(file.getName() + SDFFormat.EXTENSION))
                    .storeMeta(storeMeta)
                    .globalContig(globalContig)
                    .setLogger(logger)
                    .setEncodeMode(encodeMode)
                    .setFileID(fileID++)
                    .convert();
            SDFReader sdfReader = new SDFReader(sdfFile);
            if (globalSubject) {
                SubjectManager.getInstance().register(sdfReader);
            }
            put(sdfReader);
            if (bar != null) {
                bar.addProcessed(1);
            }
        }
        if (bar != null) {
            bar.setFinish();
        }
    }
    public void collectSDFA(File sdfaDir) throws IOException {
        int fileID = 0;
        Array<File> files = FileTool.getFilesFromDir(sdfaDir, "sdfa");
        for (File file : files) {
            SDFReader sdfReader = new SDFReader(file).setFileID(fileID++);
            if (storeMeta) {
                GlobalTemporaryContainer.add(file, sdfReader.getMeta());
            }
            if (globalContig) {
                GlobalVCFContigConvertor.Builder.getInstance().addVCFContig(sdfReader.getContig());
            }
            if (globalSubject) {
                SubjectManager.getInstance().register(sdfReader);
            }
            sdfReader.close();
            CCFTable.clear(sdfReader.getReader());
            annotatedSDFArray.add(sdfReader);
        }
    }

    public Array<Pipeline> collectTask(int thread) {
        AtomicInteger fileID = new AtomicInteger(0);
        Array<Pipeline> pipelines = new Array<>();
        if (!silent) {
            if (logger != null) {
                logger.info("Totally collect " + (sdfFileArray.size() + vcfFileArray.size()) + " SV files: "
                        + sdfFileArray.size() + " for sdf files, "
                        + vcfFileArray.size() + " for vcf files.");
            }
            pipelines.add(new Pipeline((
                            (status, context) -> {
                                int size = sdfFileArray.size() + vcfFileArray.size();
                                context.put(
                                        ProcessBar.class,
                                        new ProcessBar(size)
                                                .setHeader("Load SV Files Speed")
                                                .setUnit("files")
                                                .start());
                            })
                    )
            );
        }
        pipelines.add(Pipeline.BARRIER);
        pipelines.add(new Pipeline(((status, context) -> {
            for (File file : sdfFileArray) {
                SDFReader sdfReader = new SDFReader(file).setFileID(fileID.get());
                fileID.incrementAndGet();
                if (storeMeta) {
                    GlobalTemporaryContainer.add(file, sdfReader.getMeta());
                }
                if (globalContig) {
                    GlobalVCFContigConvertor.Builder.getInstance().addVCFContig(sdfReader.getContig());
                }
                if (globalSubject){
                    SubjectManager.getInstance().register(sdfReader);
                }
                put(sdfReader);
                if (!silent) {
                    ((ProcessBar) context.cast(ProcessBar.class)).addProcessed(1);
                }
            }
            ReusableVCFPool.init(thread);
        })));
        pipelines.add(Pipeline.BARRIER);
        for (File file : vcfFileArray) {
            pipelines.add(new Pipeline(((status, context) -> {
                File sdfFile = new VCF2SDF(file, outputDir.getSubFile(file.getName() + SDFFormat.EXTENSION))
                        .storeMeta(storeMeta)
                        .globalContig(globalContig)
                        .setLogger(logger)
                        .setEncodeMode(encodeMode)
                        .setFileID(fileID.get())
                        .convert();
                SDFReader sdfReader = new SDFReader(sdfFile);
                put(sdfReader.setFileID(fileID.get()));
                if (globalSubject){
                    SubjectManager.getInstance().register(sdfReader);
                }
                fileID.incrementAndGet();
                if (!silent) {
                    ((ProcessBar) context.cast(ProcessBar.class)).addProcessed(1);
                }
            })));
        }
        pipelines.add(Pipeline.BARRIER);
        pipelines.add(new Pipeline(((status, context) -> {
            ReusableVCFPool.close();
            if (!silent) {
                ((ProcessBar) context.cast(ProcessBar.class)).setFinish();
            }
        }
        )));
        return pipelines;
    }

    private synchronized void put(SDFReader reader) throws IOException {
        sdfReaderArray.add(reader);
        reader.close();
    }

    public SDFReader getAnnotatedSDFReaderByIndex(int index) {
        if (annotatedSDFArray.size() >= index) {
            return annotatedSDFArray.get(index);
        }
        throw new UnsupportedOperationException("Out of the annotated sdf file array bound.");
    }

    public static void reset() {
        instance = null;
    }

    public static SDFManager getInstance() {
        return instance;
    }

    public SDFManager setEncodeMode(int encodeMode) {
        this.encodeMode = encodeMode;
        return this;
    }

    public SDFManager storeContig(boolean storeContig) {
        this.globalContig = storeContig;
        return this;
    }

    public SDFManager globalContig(boolean globalContig) {
        this.globalContig = globalContig;
        return this;
    }

    public Array<SDFReader> getSdfReaderArray() {
        return sdfReaderArray;
    }

    public synchronized void addAnnotatedSDFFile(File sdfFile, int fileID) {
        try {
            SDFReader sdfReader = new SDFReader(sdfFile).setFileID(fileID);
            sdfReader.close();
            this.annotatedSDFArray.add(sdfReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addSDFile(File sdfFile) {
        this.sdfFileArray.add(sdfFile);
    }

    public Array<SDFReader> getAnnotatedSDFArray() {
        annotatedSDFArray.sort(Comparator.comparingInt(SDFReader::getFileID));
        return annotatedSDFArray;
    }

    public SDFManager setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public Array<File> getVcfFileArray() {
        return vcfFileArray;
    }
}
