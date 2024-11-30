package io.github.skydynamic.quickbackupmulti.config;

import io.github.skydynamic.increment.storage.lib.Interface.IConfig;
import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class QuickBackupMultiConfig implements IConfig {
    private ConfigStorage config = new ConfigStorage();

    private final Path path;

    public QuickBackupMultiConfig(final Path path) {
        this.path = path;
    }

    public ConfigStorage getConfig() {
        return config;
    }

    public void setConfig(final ConfigStorage config) {
        this.config = config;
    }

    public boolean save() {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                QuickBackupMulti.LOGGER.error("Save {} error: create file failed.", path, e);
                return false;
            }
        }
        try (BufferedWriter bfw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            QbmConstant.GSON.toJson(getConfig(), bfw);
        } catch (IOException e) {
            QuickBackupMulti.LOGGER.error("Save {} error: write file failed.", path, e);
            return false;
        }
        return true;
    }

    public boolean load() {
        if (!Files.exists(path)) {
            return save();
        }

        try (BufferedReader bfr = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            setConfig(QbmConstant.GSON.fromJson(bfr, ConfigStorage.class));
        } catch (IOException e) {
            QuickBackupMulti.LOGGER.error("Load {} error: read file failed.", path, e);
            return false;
        }
        return true;
    }

    public boolean isCheckUpdate() {
        return config.checkUpdate;
    }

    public ArrayList<String> getIgnoredFiles() {
        ArrayList<String> ignoredFiles = new ArrayList<>(config.ignoredFiles);
        ignoredFiles.add("session.lock");
        return ignoredFiles;
    }


    public ArrayList<String> getIgnoredFolders() {
        return config.ignoredFolders;
    }


    public String getLang() {
        return config.lang;
    }

    public void setLang(String lang) {
        config.lang = lang;
    }

    public boolean isScheduleBackup() {
        return config.scheduleBackup;
    }

    public void setScheduleBackup(boolean scheduleBackup) {
        config.scheduleBackup = scheduleBackup;
    }

    public String getScheduleCron() {
        return config.scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        config.scheduleCron = scheduleCron;
    }

    public int getScheduleInterval() {
        return config.scheduleInterval;
    }

    public void setScheduleInterval(int scheduleInterval) {
        config.scheduleInterval = scheduleInterval;
    }

    public String getScheduleMode() {
        return config.scheduleMode;
    }

    public void setScheduleMode(String scheduleMode) {
        config.scheduleMode = scheduleMode;
    }

    public int getMaxScheduleBackup() {
        return config.maxScheduleBackup;
    }

    public AutoRestartMode getAutoRestartMode() {
        return config.autoRestartMode;
    }

    public void setAutoRestartMode(AutoRestartMode autoRestartMode) {
        config.autoRestartMode = autoRestartMode;
    }


    @Override
    public boolean getUseInternalDataBase() {
        return config.useInternalDataBase;
    }

    public void setUseInternalDataBase(boolean useInternalDataBase) {
        config.useInternalDataBase = useInternalDataBase;
    }

    @Override
    public String getMongoDBUri() {
        return config.mongoDBUri;
    }

    @Override
    public String getStoragePath() {
        return config.storagePath;
    }

    @SuppressWarnings("FieldMayBeFinal")
    public static class ConfigStorage {
        private boolean checkUpdate = true;
        private ArrayList<String> ignoredFiles = new ArrayList<>();
        private ArrayList<String> ignoredFolders = new ArrayList<>();
        private String lang = "zh_cn";
        private boolean scheduleBackup = false;
        private String scheduleCron = "* * 0/4 * * ?";
        private int scheduleInterval = 14400;
        private String scheduleMode = "interval";
        private int maxScheduleBackup = 10;

        private AutoRestartMode autoRestartMode = AutoRestartMode.DEFAULT;

        private boolean useInternalDataBase = true;
        private String mongoDBUri = "mongodb://localhost:27017";
        private String storagePath = "QuickBackupMulti";

        public void setScheduleMode(String scheduleMode) {
            this.scheduleMode = scheduleMode;
        }

        public void setScheduleBackup(boolean scheduleBackup) {
            this.scheduleBackup = scheduleBackup;
        }

        public void setScheduleCron(String scheduleCron) {
            this.scheduleCron = scheduleCron;
        }

        public void setScheduleInterval(int scheduleInterval) {
            this.scheduleInterval = scheduleInterval;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public boolean isScheduleBackup() {
            return scheduleBackup;
        }

        public String getScheduleCron() {
            return scheduleCron;
        }

        public int getScheduleInterval() {
            return scheduleInterval;
        }

        public String getScheduleMode() {
            return scheduleMode;
        }

        public String getLang() {
            return lang;
        }

        @Override
        public String toString() {
            return "ConfigStorage [checkUpdate=" + checkUpdate + ", ignoredFiles=" + ignoredFiles
                + ", ignoredFolders=" + ignoredFolders + ", lang=" + lang + ", scheduleBackup=" + scheduleBackup
                + ", scheduleCron=" + scheduleCron + ", scheduleInterval=" + scheduleInterval
                + ", scheduleMode=" + scheduleMode + ", maxScheduleBackup=" + maxScheduleBackup
                + ", autoRestartMode=" + autoRestartMode + ", useInternalDataBase="
                + useInternalDataBase + ", mongoDBUri=" + mongoDBUri + ", storagePath="
                + storagePath + "]";
        }
    }
}

