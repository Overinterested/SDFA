package edu.sysu.pmglab.sdfa.sv.bed;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFWriter;
import edu.sysu.pmglab.sdfa.sv.*;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.HashSet;

/**
 * @author Wenjie Peng
 * @create 2024-05-05 21:32
 * @description
 */
public class BEDToSDF {
    final File bedFile;
    final File outputFile;

    private BEDToSDF(File bedFile, File outputDir) {
        this.bedFile = bedFile;
        this.outputFile = outputDir.getSubFile(bedFile.getName() + ".sdf");
    }

    public static BEDToSDF of(File bedFile, File outputDir) {
        return new BEDToSDF(bedFile, outputDir);
    }

    public void convert() throws IOException {
        boolean containHeader = false;
        boolean containColumns = false;
        ByteCodeArray header = new ByteCodeArray();
        ByteCodeArray columns = new ByteCodeArray();
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(bedFile, FileStream.DEFAULT_READER);
        while (fs.readLine(cache) != -1) {
            if (cache.startWith(new byte[]{ByteCode.NUMBER_SIGN, ByteCode.NUMBER_SIGN})) {
                containHeader = true;
                header.add(cache.toUnmodifiableByteCode());
                cache.reset();
                continue;
            }
            if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                containColumns = true;
                BaseArray<ByteCode> split = cache.toByteCode().split(ByteCode.TAB);
                // chr start end length type
                for (int i = 5; i < split.size(); i++) {
                    columns.add(split.get(i).asUnmodifiable());
                }
                cache.reset();
                continue;
            }
            break;
        }
        Array<UnifiedSV> svArray = new Array<>();
        ContigBlockContainer contigBlock = new ContigBlockContainer();
        HashSet<ByteCode> invalidType = new HashSet<>();
        do {
            BaseArray<ByteCode> lineSplit = cache.toByteCode().split(ByteCode.TAB);
            Chromosome chr = contigBlock.getChromosomeByName(lineSplit.get(0));
            int start;
            int end;
            int length;
            try {
                start = lineSplit.get(1).toInt();
                end = lineSplit.get(2).toInt();
                length = Math.abs(lineSplit.get(3).toInt());
                SVTypeSign svTypeSign = SVTypeSign.get(lineSplit.get(4));
                if (svTypeSign == SVTypeSign.unknown || svTypeSign.isComplex()) {
                    invalidType.add(lineSplit.get(4).asUnmodifiable());
                    cache.reset();
                    continue;
                }
                int featureSize = containColumns ? columns.size() : lineSplit.size() - 5;
                ByteCodeArray features = new ByteCodeArray(featureSize);
                for (int i = 5; i < lineSplit.size(); i++) {
                    features.add(lineSplit.get(i));
                }
                svArray.add(new UnifiedSV().setType(svTypeSign)
                        .setCoordinate(new SVCoordinate(start, end, chr))
                        .setLength(length)
                        .setSpecificInfoField(ByteCodeArray.wrap(new ByteCode[]{features.encode().toByteCode()}))
                        .setGenotypes(new SVGenotypes(new SVGenotype[0])));
            } catch (NumberFormatException e) {
                cache.reset();
                continue;
            }
        } while (fs.readLine(cache) != -1);
        fs.close();
        int lineCount = 0;
        svArray.sort(UnifiedSV::compareTo);
        int[] contigSize = new int[contigBlock.getAllChromosomes().size()];
        SDFWriter writer = SDFWriter.of(outputFile, 2);
        while(!svArray.isEmpty()){
            UnifiedSV unifiedSV = svArray.popFirst();
            contigSize[unifiedSV.getChr().getIndex()]++;
            unifiedSV.setCSVLocation(new CSVLocation(lineCount++)).setSpecificInfoField((ByteCodeArray) ArrayType.decode(unifiedSV.getSpecificInfoField().get(0)));
            writer.write(unifiedSV);
        }
        SDFMeta meta = new SDFMeta().setContigBlockContainer(contigBlock).initChrBlockRange(contigSize);
        if (containHeader){
            for (int i = 0; i < header.size(); i++) {
                meta.add("head",header.get(i));
            }
        }
        if (containColumns){
            meta.setInfoFieldSet(new CallableSet<>(columns));
        }
        writer.writeMeta(meta.write());
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        BEDToSDF.of(
                new File("/Users/wenjiepeng/Desktop/SV/AnnotFile/KnownSV/SVAFotate_core_SV_popAFs.GRCh38.bed"),
                new File("/Users/wenjiepeng/Desktop/SV/AnnotFile")).convert();
    }
}
