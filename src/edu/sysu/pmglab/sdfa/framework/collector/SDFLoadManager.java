package edu.sysu.pmglab.sdfa.framework.collector;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.sv.idividual.SubjectManager;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-30 00:56
 * @description
 */
public class SDFLoadManager {
    SubjectManager subjectManager;
    Array<SDFReader> sdfReaderArray;
    static boolean defaultLoadCSVTag = true;
    final Array<CCFFieldMeta> loadFieldArray;
    public static HashMap<ByteCode, CCFFieldMeta> allFieldMap = new HashMap<>();

    private SDFLoadManager(String... loadFieldArray) {
        this.loadFieldArray = new Array<>();
        for (String loadField : loadFieldArray) {
            CCFFieldMeta fieldMeta = allFieldMap.get(new ByteCode(loadField.toLowerCase()).asUnmodifiable());
            if (fieldMeta == null) {
                throw new UnsupportedOperationException("No field called " + loadField);
            }
            this.loadFieldArray.add(fieldMeta);
        }
        if (defaultLoadCSVTag) {
            this.loadFieldArray.add(allFieldMap.get(new ByteCode("svindex")));
            this.loadFieldArray.add(allFieldMap.get(new ByteCode("chrindexarray")));
        }
    }

    public static SDFLoadManager of(String... loadFieldArray) {
        return new SDFLoadManager(loadFieldArray);
    }

    static {
        allFieldMap.put(new ByteCode("coordinate"), CCFFieldMeta.of("Location::coordinate", FieldType.int32Array));
        allFieldMap.put(new ByteCode("length"), CCFFieldMeta.of("Location::SVLength", FieldType.varInt32));
        allFieldMap.put(new ByteCode("type"), CCFFieldMeta.of("Location::TypeSign", FieldType.varInt32));
        allFieldMap.put(new ByteCode("gty"), CCFFieldMeta.of("Format::Genotypes", FieldType.int16Array));
        allFieldMap.put(new ByteCode("format"), CCFFieldMeta.of("Format::Other", FieldType.bytecode));
        allFieldMap.put(new ByteCode("id"), CCFFieldMeta.of("Field::ID", FieldType.bytecode));
        allFieldMap.put(new ByteCode("ref"), CCFFieldMeta.of("Field::Ref", FieldType.bytecode));
        allFieldMap.put(new ByteCode("alt"), CCFFieldMeta.of("Field::Alt", FieldType.bytecode));
        allFieldMap.put(new ByteCode("qual"), CCFFieldMeta.of("Field::Qual", FieldType.bytecode));
        allFieldMap.put(new ByteCode("filter"), CCFFieldMeta.of("Field::Filter", FieldType.bytecode));
        allFieldMap.put(new ByteCode("info"), CCFFieldMeta.of("Field::InfoFields", FieldType.bytecodeArray));
        allFieldMap.put(new ByteCode("svindex"), CCFFieldMeta.of("CSVLocation::IndexOfFile", FieldType.varInt32));
        allFieldMap.put(new ByteCode("chrindexarray"), CCFFieldMeta.of("CSVLocation::CSVIndexArray", FieldType.int32Array));
    }

    public SDFLoadManager addSDFFile(Object sdf) throws IOException {
        if (sdf instanceof SDFReader) {
            SDFReader sdfReader = (SDFReader) sdf;
            sdfReader.close();
            sdfReader = new SDFReader(sdfReader.getFilePath(), loadFieldArray);
            this.sdfReaderArray.add(sdfReader);
            this.subjectManager.register(sdfReader.getFilePath(), sdfReader.getMeta().getSubjects());
            sdfReader.close();
        } else if (sdf instanceof File) {
            SDFReader sdfReader = new SDFReader(sdf, loadFieldArray);
            this.sdfReaderArray.add(sdfReader);
            this.subjectManager.register(sdfReader.getFilePath(), sdfReader.getMeta().getSubjects());
            sdfReader.close();
        } else if (sdf instanceof Array) {
            Object first = ((Array<?>) sdf).get(0);
            addSDFFile(first);
        }
        return this;
    }
}
