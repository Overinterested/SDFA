package edu.sysu.pmglab.simulation;

import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFWriter;
import edu.sysu.pmglab.sdfa.sv.*;
import edu.sysu.pmglab.sdfa.sv.idividual.Subject;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.Random;

/**
 * @author Wenjie Peng
 * @create 2024-08-21 00:32
 * @description
 */
public class PopulationSimulation {
    Random rand;
    File outputFile;
    int randSeed = -1;
    int sampleSize = -1;
    boolean initChromosome = false;
    Interval<Integer> lenThreshold;
    CallableSet<SVTypeSign> svTypeSet;
    CallableSet<String> extraChromosomeSet;
    VolumeByteStream cache = new VolumeByteStream();
    CallableSet<Chromosome> allChromosomeSet = new CallableSet<>();

    public void simulate() throws IOException {
        int sizeOfSV = 1000;
        int sizeOfSVType = svTypeSet.size();
        UnifiedSV unifiedSV = new UnifiedSV();
        SDFWriter writer = SDFWriter.of(outputFile, 0);
        SVTypeSign svTypeSign;
        SVTypeSign insType = SVTypeSign.getByName(new ByteCode("INS"));
        ByteCode insertAlt = new ByteCode("<INS>");
        SVTypeSign dupType = SVTypeSign.getByName(new ByteCode("DUP"));
        ByteCode dupAlt = new ByteCode("<DUP>");
        SVTypeSign delType = SVTypeSign.getByName(new ByteCode("DEL"));
        ByteCode delAlt = new ByteCode("<DEL>");
        int initPos = 10000;
        int len;
        BaseArray<ByteCode> info = new Array<>();
        ByteCodeArray infoField = new ByteCodeArray(new String[]{
                "SVTYPE=",
                "POS=",
                "END=",
                "SVLEN=",
                "METHOD=SDFA"
        });
        SVGenotype[] genotypes = new SVGenotype[sampleSize];
        SVGenotype[] tmpGenotypes = new SVGenotype[]{SVGenotype.of(1, 1), SVGenotype.of(0, 1), SVGenotype.of(0, 0)};
        ByteCode period = new ByteCode(ByteCode.PERIOD);
        int id = 0;
        ContigBlockContainer contigBlockContainer = new ContigBlockContainer();
        int indexOfSV = 0;
        int indexOfChromosome = 0;
        for (Chromosome chromosome : allChromosomeSet) {
            int tmpSize = rand.nextInt(sizeOfSV) + sizeOfSV;
            contigBlockContainer.putChromosomeRange(chromosome, indexOfChromosome, indexOfChromosome += tmpSize);
            for (int i = 0; i < tmpSize; i++) {
                svTypeSign = svTypeSet.getByIndex(rand.nextInt(sizeOfSVType));
                initPos += rand.nextInt(1000);
                len = rand.nextInt(lenThreshold.end() - lenThreshold.start()) + lenThreshold.start();
                if (svTypeSign == insType) {
                    unifiedSV.setCoordinate(new SVCoordinate(initPos, initPos += 1, chromosome)).setAlt(insertAlt);
                } else {
                    unifiedSV.setCoordinate(new SVCoordinate(initPos, initPos += len, chromosome));
                    unifiedSV.setAlt(svTypeSign == delType ? delAlt : dupAlt);
                }
                for (int j = 0; j < sampleSize; j++) {
                    genotypes[j] = tmpGenotypes[rand.nextInt(3)];
                }
                unifiedSV.setType(svTypeSign)
                        .setLength(len)
                        .setSpecificInfoField(buildInfo(info, infoField, svTypeSign, unifiedSV.getPos(), unifiedSV.getEnd(), len))
                        .setGenotypes(new SVGenotypes(genotypes))
                        .setQual(period)
                        .setRef(period)
                        .setFileID(0)
                        .setID(new ByteCode(String.valueOf(id++)))
                        .setCSVLocation(new CSVLocation(indexOfSV));
                writer.write(unifiedSV);
            }
        }
        writer.writeMeta(new SDFMeta()
                .setSubjects(buildSubjects(sampleSize))
                .setContigBlockContainer(contigBlockContainer)
                .setFileID(0)
                .setEncodeMode(0)
                .addInfoFieldID(new ByteCode("SVTYPE"))
                .addInfoFieldID(new ByteCode("POS"))
                .addInfoFieldID(new ByteCode("END"))
                .addInfoFieldID(new ByteCode("SVLEN"))
                .addInfoFieldID(new ByteCode("METHOD"))
                .addFormatFieldID(new ByteCode("GT"))
        );
        writer.close();
        // prepare ped
        File pedFile = outputFile.getParentFile().getSubFile("sample.ped");
        if (!pedFile.exists()) {
            FileStream fs = new FileStream(pedFile, FileStream.DEFAULT_WRITER);
            for (int i = 0; i < sampleSize; i++) {
                fs.write("Subject_" + (i + 1));
                fs.write(ByteCode.TAB);
                fs.write("Subject_" + (i + 1));
                fs.write(ByteCode.TAB);
                fs.write(ByteCode.ZERO);
                fs.write(ByteCode.TAB);
                fs.write(ByteCode.ZERO);
                fs.write(ByteCode.TAB);
                fs.write(rand.nextInt(2)+1==1?ByteCode.ONE:ByteCode.TWO);
                fs.write(ByteCode.TAB);
                fs.write(rand.nextInt(2)+1==1?ByteCode.ONE:ByteCode.TWO);
                fs.write(ByteCode.NEWLINE);
            }
            fs.close();
        }
    }

