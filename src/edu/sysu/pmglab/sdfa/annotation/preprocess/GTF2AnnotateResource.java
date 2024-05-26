package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.ccf.*;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.toolkit.Contig;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-02 07:30
 * @description
 */
public class GTF2AnnotateResource {
    String resource;
    String version;
    final File gtfFile;
    final File outputFile;
    Contig contig = new Contig();
    CallableSet<ByteCode> strandMap = new CallableSet<>();
    CallableSet<ByteCode> gtfResourceMap = new CallableSet<>();
    static CallableSet<ByteCode> geneFeatureSet = new CallableSet<>();
    ReusableMap<ByteCode, ByteCode> attributeSet = new ReusableMap<>();
    private static CallableSet<ByteCode> featureMap = new CallableSet<>();
    public static CCFFieldGroupMetas GTFField = new CCFFieldGroupMetas()
            .addField(CCFFieldMeta.of("Coordinate::coordinate", FieldType.int32Array))
            .addField(CCFFieldMeta.of("Feature::type", FieldType.varInt32))
            .addField(CCFFieldMeta.of("Feature::strand", FieldType.varInt32))
            .addField(CCFFieldMeta.of("Feature::phase", FieldType.varInt32))
            .addField(CCFFieldMeta.of("Feature::score", FieldType.float32))
            .addField(CCFFieldMeta.of("Feature::source", FieldType.int32Array))
            .addField(CCFFieldMeta.of("Feature::attributes", FieldType.bytecodeArray));

    static {
        featureMap.add(new ByteCode(new byte[]{ByteCode.g, ByteCode.e, ByteCode.n, ByteCode.e}));
        featureMap.add(new ByteCode("transcript"));
        featureMap.add(new ByteCode("exon"));
        featureMap.add(new ByteCode("UTR"));
        featureMap.add(new ByteCode("CDS"));
        featureMap.add(new ByteCode("start_codon"));
        featureMap.add(new ByteCode("stop_codon"));
    }

    public GTF2AnnotateResource(Object gtfPath, Object output) {
        this.gtfFile = new File(gtfPath.toString());
        this.outputFile = new File(output.toString());
    }

    public File convert() throws IOException {
        FileStream fs = new FileStream(gtfFile);
        VolumeByteStream cache = new VolumeByteStream();
        CCFWriter writer = CCFWriter.Builder.of(outputFile)
                .addFields(GTFField)
                .build();
        IRecord record = writer.getRecord();
        DefaultValueWrapper<Float> scoreDefault = new DefaultValueWrapper<>(-1f);
        DefaultValueWrapper<Integer> phaseDefault = new DefaultValueWrapper<>(-1);
        int[] coordinate = new int[3];
        ByteCodeArray attributeArray = new ByteCodeArray();
        while (fs.readLine(cache) != -1) {
            if (cache.startWith(new byte[]{ByteCode.NUMBER_SIGN})) {
                cache.reset();
                continue;
            }
            BaseArray<ByteCode> lineSpilt = cache.toByteCode().split(ByteCode.TAB);
            ByteCode chrName = lineSpilt.get(0);
            Chromosome chromosome = contig.get(chrName);
            coordinate[0] = chromosome.getIndex();
            int resourceIndex = gtfResourceMap.indexOfValue(lineSpilt.get(1));
            if (resourceIndex == -1) {
                gtfResourceMap.add(lineSpilt.get(1).asUnmodifiable());
            }
            int feature = featureMap.indexOfValue(lineSpilt.get(2));
            if (feature == -1){
                ByteCode featureByteCode = lineSpilt.get(2).trim();
                featureMap.add(featureByteCode.asUnmodifiable());
                feature = featureMap.indexOfValue(featureByteCode);
            }
            coordinate[1] = lineSpilt.get(3).toInt();
            coordinate[2] = lineSpilt.get(4).toInt();
            float score = scoreDefault.getByDefault(lineSpilt.get(5), ByteCode::toFloat);
            int strand = strandMap.indexOfValue(lineSpilt.get(6));
            if (strand == -1) {
                strandMap.add(lineSpilt.get(6).asUnmodifiable());
            }
            int phase = phaseDefault.getByDefault(lineSpilt.get(7), ByteCode::toInt);
            BaseArray<ByteCode> splitAttrs = lineSpilt.get(8).split(ByteCode.SEMICOLON);
            for (ByteCode kv : splitAttrs) {
                BaseArray<ByteCode> kvEntry = kv.trim().split(" ");
                ByteCode k = kvEntry.get(0).asUnmodifiable();
                ByteCode v;
                if (kvEntry.size() < 2)
                    v = ByteCode.EMPTY;
                else
                    v = DefaultValueWrapper.emptyBytecodeWrapper.getValue(kvEntry.get(1));
                attributeSet.put(k, v);
            }
            record.set(0, coordinate);
            record.set(1, feature);
            record.set(2, strand);
            record.set(3, phase);
            record.set(4, score);
            record.set(5, resourceIndex);
            for (ByteCode value : attributeSet.values()) {
                attributeArray.add(DefaultValueWrapper.emptyBytecodeWrapper.getValue(value));
            }
            record.set(6, attributeArray);
            writer.write(record);
            attributeSet.clear();
            attributeArray.clear();
            cache.reset();
        }
        fs.close();
        new CCFMeta();
        writer.close();
        return outputFile;
    }

    public GTF2AnnotateResource setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public GTF2AnnotateResource setVersion(String version) {
        this.version = version;
        return this;
    }

    public static void main(String[] args) throws IOException {
        new GTF2AnnotateResource("/Users/wenjiepeng/Downloads/gencode.v29.annotation.gtf",
                "/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/gtf/gtf.ccf")
                .convert();

    }

}
