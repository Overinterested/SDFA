package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;


public class SDFGlobalContig {
    final int fileSize;
    private static SDFGlobalContig instance;
    private static final Array<Contig> contigArray = new Array<>();
    private static final CallableSet<Chromosome> allChromosomes = new CallableSet<>();
    private static final ReusableMap<ByteCode, Chromosome> chrNameMap = new ReusableMap<>(200);

    private SDFGlobalContig(int fileSize) {
        this.fileSize = fileSize;
    }

    public static class Builder {
        int fileSize;
        boolean close = false;
        static final int initContigSize;
        Array<Contig> contigArray = new Array<>();
        Array<SDFReader> sdfReaderArray = new Array<>();
        private static final Builder instance = new Builder();

        private static final Constructor<Chromosome> constructor;
        private static final Class<Chromosome> clazz = Chromosome.class;
        static CallableSet<Chromosome> allChromosomes = new CallableSet<>();

        static {
            Set<Chromosome> initChromosomes = edu.sysu.pmglab.gbc.genome.Chromosome.support();
            for (Chromosome chromosome : initChromosomes) {
                chrNameMap.put(new ByteCode(chromosome.getName()), chromosome);
            }
            allChromosomes.addAll(initChromosomes);
            initContigSize = initChromosomes.size();
            try {
                constructor = clazz.getDeclaredConstructor(String.class, int.class);
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public synchronized SDFGlobalContig build() {
            if (close) {
                throw new UnsupportedOperationException("Global contig had been built and can't be rebuilt.");
            }
            close = true;
            if (!contigArray.isEmpty()) {
                fileSize = contigArray.size();
                for (Contig contig : contigArray) {
                    CallableSet<Chromosome> tmpContigs = contig.getExtraContig();
                    for (int i = 0; i < tmpContigs.size(); i++) {
                        Chromosome tmpContig = tmpContigs.getByIndex(i);
                        ByteCode tmpContigName = new ByteCode(tmpContig.getName());
                        Chromosome tmpChr = chrNameMap.get(tmpContigName);
                        if (tmpChr == null) {
                            addChromosome(tmpContigName);
                        }
                    }
                }
                SDFGlobalContig.instance = new SDFGlobalContig(fileSize);
                SDFGlobalContig.allChromosomes.addAll(allChromosomes);
                SDFGlobalContig.contigArray.addAll(contigArray);
                return SDFGlobalContig.instance;
            } else if (!sdfReaderArray.isEmpty()) {
                // TODO
                return null;
            } else {
                return null;
            }

        }

        private void addChromosome(ByteCode name) {
            ByteCode unmodifiedName = name.asUnmodifiable();
            try {
                Chromosome chromosome = constructor.newInstance(unmodifiedName.toString(), initContigSize + chrNameMap.size());
                chrNameMap.put(unmodifiedName, chromosome);
                allChromosomes.add(chromosome);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        public synchronized Builder addVCFContig(Contig contig) {
            this.contigArray.add(contig.asUnmodified());
            return this;
        }

        public synchronized Builder addVCFContigs(Collection<Contig> contigs) {
            this.contigArray.addAll(contigs);
            return this;
        }

        public synchronized Builder addSDF(SDFReader reader) {
            this.sdfReaderArray.add(reader);
            return this;
        }

        public synchronized Builder addSDF(Collection<SDFReader> readers) {
            this.sdfReaderArray.addAll(readers);
            return this;
        }

        public static synchronized Builder getInstance() {
            return instance;
        }
    }

    public static Chromosome convertByRawIndex(int fileID, int chrIndex) {
        if (chrIndex <= 23) {
            return allChromosomes.getByIndex(chrIndex);
        }
        if (instance == null) {
            throw new UnsupportedOperationException("Global contig has not built.");
        }
        return chrNameMap.get(
                new ByteCode(
                        contigArray.get(fileID)
                                .getByIndex(chrIndex)
                                .getName()
                ).asUnmodifiable()
        );
    }

    public static synchronized SDFGlobalContig getInstance() {
        if (instance == null) {
            Builder.getInstance().build();
        }
        return instance;
    }

    public static Chromosome convertByName(ByteCode chrName) {
        if (instance == null) {
            throw new UnsupportedOperationException("Global contig has not built.");
        }
        return chrNameMap.get(chrName);
    }

    public static Chromosome getGlobalChromosome(int index) {
        if (instance == null) {
            throw new UnsupportedOperationException("Global contig has not built.");
        }
        if (index >= allChromosomes.size()) {
            return Chromosome.unknown;
        }
        return allChromosomes.getByIndex(index);
    }

    public static Array<Contig> getContigArray() {
        return contigArray;
    }

    public static CallableSet<Chromosome> support() {
        if (instance == null) {
            throw new UnsupportedOperationException("Global contig has not built.");
        }
        return allChromosomes;
    }

    public static void reset() {
        Builder.instance.fileSize = 0;
        Builder.instance.close = false;
        Builder.instance.contigArray.clear();
        Builder.instance.sdfReaderArray.clear();
        instance = null;
        chrNameMap.clear();
        contigArray.clear();
        allChromosomes.clear();
    }
}
