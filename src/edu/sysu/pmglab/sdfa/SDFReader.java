package edu.sysu.pmglab.sdfa;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.CCFTable;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.Interval;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.toolkit.Contig;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Wenjie Peng
 * @create 2024-03-24 07:12
 * @description
 */
public class SDFReader {
    int fileID;
    SDFMeta meta;
    CCFReader reader;
    final File filePath;
    final int decodeMode;
    final SDFDecode decoder;
    IntArray loadFieldIndexArray;
    private IRecord templateRecord;
    Array<CCFFieldMeta> loadFieldArray;
    private final AtomicBoolean ALL_FEATURE_MODE = new AtomicBoolean(false);
    private final AtomicBoolean COORDINATE_FIELDS_MODE = new AtomicBoolean(false);
    private final AtomicBoolean SV_FEATURE_ANNOTATION_MODE = new AtomicBoolean(false);
    private final AtomicBoolean COORDINATE_SVLEN_SVTYPE_FIELDS_MODE = new AtomicBoolean(false);

    public static final Array<CCFFieldMeta> COORDINATE_FIELDS = new Array<>();
    public static final Array<CCFFieldMeta> COORDINATE_SVLEN_SVTYPE_FIELDS = new Array<>();

    static {
        COORDINATE_FIELDS.add(CCFFieldMeta.of("Location::Coordinate", FieldType.int32Array));
        COORDINATE_SVLEN_SVTYPE_FIELDS.add(CCFFieldMeta.of("Location::Coordinate", FieldType.int32Array));
        COORDINATE_SVLEN_SVTYPE_FIELDS.add(CCFFieldMeta.of("Location::SVLength", FieldType.varInt32));
        COORDINATE_SVLEN_SVTYPE_FIELDS.add(CCFFieldMeta.of("Location::TypeSign", FieldType.varInt32));
        COORDINATE_SVLEN_SVTYPE_FIELDS.add(CCFFieldMeta.of("Format::Genotypes", FieldType.int16Array));
        COORDINATE_SVLEN_SVTYPE_FIELDS.add(CCFFieldMeta.of("CSVLocation::IndexOfFile", FieldType.varInt32));
        COORDINATE_SVLEN_SVTYPE_FIELDS.add(CCFFieldMeta.of("CSVLocation::ChrIndexArray", FieldType.int32Array));
    }

    public SDFReader(Object filePath) throws IOException {
        this.filePath = new File(filePath.toString());
        this.reader = new CCFReader(this.filePath);
        this.meta = SDFMeta.decode(this.reader);
        this.templateRecord = reader.getRecord();
        this.decodeMode = meta.encodeMode;
        this.decoder = new SDFDecode(decodeMode);
        this.decoder.setContig(meta.getContig());
    }

    public SDFReader(Object filePath, int decodeMode) throws IOException {
        this.filePath = new File(filePath.toString());
        this.reader = new CCFReader(this.filePath);
        this.meta = SDFMeta.decode(this.reader);
        this.templateRecord = reader.getRecord();
        this.decodeMode = decodeMode;
        this.decoder = new SDFDecode(decodeMode);
        this.decoder.setContig(meta.getContig());
    }

    public SDFReader(Object filePath, Collection<CCFFieldMeta> loadMeta) throws IOException {
        this(filePath);
        this.loadFieldArray = new Array<>();
        this.loadFieldIndexArray = new IntArray();
        HashSet<CCFFieldMeta> loadMetaMap = new HashSet<>(loadMeta);
        Iterator<CCFFieldMeta> iterator = reader.getAllFields().iterator();
        int indexRecord = 0;
        while (iterator.hasNext()) {
            CCFFieldMeta next = iterator.next();
            if (loadMetaMap.contains(next)) {
                this.loadFieldIndexArray.add(indexRecord);
                this.loadFieldArray.add(next);
                loadMetaMap.remove(next);
            }
            indexRecord++;
        }
        this.reader.close();
        this.decoder.setLoadFieldArray(loadFieldIndexArray);
        this.reader = new CCFReader(this.filePath, this.loadFieldArray);
        this.templateRecord = this.reader.getRecord();
    }

