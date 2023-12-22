package dev.skydynamic.quickbackupmulti.utils.config;

import java.util.*;

public class ConfigStorage {
    @Ignore
    public static final ConfigStorage DEFAULT = new ConfigStorage(5, new ArrayList<>(List.of("session.lock")), "zh_cn", false, "* * 0/4 * * ?", 14400, "interval");
    int numOfSlots;
    ArrayList<String> ignoredFiles;
    String lang;
    boolean scheduleBackup;
    String scheduleCron;
    int scheduleInterval;
    String scheduleMode;

    public ConfigStorage(int NumOfSlots, ArrayList<String> IgnoredFiles, String lang, boolean scheduleBackup, String scheduleCron, int scheduleInterval, String scheduleMode) {
        this.numOfSlots = NumOfSlots;
        this.ignoredFiles = IgnoredFiles;
        this.lang = lang;
        this.scheduleBackup = scheduleBackup;
        this.scheduleCron = scheduleCron;
        this.scheduleInterval = scheduleInterval;
        this.scheduleMode = scheduleMode;
    }

    public int getNumOfSlots() {
        return this.numOfSlots;
    }

    public List<String> getIgnoredFiles() {
        return this.ignoredFiles;
    }

    public String getLang() {
        return this.lang;
    }

    public boolean getScheduleBackup() {
        return this.scheduleBackup;
    }

    public String getScheduleCron() {
        return this.scheduleCron;
    }

    public int getScheduleInterval() {
        return this.scheduleInterval;
    }

    public String getScheduleMode() {
        return this.scheduleMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumOfSlots(), getIgnoredFiles(), getLang(), getScheduleBackup(), getScheduleCron(), getScheduleInterval(), getScheduleMode());
    }
}
