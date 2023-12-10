package dev.skydynamic.quickbackupmulti.utils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Config {
    public static class QbmTempConfig {
        public Boolean isBackup = false;
        public MinecraftServer server;
        public int backupSlot;

        public void setIsBackupValue(Boolean value) {
            this.isBackup = value;
        }
        public void setServerValue(MinecraftServer server) {
            this.server = server;
        }
        public void setBackupSlot(int slot) {
            this.backupSlot = slot;
        }

    }

    public static class QuickBackupMultiConfig {
        private final Object lock = new Object();
        private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        private final Path configPath = Path.of(System.getProperty("user.dir") + "/config/");
        private ConfigStorage configStorage;
        File path = configPath.toFile();
        File config = configPath.resolve("QuickBackupMulti.json").toFile();

        public void load() {
            synchronized (lock){
                try {
                    if (!path.exists()|| !path.isDirectory()) {
                        path.mkdirs();
                    }
                    if (!config.exists()) {
                        saveModifiedConfig(ConfigStorage.DEFAULT);
                    }
                    var reader = new FileReader(config);
                    var result = gson.fromJson(reader, ConfigStorage.class);
                    this.configStorage = fixFields(result, ConfigStorage.DEFAULT);
                    saveModifiedConfig(this.configStorage);
                    reader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void saveModifiedConfig(ConfigStorage c) {
            synchronized (lock){
                try {
                    if (config.exists()) config.delete();
                    if (!config.exists()) config.createNewFile();
                    var writer = new FileWriter(config);
                    gson.toJson(fixFields(c, ConfigStorage.DEFAULT), writer);
                    writer.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private <T> T fixFields(T t, T defaultVal) {
            if (t == null){
                throw new NullPointerException();
            }
            if (t == defaultVal){
                return t;
            }
            try {
                var clazz = t.getClass();
                for (Field declaredField : clazz.getDeclaredFields()) {
                    if (Arrays.stream(declaredField.getDeclaredAnnotations()).anyMatch(it -> it.annotationType() == Ignore.class))
                        continue;
                    declaredField.setAccessible(true);
                    var value = declaredField.get(t);
                    var dv = declaredField.get(defaultVal);
                    if (value == null) {
                        declaredField.set(t, dv);
                    }
                }
                return t;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public List<String> getIgnoredFiles() {
            synchronized (lock) {
                return configStorage.ignoredFiles;
            }
        }

        public int getNumOfSlot() {
            synchronized (lock) {
                return configStorage.numOfSlots;
            }
        }

        public String getLang() {
            synchronized (lock) {
                return configStorage.lang;
            }
        }

        public void setLang(String lang) {
            synchronized (lock) {
                configStorage.lang = lang;
                saveModifiedConfig(configStorage);
            }
        }
    }

    public static QuickBackupMultiConfig INSTANCE = new QuickBackupMultiConfig();
    public static QbmTempConfig TEMPCONFIG = new QbmTempConfig();

}
