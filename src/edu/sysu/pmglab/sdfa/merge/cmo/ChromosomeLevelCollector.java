package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.merge.SDFAMergeManager;
import edu.sysu.pmglab.sdfa.merge.base.CSVAssemblerInFile;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.toolkit.GlobalVCFContigConvertor;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 09:37
 * @description
 */
public class ChromosomeLevelCollector extends AbstractSVCollector {
    @Override
    public boolean collect() throws IOException {
        Chromosome currChr = GlobalVCFContigConvertor.getGlobalChromosome(currChrIndex.get());
        //region check all csv of all chr when ending loop
        if (currChr.equals(Chromosome.unknown)) {
            // only extract csv
            boolean hasCSV = false;
            for (int i = 0; i < csvFileIDMergeQueueMap.size(); i++) {
                CSVAssemblerInFile CSVAssemblerInFile = csvFileIDMergeQueueMap.get(i);
                if (!CSVAssemblerInFile.isEmpty()) {
                    hasCSV = true;
                    Array<ComplexSV> csvArrayInFile = CSVAssemblerInFile.collect();
                    csvCompleteArray.addAll(csvArrayInFile);
                    csvCompleteArray.sort(ComplexSV::compareTo);
                }
            }
            return hasCSV;
        }
        //endregion
        //region collect current chr of simple SVs
        boolean hasCollect = false;
        int vcfFileSize = sdfReaderArray.size();
        for (int i = 0; i < vcfFileSize; i++) {
            SDFReader sdfReader = sdfReaderArray.get(i);
            sdfReader.restart();
            boolean success = sdfReader.limitChrBlock(currChr);
            if (!success){
                sdfReader.close();
                continue;
            }
            UnifiedSV sv;
            while ((sv = sdfReader.read()) != null) {
                hasCollect = true;
                sv.getCoordinate().setChr(currChr);
                sv.setFileID(i);
                if (sv.isComplex()) {
                    if (SDFAMergeManager.isIsParallel()) {
                        safeAddComplexBucket(sv);
                    } else {
                        unsafeAddComplexBucket(sv);
                    }
                } else {
                    if (filterSimpleSV(sv)) {
                        if (SDFAMergeManager.isIsParallel()) {
                            simpleSVTypeSVMergeQueueMap.get(sv.getTypeIndex()).safeAddSV(sv);
                        } else {
                            simpleSVTypeSVMergeQueueMap.get(sv.getTypeIndex()).unsafeAddSV(sv);
                        }
                    }
                }
            }
            sdfReader.close();
        }
        currChrIndex.incrementAndGet();
        //endregion
        //region collect current chr of csv
        for (int i = 0; i < vcfFileSize; i++) {
            CSVAssemblerInFile CSVAssemblerInFile = csvFileIDMergeQueueMap.get(i);
            if (!CSVAssemblerInFile.isEmpty()) {
                hasCollect = true;
                Array<ComplexSV> csvArrayInFile = CSVAssemblerInFile.collect();
                csvCompleteArray.addAll(csvArrayInFile);
            }
        }
        //endregion
        return hasCollect;
    }

    @Override
    public boolean filterCSV(ComplexSV csv) {
        return true;
    }

    @Override
    public boolean filterSimpleSV(UnifiedSV sv) {
        return true;
    }

    void unsafeAddComplexBucket(UnifiedSV sv) {
        csvFileIDMergeQueueMap.get(sv.getFileID()).put(sv);
    }

    synchronized void safeAddComplexBucket(UnifiedSV sv) {
        csvFileIDMergeQueueMap.get(sv.getFileID()).put(sv);
    }



}
