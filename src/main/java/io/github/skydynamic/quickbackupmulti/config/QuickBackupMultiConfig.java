package io.github.skydynamic.quickbackupmulti.config;

import io.github.skydynamic.increment.storage.lib.util.IndexUtil;
import io.github.skydynamic.quickbackupmulti.QbmConstant;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.skydynamic.quickbackupmulti.QbmConstant.gson;

public class QuickBackupMultiConfig {
    private final Object lock = new Object();
    private final Path configPath = QbmConstant.pathGetter.getConfigPath();
    private ConfigStorage configStorage;
    File path = configPath.toFile();
    File config = configPath.resolve("QuickBackupMulti.json").toFile();

    public void load() {
        synchronized (lock) {
            try {
                if (!path.exists() || !path.isDirectory()) {
                    path.mkdirs();
                }
                if (!config.exists()) {
                    saveModifiedConfig(ConfigStorage.DEFAULT);
                }
                FileReader reader = new FileReader(config);
                ConfigStorage result = gson.fromJson(reader, ConfigStorage.class);
                this.configStorage = fixFields(result, ConfigStorage.DEFAULT);
                saveModifiedConfig(this.configStorage);
                reader.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveModifiedConfig(ConfigStorage c) {
        synchronized (lock) {
            try {
                if (config.exists()) config.delete();
                if (!config.exists()) config.createNewFile();
                FileWriter writer = new FileWriter(config);
                ConfigStorage fixConfig = fixFields(c, ConfigStorage.DEFAULT);
                gson.toJson(fixConfig, writer);
                writer.close();
                IndexUtil.setConfig(fixConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ConfigStorage fixFields(ConfigStorage t, ConfigStorage defaultVal) {
        if (t == null) {
            throw new NullPointerException();
        }
        if (t.equals(defaultVal)) {
            return t;
        }
        try {
            Class<?> clazz = t.getClass();
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (Arrays.stream(declaredField.getDeclaredAnnotations()).anyMatch(it -> it.annotationType() == Ignore.class))
                    continue;
                declaredField.setAccessible(true);
                Object value = declaredField.get(t);
                Object dv = declaredField.get(defaultVal);
                if (value == null) {
                    declaredField.set(t, dv);
                }
            }
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigStorage getConfigStorage() {
        synchronized (lock) {
            return fixFields(configStorage, ConfigStorage.DEFAULT);
        }
    }

    public void setConfigStorage(ConfigStorage configStorage) {
        synchronized (lock) {
            this.configStorage = configStorage;
            saveModifiedConfig(configStorage);
        }
    }

    public List<String> getIgnoredFiles() {
        synchronized (lock) {
            List<String> list = new ArrayList<>(configStorage.getIgnoredFiles());
            list.add("session.lock");
            return list;
        }
    }

    public String getLang() {
        synchronized (lock) {
            return configStorage.getLang();
        }
    }

    public boolean getScheduleBackup() {
        synchronized (lock) {
            return configStorage.isScheduleBackup();
        }
    }

    public String getScheduleCron() {
        synchronized (lock) {
            return configStorage.getScheduleCron();
        }
    }

    public int getScheduleInterval() {
        synchronized (lock) {
            return configStorage.getScheduleInterval();
        }
    }

    public String getScheduleMode() {
        synchronized (lock) {
            return configStorage.getScheduleMode();
        }
    }

    public int getScheduleMaxBackup() {
        synchronized (lock) {
            return configStorage.getMaxScheduleBackup();
        }
    }

    public Config.AutoRestartMode getAutoRestartMode() {
        synchronized (lock) {
            return configStorage.getAutoRestartMode();
        }
    }

    public boolean getUseInternalDataBase() {
        synchronized (lock) {
            return configStorage.getUseInternalDataBase();
        }
    }

    public String getMongoDBUri() {
        synchronized (lock) {
            return configStorage.getMongoDBUri();
        }
    }

    public String getStoragePath() {
        synchronized (lock) {
            return configStorage.getStoragePath();
        }
    }

    public void setLang(String lang) {
        synchronized (lock) {
            configStorage.setLang(lang);
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleCron(String value) {
        synchronized (lock) {
            configStorage.setScheduleCron(value);
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleInterval(int value) {
        synchronized (lock) {
            configStorage.setScheduleInterval(value);
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleBackup(boolean value) {
        synchronized (lock) {
            configStorage.setScheduleBackup(value);
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleMode(String mode) {
        synchronized (lock) {
            configStorage.setScheduleMode(mode);
            saveModifiedConfig(configStorage);
        }
    }

    public void setAutoRestartMode(Config.AutoRestartMode mode) {
        synchronized (lock) {
            configStorage.setAutoRestartMode(mode);
            saveModifiedConfig(configStorage);
        }
    }

    public void setUseInternalDataBase(boolean value) {
        synchronized (lock) {
            configStorage.setUseInternalDataBase(value);
            saveModifiedConfig(configStorage);
        }
    }
}
