package edu.sysu.pmglab.sdfa.annotation.collector.sv;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.executor.Pipeline;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.toolkit.GlobalVCFContigConvertor;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-31 08:30
 * @description
 */
public class BriefSVAnnotationManager {
    int fileSize = 0;
    int fileIDCounter = 0;
    Array<SDFReader> readerArray = new Array<>();
    private static final BriefSVAnnotationManager instance = new BriefSVAnnotationManager();
    private final HashMap<Integer, Array<BriefSVAnnotationFeature>> fileIDMap = new HashMap<>();
    private final ConcurrentHashMap<Chromosome, Array<BriefSVAnnotationFeature>> briefSVContainer = new ConcurrentHashMap<>();

    private BriefSVAnnotationManager() {

    }

    public static BriefSVAnnotationManager getInstance() {
        return instance;
    }

    public BriefSVAnnotationManager initChromosomes() {
        for (Chromosome chromosome : GlobalVCFContigConvertor.support()) {
            briefSVContainer.put(chromosome, new Array<>());
        }
        return this;
    }

    public void initAnnotationFeature(Chromosome chromosome, int resourceSize) {
        Array<BriefSVAnnotationFeature> briefSVAnnotationFeatures = briefSVContainer.get(chromosome);
        if (briefSVAnnotationFeatures != null && !briefSVAnnotationFeatures.isEmpty()) {
            VolumeByteStream cache;
            for (BriefSVAnnotationFeature briefSVAnnotationFeature : briefSVAnnotationFeatures) {
                briefSVAnnotationFeature.initAnnotationFeature(resourceSize);
            }
        }
    }

    public void updateSafety(int fileID, IRecord record, int line, boolean loadType, int numOfResource) {
        int[] coordinate = record.get(0);
        BriefSVAnnotationFeature newSVPos = new BriefSVAnnotationFeature(
                fileID, coordinate[1], coordinate[2], record.get(1), line
        );
        newSVPos.initAnnotationFeature(numOfResource);
        Chromosome chr = GlobalVCFContigConvertor.convertByRawIndex(fileID, coordinate[0]);
        Array<BriefSVAnnotationFeature> briefSVAnnotationFeatures;
        synchronized (briefSVContainer) {
            briefSVAnnotationFeatures = briefSVContainer.get(chr);
            if (briefSVAnnotationFeatures == null) {
                briefSVAnnotationFeatures = new Array<>(true);
                briefSVContainer.put(chr, briefSVAnnotationFeatures);
            }
            briefSVAnnotationFeatures.add(newSVPos);
        }
        if (loadType) {
            newSVPos.setSvTypeSign(record.get(2));
        }
    }

    public ConcurrentHashMap<Chromosome, Array<BriefSVAnnotationFeature>> getBriefSVContainer() {
        return briefSVContainer;
    }

    public void load(Array<SDFReader> readerArray, int numOfResource) throws IOException {
        for (SDFReader sdfReader : readerArray) {
            sdfReader.redirectSVFeature();
            int lineCount = 0;
            sdfReader.setFileID(fileIDCounter++);
            int fileID = sdfReader.getFileID();
            IRecord record;
            while ((record = sdfReader.readRecord()) != null) {
                updateSafety(
                        fileID, record,
                        lineCount++, false, numOfResource
                );
            }
        }
        this.readerArray.addAll(readerArray);
    }

    public Array<Pipeline> loadTasks(int thread, Array<SDFReader> readerArray, int numOfResource) throws IOException {
        Array<Pipeline> pipelines = new Array<>(thread);
        for (SDFReader sdfReader : readerArray) {
            sdfReader.redirectSVFeature();
            sdfReader.setFileID(fileIDCounter++);
            pipelines.add(new Pipeline(
                    ((status, context) -> {
                        int lineCount = 0;
                        while (sdfReader.readRecord() != null) {
                            updateSafety(
                                    sdfReader.getFileID(), sdfReader.readRecord(),
                                    lineCount++, false, numOfResource
                            );
                        }
                    })
            ));
        }
        return pipelines;
    }

