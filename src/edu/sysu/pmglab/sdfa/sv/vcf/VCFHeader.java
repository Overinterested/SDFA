package edu.sysu.pmglab.sdfa.sv.vcf;

import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.idividual.Subject;
import edu.sysu.pmglab.sdfa.toolkit.SDFRefAltFieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Wenjie Peng
 * @create 2024-03-22 09:28
 * @description
 */
public class VCFHeader {

    Contig contig = new Contig();
    KVConfig kvConfig = new KVConfig();
    AltConfig altConfig = new AltConfig();
    FilterConfig filterConfig = new FilterConfig();
    InfoConfig infoConfig = new InfoConfig();
    FormatConfig formatConfig = new FormatConfig();
    Array<Subject> subjectArray = new Array<>();
    public static final byte[] EMPTY_SUBJECT_NAME = "NULL".getBytes();
    public static final byte[] CONTIG_HEADER = new byte[]{ByteCode.NUMBER_SIGN, ByteCode.NUMBER_SIGN,
            ByteCode.c, ByteCode.o, ByteCode.n, ByteCode.t, ByteCode.i, ByteCode.g};
    public static final byte[] ALT_HEADER = new byte[]{ByteCode.NUMBER_SIGN, ByteCode.NUMBER_SIGN,
            ByteCode.A, ByteCode.L, ByteCode.T};
    public static final byte[] FILTER_HEADER = new byte[]{ByteCode.NUMBER_SIGN, ByteCode.NUMBER_SIGN,
            ByteCode.F, ByteCode.I, ByteCode.L, ByteCode.T, ByteCode.E, ByteCode.R};
    public static final byte[] INFO_HEADER = new byte[]{ByteCode.NUMBER_SIGN, ByteCode.NUMBER_SIGN,
            ByteCode.I, ByteCode.N, ByteCode.F, ByteCode.O};
    public static final byte[] FORMAT_HEADER = new byte[]{ByteCode.NUMBER_SIGN, ByteCode.NUMBER_SIGN,
            ByteCode.F, ByteCode.O, ByteCode.R, ByteCode.M, ByteCode.A, ByteCode.T};
    public static final byte[] CHR_HEADER = new byte[]{ByteCode.NUMBER_SIGN, ByteCode.C, ByteCode.H};
    public static final Map<ByteCode, FieldType> TYPE_CONVERT = new HashMap<>();
    static CallableSet<HeaderFeature> noneInfoField = new CallableSet<>(5);
    ReusableMap<ByteCode, ByteCode> specificInfoFiledMap = new ReusableMap<>();

    static {
        TYPE_CONVERT.put(new ByteCode("Flag"), FieldType.bool);
        TYPE_CONVERT.put(new ByteCode("."), FieldType.bytecode);
        TYPE_CONVERT.put(new ByteCode("Boolean"), FieldType.bool);
        TYPE_CONVERT.put(new ByteCode("String"), FieldType.bytecode);
        TYPE_CONVERT.put(new ByteCode("Integer"), FieldType.int32Array);
        // ID, REF, ALT, QUAL, FILTER
        noneInfoField.add(new HeaderFeature("ID", FieldType.bytecode, 1, "ID"));
        noneInfoField.add(new HeaderFeature("REF", FieldType.bytecode, 1, "REF"));
        noneInfoField.add(new HeaderFeature("ALT", new SDFRefAltFieldType(), 1, "ALT"));
        noneInfoField.add(new HeaderFeature("QUAL", FieldType.bytecode, 1, "QUAL"));
        noneInfoField.add(new HeaderFeature("FILTER", FieldType.bytecode, 1, "FILTER"));
    }

    static class HeaderFeature {
        ByteCode name;
        FieldType type;
        int featureNumber;
        ByteCode description;

        public HeaderFeature(String name, FieldType type, int featureNumber, String description) {
            this.name = new ByteCode(name).asUnmodifiable();
            this.type = type;
            this.featureNumber = featureNumber;
            this.description = new ByteCode(description).asUnmodifiable();
        }