    public SDFReader(Object filePath, int decodeMode, Collection<CCFFieldMeta> loadMeta) throws IOException {
        this(filePath, decodeMode);
        this.loadFieldArray = new Array<>();
        this.loadFieldIndexArray = new IntArray();
        HashSet<CCFFieldMeta> loadMetaMap = new HashSet<>(loadMeta);
        Iterator<CCFFieldMeta> iterator = reader.getAllFields().iterator();
        int indexRecord = 0;
        while (iterator.hasNext()) {
            CCFFieldMeta next = iterator.next();
            if (loadMetaMap.contains(next)) {
                this.loadFieldIndexArray.add(indexRecord);
                this.loadFieldArray.add(next);
                loadMetaMap.remove(next);
            }
            indexRecord++;
        }
        this.decoder.setLoadFieldArray(this.loadFieldIndexArray);
        this.reader.close();
        this.reader = new CCFReader(this.filePath, this.loadFieldArray);
        this.templateRecord = this.reader.getRecord();
    }

    public CCFReader getReader() {
        return reader;
    }

    public BriefSVAnnotationFeature readBriefSV(int fileID, int line) throws IOException {
        return reader.read(templateRecord) ? decoder.decodeForBrief(fileID, line, templateRecord) : null;
    }

    public UnifiedSV read() throws IOException {
        boolean read = reader.read(templateRecord);
        return read ? decoder.decode(templateRecord).setFileID(fileID) : null;
    }

    public UnifiedSV read(UnifiedSV sv) throws IOException {
        return reader.read(templateRecord) ? decoder.decode(templateRecord, sv.setFileID(fileID)) : null;
    }

    public UnifiedSV read(UnifiedSV sv, IRecord record) throws IOException {
        return reader.read(record) ? decoder.decode(record, sv.setFileID(fileID)) : null;
    }

    public SDFMeta getMeta() {
        if (meta == null) {
            meta = SDFMeta.decode(this);
        }
        return meta;
    }

    public SDFReader setMeta(SDFMeta meta) {
        this.meta = meta;
        return this;
    }

    public void close() throws IOException {
        if (reader.isClosed()) {
            return;
        }
        reader.close();
    }

    public void restart() throws IOException {
        reader = reader.newInstance();
        templateRecord = reader.getRecord();
    }

    public boolean limitChrBlock(Chromosome chromosome) throws IOException {
        Interval<Integer> interval = meta.contigBlockContainer.getChromosomeRange(chromosome);
        if (interval == null) {
            return false;
        }
        if (interval.end() - interval.start() == 0) {
            return false;
        }
        reader.limit(interval.start(), interval.end());
        return true;
    }

    public File getFilePath() {
        return filePath;
    }

    public SDFReader setContig(Contig contig) {
        this.decoder.setContig(contig);
        return this;
    }

    public SDFReader setFileID(int fileID) {
        this.fileID = fileID;
        return this;
    }

    public int getFileID() {
        return fileID;
    }

    public IRecord readRecord() throws IOException {
        return reader.read(templateRecord) ? templateRecord : null;
    }

    public void redirectCoordinate() throws IOException {
        if (COORDINATE_FIELDS_MODE.get()) {
            reader = reader.newInstance();
            return;
        }
        close();
        resetLoadFields();
        this.loadFieldIndexArray.add(0);
        this.loadFieldArray.add(reader.getField(0));
        decoder.setLoadFieldArray(this.loadFieldIndexArray);
        reader = new CCFReader(filePath, COORDINATE_FIELDS);
        templateRecord = reader.getRecord();
        decoder.setLoadFieldArray(loadFieldIndexArray);
        changeMode(COORDINATE_FIELDS_MODE);
    }

    public void redirectSVFeature() throws IOException {
        if (COORDINATE_SVLEN_SVTYPE_FIELDS_MODE.get()) {
            reader = reader.newInstance();
            return;
        }
        close();
        resetLoadFields();
        reader = new CCFReader(filePath);
        this.loadFieldIndexArray.addAll(IntArray.wrap(new int[]{0, 1, 2, 3, 11, 12}));
        this.loadFieldArray.addAll(COORDINATE_SVLEN_SVTYPE_FIELDS);
        reader = new CCFReader(filePath, COORDINATE_SVLEN_SVTYPE_FIELDS);
        templateRecord = reader.getRecord();
        decoder.setLoadFieldArray(loadFieldIndexArray);
        changeMode(COORDINATE_SVLEN_SVTYPE_FIELDS_MODE);
    }

