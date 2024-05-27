package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.toolkit.CCFSorter;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.easytools.container.GlobalTemporaryContainer;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFEncode;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFWriter;
import edu.sysu.pmglab.sdfa.sv.SVCoordinate;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.sv.vcf.ReusableVCFPool;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFileLatest;
import org.slf4j.Logger;

import java.io.IOException;


public class VCF2SDF {
    int fileID;
    Logger logger;
    int encodeMode;
    SDFEncode encode;
    final File vcfFile;
    final File sdfFile;
    boolean silent = false;
    boolean storeMeta = false;
    boolean globalContig = false;
    public SVFilterManager filter;
    public static boolean dropFormat = false;
    public static boolean poolStrategy = false;
    public static boolean dropRefField = false;
    public static boolean dropInfoField = false;
    public static boolean dropQualField = false;
    public static boolean dropFilterField = false;
    public static boolean multiInfoForCSV = true;
    public static boolean lineExtractAndSort = false;


    public VCF2SDF(Object vcfFile, Object sdfFile) {
        this.sdfFile = new File(sdfFile.toString());
        this.vcfFile = new File(vcfFile.toString());
    }

    public VCF2SDF setEncodeMode(int encodeMode) {
        this.encodeMode = encodeMode;
        this.encode = new SDFEncode(encodeMode);
        return this;
    }

    public File convert() throws IOException {
        VCFFileLatest vcfFileInstance = null;
        try {
            if (poolStrategy) {
                vcfFileInstance = ReusableVCFPool.getInstance().getReusableSVArray();
            } else {
                vcfFileInstance = new VCFFileLatest();
            }
            if (filter != null) {
                filter.init();
            }
            if (encode == null) {
                encode = new SDFEncode(encodeMode);
            }
            vcfFileInstance.setFilter(filter)
                    .setSDFEncode(encode)
                    .setFilePath(vcfFile)
                    .setFileID(fileID);
            if (lineExtractAndSort) {
                SDFWriter writer = SDFWriter.of(sdfFile, encodeMode);
                vcfFileInstance.setWriter(writer.getWriter());
                IntArray chrSizeRecord = new IntArray(true);
                for (int i = 0; i < Chromosome.support().size(); i++) {
                    chrSizeRecord.add(0);
                }
                vcfFileInstance.setChrIndexBlock(chrSizeRecord);
                vcfFileInstance.parse();
                long sdfSize = chrSizeRecord.sum();
                if (sdfSize == 0) {
                    logger.trace(vcfFile.getAbsolutePath() + " has no SV.");
                    return null;
                }
                // write meta
                SDFMeta meta = SDFMeta.decode(vcfFileInstance).initChrBlockRange(chrSizeRecord.toBaseArray());
                writer.writeMeta(meta);
                writer.close();
                new CCFSorter<SVCoordinate.CoordinateInterval>(sdfFile, sdfFile)
                        .setComparableFields("Coordinate")
                        .setElementMapper(record -> SVCoordinate.CoordinateInterval.decode(record.get(0)),
                                SVCoordinate.CoordinateInterval::getChr)
                        .setRefinedBucketSize((int) Math.sqrt(sdfSize))
                        .submit();
                if (globalContig) {
                    GlobalVCFContigConvertor.Builder.getInstance().addVCFContig(vcfFileInstance.getContig());
                }
                SDFMeta copy;
                if (storeMeta) {
                    copy = meta.asUnmodified();
                    GlobalTemporaryContainer.add(sdfFile, copy);
                }
                meta.clear();
                if (logger != null && !silent) {
                    vcfFileInstance.closeInfo(logger);
                }
                return sdfFile;
            }
            vcfFileInstance.setFilter(filter)
                    .setSDFEncode(encode)
                    .setFilePath(vcfFile)
                    .setFileID(fileID)
                    .parse();
            Array<IRecord> loadEncodeSVArray = vcfFileInstance.getLoadUnifiedSVArray();
            if (loadEncodeSVArray == null || loadEncodeSVArray.isEmpty()) {
                logger.trace(vcfFile.getAbsolutePath() + " has no SV.");
                return null;
            }
            loadEncodeSVArray.sort(((o1, o2) -> {
                int[] i1 = o1.get(0);
                int[] i2 = o2.get(0);
                int status = Integer.compare(i1[0], i2[0]);
                if (status == 0) {
                    status = Integer.compare(i1[1], i2[1]);
                    return status == 0 ? Integer.compare(i1[2], i2[2]) : status;
                }
                return status;
            }));
            SDFWriter writer = SDFWriter.of(sdfFile, encodeMode);
            int[] chrSizeRecord = new int[vcfFileInstance.numOfContig()];
            // write sv
            int size = loadEncodeSVArray.size();
            for (int i = 0; i < size; i++) {
                IRecord record = loadEncodeSVArray.get(i);
                int[] coordinate = record.get(0);
                chrSizeRecord[coordinate[0]]++;
                writer.write(record);
                record.clear();
            }
            // write meta
            SDFMeta meta = SDFMeta.decode(vcfFileInstance).initChrBlockRange(chrSizeRecord);
            writer.writeMeta(meta);
            writer.close();

            if (globalContig) {
                GlobalVCFContigConvertor.Builder.getInstance().addVCFContig(vcfFileInstance.getContig());
            }
            SDFMeta copy;
            if (storeMeta) {
                copy = meta.asUnmodified();
                GlobalTemporaryContainer.add(sdfFile, copy);
            }
            meta.clear();
            if (logger != null && !silent) {
                vcfFileInstance.closeInfo(logger);
            }
        } catch (Exception e) {
            System.out.println(vcfFile);
            e.printStackTrace();
        } finally {
            if (poolStrategy) {
                if (vcfFileInstance != null) {
                    vcfFileInstance.cycle();
                }
            }
        }
        return sdfFile;
    }