    public void prepare() {
        rand = randSeed == -1 ? new Random() : new Random(randSeed);
        if (initChromosome) {
            Chromosome.clear();
            if (extraChromosomeSet == null || extraChromosomeSet.isEmpty()) {
                throw new UnsupportedOperationException("No chromosomes can be simulated");
            }
        }
        allChromosomeSet.addAll(Chromosome.support());
        if (extraChromosomeSet != null && !extraChromosomeSet.isEmpty()) {
            for (String chromosome : extraChromosomeSet) {
                allChromosomeSet.add(Chromosome.add(chromosome));
            }
        }

    }

    private BaseArray<ByteCode> buildInfo(
            BaseArray<ByteCode> info, ByteCodeArray infoField,
            SVTypeSign svTypeSign, int position, int end, int len) {
        info.clear();
        // 1. type
        cache.writeSafety(infoField.get(0));
        cache.writeSafety(svTypeSign.getName());
        info.add(cache.toByteCode().asUnmodifiable());
        cache.reset();
        // 2. pos
        cache.writeSafety(infoField.get(1));
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(position));
        info.add(cache.toByteCode().asUnmodifiable());
        cache.reset();
        // 3. end
        cache.writeSafety(infoField.get(2));
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(end));
        info.add(cache.toByteCode().asUnmodifiable());
        cache.reset();
        // 4. len
        cache.writeSafety(infoField.get(3));
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(len));
        info.add(cache.toByteCode().asUnmodifiable());
        cache.reset();
        // 5. method
        cache.writeSafety(infoField.get(4));
        info.add(cache.toByteCode().asUnmodifiable());
        cache.reset();
        return info;
    }

    private Subjects buildSubjects(int sampleSize) {
        Subjects subjects = new Subjects(outputFile);
        for (int i = 0; i < sampleSize; i++) {
            subjects.addSubject(new Subject(new ByteCode("Subject_" + (i + 1))));
        }
        return subjects;
    }

    public static void main(String[] args) throws IOException {
        PopulationSimulation tmp = new PopulationSimulation();
        tmp.sampleSize = 100;
        tmp.initChromosome = false;
        tmp.lenThreshold = new Interval<>(50, 1000);
        tmp.randSeed = 3;
        tmp.outputFile = new File("/Users/wenjiepeng/projects/sdfa_latest/test/resource/gwas/population_2.sdf");
        tmp.svTypeSet = new CallableSet<>(new SVTypeSign[]{
                SVTypeSign.getByName(new ByteCode("DEL")),
                SVTypeSign.getByName(new ByteCode("DUP")),
                SVTypeSign.getByName(new ByteCode("INS")),
        });
        tmp.prepare();
        tmp.simulate();
    }
}
