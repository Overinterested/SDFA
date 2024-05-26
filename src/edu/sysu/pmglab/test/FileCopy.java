package edu.sysu.pmglab.test;


import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.wrapper.FileTool;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-04 21:46
 * @description
 */
public class FileCopy {
    static Array<String> callMethods = new Array<>();

    static {
        callMethods.add("sniffles");
        callMethods.add("cutesv");
        callMethods.add("debreak");
        callMethods.add("delly");
        callMethods.add("svision");
        callMethods.add("picky");
        callMethods.add("pbsv");
        callMethods.add("svim");
        callMethods.add("nanovar");
        callMethods.add("nanosv");
    }

    public static void main(String[] args) throws IOException {
        File source = new File("");
        File target = new File("");
        Array<File> files = FileTool.getFilesFromDir(source, ".vcf");
        for (File file : files) {
            for (int i = 0; i < callMethods.size(); i++) {
                String callMethod = callMethods.get(i);
                if (file.getName().toLowerCase().contains(callMethod)) {
                    if (i == 0) {
                        if (callMethod.contains("sniffles2")) {
                            File targetDir = FileTool.checkDirWithCreate(new File(target + "/sniffles2"));
                            file.copyTo(targetDir.getSubFile(file.getName()));
                        }
                    } else if (i == 1) {
                        if (callMethod.contains("cutesv2")) {
                            File targetDir = FileTool.checkDirWithCreate(new File(target + "/cutesv2"));
                            file.copyTo(targetDir.getSubFile(file.getName()));
                        }
                    } else {
                        File targetDir = FileTool.checkDirWithCreate(new File(target + "/" + callMethod));
                        file.copyTo(targetDir.getSubFile(file.getName()));
                    }
                }
            }

        }

    }
}
