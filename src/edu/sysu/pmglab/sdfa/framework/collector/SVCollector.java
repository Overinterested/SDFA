package edu.sysu.pmglab.sdfa.framework.collector;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.container.Interval;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;

import java.util.Collection;
import java.util.HashMap;


/**
 * @author Wenjie Peng
 * @create 2024-03-30 00:34
 * @description
 */
public abstract class SVCollector {
    SVQueue svQueue;
    SDFLoadManager sdfLoadManager;

    abstract public SVQueue collectOne();

    abstract public SVQueue collectChr(Chromosome chr);

    abstract public SVQueue collectChr(Chromosome chr, SVFilterManager sv);

    abstract public SVQueue collectInterval(Interval<Long> interval);

    abstract public SVQueue popAll();

    abstract public SVQueue reset();

}
