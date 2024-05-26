package edu.sysu.pmglab.sdfa.annotation.base;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.unifyIO.FileStream;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Wenjie Peng
 * @create 2023-10-02 15:06
 * @description
 */
public class ReferenceResourceManager {
    Logger logger;
    public static String version;
    public static File resourceDir;
    Array<RefResource> resources = new Array<>();
    HashSet<String> loadedResourceNames = new HashSet<>();
    public final static HashSet<ByteCode> existRefTag = new HashSet<>();
    static HashMap<String, RefResource> existedRefResource = new HashMap<>();
    private static final ReferenceResourceManager instance = new ReferenceResourceManager();

    static {
        // genome_RefSeq
        existedRefResource.put(
                "genome_RefSeq",
                new RefResource("genome_RefSeq", null, AnnotManagerType.GenomeAnnot).setRelativePath("RefGene/refGene/kggseqv1.1_GRCh38_refGene_no_dup.ccf")
        );
        existRefTag.add(new ByteCode("genome_RefSeq"));
        // genome_GEncode
        existedRefResource.put(
                "genome_GEncode",
                new RefResource("genome_GEncode", null, AnnotManagerType.GenomeAnnot).setRelativePath("RefGene/GEncode/kggseqv1.1_GRCh38_GEncode_no_dup.ccf")
        );
        existRefTag.add(new ByteCode("genome_GEncode"));
        // genome_knownGene
        existedRefResource.put(
                "genome_knownGene",
                new RefResource("genome_knownGene", null, AnnotManagerType.GenomeAnnot).setRelativePath("RefGene/knownGene/kggseqv1.1_GRCh38_knownGene_no_dup.ccf")
        );
        existRefTag.add(new ByteCode("genome_knownGene"));
        // CytoBand
        existedRefResource.put(
                "CytoBand",
                new RefResource("CytoBand", null, AnnotManagerType.IntervalAnnot).setRelativePath("CytoBand/GRCh38_cytoBand.ccf")
        );
        existRefTag.add(new ByteCode("CytoBand"));
        // EnhancerAtlas_ENSEMAL
        existedRefResource.put(
                "EnhancerAtlas_ENSEMAL",
                new RefResource("EnhancerAtlas_ENSEMAL", null, AnnotManagerType.IntervalAnnot).setRelativePath("Regulated_element/EnhanceAtlas/GRCh38_EnhanceAtlas_ENSEMBL.ccf")
        );
        existRefTag.add(new ByteCode("EnhancerAtlas_ENSEMAL"));
        // EnhancerAtlas_RefSeq
        existedRefResource.put(
                "EnhancerAtlas_RefSeq",
                new RefResource("EnhancerAtlas_RefSeq", null, AnnotManagerType.IntervalAnnot).setRelativePath("Regulated_element/EnhanceAtlas/GRCh38_EnhanceAtlas_RefSeq.ccf")
        );
        existRefTag.add(new ByteCode("EnhancerAtlas_RefSeq"));
        // GeneHancer
        existedRefResource.put(
                "GeneHancer",
                new RefResource("GeneHancer", null, AnnotManagerType.IntervalAnnot).setRelativePath("Regulated_element/GeneHancer/GRCh38.ccf")
        );
        existRefTag.add(new ByteCode("GeneHancer"));
        // curatedKnownSV
        existedRefResource.put(
                "CuratedKnownSV",
                new RefResource("CuratedKnownSV", null, AnnotManagerType.KnownSVAnnot).setRelativePath("KnownSV/GRCh38.ccf")
        );
        existRefTag.add(new ByteCode("CuratedKnownSV"));
        // TAD
        existedRefResource.put(
                "TAD",
                new RefResource("TAD", null, AnnotManagerType.IntervalAnnot).setRelativePath("TAD/hg38_merged_TADs.ccf")
        );
        existRefTag.add(new ByteCode("TAD"));
        // repeat
        existedRefResource.put(
                "Repeat",
                new RefResource("Repeat", null, AnnotManagerType.IntervalAnnot).setRelativePath("Repeat/hg38_repeat.ccf")
        );
        existRefTag.add(new ByteCode("Repeat"));

    }

    private ReferenceResourceManager() {
    }

    private ReferenceResourceManager(File resourceDir) {
        this.resourceDir = resourceDir;
    }


    private void parseExtraResource() throws IOException {
        File extraFile = resourceDir.getSubFile("extra");
        if (!extraFile.exists()) {
            return;
        }
        File extraIntervalResource = extraFile.getSubFile("interval");
        if (extraIntervalResource.exists()) {
            parseExtraResource(extraIntervalResource, AnnotManagerType.IntervalAnnot);
        }
        File extraSVResource = extraFile.getSubFile("knownSV");
        if (extraSVResource.exists()) {
            parseExtraResource(extraSVResource, AnnotManagerType.KnownSVAnnot);
        }
    }

