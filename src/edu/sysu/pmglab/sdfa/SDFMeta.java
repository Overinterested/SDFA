package edu.sysu.pmglab.sdfa;

import edu.sysu.pmglab.ccf.CCFMeta;
import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.easytools.container.GlobalTemporaryContainer;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.idividual.Subject;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFileLatest;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFHeader;
import edu.sysu.pmglab.sdfa.toolkit.Contig;

import java.util.Collection;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 04:21
 * @description
 */
public class SDFMeta extends CCFMeta {
    int fileID;
    CCFMeta meta;
    int encodeMode;
    VCFHeader header;
    Subjects subjects;
    CallableSet<ByteCode> infoFieldSet = new CallableSet<>();
    CallableSet<ByteCode> svPropertySet = new CallableSet<>();
    CallableSet<ByteCode> formatFieldSet = new CallableSet<>();
    ReusableMap<ByteCode, ByteCode> metaProperties = new ReusableMap<>();
    ContigBlockContainer contigBlockContainer = new ContigBlockContainer();
    HashMap<Chromosome, Interval<Integer>> chrBlockRange = new HashMap<>();
    static final ByteCode CONTIG_BLOCK = new ByteCode(new byte[]{ByteCode.c, ByteCode.o, ByteCode.n, ByteCode.t, ByteCode.i, ByteCode.g,
            ByteCode.B, ByteCode.l, ByteCode.o, ByteCode.c, ByteCode.k});
    static final ByteCode ENCODE_MODE_TAG = new ByteCode("encode_mode").asUnmodifiable();
    static final ByteCode SUBJECT_TAG = new ByteCode(new byte[]{ByteCode.S, ByteCode.u, ByteCode.b, ByteCode.j, ByteCode.e, ByteCode.c, ByteCode.t});
    static final ByteCode KV_TAG = new ByteCode(new byte[]{ByteCode.K, ByteCode.V});
    static final ByteCode CONTIG_TAG = new ByteCode(new byte[]{ByteCode.c, ByteCode.o, ByteCode.n, ByteCode.t, ByteCode.i, ByteCode.g});
    static final ByteCode ALT_TAG = new ByteCode(new byte[]{ByteCode.A, ByteCode.L, ByteCode.T});
    static final ByteCode FILTER_TAG = new ByteCode(new byte[]{ByteCode.F, ByteCode.I, ByteCode.L, ByteCode.T, ByteCode.E, ByteCode.R});
    static final ByteCode INFO_TAG = new ByteCode(new byte[]{ByteCode.I, ByteCode.N, ByteCode.F, ByteCode.O});
    static final ByteCode FORMAT_TAG = new ByteCode(new byte[]{ByteCode.F, ByteCode.O, ByteCode.R, ByteCode.M, ByteCode.A, ByteCode.T});
    static final ByteCode INFO_FIELD_TAG = new ByteCode(new byte[]{ByteCode.I, ByteCode.n, ByteCode.f, ByteCode.o});
    static final ByteCode FORMAT_FIELD_TAG = new ByteCode(new byte[]{ByteCode.F, ByteCode.o, ByteCode.r, ByteCode.m, ByteCode.a, ByteCode.t});
    static final ByteCode SV_PROPERTY_TAG = new ByteCode(new byte[]{ByteCode.S, ByteCode.V, ByteCode.UNDERLINE, ByteCode.P, ByteCode.r, ByteCode.o,
            ByteCode.p, ByteCode.e, ByteCode.r, ByteCode.t, ByteCode.i, ByteCode.e, ByteCode.s});
    static final ByteCode META_PROPERTY_TAG = new ByteCode(new byte[]{ByteCode.M, ByteCode.e, ByteCode.t, ByteCode.a, ByteCode.UNDERLINE,
            ByteCode.P, ByteCode.r, ByteCode.o, ByteCode.p, ByteCode.e, ByteCode.r, ByteCode.t, ByteCode.i,
            ByteCode.e, ByteCode.s});
    static final ByteCode SDF_ENCODE_CONTIG_TAG = new ByteCode(new byte[]{
            ByteCode.S, ByteCode.D, ByteCode.F, ByteCode.UNDERLINE, ByteCode.C, ByteCode.o, ByteCode.n, ByteCode.t, ByteCode.i, ByteCode.g
    });

    public SDFMeta() {
        encodeMode = 0;
    }

