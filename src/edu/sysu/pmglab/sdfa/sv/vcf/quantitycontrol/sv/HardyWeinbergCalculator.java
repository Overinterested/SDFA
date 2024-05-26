package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import java.util.Arrays;

/**
 * @projectName: kggseq
 * @package: edu.sysu.pmglab.kggseq.stat
 * @className: HardyWeinbergCalculator
 * @author: Eric
 * @description: TODO
 * @date: 2023/9/11 下午11:24
 * @version: 1.0
 */
public enum  HardyWeinbergCalculator {
    INSTANCE;

    /**
     * This class is not instantiable.
     */
    private HardyWeinbergCalculator() {
    }

    /**
     * Calculates exact two-sided hardy-weinberg p-value. Parameters are number
     * of genotypes, number of rare alleles observed and number of heterozygotes
     * observed.
     *
     * (c) 2003 Jan Wigginton, Goncalo Abecasis (goncalo@umich.edu)
     */
    public static double hwCalculate(int obsAA, int obsAB, int obsBB) {
        int diplotypes = obsAA + obsAB + obsBB;
        int rare = (obsAA * 2) + obsAB;
        int hets = obsAB;

        //make sure "rare" allele is really the rare allele
        if (rare > diplotypes) {
            rare = (2 * diplotypes) - rare;
        }

        //make sure numbers aren't screwy
        if (hets > rare) {
            return -1;
        }

        if (diplotypes == 0) {
            return 1;
        }
        double[] tailProbs = new double[rare + 1];
        Arrays.fill(tailProbs, 0);
        //start at midpoint
        // int mid = (rare * ((2 * diplotypes) - rare)) / (2 * diplotypes);
        //for large number
        double rareD = rare;
        rareD = (diplotypes - rareD / 2) / diplotypes;

        int mid = (int) (rare * rareD);
        //check to ensure that midpoint and rare alleles have same parity
        if (((rare & 1) ^ (mid & 1)) != 0) {
            mid++;
        }

        int het = mid;
        int hom_r = (rare - mid) / 2;
        int hom_c = diplotypes - het - hom_r;

        //Calculate probability for each possible observed heterozygote
        //count up to a scaling constant, to avoid underflow and overflow
        tailProbs[mid] = 1.0;

        double sum = tailProbs[mid];

        for (het = mid; het > 1; het -= 2) {
            tailProbs[het - 2] = (tailProbs[het] * het * (het - 1.0)) / (4.0 * (hom_r + 1.0) * (hom_c
                    + 1.0));
            sum += tailProbs[het - 2];

            //2 fewer hets for next iteration -> add one rare and one common homozygote
            hom_r++;
            hom_c++;
        }

        het = mid;
        hom_r = (rare - mid) / 2;
        hom_c = diplotypes - het - hom_r;

        for (het = mid; het <= (rare - 2); het += 2) {
            tailProbs[het + 2] = (tailProbs[het] * 4.0 * hom_r * hom_c) / ((het + 2.0) * (het
                    + 1.0));
            sum += tailProbs[het + 2];

            //2 more hets for next iteration -> subtract one rare and one common homozygote
            hom_r--;
            hom_c--;
        }

        for (int z = 0; z < tailProbs.length; z++) {
            tailProbs[z] /= sum;
        }

        double top = tailProbs[hets];

        for (int i = hets + 1; i <= rare; i++) {
            top += tailProbs[i];
        }

        double otherSide = tailProbs[hets];

        for (int i = hets - 1; i >= 0; i--) {
            otherSide += tailProbs[i];
        }

        if ((top > 0.5) && (otherSide > 0.5)) {
            return 1.0;
        }

        if (top < otherSide) {
            return top * 2;
        }

        return otherSide * 2;
    }

    public static void main(String[] args) {
        try {
            System.out.println(hwCalculate(12, 33, 12));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
