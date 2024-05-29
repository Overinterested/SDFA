package edu.sysu.pmglab.sdfa.sv.vcf;

import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFEncode;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVGenotypes;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import edu.sysu.pmglab.sdfa.sv.vcf.calling.AbstractCallingParser;
import edu.sysu.pmglab.sdfa.sv.vcf.calling.SvisionCallingParser;
import edu.sysu.pmglab.sdfa.sv.vcf.exception.SVParseException;
import edu.sysu.pmglab.sdfa.sv.vcf.exception.SVPosParseException;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.gty.GenotypeFilterManager;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.SVFieldFilterManager;
import edu.sysu.pmglab.sdfa.toolkit.Contig;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import edu.sysu.pmglab.unifyIO.FileStream;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;


/**
 * @author Wenjie Peng
 * @create 2024-03-22 14:22
 * @description
 */
public class VCFFile {
    int fileID;
    File filePath;
    Contig contig;
    Subjects subjects;
    SVFilterManager filter;
    boolean parsed = false;
    VCFCallingParser vcfCallingParser;
    VCFHeader header = new VCFHeader();
    IntArray invalidSVIndexList = new IntArray();
    IndexableSet<ByteCode> infoFieldNameArray = new IndexableSet<>();
    HashMap<ByteCode, IntArray> formatFieldMap = new HashMap<>();
    IndexableSet<ByteCode> allFormatFieldNameArray = new IndexableSet<>();
    private CCFWriter writer;
    private IntArray chrIndexBlock;
    private final ByteCode EXIST = new ByteCode(new byte[]{ByteCode.PERIOD});
    private final ByteCodeArray tmp = new ByteCodeArray(true);
    private final ByteCodeArray fieldArray = new ByteCodeArray(5);
    private final BaseArray<ByteCode> KVArray = new ByteCodeArray(2, true);
    private static final ByteCode GT = new ByteCode("GT");
    //region VCF header
    public static final ByteCode HEADER_COLUMN = new ByteCode(new byte[]{
            // #CHR
            ByteCode.NUMBER_SIGN, ByteCode.C, ByteCode.H, ByteCode.R, ByteCode.TAB,
            // POS
            ByteCode.P, ByteCode.O, ByteCode.S, ByteCode.TAB,
            // ID
            ByteCode.I, ByteCode.D, ByteCode.TAB,
            // REF
            ByteCode.R, ByteCode.E, ByteCode.F, ByteCode.TAB,
            // ALT
            ByteCode.A, ByteCode.L, ByteCode.T, ByteCode.TAB,
            // QUAL
            ByteCode.Q, ByteCode.U, ByteCode.A, ByteCode.L, ByteCode.TAB,
            // FILTER
            ByteCode.F, ByteCode.I, ByteCode.L, ByteCode.T, ByteCode.E, ByteCode.R, ByteCode.TAB,
            // INFO
            ByteCode.I, ByteCode.N, ByteCode.F, ByteCode.O, ByteCode.TAB,
            // FORMAT
            ByteCode.F, ByteCode.O, ByteCode.R, ByteCode.M, ByteCode.A, ByteCode.T, ByteCode.TAB
    });
    //endregion

    public VCFFile() {
        this.subjects = new Subjects(filePath);
        this.contig = new Contig();
        this.vcfCallingParser = new VCFCallingParser();
    }

    public VCFFile(File filePath) {
        this.filePath = filePath;
        this.subjects = new Subjects(filePath);
        this.contig = new Contig();
        this.vcfCallingParser = new VCFCallingParser();
    }

    public VCFFile parse() throws IOException {
        if (parsed) {
            return this;
        }
        vcfCallingParser.init(filePath);
        int indexOfFile = 0;
        FileStream fs = new FileStream(filePath);
        VolumeByteStream cache = new VolumeByteStream();
        //region parse header
        while (fs.readLine(cache) != -1) {
            if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                header.parse(cache);
                cache.reset();
                continue;
            }
            break;
        }
        if (cache.size() == 0) {
            return this;
        }
        //endregion
        init();
        int subjectSize = subjects.numOfSubjects();
        boolean nonSubjectMode = subjectSize == 0;
        SVGenotypes genotypes = SVGenotypes.initSubjects(subjectSize);
        ReusableMap<ByteCode, ByteCode> infoFieldMap = header.getSpecificInfoFiledMap();
        allFormatFieldNameArray.addAll(header.getFormatConfig().getIndexableIDSet());
        if (allFormatFieldNameArray.isEmpty()) {
            allFormatFieldNameArray.add(GT);
        }
        GenotypeFilterManager gtyFilterManager = !nonSubjectMode && filter != null && filter.filterGty() ? filter.getGenotypeFilterManager() : null;
        SVFieldFilterManager fieldFilterManager = !nonSubjectMode && filter != null && filter.filterField() ? filter.getFieldFilterManager() : null;
        registerIgnoreInfoField(infoFieldMap);
        infoFieldNameArray = new IndexableSet<>(infoFieldMap.keySet());

