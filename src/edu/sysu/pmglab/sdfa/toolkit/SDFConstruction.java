package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFWriter;
import edu.sysu.pmglab.sdfa.sv.CSVLocation;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Wenjie Peng
 * @create 2025-04-24 10:51
 * @description
 */
public abstract class SDFConstruction {
    String inputFile;
    String outputFile;

    public void submit() throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        FileStream reader = new FileStream(inputFile, FileStream.DEFAULT_READER);
        Array<UnifiedSV> svs = new Array<>();
        ContigBlockContainer contigBlock = new ContigBlockContainer();
        while (true) {
            int tmp = reader.readLine(cache);
            if (tmp == -1) {
                break;
            }
            UnifiedSV sv = new UnifiedSV();
            LineConversionStatus lineConversionStatus = lineConversion(cache.toByteCode(), sv);
            cache.reset();
            if (lineConversionStatus == LineConversionStatus.ACCEPT_RECORD) {
                svs.add(sv);
            } else if (lineConversionStatus == LineConversionStatus.STOP) {
                break;
            }
        }
        reader.close();
        int lineCount = 0;
        svs.sort(UnifiedSV::compareTo);
        int[] contigSize = new int[contigBlock.getAllChromosomes().size()];
        SDFWriter writer = SDFWriter.of(outputFile, 0);
        while (!svs.isEmpty()) {
            UnifiedSV unifiedSV = svs.popFirst();
            int index = unifiedSV.getChr().getIndex();
            contigSize[unifiedSV.getChr().getIndex()]++;
            unifiedSV.setCSVLocation(new CSVLocation(lineCount++))
                    .setSpecificInfoField(
                            unifiedSV.getSpecificInfoField() == null || unifiedSV.getSpecificInfoField().get(0)== null?new Array<>():
                            (ByteCodeArray) ArrayType.decode(unifiedSV.getSpecificInfoField().get(0))
                    );
            writer.write(unifiedSV);
        }
        SDFMeta meta = new SDFMeta().setContigBlockContainer(contigBlock).initChrBlockRange(contigSize);
        writer.writeMeta(meta.write());
        writer.close();
    }


    abstract public LineConversionStatus lineConversion(ByteCode line, UnifiedSV sv);

    public enum LineConversionStatus {
        ACCEPT_RECORD,
        SKIP_STATUS,
        STOP;
    }

    public SDFConstruction setInputFile(String inputFile) {
        this.inputFile = inputFile;
        return this;
    }

    public SDFConstruction setOutputFile(String outputFile) {
        this.outputFile = outputFile;
        return this;
    }
}