        public HeaderFeature(ByteCode name, FieldType type, int featureNumber, ByteCode description) {
            this.name = name;
            this.type = type;
            this.featureNumber = featureNumber;
            this.description = description;
        }

        public ByteCode encode(ByteCode src) {
            return type.encode(src);
        }
    }

    public void parse(VolumeByteStream cache) {
        ByteCode src = cache.toByteCode();
        if (src.startsWith(CHR_HEADER)) {
            BaseArray<ByteCode> split = src.split(ByteCode.TAB);
            for (int i = 9; i < split.size(); i++) {
                subjectArray.add(new Subject(split.get(i).asUnmodifiable()));
            }
        } else if (src.startsWith(CONTIG_HEADER)) {
            contig.add(src);
        } else if (src.startsWith(ALT_HEADER)) {
            altConfig.add(src);
        } else if (src.startsWith(FILTER_HEADER)) {
            filterConfig.add(src);
        } else if (src.startsWith(INFO_HEADER)) {
            infoConfig.add(src);
        } else if (src.startsWith(FORMAT_HEADER)) {
            formatConfig.add(src);
        } else {
            kvConfig.add(src);
        }
    }

    public ReusableMap<ByteCode, ByteCode> getSpecificInfoFiledMap() {
        if (specificInfoFiledMap.isEmpty()) {
            for (ByteCode id : infoConfig.indexableIDSet) {
                specificInfoFiledMap.putKey(id.asUnmodifiable());
            }
        }
        return specificInfoFiledMap;
    }

    public static class KVConfig {
        ReusableMap<ByteCode, ByteCode> kvMap = new ReusableMap<>();
        public void add(ByteCode src) {
            ByteCode pure = src.subByteCode(2);
            BaseArray<ByteCode> split = pure.split(ByteCode.EQUAL);
            if (split.size() == 1) {
                kvMap.put(split.get(0), null);
                return;
            }
            kvMap.put(split.get(0).asUnmodifiable(), split.get(1).asUnmodifiable());
        }

        public ByteCodeArray getKeySet() {
            return new ByteCodeArray(kvMap.keySet());
        }

        public ByteCode getValue(ByteCode key) {
            return kvMap.get(key);
        }

        public Array<Entry<ByteCode, ByteCode>> KVEntries() {
            Array<Entry<ByteCode, ByteCode>> res = new Array<>();
            for (int i = 0; i < kvMap.size(); i++) {
                res.add(new Entry<>(kvMap.getKeyByIndex(i), kvMap.getByIndex(i)));
            }
            return res;
        }

        public void clear() {
            kvMap = new ReusableMap<>();
        }

        public KVConfig asUnmodified() {
            KVConfig kvConfig = new KVConfig();
            kvConfig.kvMap.putAll(kvMap);
            return kvConfig;
        }
    }

    public static class Contig {
        ReusableMap<ByteCode, Integer> IDLengthMap = new ReusableMap<>();
        private static final Pattern pattern = Pattern.compile("##contig=<ID=(?<id>[^,]+),length=(?<length>\\d+)>");

        public void add(ByteCode src) {
            Matcher matcher = pattern.matcher(src.toString());
            if (matcher.find()) {
                String id = matcher.group("id");
                String length = matcher.group("length");
                IDLengthMap.put(new ByteCode(id), new ByteCode(length).toInt());
            }
        }

        public int getLength(ByteCode id) {
            return IDLengthMap.get(id);
        }

        public ByteCodeArray getIDs() {
            return new ByteCodeArray(IDLengthMap.keySet());
        }

