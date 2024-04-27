package dev.skydynamic.quickbackupmulti.utils.storage;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

import java.util.List;

import static dev.skydynamic.quickbackupmulti.utils.DataBase.getDatabase;

@Entity(value = "BackupInfo", useDiscriminator = false)
public class BackupInfo {
    @Id private ObjectId id;
    private String name;
    private String desc;
    private long timestamp;
    private List<String> indexBackup;
    public String getDesc() {
        return desc;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public List<String> getIndexBackup() {
        return indexBackup;
    }
    public String getName() {
        return name;
    }

    public void setIndexBackup(List<String> indexBackup) {
        this.indexBackup = indexBackup;
    }

    @Deprecated // Morphia only!
    public BackupInfo() {}

    public BackupInfo(String name, String desc, long timestamp, List<String> indexBackup) {
        this.name = name;
        this.desc = desc;
        this.timestamp = timestamp;
        this.indexBackup = indexBackup;
    }

    public void save() {
        getDatabase().save(this);
    }
}