    public VCF2SDF setFileID(int fileID) {
        this.fileID = fileID;
        return this;
    }

    public File getSDFFile() {
        return sdfFile;
    }

    public File getVCFFile() {
        return vcfFile;
    }


    public VCF2SDF storeMeta(boolean storeMeta) {
        this.storeMeta = storeMeta;
        return this;
    }

    public static void dropPool() {
        poolStrategy = false;
    }

    public VCF2SDF globalContig(boolean isGlobalContig) {
        this.globalContig = isGlobalContig;
        return this;
    }

    public VCF2SDF setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public VCF2SDF setFilter(SVFilterManager filter) {
        this.filter = filter;
        return this;
    }

    /**
     * note the first GT field has been extracted
     *
     * @param encodeFormat encode method
     * @return this
     */
    public VCF2SDF setFormatEncode(FieldType encodeFormat) {
        encode.getExtendedEncodeMethods().set(0, encodeFormat);
        return this;
    }

    public VCF2SDF setIDEncode(FieldType encodeID) {
        encode.getExtendedEncodeMethods().set(1, encodeID);
        return this;
    }

    public VCF2SDF setRefEncode(FieldType encodeRef) {
        encode.getExtendedEncodeMethods().set(2, encodeRef);
        return this;
    }

    public VCF2SDF setAltEncode(FieldType encodeAlt) {
        encode.getExtendedEncodeMethods().set(3, encodeAlt);
        return this;
    }

    public VCF2SDF setQualEncode(FieldType encodeQual) {
        encode.getExtendedEncodeMethods().set(4, encodeQual);
        return this;
    }

    public VCF2SDF setFilterEncode(FieldType encodeFilter) {
        encode.getExtendedEncodeMethods().set(5, encodeFilter);
        return this;
    }

    public VCF2SDF setInfoEncode(FieldType encodeInfoValues) {
        encode.getExtendedEncodeMethods().set(6, encodeInfoValues);
        return this;
    }

}
