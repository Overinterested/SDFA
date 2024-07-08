package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.CCFMeta;
import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.IntervalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.RefSVResourceManager;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-30 06:46
 * @description
 */
public class Tsv2AnnotateResource {
    File outputDir;
    final File file;
    ByteCode version;
    ByteCode resource;
    boolean loadHeader = true;
    boolean isSVDatabase = false;
    boolean zero2oneBased = false;
    public static final String EXTENSION = ".ccf";
    public static final String Feature = "Feature::";
    ContigBlockContainer contigBlockContainer = new ContigBlockContainer();
    Array<AnnotationResourceFeature> refRecordArray = new Array<>(true);

    public Tsv2AnnotateResource(File file) {
        this.file = file;
    }

    public File convert() throws IOException {
        ByteCodeArray header = new ByteCodeArray();
        File outputPath = new DefaultValueWrapper<>(file.getParentFile())
                .getValue(outputDir)
                .getSubFile(file.getName())
                .changeExtension(EXTENSION, file.getExtension());
        try (FileStream fs = new FileStream(file)) {
            VolumeByteStream cache = new VolumeByteStream();
            //region parse header
            boolean columnName = false;
            while (fs.readLine(cache) != -1) {
                if (cache.startWith(new byte[]{ByteCode.NUMBER_SIGN, ByteCode.NUMBER_SIGN})) {
                    if (loadHeader) {
                        header.add(cache.toUnmodifiableByteCode());
                        cache.reset();
                    }
                    continue;
                }
                if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                    columnName = true;
                    break;
                }
                break;
            }
            //endregion
            if (!columnName) {
                throw new UnsupportedOperationException("The annotation file(" + file + ") has no column line(start with `#`).");
            }
            if (cache.size() == 0) {
                throw new UnsupportedOperationException("The annotation file(" + file + ") isn't valid for SDFA annotation.");
            }
            BaseArray<ByteCode> tags = cache.toByteCode().asUnmodifiable().split(ByteCode.TAB);
            cache.reset();
            HashMap<Chromosome, Integer> chrCount = new HashMap<>();
            while (fs.readLine(cache) != -1) {
                BaseArray<ByteCode> line = cache.toByteCode().asUnmodifiable().split(ByteCode.TAB);
                Chromosome chromosome = contigBlockContainer.getChromosomeByName(line.get(0));
                Integer exist = chrCount.get(chromosome);
                if (exist != null) {
                    chrCount.put(chromosome, exist + 1);
                } else {
                    chrCount.put(chromosome, 1);
                }
                int pos = 0;
                try {
                    pos = line.get(1).toInt();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                int end = line.get(2).toInt();
                if (zero2oneBased) {
                    pos--;
                }
                refRecordArray.add(new AnnotationResourceFeature(chromosome, pos, end, line));
                cache.reset();
            }
            cache.close();
            refRecordArray.sort(AnnotationResourceFeature::compareTo);
            CCFWriter.Builder builder;
            //region build writer
            if (isSVDatabase) {
                builder = CCFWriter.Builder.of(outputPath).addFields(RefSVResourceManager.FIX_INTERVAL_FIELD);
                for (int i = 5; i < tags.size(); i++) {
                    builder.addField(CCFFieldMeta.of(Feature + tags.get(i).toString(), FieldType.bytecode));
                }
            } else {
                builder = CCFWriter.Builder.of(outputPath)
                        .addFields(IntervalResourceManager.FIX_INTERVAL_FIELD);
                for (int i = 3; i < tags.size(); i++) {
                    builder.addField(CCFFieldMeta.of(Feature + tags.get(i).toString(), FieldType.bytecode));
                }
            }
            CCFWriter writer = builder.build();
            IRecord record = writer.getRecord();
            //endregion
            while (!refRecordArray.isEmpty()) {
                AnnotationResourceFeature annotationResourceFeature = refRecordArray.popFirst();
                if (isSVDatabase) {
                    annotationResourceFeature.toSVDatabaseResource(record);
                } else {
                    annotationResourceFeature.toIntervalResource(record);
                }
                writer.write(record);
                annotationResourceFeature.clear();
            }
            CCFMeta meta = new CCFMeta();
            writer.writeMeta(meta);
            int rangeStart = 0;
            int chromosomeCount = 0;
            CallableSet<Chromosome> allChromosomes = contigBlockContainer.getAllChromosomes();
            for (Chromosome chromosome : allChromosomes) {
                chromosomeCount = 0;
                if (chrCount.containsKey(chromosome)) {
                    chromosomeCount = chrCount.get(chromosome);
                }
                contigBlockContainer.putChromosomeRange(chromosome, rangeStart, rangeStart + chromosomeCount);
                rangeStart += chromosomeCount;
            }
            meta.add("contigBlock", contigBlockContainer.encode());
            meta.add("resource", DefaultValueWrapper.periodBytecodeWrapper.getValue(resource));
            meta.add("version", DefaultValueWrapper.periodBytecodeWrapper.getValue(version));
            if (zero2oneBased) {
                meta.add("posType", "1");
            } else {
                meta.add("posType", "0");
            }
            meta.add("file_name", file.getName());
            writer.writeMeta(meta).close();
        }
        return outputPath;
    }

    public Tsv2AnnotateResource loadHeader(boolean loadHeader) {
        this.loadHeader = loadHeader;
        return this;
    }

    public Tsv2AnnotateResource isSVDatabase(boolean isSVDatabase) {
        this.isSVDatabase = isSVDatabase;
        return this;
    }

    public Tsv2AnnotateResource setVersion(Object version) {
        this.version = new ByteCode(version.toString());
        return this;
    }

    public Tsv2AnnotateResource setResource(Object resource) {
        this.resource = new ByteCode(resource.toString());
        return this;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/wenjiepeng/Desktop/SV/data/annotation/SVAFotate/SVAFotate_core_SV_popAFs.GRCh37.bed");
        new Tsv2AnnotateResource(file)
                .setOutputDir("/Users/wenjiepeng/Desktop/SV/data/annotation/SVAFotate")
                .setResource("SVAFotate")
                .setVersion(1.0)
                .isSVDatabase(true)
                .loadHeader(true)
                .convert();
    }

    public Tsv2AnnotateResource setOutputDir(Object outputDir) {
        this.outputDir = new File(outputDir.toString());
        return this;
    }
}
