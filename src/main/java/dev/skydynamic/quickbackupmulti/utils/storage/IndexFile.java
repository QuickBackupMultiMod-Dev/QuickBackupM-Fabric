package dev.skydynamic.quickbackupmulti.utils.storage;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.skydynamic.quickbackupmulti.utils.Enums;
import org.bson.types.ObjectId;

import javax.annotation.Nullable;
import java.io.File;

import static dev.skydynamic.quickbackupmulti.utils.DataBase.getDatabase;

@Entity(value = "IndexFile", useDiscriminator = false)
public class IndexFile extends JavaEditLevelFormat{
    @Id private ObjectId id;
    private String name;

    @Deprecated
    public IndexFile() {}

    @Nullable
    public String isIndexAndGetIndex(File Folders, File file) {
        if (Folders.getParentFile().getName().equals("DIM1") || Folders.getParentFile().getName().equals("DIM-1")) {
            return getFileIndex(Enums.DimensionType.fromValue(Folders.getParentFile().getName()), Folders, file);
        }
        return get(Folders.getName()).get(file.getName());
    }

    public String getFileIndex(Enums.DimensionType targetDimensionName, File Folders, File file) {
        switch (targetDimensionName) {
            case OVERWORLD -> {
                return get(Folders.getName()).get(file.getName());
            }
            case NETHER -> {
                return getDIM_1().get(Folders.getName()).get(file.getName());
            }
            case END -> {
                return getDIM1().get(Folders.getName()).get(file.getName());
            }
        }
        return null;
    }

    public IndexFile(String name) {
        this.name = name;
    }

    public void save() {
        getDatabase().save(this);
    }

    public void delete() {
        getDatabase().delete(this);
    }
}
