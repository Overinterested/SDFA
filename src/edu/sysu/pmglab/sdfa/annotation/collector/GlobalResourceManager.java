package edu.sysu.pmglab.sdfa.annotation.collector;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Wenjie Peng
 * @create 2024-04-01 23:23
 * @description
 */
public class GlobalResourceManager {
    Logger logger;
    File outputDir;
    File configFile;
    boolean silent = false;
    AtomicInteger resourceIndexCounter = new AtomicInteger();
    CallableSet<AbstractResourceManager> resourceManagerSet = new CallableSet<>();
    private static final GlobalResourceManager instance = new GlobalResourceManager();

    private GlobalResourceManager() {
    }

    /**
     * parse and check the annotation config file and
     */
    public void init() {
        try {
            GlobalResourceConfigParser.register(configFile);
            check();
            if (logger != null && !silent) {
                StringBuilder sb = new StringBuilder();
                sb.append("SDFA collects ")
                        .append(resourceManagerSet.size())
                        .append(" resources from ")
                        .append(configFile.getName())
                        .append(":\n");
                VolumeByteStream cache = new VolumeByteStream();
                ByteCodeArray resourceDescription = new ByteCodeArray(resourceManagerSet.size());
                for (int i = 0; i < resourceManagerSet.size(); i++) {
                    AbstractResourceManager resourceManager = resourceManagerSet.getByIndex(i);
                    cache.writeSafety(ValueUtils.Value2Text.int2bytes(i));
                    cache.writeSafety(ByteCode.PERIOD);
                    cache.writeSafety(ByteCode.BLANK);
                    cache.writeSafety(resourceManager.getResourcePath().getName());
                    // resource type
                    cache.writeSafety(ByteCode.NEWLINE);
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety(new byte[]{ByteCode.T, ByteCode.y, ByteCode.p, ByteCode.e, ByteCode.EQUAL});
                    cache.writeSafety(resourceManager.resourceType);
                    // columns
                    ByteCodeArray outputColNameArray = resourceManager.getOutputFrame().getOutputColNameArray();
                    // resource load columns
                    cache.writeSafety(ByteCode.NEWLINE);
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety("Number of annotated columns=");
                    int outputSize = outputColNameArray.size();
                    cache.writeSafety(ValueUtils.Value2Text.int2bytes(outputSize));
                    // resource output columns name
                    cache.writeSafety(ByteCode.NEWLINE);
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety("Output names=");
                    if (outputSize > 10) {
                        for (int j = 0; j < 10; j++) {
                            cache.writeSafety(outputColNameArray.get(i));
                            if (j != 9) {
                                cache.writeSafety(ByteCode.COMMA);
                                cache.writeSafety(ByteCode.BLANK);
                            }
                        }
                        cache.writeSafety(new byte[]{ByteCode.PERIOD, ByteCode.PERIOD, ByteCode.PERIOD});
                        cache.writeSafety("(totally " + outputColNameArray.size() + " size");
                    } else {
                        for (int j = 0; j < outputSize; j++) {
                            cache.writeSafety(outputColNameArray.get(j));
                            if (j != outputSize - 1) {
                                cache.writeSafety(ByteCode.COMMA);
                                cache.writeSafety(ByteCode.BLANK);
                            }
                        }
                    }
                    resourceDescription.add(cache.toByteCode().asUnmodifiable());
                    cache.reset();
                }
                cache.close();
                for (int i = 0; i < resourceDescription.size(); i++) {
                    sb.append(resourceDescription.get(i));
                    if (i != resourceDescription.size() - 1) {
                        sb.append("\n");
                    }
                }
                logger.info(sb.toString());
                sb.delete(0, sb.length());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GlobalResourceManager getInstance() {
        return instance;
    }

    public int numOfResource() {
        return resourceManagerSet.size();
    }

    public GlobalResourceManager setResourceManagerSet(Collection<AbstractResourceManager> resourceManagerSet) {
        this.resourceManagerSet.addAll(resourceManagerSet);
        return this;
    }

    public GlobalResourceManager setOutputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public void check() {
        for (AbstractResourceManager abstractResourceManager : resourceManagerSet) {
            abstractResourceManager.checkResourceAndOutputFrame(outputDir);
        }
    }

    public GlobalResourceManager setConfigFile(File configFile) {
        this.configFile = configFile;
        return this;
    }

    public AbstractResourceManager getResourceByIndex(int resourceIndex) {
        return resourceManagerSet.getByIndex(resourceIndex);
    }

    public void clearIndex() {
        for (AbstractResourceManager resourceManager : resourceManagerSet) {
            resourceManager.clearCoordinate();
        }
    }

    public Array<CCFFieldMeta> resourceCCFField() {
        Array<CCFFieldMeta> res = new Array<>(resourceManagerSet.size());
        for (AbstractResourceManager resourceManager : resourceManagerSet) {
            res.add(CCFFieldMeta.of(resourceManager.getResourcePath().getName(), FieldType.bytecode));
        }
        return res;
    }

    public void putResource(AbstractResourceManager resourceManager) {
        resourceManager.setResourceIndex(resourceIndexCounter.get());
        resourceIndexCounter.incrementAndGet();
        this.resourceManagerSet.add(resourceManager);
    }

    public CallableSet<AbstractResourceManager> getResourceManagerSet() {
        return resourceManagerSet;
    }

    public GlobalResourceManager setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
}
