package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.array.*;
import edu.sysu.pmglab.gbc.genome.Chromosome;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 20:28
 * @description
 */
public class Contig {
    boolean close = false;
    CallableSet<ByteCode> extraName = new CallableSet<>();
    CallableSet<Chromosome> extraContig = new CallableSet<>();
    static final int normalSize = Chromosome.support().size();
    CallableSet<Chromosome> allChromosomes = new CallableSet<>();

    private static final Constructor<Chromosome> constructor;
    private static final Class<Chromosome> clazz = Chromosome.class;

    static {
        try {
            constructor = clazz.getDeclaredConstructor(String.class, int.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Chromosome addChromosome(ByteCode extraChromosome) {
        if (!close) {
            ByteCode unmodifiable = extraChromosome.asUnmodifiable();
            extraName.add(unmodifiable);
            int currSize = extraContig.size();
            try {
                Chromosome newChr = constructor.newInstance(unmodifiable.toString(), normalSize + currSize);
                extraContig.add(newChr);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return extraContig.getByIndex(currSize);
        }
        throw new UnsupportedOperationException("Can't add new Chromosome.");
    }

    /**
     * get Chromosome. If missing, create a new one
     *
     * @param chrName search Chromosome
     * @return return itself
     */
    public Chromosome get(ByteCode chrName) {
        Chromosome res;
        Chromosome chromosome = Chromosome.get(chrName);
        if (!chromosome.equals(Chromosome.unknown)) {
            res = chromosome;
        } else {
            int index = extraName.indexOfValue(chrName);
            if (index == -1) {
                res = addChromosome(chrName);
            } else {
                res = extraContig.getByIndex(index);
            }
        }
        return res;
    }

    /**
     * load chromosomes,ignoring if contig repeats,
     *
     * @param contigArray chromosome name from VCF header
     */
    public void loadChromosomes(BaseArray<ByteCode> contigArray) {
        if (contigArray == null || contigArray.isEmpty()) {
            return;
        }
        for (ByteCode contig : contigArray) {
            // register chromosomes of vcf header
            get(contig);
        }
    }

    public void clear() {
        close = false;
        extraName.clear();
        extraContig.clear();
        allChromosomes.clear();
    }

    public CallableSet<Chromosome> getAllChromosomes() {
        if (close) {
            return allChromosomes;
        }
        close = true;
        allChromosomes.addAll(Chromosome.support());
        allChromosomes.addAll(extraContig);
        return allChromosomes;
    }

    public Contig asUnmodified() {
        Contig contig = new Contig();
        contig.extraName.addAll(extraName);
        contig.extraContig.addAll(extraContig);
        contig.allChromosomes.addAll(allChromosomes);
        return contig;
    }

    public Chromosome getByIndex(int chrIndex) {
        if (chrIndex <= 23) {
            return Chromosome.get(chrIndex);
        }
        if (chrIndex - 24 >= extraContig.size()) {
            return Chromosome.unknown;
        }
        return extraContig.getByIndex(chrIndex - 24);
    }

    public static Contig decode(ByteCode src) {
        Contig res = new Contig();
        if (src.equals(ByteCode.EMPTY)) {
            return res;
        }
        ByteCodeArray decode = (ByteCodeArray) ArrayType.decode(src);
        for (ByteCode chrName : decode) {
            res.addChromosome(chrName);
        }
        return res;
    }

    public CallableSet<Chromosome> getExtraContig() {
        return extraContig;
    }

    public static Chromosome createChromosome(String contigName, int index) {
        try {
            return constructor.newInstance(contigName, index);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Contig setAllChromosomes(CallableSet<Chromosome> allChromosomes) {
        close = true;
        this.allChromosomes = allChromosomes;
        return this;
    }

    public void cycle() {
        close = false;
        extraName.clear();
        extraContig.clear();
    }
}
