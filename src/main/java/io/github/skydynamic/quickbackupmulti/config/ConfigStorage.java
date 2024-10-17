package io.github.skydynamic.quickbackupmulti.config;

import io.github.skydynamic.increment.storage.lib.Interface.IConfig;

import java.util.ArrayList;

public class ConfigStorage implements IConfig {
    @Ignore
    public static final ConfigStorage DEFAULT = new ConfigStorage(
            new ArrayList<>(),
            new ArrayList<>(),
            "zh_cn",
            false,
            "* * 0/4 * * ?",
            14400,
            "interval",
            10,
            Config.AutoRestartMode.DEFAULT,
            true,
            "mongodb://localhost:27017",
            "QuickBackupMulti"
    );

    private ArrayList<String> ignoredFiles;
    private ArrayList<String> ignoredFolders;
    private String lang;
    private boolean scheduleBackup;
    private String scheduleCron;
    private int scheduleInterval;
    private String scheduleMode;
    private int maxScheduleBackup;

    private Config.AutoRestartMode autoRestartMode;

    private boolean useInternalDataBase;
    private String mongoDBUri;
    private String storagePath;

    public ConfigStorage(
            ArrayList<String> IgnoredFiles,
            ArrayList<String> ignoredFolders,
            String lang,
            boolean scheduleBackup,
            String scheduleCron,
            int scheduleInterval,
            String scheduleMode,
            int maxScheduleBackup,
            Config.AutoRestartMode autoRestartMode,
            boolean useInternalDataBase,
            String mongoDBUri,
            String storagePath) {
        this.ignoredFiles = IgnoredFiles;
        this.ignoredFolders = ignoredFolders;
        this.lang = lang;
        this.scheduleBackup = scheduleBackup;
        this.scheduleCron = scheduleCron;
        this.scheduleInterval = scheduleInterval;
        this.scheduleMode = scheduleMode;
        this.maxScheduleBackup = maxScheduleBackup;
        this.autoRestartMode = autoRestartMode;
        this.useInternalDataBase = useInternalDataBase;
        this.mongoDBUri = mongoDBUri;
        this.storagePath = storagePath;
    }

    public ArrayList<String> getIgnoredFiles() {
        return ignoredFiles;
    }

    public void setIgnoredFiles(ArrayList<String> ignoredFiles) {
        this.ignoredFiles = ignoredFiles;
    }

    public ArrayList<String> getIgnoredFolders() {
        return ignoredFolders;
    }

    public void setIgnoredFolders(ArrayList<String> ignoredFolders) {
        this.ignoredFolders = ignoredFolders;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isScheduleBackup() {
        return scheduleBackup;
    }

    public void setScheduleBackup(boolean scheduleBackup) {
        this.scheduleBackup = scheduleBackup;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    public int getScheduleInterval() {
        return scheduleInterval;
    }

    public void setScheduleInterval(int scheduleInterval) {
        this.scheduleInterval = scheduleInterval;
    }

    public String getScheduleMode() {
        return scheduleMode;
    }

    public int getMaxScheduleBackup() {
        return maxScheduleBackup;
    }

    public void setMaxScheduleBackup(int maxScheduleBackup) {
        this.maxScheduleBackup = maxScheduleBackup;
    }

    public void setScheduleMode(String scheduleMode) {
        this.scheduleMode = scheduleMode;
    }

    public Config.AutoRestartMode getAutoRestartMode() {
        return autoRestartMode;
    }

    public void setAutoRestartMode(Config.AutoRestartMode autoRestartMode) {
        this.autoRestartMode = autoRestartMode;
    }

    @Override
    public void setUseInternalDataBase(boolean b) {
        this.useInternalDataBase = b;
    }

    @Override
    public void setMongoDBUri(String s) {
        this.mongoDBUri = s;
    }

    @Override
    public boolean getUseInternalDataBase() {
        return this.useInternalDataBase;
    }

    @Override
    public String getMongoDBUri() {
        return this.mongoDBUri;
    }

    @Override
    public String getStoragePath() {
        return this.storagePath;
    }
}
