package edu.sysu.pmglab.easytools.container;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-27 00:17
 * @description
 */
public class GlobalTemporaryContainer {
    static ConcurrentHashMap<Object, Object> container = new ConcurrentHashMap<>();

    public static void add(Object key, Object value) {
        container.put(key, value);
    }

    public static Object get(Object key) {
        return container.get(key);
    }

    public static Object pop(Object key) {
        Object o = container.get(key);
        if (o != null) {
            container.remove(key);
        }
        return o;
    }
}
