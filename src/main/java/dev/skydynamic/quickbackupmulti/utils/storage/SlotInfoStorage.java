package dev.skydynamic.quickbackupmulti.utils.storage;

import java.util.List;
import java.util.Objects;

public class SlotInfoStorage {
    String desc;
    long timestamp;
    List<String> indexFiles;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getIndexFiles() {
        return indexFiles;
    }

    public void setIndexFiles(List<String> indexFiles) {
        this.indexFiles = indexFiles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDesc(), getTimestamp());
    }
}