    public void toWriteMode() {
        for (SDFReader sdfReader : readerArray) {
            fileIDMap.put(sdfReader.getFileID(), new Array<>());
        }
        for (Array<BriefSVAnnotationFeature> SVList : briefSVContainer.values()) {
            while (!SVList.isEmpty()) {
                BriefSVAnnotationFeature sv = SVList.popFirst();
                int fileID = sv.getFileID();
                Array<BriefSVAnnotationFeature> briefSVAnnotationFeatures = fileIDMap.get(fileID);
                if (briefSVAnnotationFeatures == null) {
                    briefSVAnnotationFeatures = new Array<>();
                    fileIDMap.put(fileID, briefSVAnnotationFeatures);
                }
                briefSVAnnotationFeatures.add(sv);
            }
        }
        for (Integer fileID : fileIDMap.keySet()) {
            fileIDMap.get(fileID).sort(BriefSVAnnotationFeature::compareToLine);
        }
    }

    public void writeOut(File outputDir) throws IOException {
        while (!readerArray.isEmpty()) {
            SDFReader sdfReader = readerArray.popFirst();
            writeOne(outputDir, sdfReader);
        }
    }

    public Array<Pipeline> writeOutTasks(File outputDir) {
        Array<Pipeline> pipelines = new Array<>();
        while (!readerArray.isEmpty()) {
            SDFReader sdfReader = readerArray.popFirst();
            pipelines.add(new Pipeline((
                    (status, context) -> {
                        writeOne(outputDir, sdfReader);
                    }
            )));
        }
        return pipelines;
    }

    private void writeOne(File outputDir, SDFReader sdfReader) throws IOException {
        CCFReader ccfReader = sdfReader.getReader();
        File outputPath = outputDir.getSubFile(sdfReader.getFilePath().changeExtension("sdfa", SDFFormat.EXTENSION).getName());
        Array<BriefSVAnnotationFeature> briefSVAnnotationFeatures = fileIDMap.get(sdfReader.getFileID());
        ccfReader = new CCFReader(ccfReader.getFilePath());
        CCFWriter writer = CCFWriter.Builder.of(outputPath)
                .addFields(ccfReader.getAllFields())
                .addFields(GlobalResourceManager.getInstance().resourceCCFField())
                .build();
        IRecord inputRecord = ccfReader.getRecord();
        IRecord outputRecord = writer.getRecord();
        int inputRecordSize = inputRecord.size();
        int outputRecordSize = outputRecord.size();
        while (ccfReader.read(inputRecord)) {
            BriefSVAnnotationFeature briefSVAnnotationFeature = briefSVAnnotationFeatures.popFirst();
            for (int i = 0; i < inputRecordSize; i++) {
                outputRecord.set(i, inputRecord.get(i));
            }
            ByteCodeArray encodeAnnotationArray = briefSVAnnotationFeature.encodeAnnotationArray;
            if (encodeAnnotationArray == null) {
                // null annotation
                for (int i = inputRecordSize; i < outputRecordSize; i++) {
                    outputRecord.set(i, ByteCode.EMPTY);
                }
            } else {
                for (int i = inputRecordSize; i < outputRecordSize; i++) {
                    if (encodeAnnotationArray.get(i - inputRecordSize) == null) {
                        outputRecord.set(i, ByteCode.EMPTY);
                        continue;
                    }
                    outputRecord.set(i, encodeAnnotationArray.get(i - inputRecordSize));
                }
            }
            writer.write(outputRecord);
        }
        writer.writeMeta(sdfReader.getMeta().write());
        inputRecord.clear();
        outputRecord.clear();
        ccfReader.close();
        sdfReader.close();
        writer.close();
        if (SDFManager.getInstance() != null) {
            SDFManager.getInstance().addAnnotatedSDFFile(writer.getOutputFile(), sdfReader.getFileID());
        }
    }

    public BriefSVAnnotationManager setFileSize(int fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public BriefSVAnnotationManager setReaderArray(Array<SDFReader> sdfReaderArray) {
        this.readerArray = sdfReaderArray;
        return this;
    }

    public void clear() {
        fileSize = 0;
        fileIDMap.clear();
        fileIDCounter = 0;
        readerArray.clear();
        briefSVContainer.clear();
    }
}
