package dev.skydynamic.quickbackupmulti.utils.config;

import java.util.*;

public class ConfigStorage {
    @Ignore
    public static final ConfigStorage DEFAULT = new ConfigStorage(
        new ArrayList<>(List.of()),
        "zh_cn",
        false,
        "* * 0/4 * * ?",
        14400,
        "interval",
        true,
        "mongodb://localhost:27017"
    );

    public ArrayList<String> ignoredFiles;
    public String lang;
    public boolean scheduleBackup;
    public String scheduleCron;
    public int scheduleInterval;
    public String scheduleMode;

    public boolean useInternalDataBase;
    public String mongoDBUri;

    public ConfigStorage(
        ArrayList<String> IgnoredFiles,
        String lang,
        boolean scheduleBackup,
        String scheduleCron,
        int scheduleInterval,
        String scheduleMode,
        boolean useInternalDataBase,
        String mongoDBUri) {
        this.ignoredFiles = IgnoredFiles;
        this.lang = lang;
        this.scheduleBackup = scheduleBackup;
        this.scheduleCron = scheduleCron;
        this.scheduleInterval = scheduleInterval;
        this.scheduleMode = scheduleMode;
        this.useInternalDataBase = useInternalDataBase;
        this.mongoDBUri = mongoDBUri;
    }

    public boolean getScheduleBackup() {
        return this.scheduleBackup;
    }

    public String getScheduleMode() {
        return this.scheduleMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigStorage that)) return false;
        return getScheduleBackup() == that.getScheduleBackup() && scheduleInterval == that.scheduleInterval && useInternalDataBase == that.useInternalDataBase && Objects.equals(ignoredFiles, that.ignoredFiles) && Objects.equals(lang, that.lang) && Objects.equals(scheduleCron, that.scheduleCron) && Objects.equals(getScheduleMode(), that.getScheduleMode()) && Objects.equals(mongoDBUri, that.mongoDBUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoredFiles, lang, getScheduleBackup(), scheduleCron, scheduleInterval, getScheduleMode(), useInternalDataBase, mongoDBUri);
    }
}
