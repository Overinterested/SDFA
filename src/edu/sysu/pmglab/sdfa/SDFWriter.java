package edu.sysu.pmglab.sdfa;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFile;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 07:12
 * @description
 */
public class SDFWriter {
    final File filePath;
    final int encodeMode;
    final CCFWriter writer;
    final SDFEncode encoder;
    final IRecord templateRecord;
    CallableSet<CCFFieldMeta> extraFieldSet;
    public static SDFFormat format = new SDFFormat(SDFFormat.DEFAULT_COMPRESSION_LEVEL, SDFFormat.DEFAULT_VARIANT_NUM);

    private SDFWriter(Object filePath, int encodeMode) throws IOException {
        this.filePath = new File(filePath.toString());
        CCFWriter.Builder builder = CCFWriter.Builder.of(this.filePath)
                .addFields(SDFFormat.SDFFields)
                .setFormat(format);
        this.writer = builder.build();
        this.encodeMode = encodeMode;
        this.encoder = new SDFEncode(encodeMode);
        this.templateRecord = this.writer.getRecord();
    }

    private SDFWriter(Object filePath, int encodeMode, Iterator<CCFFieldMeta> extraFieldSet) throws IOException {
        this.filePath = new File(filePath.toString());
        this.extraFieldSet = new CallableSet<>();
        while (extraFieldSet.hasNext()) {
            this.extraFieldSet.add(extraFieldSet.next());
        }
        this.encodeMode = encodeMode;
        this.encoder = new SDFEncode(encodeMode);
        CCFWriter.Builder builder = CCFWriter.Builder.of(this.filePath)
                .addFields(SDFFormat.SDFFields)
                .setFormat(format);
        if (!this.extraFieldSet.isEmpty()) {
            for (int i = 0; i < this.extraFieldSet.size(); i++) {
                builder.addField(this.extraFieldSet.getByIndex(i));
            }
        }
        this.writer = builder.build();
        this.templateRecord = this.writer.getRecord();
    }

    public static SDFWriter of(Object outputPath, int encodeMode) throws IOException {
        return new SDFWriter(outputPath, encodeMode);
    }

    public static SDFWriter of(Object outputPath, int encodeMode, Iterator<CCFFieldMeta> extraFieldSet) throws IOException {
        return new SDFWriter(outputPath, encodeMode, extraFieldSet);
    }

    public static File vcf2sdf(Object vcfFile, Object outputFile) throws IOException {
        File output = new File(outputFile.toString());
        if (vcfFile instanceof VCFFile) {
            return vcf2sdf((VCFFile) vcfFile, output);
        } else {
            File vcfFilePath = new File(vcfFile.toString());
            return vcf2sdf(vcfFilePath, output);
        }
    }

    private static File vcf2sdf(File inputFile, File outputFile) throws IOException {
        VCFFile vcfFile = new VCFFile(inputFile).parse();
        SDFWriter.vcf2sdf(vcfFile, outputFile);
        return outputFile;
    }

    public CCFWriter getWriter() {
        return writer;
    }

    public File getFilePath() {
        return filePath;
    }

    public static File extendSDF(SDFReader inputSDFReader, Array<CCFFieldMeta> extraField,
                                 Array<Object> elements, Function<Object, Array<Object>> extraFields,
                                 Object output) {
        return null;
    }

    public void write(UnifiedSV sv) throws IOException {
        writer.write(encoder.encode(templateRecord, sv));
    }

    public void write(IRecord record) throws IOException {
        writer.write(record);
    }

    public void writeMeta(SDFMeta meta) throws IOException {
        writer.writeMeta(meta.write());
    }

    public void close() throws IOException {
        templateRecord.clear();
        writer.close();
    }
}