    public SDFMeta write() {
        VolumeByteStream cache = new VolumeByteStream();
        add(ENCODE_MODE_TAG, new ByteCode(String.valueOf(encodeMode)));
        //region parse info and format field
        if (header != null) {
            getInfoFieldSet();
            getFormatFieldSet();
        }
        //endregion
        //region write header: KV, contig, alt, filter, info, format
        if (header != null) {
            // write kv value
            for (Entry<ByteCode, ByteCode> kvEntry : header.getKVConfig().KVEntries()) {
                ByteCode value = DefaultValueWrapper.emptyBytecodeWrapper.getValue(kvEntry.getValue());
                add(KV_TAG.toString(), kvEntry.getKey() + "=" + value);
            }
            // write contig
            for (ByteCode contigLine : header.getContig().getToStringArray()) {
                add(CONTIG_TAG, contigLine);
            }
            // write alt
            for (ByteCode altLine : header.getAltConfig().getToStringArray()) {
                add(ALT_TAG, altLine);
            }
            // write filter
            for (ByteCode filterLine : header.getFilterConfig().getToStringArray()) {
                add(FILTER_TAG, filterLine);
            }
            // write info
            for (ByteCode infoLine : header.getInfoConfig().getToStringArray()) {
                add(INFO_TAG, infoLine);
            }
            // write format
            for (ByteCode formatLine : header.getFormatConfig().getToStringArray()) {
                add(FORMAT_TAG, formatLine);
            }
        }
        //endregion
        //region write subject
        if (subjects != null) {
            if (!subjects.getSubjectSet().isEmpty()) {
                CallableSet<Subject> subjectSet = subjects.getSubjectSet();
                for (int i = 0; i < subjectSet.size(); i++) {
                    cache.writeSafety(subjectSet.getByIndex(i).getName());
                    if (i != subjectSet.size() - 1) {
                        cache.write(ByteCode.COMMA);
                    }
                }
                add(SUBJECT_TAG, cache.toByteCode().asUnmodifiable());
                cache.reset();
            }
        } else {
            add(SUBJECT_TAG, new ByteCode(new byte[]{ByteCode.N, ByteCode.U, ByteCode.L, ByteCode.L}));
        }
        //endregion
        //region write Info fieldName
        if (!infoFieldSet.isEmpty()) {
            for (int i = 0; i < infoFieldSet.size(); i++) {
                cache.writeSafety(infoFieldSet.getByIndex(i));
                if (i != infoFieldSet.size() - 1) {
                    cache.writeSafety(ByteCode.COMMA);
                }
            }
            add(INFO_FIELD_TAG, cache.toByteCode().asUnmodifiable());
            cache.reset();
        }
        //endregion
        //region write format fieldName
        if (!formatFieldSet.isEmpty()) {
            for (int i = 0; i < formatFieldSet.size(); i++) {
                cache.writeSafety(formatFieldSet.getByIndex(i));
                if (i != formatFieldSet.size() - 1) {
                    cache.writeSafety(ByteCode.COMMA);
                }
            }
            add(FORMAT_FIELD_TAG, cache.toByteCode().asUnmodifiable());
            cache.reset();
        }
        //endregion
        //region write sv properties
        if (!svPropertySet.isEmpty()) {
            for (int i = 0; i < svPropertySet.size(); i++) {
                cache.writeSafety(svPropertySet.getByIndex(i));
                if (i != svPropertySet.size() - 1) {
                    cache.writeSafety(ByteCode.COMMA);
                }
            }
            add(SV_PROPERTY_TAG, cache.toByteCode().asUnmodifiable());
            cache.reset();
        }
        //endregion
        //region write meta properties
        if (!metaProperties.isEmpty()) {
            for (int i = 0; i < metaProperties.size(); i++) {
                cache.writeSafety(metaProperties.getKeyByIndex(i));
                cache.writeSafety(ByteCode.EQUAL);
                cache.writeSafety(metaProperties.getByIndex(i));
                if (i != metaProperties.size() - 1) {
                    cache.writeSafety(ByteCode.COMMA);
                }
            }
            add(META_PROPERTY_TAG, cache.toByteCode().asUnmodifiable());
            cache.reset();
        }
        //endregion
        //region write chr block interval
        add(CONTIG_BLOCK, contigBlockContainer.encode());
        //endregion
        cache.close();
        return this;
    }

    public static SDFMeta decode(SDFReader reader) {
        return decode(reader.getReader());
    }