        ByteCodeArray lineCache = new ByteCodeArray(true);
        int pos;
        do {
            try {
                ByteCode line = cache.toByteCode();
                BaseArray<ByteCode> lineSplit = line.split(ByteCode.TAB, lineCache);
                Chromosome chromosome = contig.get(lineSplit.get(0));
                //region parse genotypes
                if (!nonSubjectMode) {
                    if (filter != null && !filter.isCheck()) {
                        filter.check(new ByteCodeArray(allFormatFieldNameArray));
                    }
                    int formatFieldSize = allFormatFieldNameArray.size() - 1;
                    if (!VCF2SDF.dropFormat) {
                        genotypes.initFormatField(formatFieldSize);
                    }
                    // FORMAT col
                    ByteCode formatCol = lineSplit.get(8);
                    IntArray fieldIndexArrayOfFormat = formatFieldMap.get(formatCol);
                    if (fieldIndexArrayOfFormat == null) {
                        BaseArray<ByteCode> split = formatCol.split(ByteCode.COLON, tmp);
                        IntArray tmpIndexArrayOfFormat = new IntArray();
                        for (ByteCode formatItem : split) {
                            int index = allFormatFieldNameArray.indexOf(formatItem);
                            if (index == 0) {
                                continue;
                            }
                            if (index == -1) {
                                throw new UnsupportedEncodingException("Format item(" + formatItem + ") isn't recorded by VCF header.");
                            }
                            tmpIndexArrayOfFormat.add(index - 1);
                        }
                        formatFieldMap.put(formatCol.asUnmodifiable(), tmpIndexArrayOfFormat);
                        fieldIndexArrayOfFormat = tmpIndexArrayOfFormat;
                        tmp.clear();
                    }
                    genotypes.clearProperties();
                    for (int i = 0; i < subjectSize; i++) {
                        BaseArray<ByteCode> split = lineSplit.get(i + 9).split(ByteCode.COLON, tmp);
                        SVGenotype genotype = SVGenotype.of(split.popFirst());
                        BaseArray<ByteCode> property = split.popFirst(split.size());
                        if (gtyFilterManager != null) {
                            genotype = gtyFilterManager.filter(genotype, property);
                        }
                        genotypes.addGtyAndProperty(i, genotype, property, fieldIndexArrayOfFormat);
                    }
                }
                // endregion
                try {
                    pos = lineSplit.get(1).toInt();
                } catch (NumberFormatException e) {
                    throw new SVPosParseException();
                }
                fillInfoFieldMap(infoFieldMap, lineSplit.get(7));
                if (fieldFilterManager != null) {
                    boolean filterField = fieldFilterManager.filter(
                            // QUAL, FILTER, INFO, GTs
                            lineSplit.get(5), lineSplit.get(6), infoFieldMap, genotypes
                    );
                    if (!filterField) {
                        vcfCallingParser.addTotalSVSizeInVCF();
                        infoFieldMap.clear();
                        continue;
                    }
                }
                getEncodeNonInfoFieldArray(lineSplit);
                vcfCallingParser.parse(indexOfFile, chromosome, pos, fieldArray, infoFieldMap, genotypes);
                filter();
            } catch (SVParseException e) {
                invalidSVIndexList.add(indexOfFile);
            } finally {
                indexOfFile++;
                cache.reset();
                if (VCF2SDF.lineExtractAndSort) {
                    Array<IRecord> loadUnifiedSVArray = getLoadUnifiedSVArray();
                    if (loadUnifiedSVArray != null) {
                        for (IRecord record : loadUnifiedSVArray) {
                            writer.write(record);
                            int chrIndex = ((int[]) record.get(0))[0];
                            if (chrIndex >= chrIndexBlock.size()) {
                                for (int i = chrIndexBlock.size(); i < chrIndex + 1; i++) {
                                    chrIndexBlock.add(0);
                                }
                            }
                            chrIndexBlock.set(chrIndex, chrIndexBlock.get(chrIndex) + 1);
                            record.clear();
                        }
                    }
                }
                lineCache.clear();
                tmp.clear();
            }
        } while (fs.readLine(cache) != -1);
        fs.close();
        parsed = true;
        return this;
    }

    void getEncodeNonInfoFieldArray(BaseArray<ByteCode> splitLine) {
        fieldArray.clear();
        //region encode 5 field: ID, REF, ALT, QUAL, FILTER
        fieldArray.add(splitLine.get(2));
        if (!VCF2SDF.dropRefField) {
            fieldArray.add(splitLine.get(3));
        } else {
            fieldArray.add(ByteCode.EMPTY);
        }
        fieldArray.add(splitLine.get(4));
        if (!VCF2SDF.dropQualField) {
            fieldArray.add(splitLine.get(5));
        } else {
            fieldArray.add(ByteCode.EMPTY);
        }
        if (!VCF2SDF.dropFilterField) {
            fieldArray.add(splitLine.get(6));
        } else {
            fieldArray.add(ByteCode.EMPTY);
        }
        //endregion
    }

    /**
     * fill in the info fields
     *
     * @param infoFieldMap info field map
     * @param info         a info field
     */
    public void fillInfoFieldMap(ReusableMap<ByteCode, ByteCode> infoFieldMap, ByteCode info) {
        tmp.clear();
        BaseArray<ByteCode> splitInfo = info.split(ByteCode.SEMICOLON, tmp);
        for (int i = 0; i < splitInfo.size(); i++) {
            ByteCode item = splitInfo.get(i);
            KVArray.clear();
            item.split(ByteCode.EQUAL, KVArray);
            ByteCode k = KVArray.get(0);
            ByteCode v;
            if (KVArray.size() == 1) {
                v = EXIST;
            } else {
                v = KVArray.get(1);
                v = v == null ? ByteCode.EMPTY : v;
            }
            int indexOfKey = infoFieldNameArray.indexOf(k);
            if (indexOfKey != -1) {
                infoFieldMap.putByIndex(indexOfKey, v);
                continue;
            }
            ByteCode newKey = k.asUnmodifiable();
            infoFieldNameArray.add(newKey);
            infoFieldMap.put(newKey, v);
        }
    }

    public VCFFile setFileID(int fileID) {
        this.fileID = fileID;
        return this;
    }

    public void filter() {
        vcfCallingParser.filter(filter);
    }

    public Array<IRecord> getLoadUnifiedSVArray() {
        return vcfCallingParser.getLoadUnifiedSVArray();
    }

    public Subjects getSubjects() {
        return subjects;
    }

    public int getFileID() {
        return fileID;
    }

    public VCFHeader getHeader() {
        return header;
    }

    public ByteCodeArray mergeVCFFormatField(Array<VCFFormatField> vcfFormatFieldArray) {
        int size = vcfFormatFieldArray.size();
        ByteCodeArray res = new ByteCodeArray(size);
        for (int i = 0; i < size; i++) {
            res.add(vcfFormatFieldArray.get(i).mergeTags());
        }
        return res;
    }

    public void reset() {
        fileID = -1;
        filePath = null;
        contig.clear();
        parsed = false;
        subjects.clear();
        vcfCallingParser.reset();
        invalidSVIndexList.clear();
        infoFieldNameArray.clear();
        header.reset();
        KVArray.clear();
        tmp.clear();
        fieldArray.clear();
        chrIndexBlock.clear();
        allFormatFieldNameArray.clear();
        formatFieldMap.clear();
    }

    public Chromosome getChr(ByteCode contigName) {
        return contig.get(contigName);
    }

    public void cycle() {
        reset();
        ReusableVCFPool.getInstance().cycle(this);
    }

    public int numOfContig() {
        return contig.getAllChromosomes().size();
    }

    public VCFFile setFilePath(File filePath) {
        this.filePath = filePath;
        this.subjects.setFilePath(filePath);
        return this;
    }

    private void init() {
        // init contig
        contig.loadChromosomes(header.getContigIDArray());
        vcfCallingParser.SVParser.setContig(contig);
        // init subject
        subjects.addAll(header.subjectArray);
    }

    public Contig getContig() {
        return contig;
    }

    public void closeInfo(Logger logger) {
        VolumeByteStream cache = new VolumeByteStream();
        cache.writeSafety(filePath.getName());
        int totally = vcfCallingParser.unifiedArray.totalSVSizeInVCF;
        int store = vcfCallingParser.unifiedArray.verifiedSVSize;
        int filterNum = totally - store;
        cache.writeSafety(" totally contains " + totally + " SVs and filters " + filterNum + " SVs. ");
        if (filter != null) {
            cache.writeSafety(filter.toString());
        }
        logger.trace(cache.toByteCode().toString());
        cache.close();
    }

    public void registerIgnoreInfoField(ReusableMap<ByteCode, ByteCode> infoFieldMap) {
        for (int i = 0; i < infoFieldMap.size(); i++) {
            ByteCode key = infoFieldMap.getKeyByIndex(i);
            if (key.startsWith(AbstractCallingParser.END_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.CHR_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.CHR2_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.POS_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.POS2_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.SVLEN_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.END2_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.SVTYPE_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(AbstractCallingParser.SVTYPE2_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
                continue;
            }
            if (key.startsWith(SvisionCallingParser.BKPS_BYTECODE)) {
                vcfCallingParser.SVParser.addIgnoreInfoIndex(i);
            }
        }
    }

    public VCFFile setFilter(SVFilterManager filter) {
        this.filter = filter;
        return this;
    }

    public VCFFile setSDFEncode(SDFEncode encode) {
        this.vcfCallingParser.unifiedArray.setEncodeSV(encode);
        return this;
    }

    public CCFWriter getWriter() {
        return writer;
    }

    public VCFFile setWriter(CCFWriter writer) {
        this.writer = writer;
        return this;
    }

    public IntArray getChrIndexBlock() {
        return chrIndexBlock;
    }

    public VCFFile setChrIndexBlock(IntArray chrIndexBlock) {
        this.chrIndexBlock = chrIndexBlock;
        return this;
    }

    public IndexableSet<ByteCode> getInfoFieldNameArray() {
        return infoFieldNameArray;
    }

    public IndexableSet<ByteCode> getAllFormatFieldNameArray() {
        return allFormatFieldNameArray;
    }
}
