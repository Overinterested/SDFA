package edu.sysu.pmglab.sdfa.toolkit;

import java.io.*;
import java.util.*;

public class VcfToBedConverter {

    private static final byte[] BED_HEADER = {0x6C, 0x1B, 0x01};

    public static void main(String[] args) {
        String vcfFilePath = "path/to/input.vcf";
        String bedFilePath = "path/to/output.bed";
        String bimFilePath = "path/to/output.bim";
        String famFilePath = "path/to/output.fam";

        try {
            convertVcfToBed(vcfFilePath, bedFilePath, bimFilePath, famFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertVcfToBed(String vcfFilePath, String bedFilePath, String bimFilePath, String famFilePath) throws IOException {
        try (BufferedReader vcfReader = new BufferedReader(new FileReader(vcfFilePath));
             DataOutputStream bedWriter = new DataOutputStream(new FileOutputStream(bedFilePath));
             BufferedWriter bimWriter = new BufferedWriter(new FileWriter(bimFilePath));
             BufferedWriter famWriter = new BufferedWriter(new FileWriter(famFilePath))) {

            // Write BED header
            bedWriter.write(BED_HEADER);

            String line;
            List<String> sampleIds = new ArrayList<>();
            boolean headerParsed = false;

            while ((line = vcfReader.readLine()) != null) {
                if (line.startsWith("##")) {
                    // Skip metadata lines
                    continue;
                }
                if (line.startsWith("#")) {
                    // Parse header line
                    String[] headerParts = line.split("\t");
                    sampleIds.addAll(Arrays.asList(headerParts).subList(9, headerParts.length));
                    headerParsed = true;
                    writeFamFile(famWriter, sampleIds);
                    continue;
                }
                if (headerParsed) {
                    // Parse VCF data lines
                    String[] parts = line.split("\t");
                    String chrom = parts[0];
                    String snpId = parts[2];
                    int pos = Integer.parseInt(parts[1]);
                    String ref = parts[3];
                    String alt = parts[4];

                    // Write to BIM file
                    bimWriter.write(String.format("%s\t%s\t0\t%d\t%s\t%s%n", chrom, snpId, pos, ref, alt));

                    // Convert genotype data to BED format
                    byte[] genotypeBytes = convertGenotypesToBytes(parts, sampleIds.size());
                    bedWriter.write(genotypeBytes);
                }
            }
        }
    }

    private static void writeFamFile(BufferedWriter famWriter, List<String> sampleIds) throws IOException {
        for (String sampleId : sampleIds) {
            famWriter.write(String.format("0\t%s\t0\t0\t0\t-9%n", sampleId));
        }
    }

    private static byte[] convertGenotypesToBytes(String[] parts, int sampleCount) {
        byte[] genotypeBytes = new byte[(sampleCount + 3) / 4];
        Arrays.fill(genotypeBytes, (byte) 0x00);

        for (int i = 0; i < sampleCount; i++) {
            String genotype = parts[9 + i].split(":")[0];
            byte genoCode;
            switch (genotype) {
                case "0/0":
                    genoCode = 0b00;
                    break;
                case "0/1":
                case "1/0":
                    genoCode = 0b01;
                    break;
                case "1/1":
                    genoCode = 0b11;
                    break;
                default:
                    genoCode = 0b10; // Missing genotype
                    break;
            }
            genotypeBytes[i / 4] |= (genoCode << ((i % 4) * 2));
        }

        return genotypeBytes;
    }
}