        public ByteCodeArray getToStringArray() {
            int size = IDLengthMap.size();
            if (size == 0) {
                return ByteCodeArray.wrap(new ByteCode[0]);
            }
            VolumeByteStream cache = new VolumeByteStream();
            ByteCodeArray res = new ByteCodeArray(size);
            for (int i = 0; i < size; i++) {
                cache.writeSafety(new byte[]{ByteCode.I, ByteCode.D, ByteCode.EQUAL});
                cache.writeSafety(IDLengthMap.getKeyByIndex(i));
                cache.writeSafety(new byte[]{ByteCode.COMMA, ByteCode.l, ByteCode.e, ByteCode.n, ByteCode.g, ByteCode.t, ByteCode.h, ByteCode.EQUAL});
                cache.writeSafety(ValueUtils.Value2Text.int2bytes(IDLengthMap.getByIndex(i)));
                res.add(cache.toByteCode().asUnmodifiable());
                cache.reset();
            }
            cache.close();
            return res;
        }

        public Array<Entry<ByteCode, Integer>> getEntries() {
            int size = IDLengthMap.size();
            Array<Entry<ByteCode, Integer>> res = new Array<>(size);
            for (int i = 0; i < size; i++) {
                res.add(new Entry<>(IDLengthMap.getKeyByIndex(i), IDLengthMap.getByIndex(i)));
            }
            return res;
        }

        public void clear() {
            IDLengthMap = new ReusableMap<>();
        }

        /**
         * here we keep a fake unmodified mode, just store its citations
         */
        public Contig asUnmodified() {
            Contig contig = new Contig();
            contig.IDLengthMap.putAll(IDLengthMap);
            return contig;
        }
    }

    public static class AltConfig {
        ReusableMap<ByteCode, ByteCode> IDDescriptionMap = new ReusableMap<>();
        private static final Pattern pattern = Pattern.compile("##ALT=<ID=(?<id>[^,]+),Description=\"(?<description>[^\"]+)\">");


        public void add(ByteCode src) {
            Matcher matcher = pattern.matcher(src.toString());
            if (matcher.find()) {
                String id = matcher.group("id");
                String description = matcher.group("description");
                IDDescriptionMap.put(new ByteCode(id), new ByteCode(description));
            }
        }

        public ByteCodeArray getIDs() {
            return new ByteCodeArray(IDDescriptionMap.keySet());
        }

        public ByteCode getDescription(ByteCode id) {
            return IDDescriptionMap.get(id);
        }

        public ByteCodeArray getToStringArray() {
            int size = IDDescriptionMap.size();
            if (size == 0) {
                return ByteCodeArray.wrap(new ByteCode[0]);
            }
            VolumeByteStream cache = new VolumeByteStream();
            ByteCodeArray res = new ByteCodeArray(size);
            for (int i = 0; i < size; i++) {
                cache.writeSafety(new byte[]{ByteCode.I, ByteCode.D, ByteCode.EQUAL});
                cache.writeSafety(IDDescriptionMap.getKeyByIndex(i));
                cache.writeSafety(new byte[]{ByteCode.COMMA, ByteCode.d, ByteCode.e, ByteCode.s, ByteCode.c, ByteCode.r, ByteCode.i,
                        ByteCode.p, ByteCode.t, ByteCode.i, ByteCode.o, ByteCode.n, ByteCode.EQUAL});
                cache.writeSafety(IDDescriptionMap.getByIndex(i));
                res.add(cache.toByteCode().asUnmodifiable());
                cache.reset();
            }
            cache.close();
            return res;
        }

        public Array<Entry<ByteCode, ByteCode>> IDDescriptionEntries() {
            Array<Entry<ByteCode, ByteCode>> res = new Array<>();
            for (int i = 0; i < IDDescriptionMap.size(); i++) {
                res.add(new Entry<>(IDDescriptionMap.getKeyByIndex(i), IDDescriptionMap.getByIndex(i)));
            }
            return res;
        }

        public void clear() {
            IDDescriptionMap = new ReusableMap<>();
        }

        /**
         * here we keep a fake unmodified mode, just store its citations
         */
        public AltConfig asUnmodified() {
            AltConfig altConfig = new AltConfig();
            altConfig.IDDescriptionMap.putAll(IDDescriptionMap);
            return altConfig;
        }
    }

