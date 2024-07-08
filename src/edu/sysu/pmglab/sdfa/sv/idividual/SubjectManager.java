package edu.sysu.pmglab.sdfa.sv.idividual;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFReader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Wenjie Peng
 * @create 2024-03-22 20:10
 * @description
 */
public class SubjectManager {
    Map<File, Subjects> fileSubjectsMap = new HashMap<>();
    Map<ByteCode, Subject> nameSubjectMap = new HashMap<>();
    CallableSet<Subject> indexableSubjects = new CallableSet<>();
    private static final SubjectManager instance = new SubjectManager();

    public void register(File file, Subjects subjects) {
        if (fileSubjectsMap.containsKey(file)) {
            return;
        }
        this.fileSubjectsMap.put(file, subjects);
        CallableSet<Subject> subjectSet = subjects.getSubjectSet();
        for (Subject subject : subjectSet) {
            subject = subject.asUnmodified();
            this.nameSubjectMap.put(subject.name, subject);
            this.indexableSubjects.add(subject);
        }
    }

    public int numOfAllSubjects() {
        return indexableSubjects.size();
    }

    public int numOfFileSubjects(File file) {
        Subjects subjects = fileSubjectsMap.get(file);
        if (subjects == null) {
            return 0;
        } else {
            return subjects.numOfSubjects();
        }
    }

    public void register(SDFReader reader) {
        SDFMeta meta = reader.getMeta();
        if (meta == null) {
            meta = SDFMeta.decode(reader);
        }
        Subjects subjects = meta.getSubjects();
        if (subjects == null) {
            return;
        }
        register(reader.getFilePath(), subjects);
    }

    public static SubjectManager getInstance() {
        return instance;
    }

    public CallableSet<Subject> getIndexableSubjects() {
        return indexableSubjects;
    }
}
