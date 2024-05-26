package edu.sysu.pmglab.sdfa.sv.idividual;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.gbc.genome.Individual;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;


/**
 * @author Wenjie Peng
 * @create 2024-03-22 20:10
 * @description
 */
public class PedManager {
    final File pedPath;
    boolean parsed = false;
    ByteCodeArray config = new ByteCodeArray();
    static CallableSet<ByteCode> properties = new CallableSet<>();
    CallableSet<Subject> subjectArray = new CallableSet<>();

    public PedManager(Object pedPath) {
        this.pedPath = new File(pedPath.toString());
    }

    private void parse() throws IOException {
        if (parsed) {
            return;
        }
        FileStream fs = new FileStream(pedPath);
        VolumeByteStream cache = new VolumeByteStream();
        while (fs.readLine(cache) != -1) {
            // ##开头的注释信息
            if (cache.startWith(new byte[]{ByteCode.NUMBER_SIGN,})) {
                config.add(cache.toUnmodifiableByteCode());
                cache.reset();
                continue;
            }
            if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                properties.addAll(cache.toUnmodifiableByteCode().split(ByteCode.TAB));
                cache.reset();
                break;
            }
        }

        int size = properties.size();
        do {
            ByteCode subjectByteCode = cache.toUnmodifiableByteCode();
            BaseArray<ByteCode> split = subjectByteCode.split(ByteCode.TAB);
            Subject subject = new Subject(split.get(1));
            for (int i = 0; i < size; i++) {
                subject.addProperty(properties.getByIndex(i), split.get(i));
            }
            subjectArray.add(subject);
        } while (fs.readLine(cache) != -1);
        fs.close();
        parsed = true;
    }
}
