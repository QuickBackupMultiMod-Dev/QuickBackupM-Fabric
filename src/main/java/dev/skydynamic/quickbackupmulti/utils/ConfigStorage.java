package dev.skydynamic.quickbackupmulti.utils;

import java.util.*;

public class ConfigStorage {
    @Ignore
    public static final ConfigStorage DEFAULT = new ConfigStorage(5, new ArrayList<>(List.of("session.lock")), "zh_cn");
    int numOfSlots;
    ArrayList<String> ignoredFiles;
    String lang;

    public ConfigStorage(int NumOfSlots, ArrayList<String> IgnoredFiles, String lang) {
        this.numOfSlots = NumOfSlots;
        this.ignoredFiles = IgnoredFiles;
        this.lang = lang;
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

    @Override
    public int hashCode() {
        return Objects.hash(getNumOfSlots(), getIgnoredFiles(), getLang());
    }
}
