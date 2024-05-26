package edu.sysu.pmglab.sdfa.sv;


import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;

import java.util.Collection;

/**
 * @author Wenjie Peng
 * @create 2024-03-23 23:38
 * @description
 */
public class ComplexSV implements Comparable<ComplexSV> {
    int fileID;
    SVTypeSign type;
    int indexOfFile = -1;
    Boolean spanChromosome;
    Array<UnifiedSV> svs = new Array<>();
    static SVTypeSign CSV_TYPE = SVTypeSign.get("CSV");
    public static ComplexSV DEFAULT_CSV = new ComplexSV().setType(CSV_TYPE);
    public static ComplexSV of(Collection<UnifiedSV> svs) {
        return new ComplexSV().setSVs(svs);
    }

    public ComplexSV bind(UnifiedSV sv) {
        svs.add(sv);
        return this;
    }

    public int numOfSubSV() {
        return svs.size();
    }

    public boolean spanChromosome() {
        if (spanChromosome == null) {
            if (svs.isEmpty()) {
                throw new UnsupportedOperationException("Error for no SV can be checked.");
            }
            Chromosome chr = svs.get(0).getChr();
            for (int i = 1; i < svs.size(); i++) {
                if (!chr.equals(svs.get(i).getChr())) {
                    spanChromosome = true;
                    break;
                }
            }
        }
        return spanChromosome;
    }

    public ComplexSV setSVs(Collection<UnifiedSV> svs) {
        this.svs = new Array<>(svs);
        UnifiedSV first = this.svs.get(0);
        this.fileID = first.fileID;
        this.indexOfFile = first.getIndexOfFile();
        if (!first.isComplex()){
            this.type = CSV_TYPE;
        }else {
            this.type = first.type;
        }
        return this;
    }

    public int indexOfFile() {
        if (indexOfFile == -1) {
            indexOfFile = svs.get(0).getIndexOfFile();
        }
        return indexOfFile;
    }

    public int getFileID() {
        if (fileID == -1) {
            fileID = svs.get(0).fileID;
        }
        return fileID;
    }

    @Override
    public int compareTo(ComplexSV o) {
        int min = Math.min(svs.size(), o.svs.size());
        for (int i = 0; i < min; i++) {
            int status = svs.get(i).coordinate.compareTo(o.svs.get(i).coordinate);
            if (status != 0) {
                return status;
            }
        }
        return Integer.compare(svs.size(), o.svs.size());
    }

    public SVTypeSign getType() {
        return svs.get(0).type;
    }

    public ComplexSV setType(SVTypeSign type) {
        this.type = type;
        return this;
    }

    public Array<UnifiedSV> getSVs() {
        return svs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Complex SV contains "+svs.size()+" subSVs, ");
        for (int i = 0; i < svs.size(); i++) {
            sb.append(i).append(" SV is in ");
            sb.append(svs.get(i).coordinate.getChr().getName());
            if (i!=svs.size()-1){
                sb.append(", ");
            }else {
                sb.append(".");
            }
        }
        return sb.toString();
    }
}
