package dev.skydynamic.quickbackupmulti.utils;

public final class Enums {
    public enum Type {
        FILES_HASHES("FileHashes"),
        BACKUP_INFO("BackupInfo"),
        FILE_INDEX("IndexFile");
        public final String type;
        Type(String type) {
            this.type = type;
        }
    }

    public enum DimensionType {
        OVERWORLD("overworld", ""),
        NETHER("nether", "DIM-1"),
        END("end", "DIM1");
        public final String type;
        public final String dirName;
        DimensionType(String type, String dirName) {
            this.type = type;
            this.dirName = dirName;
        }

        public static DimensionType fromValue(String value) {
            for (DimensionType dimensionType : DimensionType.values()) {
                if (dimensionType.type.equals(value) || dimensionType.dirName.equals(value)) {
                    return dimensionType;
                }
            }
            throw new IllegalArgumentException("No enum constant for value: " + value);
        }
    }
}
