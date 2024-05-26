package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.container.File;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-05-21 00:33
 * @description
 */
public class ResourceConvertorTest {
    public static void main(String[] args) throws IOException {
        //region genome convert
        String tmp = "/Users/wenjiepeng/Desktop/SV/AnnotFile/RefGene/resource/kggseqv1.1_hg19_refGene.txt.gz";
//        String outputDir = "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/RefGene";
//        String[] genomeDatabaseVersion = new String[]{"knownGene","refGene","GEncode"};
//        for (String genomeDatabase : genomeDatabaseVersion) {
//            String newTmp = tmp.replace("refGene", genomeDatabase);
//            File kggseqVersion = new File(newTmp);
//            new KggSeqGeneResource2SDFAResource(
//                    kggseqVersion,
//                    new File(outputDir).getSubFile(kggseqVersion.getName()).addExtension(".ccf")
//            ).convert();
//            newTmp = newTmp.replace("hg19", "hg38");
//            kggseqVersion = new File(newTmp);
//            new KggSeqGeneResource2SDFAResource(
//                    kggseqVersion,
//                    new File(outputDir).getSubFile(kggseqVersion.getName()).addExtension(".ccf")
//            ).convert();
//        }
        //endregion

        //region CytoBand
        tmp = "/Users/wenjiepeng/Desktop/SV/AnnotFile/CytoBand/resource/GRCH37_cytoBand.txt";
        convertTSV(tmp, "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/CytoBand",false);
        convertTSV(tmp.replace("GRCH37","GRCh38"), "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/CytoBand",false);
        //endregion
        //region TAD
        tmp = "/Users/wenjiepeng/Desktop/SV/AnnotFile/TAD/resource/20171024_boundariesTAD_GRCh37.sorted.bed";
        String dirOfTAD = "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/TAD";
        convertTSV(tmp, dirOfTAD, false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/TAD/resource/20171127_boundariesTAD_GRCh38.sorted.bed",dirOfTAD,false);
        //endregion
        //region parse EnhanceAtlas, GeneHancer, miRTargetLink
        String dirOfEnhanceAtlas = "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/Regulated_element/EnhanceAtlas";
        String dirOfGeneHancer = "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/Regulated_element/GeneHancer";
        String dirOfTargetLink = "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/Regulated_element/miRTargetLink";
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/EnhanceAtlas/EA_ENSEMBL_GRCh37.sorted.bed", dirOfEnhanceAtlas,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/EnhanceAtlas/EA_ENSEMBL_GRCh38.sorted.bed",dirOfEnhanceAtlas,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/EnhanceAtlas/EA_RefSeq_GRCh37.sorted.bed",dirOfEnhanceAtlas, false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/EnhanceAtlas/EA_RefSeq_GRCh38.sorted.bed",dirOfEnhanceAtlas,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/GeneHancer/GRCh37_GeneHancer_regulatory_tissue_sorted.txt",dirOfGeneHancer,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/GeneHancer/GRCh38_GeneHancer_regulatory_tissue_sorted.txt",dirOfGeneHancer,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/miRTargetLink/miRTargetLink_ENSEMBL_GRCh37.sorted.bed",dirOfTargetLink,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/miRTargetLink/miRTargetLink_ENSEMBL_GRCh38.sorted.bed",dirOfTargetLink,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/miRTargetLink/miRTargetLink_RefSeq_GRCh37.sorted.bed",dirOfTargetLink,false);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/Regulated_element/resource/miRTargetLink/miRTargetLink_RefSeq_GRCh38.sorted.bed",dirOfTargetLink,false);
        //endregion
        //region parse SV database
        String dirOfSVDatabase = "/Users/wenjiepeng/Desktop/SV/AnnotFile/convertedCCF/SVDatabase";
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/KnownSV/SVAFotate_core_SV_popAFs.GRCh37.bed",dirOfSVDatabase,true);
        convertTSV("/Users/wenjiepeng/Desktop/SV/AnnotFile/KnownSV/SVAFotate_core_SV_popAFs.GRCh38.bed",dirOfSVDatabase,true);
        //endregion
    }

    public static void convertTSV(String file, String outputDir, boolean isSVDatabase) throws IOException {
        new Tsv2AnnotateResource(new File(file))
                .setOutputDir(outputDir)
                .isSVDatabase(isSVDatabase)
                .convert();
    }
}
