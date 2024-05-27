package edu.sysu.pmglab.sdfa.sv.vcf;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 23:41
 * @description
 */
public class ReusableVCFPool {
    final int thread;
    ArrayBlockingQueue<VCFFile> pool;
    private static boolean init = false;
    private static ReusableVCFPool instance;

    private ReusableVCFPool(int thread) {
        this.thread = thread;
        this.pool = new ArrayBlockingQueue<>(thread);
        for (int i = 0; i < thread; i++) {
            this.pool.add(new VCFFile());
        }
    }

    public synchronized static void init(int thread) {
        if (init) {
            return;
        }
        instance = new ReusableVCFPool(thread);
        init = true;
    }

    public synchronized static ReusableVCFPool getInstance() {
        if (instance == null) {
            init(1);
        }
        return instance;
    }

    public void cycle(VCFFile reusableVCF) {
        try {
            pool.put(reusableVCF);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public VCFFile getReusableSVArray() {
        VCFFile tmp;
        try {
            tmp = pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return tmp;
    }

    public VCFFile getReusableSVArray(int second) {
        try {
            return pool.poll(second, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void close() {
        instance.pool.clear();
        System.gc();
    }

    public int getThread() {
        return thread;
    }
}
