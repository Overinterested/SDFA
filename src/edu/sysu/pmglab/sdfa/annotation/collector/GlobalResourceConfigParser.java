package edu.sysu.pmglab.sdfa.annotation.collector;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.sdfa.annotation.genome.GenomeOutputFrame;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.annotation.toolkit.OutputFrame;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.AnnotationFunctionSummary;
import edu.sysu.pmglab.sdfa.annotation.toolkit.function.AnnotationOutputFunction;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Wenjie Peng
 * @create 2024-04-03 19:18
 * @description
 */
public class GlobalResourceConfigParser {

    private static int resourceIndexRecord = 0;
    static ByteCode header = new ByteCode(new byte[]{91, 91, ByteCode.a, ByteCode.n,
            ByteCode.n, ByteCode.o, ByteCode.t, ByteCode.a, ByteCode.t, ByteCode.i, ByteCode.o, ByteCode.n, 93, 93});
    static ByteCode file_header = new ByteCode(new byte[]{ByteCode.f, ByteCode.i, ByteCode.l, ByteCode.e});
    static ByteCode type_header = new ByteCode(new byte[]{ByteCode.t, ByteCode.y, ByteCode.p, ByteCode.e});
    static ByteCode columnsIndexHeader = new ByteCode(new byte[]{ByteCode.c, ByteCode.o, ByteCode.l, ByteCode.u, ByteCode.m, ByteCode.n, ByteCode.s});
    static ByteCode fields_header = new ByteCode(new byte[]{ByteCode.f, ByteCode.i, ByteCode.e, ByteCode.l, ByteCode.d, ByteCode.s});
    static ByteCode names_header = new ByteCode(new byte[]{ByteCode.n, ByteCode.a, ByteCode.m, ByteCode.e, ByteCode.s});
    static ByteCode opt_header = new ByteCode(new byte[]{ByteCode.o, ByteCode.p, ByteCode.t, ByteCode.s});
    static Pattern numberPattern = Pattern.compile("\\d+");
    static Pattern colsPattern = Pattern.compile("\"([^\"]*)\"");


    public static void register(Object filePath) throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(filePath.toString(), FileStream.DEFAULT_READER);
        Array<AbstractResourceManager> managerArray = new Array<>();
        while (fs.readLine(cache) != -1) {
            if (cache.toByteCode().startsWith(header)) {
                cache.reset();
                File file = null;
                String type = null;
                boolean loadColIndex = false;
                IntArray rawIndexOfLoad = null;
                ByteCodeArray outputFieldArray = null;
                OutputFrame tmpOutputFrame = new OutputFrame();
                Array<ByteCodeArray> rawColNameArrayOfLoad = null;
                Array<Class<? extends AnnotationOutputFunction>> optArray = null;
                //region parse tag
                while (fs.readLine(cache) != -1) {
                    ByteCode line = cache.toByteCode();
                    if (line.startsWith(file_header)) {
                        file = new File(extractFile(line.toString()));
                        cache.reset();
                        continue;
                    }
                    if (line.startsWith(type_header)) {
                        type = extractFile(line.toString());
                        cache.reset();
                        if (type.equals(AbstractResourceManager.GENOME_ANNOTATION_RESOURCE)) {
                            break;
                        }
                        continue;
                    }
                    if (line.startsWith(columnsIndexHeader)) {
                        loadColIndex = true;
                        rawIndexOfLoad = extractCols(line.toString());
                        cache.reset();
                        continue;
                    }
                    if (line.startsWith(fields_header)) {
                        loadColIndex = false;
                        rawColNameArrayOfLoad = extractFields(line.toString());
                        cache.reset();
                        continue;
                    }
                    if (line.startsWith(names_header)) {
                        outputFieldArray = extractColNames(line.toString());
                        cache.reset();
                        continue;
                    }
                    if (line.startsWith(opt_header)) {
                        optArray = new Array<>();
                        ByteCodeArray functionArray = extractColNames(line.toString());
                        for (int i = 0; i < functionArray.size(); i++) {
                            ByteCode function = functionArray.get(i);
                            //region concat2concatN logic
                            if (function.equals("concat") && !loadColIndex) {
                                assert rawColNameArrayOfLoad != null;
                                if (rawColNameArrayOfLoad.get(i).size() > 1) {
                                    function = new ByteCode("concatN").asUnmodifiable();
                                }
                            }
                            //endregion
                            //region string2functionClass
                            Class<? extends AnnotationOutputFunction> functionClass = AnnotationFunctionSummary.getFunction(function);
                            if (functionClass == null) {
                                throw new UnsupportedOperationException(function + " function is not defined.");
                            }
                            optArray.add(functionClass);
                            //endregion
                        }
                        cache.reset();
                        break;
                    }
                }
                //endregion
                assert type != null;
                AbstractResourceManager tmpResourceManager = AbstractResourceManager.of(file, type)
                        .setResourceIndex(resourceIndexRecord++);
                if (loadColIndex) {
                    tmpOutputFrame.setRawIndexOfOutputCol(rawIndexOfLoad);
                } else {
                    tmpOutputFrame.setRawNamesInEachOutputCol(rawColNameArrayOfLoad);
                }
                if (optArray != null) {
                    tmpOutputFrame.setOutputColFunctionArray(optArray);
                }
                tmpOutputFrame.setOutputColNameArray(outputFieldArray);
                tmpResourceManager.setOutputFrame(tmpOutputFrame);
                if (tmpResourceManager instanceof RefGeneManager){
                    tmpResourceManager.setOutputFrame(new GenomeOutputFrame());
                }
                managerArray.add(tmpResourceManager);
                cache.reset();
            }
        }
        fs.close();
        cache.close();
        GlobalResourceManager.getInstance().setResourceManagerSet(managerArray);
    }

    private static String extractFile(String file) {
        int start = file.indexOf("\"");
        int end = file.lastIndexOf("\"");

        // 提取出双引号中的内容
        if (start != -1 && end != -1 && start < end) {
            return file.substring(start + 1, end);
        } else {
            // 如果找不到双引号，返回空字符串或者其他默认值，根据需要进行调整
            return "";
        }
    }

    private static Array<ByteCodeArray> extractFields(String cols) {
        Matcher matcher = colsPattern.matcher(cols);
        Array<ByteCodeArray> fieldNames = new Array<>();
        while (matcher.find()) {
            String fields = matcher.group(1);
            ByteCodeArray col = new ByteCodeArray();
            String[] fieldsArray = fields.split(",");
            col.addAll(fieldsArray);
            fieldNames.add(col);
        }
        return fieldNames;
    }

    private static IntArray extractCols(String cols) {
        Matcher matcher = numberPattern.matcher(cols);
        IntArray fieldIndexArray = new IntArray();
        while (matcher.find()) {
            String fieldIndex = matcher.group();
            fieldIndexArray.add(Integer.parseInt(fieldIndex));
        }
        return fieldIndexArray;
    }

    private static ByteCodeArray extractColNames(String colNames) {
        Matcher matcher = colsPattern.matcher(colNames);
        ByteCodeArray outputCols = new ByteCodeArray();
        while (matcher.find()) {
            String fields = matcher.group(1);
            outputCols.add(fields);
        }
        return outputCols;
    }

}
