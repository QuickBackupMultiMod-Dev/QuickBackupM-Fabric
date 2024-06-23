package dev.skydynamic.quickbackupmulti.config;

import dev.skydynamic.quickbackupmulti.QbmConstant;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static dev.skydynamic.quickbackupmulti.QbmConstant.gson;

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
                gson.toJson(fixFields(c, ConfigStorage.DEFAULT), writer);
                writer.close();
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

    public String getConfigStorage() {
        synchronized (lock) {
            return gson.toJson(fixFields(configStorage, ConfigStorage.DEFAULT));
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
            List<String> list = configStorage.ignoredFiles;
            list.add("session.lock");
            return list;
        }
    }

    public String getLang() {
        synchronized (lock) {
            return configStorage.lang;
        }
    }

    public boolean getScheduleBackup() {
        synchronized (lock) {
            return configStorage.scheduleBackup;
        }
    }

    public String getScheduleCron() {
        synchronized (lock) {
            return configStorage.scheduleCron;
        }
    }

    public int getScheduleInrerval() {
        synchronized (lock) {
            return configStorage.scheduleInterval;
        }
    }

    public String getScheduleMode() {
        synchronized (lock) {
            return configStorage.scheduleMode;
        }
    }

    public boolean getUseInternalDataBase() {
        synchronized (lock) {
            return configStorage.useInternalDataBase;
        }
    }

    public String getMongoDBUri() {
        synchronized (lock) {
            return configStorage.mongoDBUri;
        }
    }

    public boolean getUseFastHash() {
        synchronized (lock) {
            return configStorage.useFastHash;
        }
    }

    public void setLang(String lang) {
        synchronized (lock) {
            configStorage.lang = lang;
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleCron(String value) {
        synchronized (lock) {
            configStorage.scheduleCron = value;
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleInterval(int value) {
        synchronized (lock) {
            configStorage.scheduleInterval = value;
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleBackup(boolean value) {
        synchronized (lock) {
            configStorage.scheduleBackup = value;
            saveModifiedConfig(configStorage);
        }
    }

    public void setScheduleMode(String mode) {
        synchronized (lock) {
            configStorage.scheduleMode = mode;
            saveModifiedConfig(configStorage);
        }
    }

    public void setUseInternalDataBase(boolean value) {
        synchronized (lock) {
            configStorage.useInternalDataBase = value;
            saveModifiedConfig(configStorage);
        }
    }

}
