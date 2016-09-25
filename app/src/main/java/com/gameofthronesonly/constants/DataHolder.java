package com.gameofthronesonly.constants;

import android.util.SparseArray;

import com.quickblox.content.model.QBFile;

import java.util.Collection;

/**
 * Created by NilayS on 7/6/2016.
 */
public class DataHolder {

    private static DataHolder instance;
    private SparseArray<QBFile> qbFiles;

    private DataHolder() {
        qbFiles = new SparseArray<>();
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void addQbFiles(Collection<QBFile> qbFileList) {
        for (QBFile qbFile : qbFileList) {
            addQbFile(qbFile);
        }
    }

    public SparseArray<QBFile> getQBFiles() {
        return qbFiles;
    }

    public boolean isEmpty() {
        return qbFiles.size() == 0;
    }

    public void clear() {
        qbFiles.clear();
    }

    public boolean isEnd(int count) {
        return qbFiles.size() == count;
    }

    public int getSize() {
        return qbFiles.size();
    }

    public void addQbFile(QBFile qbFile) {
        qbFiles.put(qbFile.getId(), qbFile);
    }

    public QBFile getQBFileByCount(int counter) {
        return qbFiles.valueAt(counter);
    }

    public QBFile getQBFileById(int id) {
        return qbFiles.get(id);
    }
}
