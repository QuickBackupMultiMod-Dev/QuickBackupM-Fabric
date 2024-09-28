package io.github.skydynamic.quickbackupmulti.config;

public class Config {
    public static QuickBackupMultiConfig INSTANCE = new QuickBackupMultiConfig();
    public static QbmTempConfig TEMP_CONFIG = new QbmTempConfig();

    public enum AutoRestartMode {
        DISABLE,
        DEFAULT,
        MCSM
    }
}
