package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.intervaltree.IntervalObject;
import edu.sysu.pmglab.container.intervaltree.IntervalTree;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.GeneFeatureAnnotationType;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;
import edu.sysu.pmglab.sdfa.genome.RefGene;
import edu.sysu.pmglab.sdfa.genome.RefRNA;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2024-04-09 10:09
 * @description
 */
public class RefGeneFeatureCoordinateTree {
    int size;
    boolean geneCoordinate = false;
    IntervalTree<Entry<RefGene, RefRNA>, Integer> rnaCoordinateTree;
    IntervalTree<RefGene, Integer> geneCoordinateTree;

    public RefGeneFeatureCoordinateTree(GeneFeatureAnnotationType annotationType) {
        if (annotationType.equals(GeneFeatureAnnotationType.HGVS_GENE_LEVEL)) {
            geneCoordinate = true;
            geneCoordinateTree = new IntervalTree<>();
        } else  {
            // here snp and rna all need rna level
            rnaCoordinateTree = new IntervalTree<>();
        }
    }

    public void update(RefGene refGene) {
        size++;
        geneCoordinateTree.addInterval(refGene.getGeneStartPos(), refGene.getGeneEndPos(), refGene);
    }

    public void update(RefGene refGene, RefRNA refRNA) {
        size++;
        rnaCoordinateTree.addInterval(refRNA.getStartPos(), refRNA.getEndPos(), new Entry<>(refGene, refRNA));
    }

    public int[] getOverlap(BriefSVAnnotationFeature sv) {
        if (geneCoordinateTree == null || size == 0) {
            return new int[0];
        }
        int[] relatedLines;
        if (geneCoordinate) {
            BaseArray<IntervalObject<RefGene, Integer>> overlapsIntervals = geneCoordinateTree.getOverlapsIntervals(sv.getStart(), sv.getEnd());
            if (overlapsIntervals.isEmpty()) {
                return new int[0];
            }
            relatedLines = new int[overlapsIntervals.size()];
            int count = 0;
            for (IntervalObject<RefGene, Integer> overlapsInterval : overlapsIntervals) {
                relatedLines[count++] = overlapsInterval.getData().getGeneIndex();
            }
        } else {
            BaseArray<IntervalObject<Entry<RefGene, RefRNA>, Integer>> overlapsIntervals = rnaCoordinateTree.getOverlapsIntervals(sv.getStart(), sv.getEnd());
            if (overlapsIntervals.isEmpty()) {
                return new int[0];
            }
            relatedLines = new int[overlapsIntervals.size()];
            int count = 0;
            for (IntervalObject<Entry<RefGene, RefRNA>, Integer> overlapsInterval : overlapsIntervals) {
                Entry<RefGene, RefRNA> data = overlapsInterval.getData();
                short geneIndex = data.getKey().getGeneIndex();
                short rnaIndex = data.getValue().getRNAIndex();
                relatedLines[count++] = (geneIndex) << 16 | rnaIndex;
            }
        }
        Arrays.sort(relatedLines);
        return relatedLines;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void reset(){
        if (rnaCoordinateTree != null){
            rnaCoordinateTree.clear();
        }
        if (geneCoordinateTree != null){
            geneCoordinateTree.clear();
        }
    }
}
