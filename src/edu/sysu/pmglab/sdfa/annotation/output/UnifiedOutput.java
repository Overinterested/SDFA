package edu.sysu.pmglab.sdfa.annotation.output;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.executor.Workflow;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.toolkit.OutputFrame;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.unifyIO.FileStream;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author Wenjie Peng
 * @create 2024-04-01 22:11
 * @description
 */
public class UnifiedOutput {
    int thread;
    Logger logger;
    ByteCode header;
    final File outputDir;
    private Workflow workflow;
    Array<SDFReader> outputQueue;
    Array<OutputFrame> outputFrameArray;
    static boolean unifiedOutput = false;
    Array<FileStream> outputFileStreamArray;
    Array<SlideResourceReaderForOutputter> slideResourceReaderForOutputterArray;

    public UnifiedOutput(File outputDir) {
        this.outputDir = outputDir;
    }

    public void execute(Workflow workflow) throws IOException {
        // init slide queue
        for (AbstractResourceManager resourceManager : GlobalResourceManager.getInstance().getResourceManagerSet()) {
            resourceManager.outputMode();
        }
        outputFrameArray = Array.wrap(GlobalResourceManager.getInstance()
                .getResourceManagerSet()
                .stream().map(AbstractResourceManager::getOutputFrame)
                .collect(Collectors.toList()));
        slideResourceReaderForOutputterArray = Array.wrap(GlobalResourceManager.getInstance()
                .getResourceManagerSet()
                .stream().map(AbstractResourceManager::getOutputter)
                .collect(Collectors.toList()));
        this.workflow = workflow;
        workflow.addTasks(((status, context) -> {
                Logger logger = context.cast(Logger.class);
                logger.info("Start outputting annotation results with tsv format.");
                if (unifiedOutput){
                    context.put(
                            ProcessBar.class,
                            new ProcessBar(outputQueue.size())
                                    .setHeader("Outputting speed")
                                    .setUnit("chromosomes")
                                    .start()
                    );
                }else {
                    context.put(
                            ProcessBar.class,
                            new ProcessBar(outputQueue.size())
                                    .setHeader("Outputting speed")
                                    .setUnit("files")
                                    .start()
                    );
                }
        })).execute();
        workflow.clearTasks();
        if (unifiedOutput) {
            executeUnifiedOutput();
        } else {
            // load thread SVs
            executeSingleOutput();
        }
        workflow.addTasks(((status, context) ->
            ((ProcessBar)context.get(ProcessBar.class)).setFinish()
        )).execute();
        workflow.clearTasks();
    }

