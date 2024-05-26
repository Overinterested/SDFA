package edu.sysu.pmglab.sdfa.framework.collector;

import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.sdfa.toolkit.GlobalVCFContigConvertor;


/**
 * @author Wenjie Peng
 * @create 2024-03-30 00:35
 * @description
 */
public class DataCollector {
    SVCollector collector;
    GlobalVCFContigConvertor convertor;
    ReusableMap<String, ExtraResource> extraResourceHashMap;
    private static final DataCollector instance = new DataCollector();

    private DataCollector() {

    }

    public synchronized static DataCollector getInstance() {
        return instance;
    }
}
