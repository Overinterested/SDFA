package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.merge.SDFAMergeManager;
import edu.sysu.pmglab.sdfa.merge.base.CSVAssemblerInFile;
import edu.sysu.pmglab.sdfa.merge.base.SimpleSVMergeQueue;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 09:33
 * @description
 */
public abstract class AbstractSVCollector {
    static AtomicInteger currChrIndex = new AtomicInteger();
    static Array<SDFReader> sdfReaderArray = new Array<>();
    static final Array<ComplexSV> csvCompleteArray = new Array<>();
    static HashMap<ByteCode, CCFFieldMeta> loadFieldMap = new HashMap<>();
    static final Array<CSVAssemblerInFile> csvFileIDMergeQueueMap = new Array<>();
    static final Array<SimpleSVMergeQueue> simpleSVTypeSVMergeQueueMap = new Array<>();

    static {
        loadFieldMap.put(new ByteCode("coordinate"), CCFFieldMeta.of("Location::Coordinate", FieldType.int32Array));
        loadFieldMap.put(new ByteCode("length"), CCFFieldMeta.of("Location::SVLength", FieldType.varInt32));
        loadFieldMap.put(new ByteCode("type"), CCFFieldMeta.of("Location::TypeSign", FieldType.varInt32));
        loadFieldMap.put(new ByteCode("gty"), CCFFieldMeta.of("Format::Genotypes", FieldType.int16Array));
//        loadFieldMap.put(new ByteCode("format"), CCFFieldMeta.of("Format::Other", FieldType.bytecode));
        loadFieldMap.put(new ByteCode("id"), CCFFieldMeta.of("Field::ID", FieldType.bytecode));
        loadFieldMap.put(new ByteCode("ref"), CCFFieldMeta.of("Field::Ref", FieldType.bytecode));
        loadFieldMap.put(new ByteCode("alt"), CCFFieldMeta.of("Field::Alt", FieldType.bytecode));
//        loadFieldMap.put(new ByteCode("qual"), CCFFieldMeta.of("Field::Qual", FieldType.bytecode));
//        loadFieldMap.put(new ByteCode("filter"), CCFFieldMeta.of("Field::Filter", FieldType.bytecode));
        loadFieldMap.put(new ByteCode("info"), CCFFieldMeta.of("Field::InfoField", FieldType.bytecodeArray));
        loadFieldMap.put(new ByteCode("svindex"), CCFFieldMeta.of("CSVLocation::IndexOfFile", FieldType.varInt32));
        loadFieldMap.put(new ByteCode("chrindexarray"), CCFFieldMeta.of("CSVLocation::CSVIndexArray", FieldType.int32Array));
        int size = SVTypeSign.support().size();
        for (int i = 0; i < size; i++) {
            simpleSVTypeSVMergeQueueMap.add(new SimpleSVMergeQueue());
        }
    }

    /**
     * collect SVs from SDF files
     *
     * @return boolean true represents collecting SVs, while false represents has no collected SVs
     */
    abstract public boolean collect() throws IOException;

    abstract public boolean filterCSV(ComplexSV csv);

    abstract public boolean filterSimpleSV(UnifiedSV sv);

    public static Chromosome getChromosome() {
        return SDFGlobalContig.getGlobalChromosome(currChrIndex.get());
    }

    public static int getChrIndex() {
        return currChrIndex.get();
    }

    /**
     * load meta, contig and subjects of sdf readers
     */
    public void buildCSVAssembly() {
        for (int i = 0; i < SDFAMergeManager.fileSize; i++) {
            csvFileIDMergeQueueMap.add(new CSVAssemblerInFile());
        }
    }


    public AbstractSVCollector setSdfReaderArray(Array<SDFReader> sdfReaderArray) {
        this.sdfReaderArray = sdfReaderArray;
        return this;
    }

    public AtomicInteger getCurrChrIndex() {
        return currChrIndex;
    }
}
