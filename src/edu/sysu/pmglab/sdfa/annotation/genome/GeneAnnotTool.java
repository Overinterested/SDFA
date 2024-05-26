//package edu.sysu.pmglab.sdfa.annotation.genome;
//
//import edu.sysu.pmglab.ccf.CCFReader;
//import edu.sysu.pmglab.ccf.CCFTable;
//import edu.sysu.pmglab.ccf.record.IRecord;
//import edu.sysu.pmglab.container.array.Array;
//import edu.sysu.pmglab.gbc.genome.Chromosome;
//import edu.sysu.pmglab.sdfa.annotation.base.SVPosManager;
//import edu.sysu.pmglab.sdfa.config.ConstParam;
//import edu.sysu.pmglab.sdfa.genome.transcript.UnknownRegion;
//import edu.sysu.pmglab.sdfa.sv.SVPos;
//
//import java.io.IOException;
//import java.util.HashMap;
//
///**
// * @author Wenjie Peng
// * @create 2023-02-04 12:36
// * @description
// */
//public class GeneAnnotTool {
//    private static final GeneAnnotTool instance = new GeneAnnotTool();
//    private static final RNAAnnot unknownRegion = UnknownRegion.convert(0, 0);
//
//    private GeneAnnotTool() {
//    }
//
//    public boolean annotChr0(Chromosome chr, int annotIndex, GeneAnnotManager geneAnnotManager) {
//        HashMap<Chromosome, Array<GeneAnnotRecord>> SVByChr = geneAnnotManager.refAnnotByChr;
//        try {
//            Array<GeneAnnotRecord> genes = SVByChr.get(chr);
//            if (genes == null || genes.size() == 0) {
//                return true;
//            }
//            Array<SVPos> svList = SVPosManager.getInstance().getSVPosByChr().get(chr);
//            if (svList == null || svList.size() == 0) {
//                return true;
//            }
//
//            // 1. first: quick start
////            int svIndex = 0;
////            for (; svIndex < svList.size(); svIndex++) {
////                if (svList.get(svIndex).getEnd() >= chr.getFirstGeneStartPos() - upstreamDistance) {
////                    break;
////                }
////            }
//            // 2. second: Two pointer algorithm and Odd-Even algorithm
//
//            // 2.1 initial
//            int count;
//            int geneMarker = 0;
//            GeneAnnotRecord currGene;
//            int currSVStart;
//            int currSVEnd;
//            GeneAnnotFeature geneAnnotFeature;
//            // 2.2 enter loop
//            GeneAnnotation:
//            for (SVPos sv : svList) {
//                //SV loop
//                count = 0;
//                currSVStart = sv.getStart();
//                currSVEnd = sv.getEnd();
//                if (currSVStart == 10664){
//                    int a = 1;
//                }
//                geneAnnotFeature = new GeneAnnotFeature();
//                boolean secondCannotAnnot = currSVEnd <= -1;
//                for (int geneIndex = geneMarker; geneIndex < genes.size(); geneIndex++) {
//                    currGene = genes.get(geneIndex);
//                    if (currSVEnd != -1 && currSVEnd <= currGene.geneStartPos - ConstParam.upstreamDis) {
//                        // sv has no relationship with current gene and iteration will stop
//                        // marker reset will come back the first matched gene
//                        geneMarker = geneIndex - count;
//                        sv.addAnnot(geneAnnotFeature, annotIndex);
//                        continue GeneAnnotation;
//                    }
//
//                    if (currSVEnd == -1 && currSVStart <= currGene.geneStartPos - ConstParam.upstreamDis) {
//                        geneMarker = geneIndex - count;
//                        sv.addAnnot(geneAnnotFeature, annotIndex);
//                        continue GeneAnnotation;
//                    }
//
//                    if (currSVStart >= currGene.geneEndPos + ConstParam.downstreamDis) {
//                        // sv may relate the later gene, so continue to next gene
//                        // search for QuickEnd situation
//                        count++;
//                        continue;
//                    }
//                    // search for each RNA of currGene
//                    count++;
//                    for (RNAAnnotRecord currGeneRNA : currGene.RNAList) {
//                        if (currSVEnd != -1 && currSVEnd - 1 - currGeneRNA.startPos < -ConstParam.upstreamDis) {
//                            continue;
//                        }
//                        if (currSVStart - currGeneRNA.endPos >= ConstParam.downstreamDis) {
//                            continue;
//                        }
//                        if (secondCannotAnnot) {
//                            geneAnnotFeature.add(
//                                    currGeneRNA.annot(currSVStart - 1, ConstParam.upstreamDis, ConstParam.downstreamDis, geneIndex)
//                                            .append(unknownRegion)
////                                            .setQuantificationAnnotation(currGeneRNA.toRefRNA().parseQuantificationAnnotation(currSVStart - 1, currSVEnd - 1, sv.getLength(), sv.getType()))
//                            );
//                        } else {
//                            geneAnnotFeature.add(
//                                    currGeneRNA.annot(currSVStart - 1, ConstParam.upstreamDis, ConstParam.downstreamDis, geneIndex)
//                                            .append(currGeneRNA.annot(currSVEnd - 1, ConstParam.upstreamDis, ConstParam.downstreamDis, geneIndex))
////                                            .setQuantificationAnnotation(currGeneRNA.toRefRNA().parseQuantificationAnnotation(currSVStart - 1, currSVEnd - 1, sv.getLength(), sv.getType()))
//                            );
//                        }
//                    }
//                }
//                sv.addAnnot(geneAnnotFeature, annotIndex);
//                geneMarker = geneMarker - count - 1;
//                geneMarker = Math.max(geneMarker, 0);
//            }
//            for (GeneAnnotRecord gene : genes) {
//                gene.clear();
//            }
//        } catch (Exception | Error e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
//
//    public void parseItself(GeneAnnotManager geneAnnotManager) throws IOException {
//        CCFReader reader = new CCFReader(geneAnnotManager.getAnnotFile());
//        IRecord record = reader.getRecord();
//        while (reader.read(record)) {
//            GeneAnnotRecord geneAnnotRecord = GeneAnnotRecord.getInstance(record);
//            for (int i = 0; i < geneAnnotRecord.RNACount; i++) {
//                reader.read(record);
//                geneAnnotRecord.appendRNA(RNAAnnotRecord.getInstance(record));
//            }
//            geneAnnotManager.updateRecord(geneAnnotRecord);
//        }
//        reader.close();
//        CCFTable.clear(reader);
//    }
//
//    public static GeneAnnotTool getInstance() {
//        return instance;
//    }
//}
