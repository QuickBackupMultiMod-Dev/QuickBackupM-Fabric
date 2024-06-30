package io.github.skydynamic.quickbackupmulti;

import io.github.skydynamic.increment.storage.lib.Interface.IDataBaseManager;

import java.nio.file.Path;

public class DataBaseManager implements IDataBaseManager {
    String fileName;
    String collectionName;
    Path dataBasePath;

    public DataBaseManager(String fileName, String collectionName, Path dataBasePath) {
        this.fileName = fileName;
        this.collectionName = collectionName;
        this.dataBasePath = dataBasePath;
    }

    @Override
    public void setFileName(String s) {
        this.fileName = s;
    }

    @Override
    public void setCollectionName(String s) {
        this.collectionName = s;
    }

    @Override
    public void setDataBasePath(Path path) {
        this.dataBasePath = path;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getCollectionName() {
        return this.collectionName;
    }

    @Override
    public Path getDataBasePath() {
        return this.dataBasePath;
    }
}