    public static SDFMeta decode(CCFReader reader) {
        File file = reader.getFilePath().getFileObject();
        Object o = GlobalTemporaryContainer.get(file);
        if (o instanceof SDFMeta) {
            return (SDFMeta) o;
        }
        SDFMeta res = new SDFMeta();
        CCFMeta meta = reader.getMeta();
        res.meta = meta;
        res.encodeMode = meta.getValues(ENCODE_MODE_TAG).get(0).toInt();

//        res.contig = Contig.decode(meta.getValues(SDF_ENCODE_CONTIG_TAG).get(0));
//        res.contig.getAllChromosomes();
        //region parse subjects
        BaseArray<ByteCode> subjectArray = splitWithComma(meta, SUBJECT_TAG);
        if (res.subjects == null) {
            res.subjects = new Subjects(reader.getFilePath().getFileObject());
        }
        if (subjectArray != null) {
            res.subjects.addAll(subjectArray.apply(Subject::new));
        }
        //endregion
        //region parse info field
        BaseArray<ByteCode> infoFieldArray = splitWithComma(meta, INFO_FIELD_TAG);
        if (infoFieldArray != null) {
            res.infoFieldSet.addAll(infoFieldArray);
        }
        //endregion
        //region parse format field
        BaseArray<ByteCode> formatFieldArray = splitWithComma(meta, FORMAT_FIELD_TAG);
        if (formatFieldArray != null) {
            res.formatFieldSet.addAll(formatFieldArray);
        }
        //endregion
        //region parse properties of sv
        BaseArray<ByteCode> svProperties = splitWithComma(meta, SV_PROPERTY_TAG);
        if (svProperties != null) {
            res.svPropertySet.addAll(svProperties);
        }
        //endregion
        //region parse chr block range
        BaseArray<ByteCode> encodeContigBlock = meta.getValues(CONTIG_BLOCK);
        if (encodeContigBlock != null) {
            res.contigBlockContainer = ContigBlockContainer.decode(encodeContigBlock.get(0).asUnmodifiable());
        }
        //endregion
        return res;
    }

    public static SDFMeta decode(VCFFileLatest vcfFile) {
        SDFMeta meta = new SDFMeta();
        meta.setFileID(vcfFile.getFileID())
                .setSubjects(vcfFile.getSubjects())
                .setHeader(vcfFile.getHeader());
        meta.contigBlockContainer.setContig(vcfFile.getContig());
        meta.infoFieldSet = new CallableSet<>(vcfFile.getInfoFieldNameArray());
        meta.getFormatFieldSet();
        return meta;
    }

    public SDFMeta setEncodeMode(int encodeMode) {
        this.encodeMode = encodeMode;
        return this;
    }

    public SDFMeta setSVPropertySet(CallableSet<ByteCode> svPropertySet) {
        this.svPropertySet = svPropertySet;
        return this;
    }

    public SDFMeta setHeader(VCFHeader header) {
        this.header = header;
        return this;
    }

    public SDFMeta setSubjects(Subjects subjects) {
        this.subjects = subjects;
        return this;
    }

    public Interval<Integer> getChrBlockRange(Chromosome chromosome) {
//        return chrBlockRange.get(chromosome);
        return contigBlockContainer.getChromosomeRange(chromosome);
    }

    public SDFMeta addProperties(ByteCode key, ByteCode value) {
        metaProperties.put(key.asUnmodifiable(), value.asUnmodifiable());
        return this;
    }

    public SDFMeta addSVProperty(ByteCode propertyID) {
        svPropertySet.add(propertyID.asUnmodifiable());
        return this;
    }

    public SDFMeta addSVProperties(Collection<ByteCode> propertyIDs) {
        propertyIDs.forEach(this::addSVProperty);
        return this;
    }

    public SDFMeta addInfoFieldID(ByteCode id) {
        infoFieldSet.add(id.asUnmodifiable());
        return this;
    }

    public SDFMeta addInfoFieldIDs(Collection<ByteCode> infoFieldIDs) {
        infoFieldIDs.forEach(this::addInfoFieldID);
        return this;
    }

    public SDFMeta addFormatFieldID(ByteCode id) {
        formatFieldSet.add(id.asUnmodifiable());
        return this;
    }

    public SDFMeta initChrBlockRange(int[] chrBlockSize) {
        CallableSet<Chromosome> allChromosomes = contigBlockContainer.getAllChromosomes();
        if (allChromosomes.size() >= chrBlockSize.length) {
            int updateIndex = 0;
            int startLine = 0;
            for (Chromosome chromosome : allChromosomes) {
                int endLine = 0;
                if (updateIndex < chrBlockSize.length) {
                    endLine = startLine + chrBlockSize[updateIndex++];
                }
                contigBlockContainer.putChromosomeRange(chromosome, startLine, endLine);
                startLine = endLine;
            }
            return this;
        }
        throw new UnsupportedOperationException("Chromosome block size is not equal to the array of each chromosome size.");
    }

    public SDFMeta setFileID(int fileID) {
        this.fileID = fileID;
        return this;
    }

    public CallableSet<ByteCode> getInfoFieldSet() {
        if (infoFieldSet.isEmpty() && header != null) {
            infoFieldSet = header.getInfoConfig().getIndexableIDSet();
        }
        return infoFieldSet;
    }

