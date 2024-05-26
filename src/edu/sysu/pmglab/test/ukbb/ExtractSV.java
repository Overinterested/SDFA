package edu.sysu.pmglab.test.ukbb;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractSV {
    public static void main(String[] args) throws IOException {
        // Specify the directory containing the files
        String directoryPath = "/Users/wenjiepeng/Desktop/SV/data/ukbb";

        // Get all files in the directory
        File directory = new File(directoryPath);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".trace"));
        if (files != null) {
            CallableSet<File> indexableFiles = new CallableSet<>(files);
            Array<IntArray> countArrayOfFiles = new Array<>(files.length);
            for (File file : files) {
                IntArray tmp = new IntArray();
                processFile(file,tmp);
                countArrayOfFiles.add(tmp);
            }
            FileStream fs = new FileStream("/Users/wenjiepeng/Desktop/SV/data/ukbb/extract/chr_summmary",FileStream.DEFAULT_WRITER);
            int count = 0;
            for (File indexableFile : indexableFiles) {
                fs.write(String.valueOf(extractAndPrintChrNumber(indexableFile)));
                fs.write(ByteCode.TAB);
                IntArray tmpFileCount = countArrayOfFiles.get(count);
                long sum = tmpFileCount.sum();
                fs.write(ValueUtils.Value2Text.long2bytes(sum));
                fs.write(ByteCode.NEWLINE);
                count++;
            }
            fs.close();
        }
    }

    private static void processFile(File file, IntArray tmp) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                tmp.add(extractAndPrintSVs(line));
            }
        } catch (IOException e) {
            return;
        }
    }

    private static int extractAndPrintSVs(String line) {
        String containsKeyword = "contains";
        String svsKeyword = "SVs";
        int containsIndex = line.indexOf(containsKeyword);
        int svsIndex = line.indexOf(svsKeyword);
        if (containsIndex != -1 && svsIndex != -1 && containsIndex < svsIndex) {
            int startIndex = containsIndex + containsKeyword.length();
            String numberStr = line.substring(startIndex, svsIndex).trim();
            try {
                return Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    private static String extractAndPrintChrNumber(File file) {
        String filePath = file.getAbsolutePath();
        String regex = "chr(\\d+|X)_";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(filePath);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }
}
