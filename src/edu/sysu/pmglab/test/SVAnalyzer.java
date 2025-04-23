package edu.sysu.pmglab.test;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.GZIPInputStream;


public class SVAnalyzer {
    // Map to store SV type counts
    private Map<String, Integer> svTypeCounts = new HashMap<>();
    // Root directory containing VCF files
    private String rootDirectory;

    public SVAnalyzer(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void analyze() throws IOException {
        // Get all .vcf.gz files from the directory
        List<Path> vcfFiles = findVcfFiles(rootDirectory);
        System.out.println("Found " + vcfFiles.size() + " VCF files to analyze");

        // Process each file
        for (Path filePath : vcfFiles) {
            processVcfFile(filePath.toString());
        }

        // Display results
        printResults();
    }

    private List<Path> findVcfFiles(String directory) throws IOException {
        List<Path> vcfFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path entry : stream) {
                if (entry.toString().endsWith(".vcf.gz")) {
                    vcfFiles.add(entry);
                }
            }
        }
        return vcfFiles;
    }

    private void processVcfFile(String filePath) throws IOException {
        System.out.println("Processing file: " + filePath);
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fileStream = new FileStream(filePath, FileStream.GZIP_READER);

        int lineNumber = 0;

        while (fileStream.readLine(cache) != -1) {
            lineNumber++;
            String line = cache.toByteCode().toString();
            cache.reset();

            // Skip header lines (starting with #)
            if (!line.startsWith("#")) {
                processSVLine(line, filePath, lineNumber);
            }
        }

        fileStream.close();
    }

    private void processSVLine(String line, String filePath, int lineNumber) {
        // Split the line by tabs to get columns
        String[] columns = line.split("\t");

        // Ensure we have enough columns
        if (columns.length < 8) {
            System.err.println("Warning: Line " + lineNumber + " in file " + filePath + " has insufficient columns");
            return;
        }

        // Extract the INFO column (index 7)
        String infoColumn = columns[7];

        // Search for SVTYPE in INFO column
        Pattern pattern = Pattern.compile("SVTYPE=([^;]+)");
        Matcher matcher = pattern.matcher(infoColumn);

        if (matcher.find()) {
            String svType = matcher.group(1);
            // Update counts
            svTypeCounts.put(svType, svTypeCounts.getOrDefault(svType, 0) + 1);
        } else {
            // No SVTYPE found - report error and exit
            System.err.println("Error: No SVTYPE field found in file " + filePath + " at line " + lineNumber);
            System.err.println("Line content: " + line);
            System.exit(1);
        }
    }

    private void printResults() {
        System.out.println("\n----- SV Type Count Results -----");
        int total = 0;

        // Sort by SV type
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(svTypeCounts.entrySet());
        Collections.sort(sortedEntries, Map.Entry.comparingByKey());

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            total += entry.getValue();
        }

        System.out.println("--------------------------------");
        System.out.println("Total SVs analyzed: " + total);
    }

    public static void main(String[] args) {
        String rootDir = "/Users/pwj/Desktop/SV/data/pmglab/1100VCF";

        // Allow custom directory via command-line argument
        if (args.length > 0) {
            rootDir = args[0];
        }

        try {
            SVAnalyzer analyzer = new SVAnalyzer(rootDir);
            analyzer.analyze();
        } catch (IOException e) {
            System.err.println("Error processing VCF files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// Note: These classes would need to be implemented or imported from your existing codebase
class VolumeByteStream {
    // Implementation for your existing class
    private ByteArrayOutputStream outputStream;

    public VolumeByteStream() {
        outputStream = new ByteArrayOutputStream();
    }

    public ByteBuffer toByteCode() {
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    public void reset() {
        outputStream.reset();
    }

    // Other required methods
}

class ByteBuffer {
    private byte[] buffer;

    public static ByteBuffer wrap(byte[] array) {
        ByteBuffer buffer = new ByteBuffer();
        buffer.buffer = array;
        return buffer;
    }

    public String toString() {
        return new String(buffer);
    }

    // Other required methods
}

class FileStream {
    public static final int GZIP_READER = 1;
    private InputStream inputStream;
    private BufferedReader reader;

    public FileStream(String filePath, int mode) throws IOException {
        if (mode == GZIP_READER) {
            this.inputStream = new GZIPInputStream(new FileInputStream(filePath));
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        } else {
            this.inputStream = new FileInputStream(filePath);
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        }
    }

    public int readLine(VolumeByteStream cache) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return -1;
        }

        // Assuming there's a method to write to the cache
        // This would need to be implemented based on your existing code
        writeToCache(line, cache);
        return line.length();
    }

    private void writeToCache(String line, VolumeByteStream cache) {
        // Implementation depends on your actual VolumeByteStream class
        // This is a placeholder
    }

    public void close() throws IOException {
        reader.close();
        inputStream.close();
    }
}