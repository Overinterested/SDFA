package edu.sysu.pmglab.sdfa.sv.idividual;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.ReusableMap;


/**
 * @author Wenjie Peng
 * @create 2024-03-22 20:06
 * @description
 */
public class Subject {
    ByteCode name;
    int indexInFile;
    ReusableMap<ByteCode, Object> property;

    public Subject(ByteCode name) {
        this.name = name;
        property = new ReusableMap<>(4);
    }


    public Subject setName(ByteCode name) {
        this.name = name;
        return this;
    }


    public Subject setIndexInFile(int indexInFile) {
        this.indexInFile = indexInFile;
        return this;
    }

    public Subject setProperty(ReusableMap<ByteCode, Object> property) {
        this.property = property;
        return this;
    }

    public ByteCode getName() {
        return name;
    }

    public Subject addProperty(ByteCode key, ByteCode value) {
        this.property.put(key, value);
        return this;
    }

    public Subject asUnmodified() {
        Subject subject = new Subject(this.name.asUnmodifiable());
        subject.indexInFile = indexInFile;
        subject.property = property;
        return subject;
    }
}
