package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Interval;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.container.array.LongArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFEncode;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.SDFWriter;
import edu.sysu.pmglab.sdfa.command.SDFAEntry;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2025-04-24 10:50
 * @description
 */
public class SDFIndex {
    String chrName;
    String fileName;
    String rawSearch;
    int[] startPosInterval = new int[2];

    public SDFIndex() {
    }

    public void submit() throws IOException {
        parseRawSearch();
        SDFReader sdfReader = new SDFReader(fileName);
        boolean limit = sdfReader.limitChrBlock(Chromosome.get(chrName));
        if (!limit){
            return;
        }
        boolean existEnd = startPosInterval[1] != Integer.MAX_VALUE;
        UnifiedSV sv;
        VolumeByteStream cache = new VolumeByteStream();
        while(true){
            sv = sdfReader.read();
            if (sv == null){
                sdfReader.close();
                return;
            }
            if (existEnd){
                SearchStatus overlapStatus = SearchStatus.overlap(sv, startPosInterval);
                if (overlapStatus == SearchStatus.OVERLAP){
                    System.out.println(sv.toCCFOutput(cache,-1));
                    cache.reset();
                }else if (overlapStatus == SearchStatus.AFTER){
                    sdfReader.close();
                    return;
                }
            }else {
                SearchStatus overlapStatus = SearchStatus.overlap(sv, startPosInterval[0]);
                if (overlapStatus == SearchStatus.OVERLAP){
                    System.out.println(sv.toCCFOutput(cache,-1));
                    cache.reset();
                }else if (overlapStatus == SearchStatus.AFTER){
                    sdfReader.close();
                    return;
                }
            }
        }
    }

    private void parseRawSearch(){
        String[] split = rawSearch.split(":");
        chrName = split[0];
        String positions = split[1];
        if (positions.contains("-")){
            // interval
            String[] range = positions.split("-");
            startPosInterval[0] = Integer.parseInt(range[0]);
            startPosInterval[1] = Integer.parseInt(range[1]);
        }else {
            startPosInterval[0] = Integer.parseInt(positions);
            startPosInterval[1] = Integer.MAX_VALUE;
        }
    }

    enum SearchStatus {
        BEFORE,
        OVERLAP,
        AFTER;

        SearchStatus() {

        }

        public static SearchStatus overlap(UnifiedSV sv, int[] interval) {
            if (sv.getEnd() <= interval[0]) {
                return BEFORE;
            }
            if (sv.getPos() >= interval[1]) {
                return AFTER;
            }
            return OVERLAP;
        }

        public static SearchStatus overlap(UnifiedSV sv, int pos) {
            if (sv.getEnd() <= pos) {
                return BEFORE;
            }
            return OVERLAP;
        }
    }


    public SDFIndex setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public SDFIndex setRawSearch(String rawSearch) {
        this.rawSearch = rawSearch;
        return this;
    }

    public static void main(String[] args) throws IOException {
        SDFAEntry.main("index -f /Users/wenjiepeng/Desktop/PaperWriter/SV/SDFA/revise/version_1/github/SDFA/test/resource/gwas/population_1.sdf --search chr1:1000-30000".split(" "));
    }
}
