package dev.skydynamic.quickbackupmulti.utils.config;

import java.util.*;

public class ConfigStorage {
    @Ignore
    public static final ConfigStorage DEFAULT = new ConfigStorage(5, new ArrayList<>(List.of("session.lock")), "zh_cn", true);
    int numOfSlots;
    ArrayList<String> ignoredFiles;
    String lang;
    boolean shouldCheckUpdate;

    public ConfigStorage(int NumOfSlots, ArrayList<String> IgnoredFiles, String lang, boolean shouldCheckUpdate) {
        this.numOfSlots = NumOfSlots;
        this.ignoredFiles = IgnoredFiles;
        this.lang = lang;
        this.shouldCheckUpdate = shouldCheckUpdate;
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

    public boolean getShouldCheckUpdate() {
        return this.shouldCheckUpdate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumOfSlots(), getIgnoredFiles(), getLang(), getShouldCheckUpdate());
    }
}
