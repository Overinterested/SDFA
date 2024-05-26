package edu.sysu.pmglab.sdfa.sv.idividual;

import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;

import java.util.Collection;


/**
 * @author Wenjie Peng
 * @create 2024-03-22 20:21
 * @description
 */
public class Subjects {
    int fileID;
    File filePath;
    final CallableSet<Subject> subjectSet;

    public Subjects(File filePath) {
        this.filePath = filePath;
        subjectSet = new CallableSet<>(4);
    }

    public Subjects addSubject(Subject subject) {
        subjectSet.add(subject);
        return this;
    }

    public int numOfSubjects() {
        return subjectSet.size();
    }

    public Subjects addAll(Array<Subject> subjects) {
        this.subjectSet.addAll(subjects);
        return this;
    }

    public Subjects addAll(Collection<Subject> subjects) {
        this.subjectSet.addAll(subjects);
        return this;
    }

    public CallableSet<Subject> getSubjectSet() {
        return subjectSet;
    }

    public int getSize() {
        return subjectSet.size();
    }

    public void clear() {
        fileID = -1;
        filePath = null;
        subjectSet.clear();
    }

    public Subject getSubject(int index) {
        return subjectSet.getByIndex(index);
    }

    public Subjects asUnmodified() {
        Subjects subjects = new Subjects(new File(filePath.toString()));
        for (int i = 0; i < subjectSet.size(); i++) {
            subjects.addSubject(subjectSet.getByIndex(i).asUnmodified());
        }
        subjects.fileID = fileID;
        return subjects;
    }

    public Subjects setFilePath(File filePath) {
        this.filePath = filePath;
        return this;
    }
}