    public void executeUnifiedOutput() throws IOException {
        int annotSize = outputFrameArray.size();
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(outputDir.getSubFile("annot.tsv"), FileStream.DEFAULT_WRITER);
        fs.write(getHeaderColNames());
        // load all SVs
        for (Chromosome chromosome : SDFGlobalContig.support()) {
            Array<SVRecordWithID> svRecordWithIDArray = new Array<>();
            //region load all SVs
            for (SDFReader sdfReader : outputQueue) {
                sdfReader.redirectSVFeaturesAndAnnotationFeature();
                boolean limitChr = sdfReader.limitChrBlock(chromosome);
                if (!limitChr) {
                    sdfReader.close();
                    continue;
                }
                int fileID = sdfReader.getFileID();
                CCFReader reader = sdfReader.getReader();
                IRecord record;
                while ((record = reader.read()) != null) {
                    svRecordWithIDArray.add(new SVRecordWithID(fileID, record));
                }
            }
            if (svRecordWithIDArray.isEmpty()) {
                continue;
            }
            //endregion
            cache.reset();
            boolean hasAnnotation;
            svRecordWithIDArray.sort(SVRecordWithID::compareTo);
            for (SVRecordWithID svRecordWithID : svRecordWithIDArray) {
                hasAnnotation = false;
                IRecord record = svRecordWithID.getRecord();
                int[] coordinate = record.get(0);
                //region output SV features: Chromosome, ID, FileID, Pos, End, Type
                cache.writeSafety(chromosome.getName());
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(svRecordWithID.fileID));
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate[1]));
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate[2]));
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(SVTypeSign.getByIndex(record.get(2)).getName());
                cache.writeSafety(ByteCode.TAB);
                //endregion
                for (int i = 0; i < annotSize; i++) {
                    ByteCode src = record.get(5 + i);
                    if (src.equals(ByteCode.EMPTY)) {
                        cache.writeSafety(slideResourceReaderForOutputterArray.get(i).getNullAnnotationRes());
                    } else {
                        hasAnnotation = true;
                        cache.writeSafety(slideResourceReaderForOutputterArray.get(i).output(chromosome, src));
                    }
                    if (i != annotSize - 1) {
                        cache.writeSafety(ByteCode.TAB);
                    }
                }
                if (hasAnnotation) {
                    cache.writeSafety(ByteCode.NEWLINE);
                    fs.write(cache);
                }
                cache.reset();
            }
            workflow.addTasks(((status, context) ->
                ((ProcessBar)context.get(ProcessBar.class)).addProcessed(1)
            ));
            workflow.execute();
            workflow.clearTasks();
        }
        cache.close();
        fs.close();
        // close
    }

    public void executeSingleOutput() throws IOException {
        int annotSize = outputFrameArray.size();
        VolumeByteStream cache = new VolumeByteStream();
        while (!outputQueue.isEmpty()) {
            //region load multi files
            Array<SDFReader> sdfReaderArray = new Array<>(thread);
            if (!outputQueue.isEmpty()) {
                if (outputQueue.size() > thread) {
                    sdfReaderArray.addAll(outputQueue.popFirst(thread));
                } else {
                    sdfReaderArray.addAll(outputQueue.popFirst(outputQueue.size()));
                }
            } else {
                break;
            }
            outputFileStreamArray = new Array<>(sdfReaderArray.size());
            int startFileID = sdfReaderArray.get(0).getFileID();
            //endregion
            //region reset slide queue
            for (SlideResourceReaderForOutputter slideResourceReaderForOutputter : slideResourceReaderForOutputterArray) {
                slideResourceReaderForOutputter.reset();
            }
            for (SDFReader sdfReader : sdfReaderArray) {
                FileStream fs = new FileStream(
                        outputDir.getSubFile(sdfReader.getFilePath().getName())
                                .changeExtension("annot.tsv", "sdfa"),
                        FileStream.DEFAULT_WRITER
                );
                fs.write(getHeaderColNames());
                outputFileStreamArray.add(fs);
            }
            //endregion
            // load thread SVs
            for (Chromosome chromosome : SDFGlobalContig.support()) {
                Array<SVRecordWithID> svRecordWithIDArray = new Array<>();
                //region load all SVs
                for (SDFReader sdfReader : sdfReaderArray) {
                    sdfReader.redirectSVFeaturesAndAnnotationFeature();
                    boolean limitChr = sdfReader.limitChrBlock(chromosome);
                    if (!limitChr) {
                        sdfReader.close();
                        continue;
                    }
                    int fileID = sdfReader.getFileID();
                    CCFReader reader = sdfReader.getReader();
                    IRecord record;
                    while ((record = reader.read()) != null) {
                        svRecordWithIDArray.add(new SVRecordWithID(fileID, record));
                    }
                }
                if (svRecordWithIDArray.isEmpty()) {
                    continue;
                }
                //endregion
                boolean hasAnnotation;
                svRecordWithIDArray.sort(SVRecordWithID::compareTo);
                for (SVRecordWithID svRecordWithID : svRecordWithIDArray) {
                    hasAnnotation = false;
                    IRecord record = svRecordWithID.getRecord();
                    int[] coordinate = record.get(0);
                    //region output SV features: Chromosome, ID, FileID, Pos, End, Type
                    cache.writeSafety(chromosome.getName());
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety(ValueUtils.Value2Text.int2bytes(svRecordWithID.fileID));
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate[1]));
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate[2]));
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety(SVTypeSign.getByIndex(record.get(2)).getName());
                    cache.writeSafety(ByteCode.TAB);
                    //endregion
                    for (int i = 0; i < annotSize; i++) {
                        ByteCode src = record.get(5 + i);
                        if (src.equals(ByteCode.EMPTY)) {
                            cache.writeSafety(slideResourceReaderForOutputterArray.get(i).getNullAnnotationRes());
                        } else {
                            hasAnnotation = true;
                            cache.writeSafety(slideResourceReaderForOutputterArray.get(i).output(chromosome, src));
                        }
                        if (i != annotSize - 1) {
                            cache.writeSafety(ByteCode.TAB);
                        }
                    }
                    if (hasAnnotation) {
                        cache.writeSafety(ByteCode.NEWLINE);
                        outputFileStreamArray.get(svRecordWithID.fileID - startFileID).write(cache);
                    }
                    cache.reset();
                }
            }
            // close
            cache.reset();
            while (!outputFileStreamArray.isEmpty()) {
                outputFileStreamArray.popFirst().close();
            }
            workflow.addTasks(((status, context) ->
                ((ProcessBar)context.get(ProcessBar.class)).addProcessed(sdfReaderArray.size())
            ));
            workflow.execute();
            workflow.clearTasks();
        }
    }

    public UnifiedOutput setOutputQueue(Array<SDFReader> outputQueue) {
        this.outputQueue = outputQueue;
        return this;
    }

    public UnifiedOutput setThread(int thread) {
        this.thread = thread;
        return this;
    }

    static class SVRecordWithID implements Comparable<SVRecordWithID> {
        int fileID;
        IRecord record;

        public SVRecordWithID(int fileID, IRecord record) {
            this.fileID = fileID;
            this.record = record;
        }

        public IRecord getRecord() {
            return record;
        }

        public int getFileID() {
            return fileID;
        }

        public boolean isComplex() {
            return ((int[]) record.get(4)).length != 1;
        }

        @Override
        public int compareTo(SVRecordWithID o) {
            int[] coordinate1 = record.get(0);
            int[] coordinate2 = o.record.get(0);
            int status = Integer.compare(coordinate1[1], coordinate2[1]);
            return status == 0 ? Integer.compare(coordinate1[2], coordinate2[2]) : status;
        }
    }

    private ByteCode getHeaderColNames() {
        if (header == null) {
            ByteCodeArray headCols = new ByteCodeArray();
            VolumeByteStream headerCache = new VolumeByteStream();
            headCols.add(new byte[]{ByteCode.NUMBER_SIGN, ByteCode.C, ByteCode.H, ByteCode.R});
            headCols.add(new byte[]{ByteCode.F, ByteCode.I, ByteCode.L, ByteCode.E, ByteCode.I, ByteCode.D});
            headCols.add(new byte[]{ByteCode.P, ByteCode.O, ByteCode.S});
            headCols.add(new byte[]{ByteCode.E, ByteCode.N, ByteCode.D});
            headCols.add(new byte[]{ByteCode.T, ByteCode.Y, ByteCode.P, ByteCode.E});
            for (OutputFrame outputFrame : outputFrameArray) {
                ByteCodeArray outputColNameArray = outputFrame.getOutputColNameArray();
                headCols.addAll(outputColNameArray);
            }
            for (int i = 0; i < headCols.size(); i++) {
                headerCache.writeSafety(headCols.get(i));
                if (i != headCols.size() - 1) {
                    headerCache.writeSafety(ByteCode.TAB);
                } else {
                    headerCache.writeSafety(ByteCode.NEWLINE);
                }
            }
            header = headerCache.toUnmodifiableByteCode();
            headerCache.close();
        }
        return header;
    }

    public UnifiedOutput setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
}