    public void redirectSVFeaturesAndAnnotationFeatureWithGty() throws IOException {
        if (SV_FEATURE_ANNOTATION_MODE.get()) {
            reader = reader.newInstance();
            return;
        }
        close();
        resetLoadFields();
        reader = new CCFReader(filePath);
        int size = reader.getRecord().size();
        this.loadFieldIndexArray.addAll(IntArray.wrap(new int[]{0, 1, 2, 3, 11, 12}));
        for (int i = 13; i < size; i++) {
            this.loadFieldIndexArray.add(i);
        }
        //region load fields
        for (int i = 0; i < this.loadFieldIndexArray.size(); i++) {
            this.loadFieldArray.add(reader.getField(loadFieldIndexArray.get(i)));
        }
        //endregion
        reader = new CCFReader(filePath, loadFieldArray);
        templateRecord = reader.getRecord();
        decoder.setLoadFieldArray(loadFieldIndexArray);
        changeMode(SV_FEATURE_ANNOTATION_MODE);
    }

    public void redirectSVFeaturesAndAnnotationFeature() throws IOException {
        if (SV_FEATURE_ANNOTATION_MODE.get()) {
            reader = reader.newInstance();
            return;
        }
        close();
        resetLoadFields();
        reader = new CCFReader(filePath);
        int size = reader.getRecord().size();
        this.loadFieldIndexArray.addAll(IntArray.wrap(new int[]{0, 1, 2, 11, 12}));
        for (int i = 13; i < size; i++) {
            this.loadFieldIndexArray.add(i);
        }
        //region load fields
        for (int i = 0; i < this.loadFieldIndexArray.size(); i++) {
            this.loadFieldArray.add(reader.getField(loadFieldIndexArray.get(i)));
        }
        //endregion
        reader = new CCFReader(filePath, loadFieldArray);
        templateRecord = reader.getRecord();
        decoder.setLoadFieldArray(loadFieldIndexArray);
        changeMode(SV_FEATURE_ANNOTATION_MODE);
    }

    public void redirectAllFeatures() throws IOException {
        if (ALL_FEATURE_MODE.get()) {
            reader = reader.newInstance();
            return;
        }
        close();
        resetLoadFields();
        reader = new CCFReader(filePath);
        templateRecord = reader.getRecord();
        decoder.setLoadFieldArray(loadFieldIndexArray);
        changeMode(ALL_FEATURE_MODE);
    }


    public SDFReader resetLoadFieldIndexArray(Array<CCFFieldMeta> loadMetaArray) throws IOException {
        if (loadFieldIndexArray == null) {
            loadFieldIndexArray = new IntArray();
        } else {
            this.loadFieldIndexArray.clear();
        }
        if (loadFieldArray == null) {
            loadFieldArray = new Array<>();
        } else {
            loadFieldArray.clear();
        }
        int indexRecord = 0;
        try {
            reader = new CCFReader(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Iterator<CCFFieldMeta> iterator = reader.getAllFields().iterator();
        reader.seek(0);
        HashSet<CCFFieldMeta> loadMetaMap = new HashSet<>(loadMetaArray);
        while (iterator.hasNext()) {
            CCFFieldMeta next = iterator.next();
            if (loadMetaMap.contains(next)) {
                this.loadFieldIndexArray.add(indexRecord);
                this.loadFieldArray.add(next);
                loadMetaMap.remove(next);
            }
            indexRecord++;
        }
        CCFTable.clear(reader);
        return this;
    }

    public Contig getContig() {
        return meta.getContig();
    }

    private void resetLoadFields() {
        if (loadFieldArray == null) {
            loadFieldArray = new Array<>();
        }
        if (loadFieldIndexArray == null) {
            loadFieldIndexArray = new IntArray();
        }
        this.loadFieldArray.clear();
        this.loadFieldIndexArray.clear();
    }

    private void changeMode(AtomicBoolean mode) {
        Array<AtomicBoolean> modeArray = new Array<>();
        modeArray.add(COORDINATE_FIELDS_MODE);
        modeArray.add(COORDINATE_SVLEN_SVTYPE_FIELDS_MODE);
        modeArray.add(SV_FEATURE_ANNOTATION_MODE);
        modeArray.add(ALL_FEATURE_MODE);
        int excludeIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (mode == modeArray.get(i)) {
                excludeIndex = i;
                break;
            }
        }
        if (excludeIndex != -1) {
            for (int i = 0; i < 4; i++) {
                if (i != excludeIndex) {
                    modeArray.get(i).set(false);
                }
            }
            mode.set(true);
        }
    }
}