    public static class FilterConfig {
        ReusableMap<ByteCode, ByteCode> IDDescriptionMap = new ReusableMap<>();
        private static final Pattern pattern = Pattern.compile("##FILTER=<ID=(?<id>[^,]+),Description=\"(?<description>[^\"]+)\">");

        public void add(ByteCode src) {
            Matcher matcher = pattern.matcher(src.toString());
            if (matcher.find()) {
                String id = matcher.group("id");
                String description = matcher.group("description");
                IDDescriptionMap.put(new ByteCode(id), new ByteCode(description));
            }
        }

        public ByteCodeArray getIDs() {
            return new ByteCodeArray(IDDescriptionMap.keySet());
        }

        public ByteCode getDescription(ByteCode id) {
            return IDDescriptionMap.get(id);
        }

        public ByteCodeArray getToStringArray() {
            int size = IDDescriptionMap.size();
            if (size == 0) {
                return ByteCodeArray.wrap(new ByteCode[0]);
            }
            VolumeByteStream cache = new VolumeByteStream();
            ByteCodeArray res = new ByteCodeArray(size);
            for (int i = 0; i < size; i++) {
                cache.writeSafety(new byte[]{ByteCode.I, ByteCode.D, ByteCode.EQUAL});
                cache.writeSafety(IDDescriptionMap.getKeyByIndex(i));
                cache.writeSafety(new byte[]{ByteCode.COMMA, ByteCode.d, ByteCode.e, ByteCode.s, ByteCode.c, ByteCode.r, ByteCode.i,
                        ByteCode.p, ByteCode.t, ByteCode.i, ByteCode.o, ByteCode.n, ByteCode.EQUAL});
                cache.writeSafety(IDDescriptionMap.getByIndex(i));
                res.add(cache.toByteCode().asUnmodifiable());
                cache.reset();
            }
            cache.close();
            return res;
        }

        public Array<Entry<ByteCode, ByteCode>> IDDescriptionEntries() {
            Array<Entry<ByteCode, ByteCode>> res = new Array<>();
            for (int i = 0; i < IDDescriptionMap.size(); i++) {
                res.add(new Entry<>(IDDescriptionMap.getKeyByIndex(i), IDDescriptionMap.getByIndex(i)));
            }
            return res;
        }

        public void clear() {
            IDDescriptionMap = new ReusableMap<>();
        }

        /**
         * here we keep a fake unmodified mode, just store its citations
         */
        public FilterConfig asUnmodifiedMode() {
            FilterConfig filterConfig = new FilterConfig();
            filterConfig.IDDescriptionMap.putAll(IDDescriptionMap);
            return filterConfig;
        }
    }

    public static class InfoConfig {
        ByteCodeArray typeArray = new ByteCodeArray();
        ByteCodeArray numberArray = new ByteCodeArray();
        ByteCodeArray descriptionArray = new ByteCodeArray();
        CallableSet<ByteCode> indexableIDSet = new CallableSet<>();
        Array<ReusableMap<ByteCode, ByteCode>> otherFields = new Array<>();
        private static final Pattern pattern = Pattern.compile("##INFO=<ID=(?<id>\\w+),(?<fields>(?:[^,>]+=[^,>]+,?)+)Description=\"(?<description>[^\"]+)\">");

