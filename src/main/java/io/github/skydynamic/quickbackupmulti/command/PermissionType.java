package io.github.skydynamic.quickbackupmulti.command;

public enum PermissionType {
    USER(0),
    HELPER(1),
    ADMIN(2);

    final int level;

    PermissionType(int level) {
        this.level = level;
    }

    public static PermissionType getByLevelInt(int level) {
        for (PermissionType type : PermissionType.values()) {
            if(type.level == level) {
                return type;
            }
        }
        throw new IllegalArgumentException("Level is invalid");
    }
}