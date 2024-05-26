package edu.sysu.pmglab.easytools.wrapper;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import org.slf4j.Logger;


/**
 * @author Wenjie Peng
 * @create 2024-03-28 09:22
 * @description
 */
public class FileTool {
    public static File checkDirWithCreate(File dir) {
        dir.mkdirs();
        return dir;
    }

    public static File checkDirWithCreate(File dir, Logger logger) {
        boolean mkdirs = dir.mkdirs();
        if (mkdirs) {
            logger.warn(dir.getAbsolutePath() + " don't exist and create a new directory.");
        }
        return dir;
    }

    public static File checkDirWithoutCreate(File dir, Logger logger) {
        if (!dir.exists()) {
            logger.error(dir.getAbsolutePath() + " don't exist. Please check its path!");
        }
        return dir;
    }

    public static File checkFileExist(File file, Logger logger) {
        if (!file.exists()) {
            logger.error(file.getAbsolutePath() + " don't exist. Please check its path!");
        }
        return file;
    }

    public static Array<File> getFilesFromDirWithHandle(File dir, String... extensions) {
        try {
            return getFilesFromDir(dir, extensions);
        } catch (UnsupportedOperationException e) {
            return new Array<>();
        }
    }
    public static Array<File> getFilesFromDirWithValid(File dir, String... extensions) {
        Array<File> res = getFilesFromDir(dir, extensions);
        if (res.isEmpty()){
            throw new UnsupportedOperationException("No " + ByteCodeArray.wrap(extensions) + " files in " + dir);
        }
        return res;
    }
    public static Array<File> getFilesFromDir(File dir, String... extensions) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new UnsupportedOperationException("No " + ByteCodeArray.wrap(extensions) + " files in " + dir);
        }
        Array<File> res = new Array<>();
        for (File file : files) {
            for (String fileType : extensions) {
                if (file.withExtension(fileType)) {
                    res.add(file);
                    break;
                }
            }
        }
        return res;
    }

    public static File covert2File(Object filePath) {
        return new File(filePath.toString());
    }
}
