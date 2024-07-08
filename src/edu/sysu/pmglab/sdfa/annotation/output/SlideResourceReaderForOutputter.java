package edu.sysu.pmglab.sdfa.annotation.output;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.genome.GeneAnnotFeature;
import edu.sysu.pmglab.sdfa.annotation.preprocess.AnnotationResourceMeta;
import edu.sysu.pmglab.sdfa.annotation.toolkit.OutputFrame;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Wenjie Peng
 * @create 2024-04-13 23:10
 * @description
 */
public class SlideResourceReaderForOutputter {
    int currLine;
    int endLine;
    Chromosome chr;
    int outputColSize;
    boolean init = false;
    CCFReader fileReader;
    IRecord templateRecord;
    OutputFrame outputFrame;
    AnnotationResourceMeta fileMeta;
    protected ByteCode nullAnnotationRes;
    protected Function<ByteCode, Object> indexParser;
    Array<IRecord> slideRecordQueue = new Array<>(true);
    Array<IRecord> relatedRecordQueue = new Array<>(true);
    private final VolumeByteStream cache = new VolumeByteStream(4 * 1024);

    public SlideResourceReaderForOutputter(AbstractResourceManager resourceManager, OutputFrame outputFrame) {
        currLine = -1;
        endLine = -1;
        this.outputFrame = outputFrame;
        this.outputFrame.check();
        outputColSize = outputFrame.getOutputColNameArray().size();
        try {
            initNullAnnotationResult();
            fileReader = new CCFReader(resourceManager.getResourcePath());
            fileReader.seek(0);
            fileReader.close();
            templateRecord = fileReader.getRecord();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!resourceManager.getResourceType().equals("genome")) {
            indexParser = FieldType.int32Array::decode;
        } else {
            indexParser = GeneAnnotFeature::decode;
        }
        fileMeta = AnnotationResourceMeta.decode(fileReader.getMeta());
    }

    public void initNullAnnotationResult() {
        VolumeByteStream cache = new VolumeByteStream();
        for (int i = 0; i < outputColSize; i++) {
            cache.writeSafety(ByteCode.PERIOD);
            if (i != outputColSize - 1) {
                cache.writeSafety(ByteCode.TAB);
            }
        }
        this.nullAnnotationRes = cache.toByteCode().asUnmodifiable();
        cache.close();
    }

    public ByteCode output(Chromosome chromosome, ByteCode featureSrc) {
        if (!init) {
            chr = chromosome;
            init = true;
        }
        boolean flag = chromosome.equals(chr);
        if (!flag) {
            return nullAnnotationRes;
        }
        return output(featureSrc);
    }

    public ByteCode output(ByteCode featureSrc) {
        cache.reset();
        int[] relatedIndexArray = (int[]) indexParser.apply(featureSrc);
        int size = relatedIndexArray.length;
        int firstLine = relatedIndexArray[0];
        int lastIndex = relatedIndexArray[size - 1];
        try {
            alignQueue(firstLine, lastIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int lineIndex : relatedIndexArray) {
            relatedRecordQueue.add(slideRecordQueue.get(lineIndex - firstLine));
        }
        for (int i = 0; i < outputColSize; i++) {
            cache.writeSafety(outputFrame.outputCol(relatedRecordQueue, i));
            if (i != outputColSize - 1) {
                cache.writeSafety(ByteCode.TAB);
            }
        }
        relatedRecordQueue.clear();
        return cache.toByteCode();
    }


    public void alignQueue(int first, int end) throws IOException {
        fileReader = fileReader.newInstance();
        if (endLine < first) {
            // has no overlap
            // 1. reach feature start line
            for (int i = 0; i < first - endLine - 1; i++) {
                fileReader.read();
            }
            slideRecordQueue.clear();
            currLine = first;
            endLine = first - 1;
            int size = end - first + 1;
            // 2. get same size as annotation size
            for (int i = 0; i < size; i++) {
                fileReader.read(templateRecord);
                slideRecordQueue.add(templateRecord.clone());
                endLine++;
            }
        } else {
            // 1. make sure that currLine is equal to feature start line
            int tmp = currLine;
            for (int i = 0; i < first - tmp; i++) {
                fileReader.read(templateRecord);
                slideRecordQueue.popFirst();
                slideRecordQueue.add(templateRecord.clone());
                currLine++;
                endLine++;
            }
            // 2. make sure that end line is equal or larger than feature
            tmp = endLine;
            for (int i = 0; i < end - tmp; i++) {
                fileReader.read(templateRecord);
                slideRecordQueue.add(templateRecord.clone());
                endLine++;
            }
        }
        fileReader.close();
    }

    public void reset() {
        currLine = -1;
        endLine = -1;
        cache.reset();
        chr = Chromosome.get(0);
        slideRecordQueue.clear();
        try {
            fileReader = new CCFReader(fileReader.getFilePath());
            fileReader.seek(0);
            fileReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        relatedRecordQueue.clear();
    }

    public ByteCode getNullAnnotationRes() {
        return nullAnnotationRes;
    }
}