        public void add(ByteCode src) {
            Matcher matcher = pattern.matcher(src.toString());
            if (matcher.find()) {
                String id = matcher.group("id");
                String fields = matcher.group("fields");
                String description = matcher.group("description");
                boolean type = false;
                boolean number = false;
                if (id == null) {
                    indexableIDSet.add(ByteCode.EMPTY);
                } else {
                    indexableIDSet.add(new ByteCode(id));
                }
                if (description == null) {
                    descriptionArray.add(ByteCode.EMPTY);
                } else {
                    descriptionArray.add(new ByteCode(description));
                }
                BaseArray<ByteCode> split = new ByteCode(fields).asUnmodifiable().split(ByteCode.COMMA);
                ReusableMap<ByteCode, ByteCode> otherField = new ReusableMap<>();
                otherFields.add(otherField);
                for (ByteCode byteCode : split) {
                    if (byteCode.length() == 0) {
                        continue;
                    }
                    BaseArray<ByteCode> split1 = byteCode.split(ByteCode.EQUAL);
                    if (split1.isEmpty()) {
                        continue;
                    }
                    if (split1.get(0).equals(new byte[]{ByteCode.T, ByteCode.y, ByteCode.p, ByteCode.e})) {
                        typeArray.add(split1.get(1));
                        type = true;
                    } else if (split1.get(0).equals(new byte[]{ByteCode.N, ByteCode.u, ByteCode.m, ByteCode.b, ByteCode.e, ByteCode.r})) {
                        numberArray.add(split1.get(1));
                        number = true;
                    } else {
                        otherField.put(split1.get(0), split1.get(1));
                    }
                }
                if (!type) {
                    typeArray.add(ByteCode.EMPTY);
                }
                if (!number) {
                    numberArray.add(ByteCode.EMPTY);
                }
            }
        }

        public ByteCode getType(ByteCode ID) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : typeArray.get(index);
        }