    public CallableSet<ByteCode> getFormatFieldSet() {
        if (formatFieldSet.isEmpty() && header != null) {
            formatFieldSet = header.getFormatConfig().getIndexableIDSet();
        }
        return formatFieldSet;
    }

    public CallableSet<ByteCode> getSVPropertySet() {
        return svPropertySet;
    }

    private static BaseArray<ByteCode> splitWithSeparate(CCFMeta meta, ByteCode tag, byte separator) {
        BaseArray<ByteCode> values = meta.getValues(tag);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0).split(separator);
    }

    private static BaseArray<ByteCode> splitWithComma(CCFMeta meta, ByteCode tag) {
        return splitWithSeparate(meta, tag, ByteCode.COMMA);
    }

    public VCFHeader getHeader() {
        if (header == null) {
            header = new VCFHeader();
            BaseArray<ByteCode> kvByteCodeArray = splitWithComma(meta, KV_TAG);
            if (kvByteCodeArray != null) {
                for (ByteCode kvByteCode : kvByteCodeArray) {
                    header.addKVConfig(kvByteCode);
                }
            }
            BaseArray<ByteCode> contigByteCodeArray = meta.getValues(CONTIG_TAG);
            if (contigByteCodeArray != null) {
                for (ByteCode contigByteCode : contigByteCodeArray) {
                    header.addContig(contigByteCode);
                }
            }
            BaseArray<ByteCode> altByteCodeArray = meta.getValues(ALT_TAG);
            if (altByteCodeArray != null) {
                for (ByteCode altByteCode : altByteCodeArray) {
                    header.addAltConfig(altByteCode);
                }

            }
            BaseArray<ByteCode> filterByteCodeArray = meta.getValues(FILTER_TAG);
            if (filterByteCodeArray != null) {
                for (ByteCode filterByteCode : filterByteCodeArray) {
                    header.addFilterConfig(filterByteCode);
                }
            }
            BaseArray<ByteCode> infoByteCodeArray = meta.getValues(INFO_TAG);
            if (infoByteCodeArray != null) {
                for (ByteCode infoByteCode : infoByteCodeArray) {
                    header.addInfoConfig(infoByteCode);
                }
            }
            BaseArray<ByteCode> formatByteCodeArray = meta.getValues(FORMAT_TAG);
            if (formatByteCodeArray != null) {
                for (ByteCode formatByteCode : formatByteCodeArray) {
                    header.addFormatConfig(formatByteCode);
                }
            }
        }
        return header;
    }

    public ReusableMap<ByteCode, ByteCode> getMetaProperties() {
        if (metaProperties == null) {
            metaProperties = new ReusableMap<>();
            BaseArray<ByteCode> metaPropertiesByteCode = splitWithComma(meta, META_PROPERTY_TAG);
            if (metaPropertiesByteCode != null) {
                for (ByteCode metaPropertyKV : metaPropertiesByteCode) {
                    BaseArray<ByteCode> kv = metaPropertyKV.split(ByteCode.EQUAL);
                    metaProperties.put(kv.get(0), kv.get(1));
                }
            }
        }
        return metaProperties;
    }

    public Subjects getSubjects() {
        return subjects;
    }

    public BaseArray<ByteCode> getConfigIDArray() {
        BaseArray<ByteCode> res = new ByteCodeArray();
        BaseArray<ByteCode> contigByteCodeArray = meta.getValues(CONTIG_TAG);
        if (contigByteCodeArray != null) {
            for (ByteCode contigByteCode : contigByteCodeArray) {
                BaseArray<ByteCode> split = contigByteCode.split(ByteCode.COMMA);
                ByteCode id = split.get(0).split(ByteCode.EQUAL).get(1).asUnmodifiable();
                res.add(id);
            }
        }
        return res;
    }

    public SDFMeta asUnmodified() {
        SDFMeta copy = new SDFMeta();
        copy.header = header.asUnmodified();
        copy.subjects = subjects.asUnmodified();
        copy.contigBlockContainer = contigBlockContainer.asUnmodified();
        return copy;
    }

    public void clear() {
        if (meta != null) {
            meta.clear();
        }
    }

    public SDFMeta setInfoFieldSet(CallableSet<ByteCode> infoFieldSet) {
        this.infoFieldSet = infoFieldSet;
        return this;
    }

    public SDFMeta setContigBlockContainer(ContigBlockContainer contigBlockContainer) {
        this.contigBlockContainer = contigBlockContainer;
        return this;
    }

    public Contig getContig() {
        return contigBlockContainer.getContig();
    }
}