    private void parseExistedResource() throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        File resourceConfig = new File(resourceDir.getSubFile("annotation.config"));
//        if (!resourceConfig.exists()) {
//            logger.warn(resourceConfig.getAbsolutePath() + " doesn't exist!");
//            return;
//        }
        FileStream fs = new FileStream(resourceConfig, FileStream.DEFAULT_READER);
        Set<String> existedResourceNames = existedRefResource.keySet();
        while (fs.readLine(cache) != -1) {
//            if (cache.startWith(ConstParam.DOUBLENUMBERSIGN)) {
//                cache.reset();
//            }
            String currResourceName = cache.toByteCode().clone().toString();
            if (existedResourceNames.contains(currResourceName)) {
                RefResource refResource = existedRefResource.get(currResourceName);
                String relativePath = refResource.getRelativePath();
                if (version != null && version.equals("GRCh37")) {
                    relativePath = relativePath.replace("GRCh38", version);
                    refResource.setRelativePath(relativePath);
                }
                File currResourcePath = new File(resourceDir.getSubFile(relativePath));
                if (currResourcePath.exists()) {
                    loadedResourceNames.add(currResourceName);
                    resources.add(refResource.setFile(currResourcePath));
                } else {
//                    logger.error(currResourceName + " is not existed");
                    throw new UnsupportedOperationException();
                }
            }
        }
//        fs.close();
    }

    private void parseExtraResource(File extraItemDir, AnnotManagerType resourceType) throws IOException {
        File[] currPathFileList = extraItemDir.listFiles();
        if (currPathFileList == null || currPathFileList.length == 0) {
            return;
        }
        HashSet<String> loadedResourceNames = new HashSet<>();
        HashMap<String, File> fileNameToFileMap = new HashMap<>();
        Set<String> fileNameSet = new HashSet<>();
        for (File fileItem : currPathFileList) {
            if (fileItem.isDirectory()) {
                continue;
            }
            String tmpName = fileItem.getName();
            fileNameToFileMap.put(tmpName, fileItem);
            fileNameSet.add(tmpName);
        }
        for (String currFileName : fileNameSet) {
            if (currFileName.startsWith(".")) {
                continue;
            }
            if (currFileName.endsWith(".ccf")) {
                loadedResourceNames.add(currFileName);
                RefResource refResource = new RefResource(fileNameToFileMap.get(currFileName), resourceType);
                if (resourceType.equals(AnnotManagerType.IntervalAnnot)) {
                    refResource.setRelativePath("extra/interval/" + currFileName);
                } else if (resourceType.equals(AnnotManagerType.KnownSVAnnot)) {
                    refResource.setRelativePath("extra/knownSV/" + currFileName);
                }
                resources.add(refResource);
            } else {
                // check whether the file is converted
                File tmpFile = new File(currFileName);
                String tmpFilePath = currFileName.replace(tmpFile.getExtension(), "ccf");
                if (loadedResourceNames.contains(tmpFilePath) || fileNameSet.contains(tmpFilePath)) {
                    continue;
                }
//                if (resourceType.equals(AnnotManagerType.KnownSVAnnot)) {
//                    NormalKnownSVFileParser.parseItself(fileNameToFileMap.get(currFileName).getAbsolutePath()).toCCF();
//                } else if (resourceType.equals(AnnotManagerType.IntervalAnnot)) {
//                    IntervalFileParser.parseItself(fileNameToFileMap.get(currFileName)).toCCF();
//                }
                currFileName = tmpFilePath;
                File resourceFile = new File(extraItemDir.getAbsolutePath() + File.separatorChar + tmpFilePath);
                RefResource tmpRefResource = new RefResource(resourceFile, resourceType);
                if (resourceType.equals(AnnotManagerType.IntervalAnnot)) {
                    tmpRefResource.setRelativePath("extra/interval/" + resourceFile.getName());
                } else if (resourceType.equals(AnnotManagerType.KnownSVAnnot)) {
                    tmpRefResource.setRelativePath("extra/knownSV/" + resourceFile.getName());
                }
                if (loadedResourceNames.contains(tmpRefResource.getTag())) {
//                    logger.error("The fileName " + tmpRefResource.getTag() + " is duplicated");
                }
                resources.add(tmpRefResource);
            }
            fileNameToFileMap.remove(currFileName);
        }
    }

}