        public ByteCode getNumber(ByteCode ID) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : numberArray.get(index);
        }

        public ByteCode getDescription(ByteCode ID) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : descriptionArray.get(index);
        }

        public ByteCode getOther(ByteCode ID, ByteCode key) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : otherFields.get(index).get(key);
        }

        public CallableSet<ByteCode> getIndexableIDSet() {
            return indexableIDSet;
        }

        public ByteCodeArray getToStringArray() {
            int size = indexableIDSet.size();
            if (size == 0) {
                return ByteCodeArray.wrap(new ByteCode[0]);
            }
            ByteCodeArray res = new ByteCodeArray();
            VolumeByteStream cache = new VolumeByteStream();
            DefaultValueWrapper<ByteCode> wrapper = DefaultValueWrapper.emptyBytecodeWrapper;
            for (int i = 0; i < size; i++) {
                // id
                cache.writeSafety(new ByteCode(new byte[]{ByteCode.i, ByteCode.d, ByteCode.EQUAL}));
                cache.writeSafety(indexableIDSet.getByIndex(i));
                cache.writeSafety(ByteCode.COMMA);
                // type
                cache.writeSafety(new byte[]{ByteCode.t, ByteCode.y, ByteCode.p, ByteCode.e, ByteCode.EQUAL});
                cache.writeSafety(wrapper.getValue(typeArray.get(i)));
                cache.writeSafety(ByteCode.COMMA);
                // number
                cache.writeSafety(new ByteCode(new byte[]{ByteCode.n, ByteCode.u, ByteCode.m,
                        ByteCode.b, ByteCode.e, ByteCode.r, ByteCode.EQUAL}));
                cache.writeSafety(wrapper.getValue(numberArray.get(i)));
                cache.writeSafety(ByteCode.COMMA);
                // description
                cache.writeSafety(new ByteCode(new byte[]{
                        ByteCode.d, ByteCode.e, ByteCode.s, ByteCode.c, ByteCode.r, ByteCode.i, ByteCode.p,
                        ByteCode.t, ByteCode.i, ByteCode.o, ByteCode.n, ByteCode.EQUAL
                }));
                cache.writeSafety(wrapper.getValue(descriptionArray.get(i)));
                res.add(cache.toByteCode().asUnmodifiable());
                cache.reset();
            }
            cache.close();
            return res;
        }

        public Array<ByteCodeArray> getAllInfoConfig() {
            Array<ByteCodeArray> res = new Array<>();
            for (int i = 0; i < indexableIDSet.size(); i++) {
                ByteCodeArray tmp = new ByteCodeArray();
                tmp.add(indexableIDSet.getByIndex(i));
                tmp.add(typeArray.get(i));
                tmp.add(numberArray.get(i));
                tmp.add(descriptionArray.get(i));
                if (!otherFields.get(i).isEmpty()) {
                    for (Map.Entry<ByteCode, ByteCode> byteCodeByteCodeEntry : otherFields.get(i).entrySet()) {
                        tmp.add(byteCodeByteCodeEntry.getKey());
                        tmp.add(byteCodeByteCodeEntry.getValue());
                    }
                }
                res.add(tmp);
            }
            return res;
        }

        public void clear() {
            typeArray.clear();
            numberArray.clear();
            descriptionArray.clear();
            indexableIDSet.clear();
            otherFields.clear();
        }

        public InfoConfig asUnmodified() {
            InfoConfig infoConfig = new InfoConfig();
            infoConfig.typeArray.addAll(typeArray);
            infoConfig.numberArray.addAll(numberArray);
            infoConfig.descriptionArray.addAll(descriptionArray);
            infoConfig.indexableIDSet.addAll(indexableIDSet);
            infoConfig.otherFields.addAll(otherFields);
            return infoConfig;
        }
    }

    public static class FormatConfig {
        ByteCodeArray typeArray = new ByteCodeArray();
        ByteCodeArray numberArray = new ByteCodeArray();
        ByteCodeArray descriptionArray = new ByteCodeArray();
        CallableSet<ByteCode> indexableIDSet = new CallableSet<>();
        Array<ReusableMap<ByteCode, ByteCode>> otherFields = new Array<>();
        private static final Pattern pattern = Pattern.compile("##FORMAT=<ID=(?<id>\\w+),(?<fields>(?:[^,>]+=[^,>]+,?)+)Description=\"(?<description>[^\"]+)\">");

        public void add(ByteCode src) {
            Matcher matcher = pattern.matcher(src.toString());
            if (matcher.find()) {
                String id = matcher.group("id");
                String fields = matcher.group("fields");
                String description = matcher.group("description");
                boolean type = false;
                boolean number = false;
                if (id == null) {
                    indexableIDSet.add(ByteCode.EMPTY);
                } else {
                    indexableIDSet.add(new ByteCode(id));
                }
                if (description == null) {
                    descriptionArray.add(ByteCode.EMPTY);
                } else {
                    descriptionArray.add(new ByteCode(description));
                }
                BaseArray<ByteCode> split = new ByteCode(fields).asUnmodifiable().split(ByteCode.COMMA);
                ReusableMap<ByteCode, ByteCode> otherField = new ReusableMap<>();
                otherFields.add(otherField);
                for (ByteCode byteCode : split) {
                    if (byteCode.length() == 0) {
                        continue;
                    }
                    BaseArray<ByteCode> split1 = byteCode.split(ByteCode.EQUAL);
                    if (split1.isEmpty()) {
                        continue;
                    }
                    if (split1.get(0).equals(new byte[]{ByteCode.T, ByteCode.y, ByteCode.p, ByteCode.e})) {
                        typeArray.add(split1.get(1));
                        type = true;
                    } else if (split1.get(0).equals(new byte[]{ByteCode.N, ByteCode.u, ByteCode.m, ByteCode.b, ByteCode.e, ByteCode.r})) {
                        numberArray.add(split1.get(1));
                        number = true;
                    } else {
                        otherField.put(split1.get(0), split1.get(1));
                    }
                }
                if (!type) {
                    typeArray.add(ByteCode.EMPTY);
                }
                if (!number) {
                    numberArray.add(ByteCode.EMPTY);
                }
            }
        }

        public CallableSet<ByteCode> getIndexableIDSet() {
            return indexableIDSet;
        }

        public ByteCode getType(ByteCode ID) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : typeArray.get(index);
        }

        public ByteCode getNumber(ByteCode ID) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : numberArray.get(index);
        }

        public ByteCode getDescription(ByteCode ID) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : descriptionArray.get(index);
        }

        public ByteCode getOther(ByteCode ID, ByteCode key) {
            int index = indexableIDSet.indexOfValue(ID);
            return index == -1 ? null : otherFields.get(index).get(key);
        }

        public ByteCodeArray getToStringArray() {
            int size = indexableIDSet.size();
            if (size == 0) {
                return ByteCodeArray.wrap(new ByteCode[0]);
            }
            ByteCodeArray res = new ByteCodeArray();
            VolumeByteStream cache = new VolumeByteStream();
            DefaultValueWrapper<ByteCode> wrapper = DefaultValueWrapper.emptyBytecodeWrapper;
            for (int i = 0; i < size; i++) {
                // id
                cache.writeSafety(new ByteCode(new byte[]{ByteCode.i, ByteCode.d, ByteCode.EQUAL}));
                cache.writeSafety(indexableIDSet.getByIndex(i));
                cache.writeSafety(ByteCode.COMMA);
                // type
                cache.writeSafety(new byte[]{ByteCode.t, ByteCode.y, ByteCode.p, ByteCode.e, ByteCode.EQUAL});
                cache.writeSafety(wrapper.getValue(typeArray.get(i)));
                cache.writeSafety(ByteCode.COMMA);
                // number
                cache.writeSafety(new ByteCode(new byte[]{ByteCode.n, ByteCode.u, ByteCode.m,
                        ByteCode.b, ByteCode.e, ByteCode.r, ByteCode.EQUAL}));
                cache.writeSafety(wrapper.getValue(numberArray.get(i)));
                cache.writeSafety(ByteCode.COMMA);
                // description
                cache.writeSafety(new ByteCode(new byte[]{
                        ByteCode.d, ByteCode.e, ByteCode.s, ByteCode.c, ByteCode.r, ByteCode.i, ByteCode.p,
                        ByteCode.t, ByteCode.i, ByteCode.o, ByteCode.n, ByteCode.EQUAL
                }));
                cache.writeSafety(wrapper.getValue(descriptionArray.get(i)));
                res.add(cache.toByteCode().asUnmodifiable());
                cache.reset();
            }
            cache.close();
            return res;
        }

        public Array<ByteCodeArray> getAllInfoConfig() {
            Array<ByteCodeArray> res = new Array<>();
            for (int i = 0; i < indexableIDSet.size(); i++) {
                ByteCodeArray tmp = new ByteCodeArray();
                tmp.add(indexableIDSet.getByIndex(i));
                tmp.add(typeArray.get(i));
                tmp.add(numberArray.get(i));
                tmp.add(descriptionArray.get(i));
                if (!otherFields.get(i).isEmpty()) {
                    for (Map.Entry<ByteCode, ByteCode> byteCodeByteCodeEntry : otherFields.get(i).entrySet()) {
                        tmp.add(byteCodeByteCodeEntry.getKey());
                        tmp.add(byteCodeByteCodeEntry.getValue());
                    }
                }
                res.add(tmp);
            }
            return res;
        }

        public void clear() {
            typeArray.clear();
            numberArray.clear();
            descriptionArray.clear();
            indexableIDSet.clear();
            otherFields.clear();
        }

        public FormatConfig asUnmodified() {
            FormatConfig formatConfig = new FormatConfig();
            formatConfig.typeArray.addAll(typeArray);
            formatConfig.numberArray.addAll(numberArray);
            formatConfig.descriptionArray.addAll(descriptionArray);
            formatConfig.indexableIDSet.addAll(indexableIDSet);
            formatConfig.otherFields.addAll(otherFields);
            return formatConfig;
        }
    }

    public KVConfig getKVConfig() {
        return kvConfig;
    }

    public Contig getContig() {
        return contig;
    }

    public AltConfig getAltConfig() {
        return altConfig;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public InfoConfig getInfoConfig() {
        return infoConfig;
    }

    public FormatConfig getFormatConfig() {
        return formatConfig;
    }

    public Array<Subject> getSubjectArray() {
        return subjectArray;
    }

    public static CallableSet<HeaderFeature> getNoneInfoField() {
        return noneInfoField;
    }

    public ByteCodeArray getContigIDArray() {
        return contig.getIDs();
    }

    public void loadChrFromContig() {
        for (ByteCode id : contig.getIDs()) {
            Chromosome.add(id.toString());
        }
    }

    public void addKVConfig(ByteCode kvByteCode) {
        BaseArray<ByteCode> split = kvByteCode.split(ByteCode.EQUAL);
        kvConfig.kvMap.put(split.get(0).asUnmodifiable(), split.get(1).asUnmodifiable());
    }

    public void addContig(ByteCode contigByteCode) {
        BaseArray<ByteCode> split = contigByteCode.split(ByteCode.COMMA);
        ByteCode id = split.get(0).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        int length = split.get(1).split(ByteCode.EQUAL).get(1).toInt();
        contig.IDLengthMap.put(id, length);
    }

    public void addAltConfig(ByteCode altByteCode) {
        BaseArray<ByteCode> split = altByteCode.split(ByteCode.COMMA);
        ByteCode id = split.get(0).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode description = split.get(1).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        altConfig.IDDescriptionMap.put(id, description);
    }

    public void addFilterConfig(ByteCode filterByteCode) {
        BaseArray<ByteCode> split = filterByteCode.split(ByteCode.COMMA);
        ByteCode id = split.get(0).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode description = split.get(1).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        filterConfig.IDDescriptionMap.put(id, description);
    }

    public void addInfoConfig(ByteCode infoByteCode) {
        BaseArray<ByteCode> split = infoByteCode.split(ByteCode.COMMA);
        ByteCode id = split.get(0).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode type = split.get(1).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode number = split.get(2).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode description = split.get(3).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        infoConfig.indexableIDSet.add(id.asUnmodifiable());
        infoConfig.typeArray.add(type.asUnmodifiable());
        infoConfig.numberArray.add(number.asUnmodifiable());
        infoConfig.descriptionArray.add(description.asUnmodifiable());
    }

    public void addFormatConfig(ByteCode formatByteCode) {
        BaseArray<ByteCode> split = formatByteCode.split(ByteCode.COMMA);
        ByteCode id = split.get(0).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode type = split.get(1).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode number = split.get(2).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        ByteCode description = split.get(3).split(ByteCode.EQUAL).get(1).asUnmodifiable();
        formatConfig.indexableIDSet.add(id.asUnmodifiable());
        formatConfig.typeArray.add(type.asUnmodifiable());
        formatConfig.numberArray.add(number.asUnmodifiable());
        formatConfig.descriptionArray.add(description.asUnmodifiable());
    }

    public void reset() {
        contig.clear();
        kvConfig.clear();
        altConfig.clear();
        filterConfig.clear();
        infoConfig.clear();
        formatConfig.clear();
        subjectArray.clear();
        specificInfoFiledMap.clear();
        specificInfoFiledMap = new ReusableMap<>();
    }

    public VCFHeader asUnmodified() {
        VCFHeader vcfHeader = new VCFHeader();
        vcfHeader.contig = contig.asUnmodified();
        vcfHeader.kvConfig = kvConfig.asUnmodified();
        vcfHeader.altConfig = altConfig.asUnmodified();
        vcfHeader.filterConfig = filterConfig.asUnmodifiedMode();
        vcfHeader.infoConfig = infoConfig.asUnmodified();
        vcfHeader.formatConfig = formatConfig.asUnmodified();
        vcfHeader.subjectArray.addAll(subjectArray);
        vcfHeader.specificInfoFiledMap.putAll(specificInfoFiledMap);
        return vcfHeader;
    }
}
