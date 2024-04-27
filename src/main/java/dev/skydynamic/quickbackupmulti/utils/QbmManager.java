package dev.skydynamic.quickbackupmulti.utils;

import dev.skydynamic.quickbackupmulti.QbmConstant;
import dev.skydynamic.quickbackupmulti.i18n.Translate;
import dev.skydynamic.quickbackupmulti.utils.config.Config;

import dev.skydynamic.quickbackupmulti.utils.config.ConfigStorage;
import dev.skydynamic.quickbackupmulti.utils.filefilter.NonRecursiveDirFilter;
import dev.skydynamic.quickbackupmulti.utils.storage.DimensionFormat;
import dev.skydynamic.quickbackupmulti.utils.storage.IndexFile;
import net.fabricmc.api.EnvType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.quartz.SchedulerException;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static dev.skydynamic.quickbackupmulti.i18n.Translate.supportLanguage;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.DataBase.getDatabase;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.*;
import static dev.skydynamic.quickbackupmulti.utils.storage.JavaEditLevelFormat.dimFormatDirs;
import static dev.skydynamic.quickbackupmulti.utils.storage.JavaEditLevelFormat.saveFormatDirs;

public class QbmManager {
    public static Path backupDir = Path.of(QbmConstant.gameDir + "/QuickBackupMulti/");
    public static Path savePath = Config.TEMP_CONFIG.server.getSavePath(WorldSavePath.ROOT);
    public static IOFileFilter fileFilter = new NotFileFilter(new NameFileFilter(Config.INSTANCE.getIgnoredFiles()));
    public static IOFileFilter dirFilter = new NonRecursiveDirFilter();
    private static final DataBase dataBase = getDatabase();

    public static Path getBackupDir() {
        if (Config.TEMP_CONFIG.env == EnvType.SERVER) {
            return backupDir;
        } else {
            return backupDir.resolve(Config.TEMP_CONFIG.worldName);
        }
    }

    public static boolean checkSlotExist(String name) {
        return dataBase.getSlotExists(name) && getBackupDir().resolve(name).toFile().exists();
    }

    public static void copyIndexFiles(String name, Path targetPath) throws IOException {
        IndexFile indexFile = getDatabase().getIndexFile(name);
        for (String dir : saveFormatDirs) {
            if (dir.equals("DIM1") || dir.equals("DIM-1") || dir.equals(".")) {
                HashMap<String, String> indexFiles;
                for (String dimDir : dimFormatDirs) {
                    if (dir.equals(".")) {
                        indexFiles = indexFile.get(dimDir);
                        for (String fileName : indexFile.get(".").keySet()) {
                            FileUtils.copyFileToDirectory(getBackupDir().resolve(indexFile.get(".").get(fileName)).resolve(fileName).toFile(), targetPath.toFile());
                        }
                    } else {
                        indexFiles = indexFile.getDim(dir).get(dimDir);
                    }
                    for (String fileName : indexFiles.keySet()) {
                        File indexFilePath = getBackupDir().resolve(indexFiles.get(fileName)).resolve(dir).resolve(dimDir).resolve(fileName).toFile();
                        File targetFilePath = targetPath.resolve(dir).resolve(dimDir).toFile();
                        if (!targetFilePath.exists()) targetFilePath.mkdirs();
                        FileUtils.copyFileToDirectory(indexFilePath, targetFilePath);
                    }
                }
            } else {
                HashMap<String, String> indexFiles = indexFile.get(dir);
                for (String fileName : indexFiles.keySet()) {
                    File indexFilePath = getBackupDir().resolve(indexFiles.get(fileName)).resolve(dir).resolve(fileName).toFile();
                    File targetFilePath = targetPath.resolve(dir).toFile();
                    if (!targetFilePath.exists()) targetFilePath.mkdirs();
                    FileUtils.copyFileToDirectory(indexFilePath, targetFilePath);
                }
            }
        }
    }

    public static void reIndexFile(IndexFile indexFile, String sourceName, String targetName, boolean isDelete) {
        for (String dir : saveFormatDirs) {
            if (dir.equals("DIM1") || dir.equals("DIM-1") || dir.equals(".")) {
                HashMap<String, String> map;
                HashMap<String, String> newMap;
                for (String dimDir : dimFormatDirs) {
                    if (dir.equals(".")) {
                        map = indexFile.get(dimDir);
                        newMap = new HashMap<>(map);
                        for (String fileName : map.keySet()) {
                            if (map.get(fileName).equals(sourceName)) {
                                newMap.replace(fileName, targetName);
                                if (isDelete) newMap.remove(fileName);
                            }
                        }
                        indexFile.set(dimDir, newMap);

                        map = indexFile.get(".");
                        newMap = new HashMap<>(map);
                        for (String fileName : map.keySet()) {
                            if (map.get(fileName).equals(sourceName)) {
                                newMap.replace(fileName, targetName);
                                if (isDelete) newMap.remove(fileName);
                            }
                        }
                        indexFile.set("root", newMap);
                    } else {
                        DimensionFormat dimensionFormat = indexFile.getDim(dir);
                        map = dimensionFormat.get(dimDir);
                        newMap = new HashMap<>(map);
                        for (String fileName : map.keySet()) {
                            if (map.get(fileName).equals(sourceName)) {
                                newMap.replace(fileName, targetName);
                                if (isDelete) newMap.remove(fileName);
                            }
                        }
                        dimensionFormat.set(dimDir, newMap);
                        indexFile.setDim(dir, dimensionFormat);
                    }

                }
            } else {
                HashMap<String, String> map = indexFile.get(dir);
                HashMap<String, String> newMap = new HashMap<>(map);
                for (String fileName : map.keySet()) {
                    if (map.get(fileName).equals(sourceName)) {
                        newMap.replace(fileName, targetName);
                        if (isDelete) newMap.remove(fileName);
                    }
                }
                indexFile.set(dir, newMap);
            }
        }
        indexFile.save();
    }

    public static void restoreClient(String slot) {
        File targetBackupSlot = getBackupDir().resolve(slot).toFile();
        try {
            savePath.resolve("level.dat").toFile().delete();
            savePath.resolve("level.dat_old").toFile().delete();
            File[] fileList = savePath.toFile().listFiles((FilenameFilter) fileFilter);
            if (fileList != null) {
                Arrays.sort(fileList, ((o1, o2) -> {
                    if (o1.isDirectory() && o2.isDirectory()) {
                        return -1;
                    } else if (!o1.isDirectory() && o2.isDirectory()) {
                        return 1;
                    } else {
                        return o1.compareTo(o2);
                    }
                }));
                for (File file : fileList) {
                    FileUtils.forceDelete(file);
                }
            }
            FileUtils.copyDirectory(targetBackupSlot, savePath.toFile());
            copyIndexFiles(slot, savePath);
        } catch (IOException e) {
            restoreClient(slot);
        }
    }

    public static void restore(String slot) {
        File targetBackupSlot = getBackupDir().resolve(slot).toFile();
        try {
//            var it = Files.walk(savePath,5).sorted(Comparator.reverseOrder()).iterator();
//            while (it.hasNext()){
//                Files.delete(it.next());
//            }
            for (File file : Objects.requireNonNull(savePath.toFile().listFiles((FilenameFilter) fileFilter))) {
                FileUtils.forceDelete(file);
            }
            FileUtils.copyDirectory(targetBackupSlot, savePath.toFile());
            copyIndexFiles(slot, savePath);
        } catch (IOException e) {
            restore(slot);
        }
    }

    public static List<String> getBackupsList() {
        List<String> backupsDirList = new ArrayList<>();
        for (File file : Objects.requireNonNull(getBackupDir().toFile().listFiles())) {
            // if (file.isDirectory() && backupDir.resolve(file.getName()).toFile().exists() && backupDir.resolve(file.getName() + "_info.json").toFile().exists()) {
            if (file.isDirectory() && checkSlotExist(file.getName())) {
                backupsDirList.add(file.getName());
            }
        }
        return backupsDirList;
    }

    public static boolean delete(String name) {
        if (checkSlotExist(name)) {
            try {
                dataBase.reIndex(name);
                dataBase.deleteSlot(name);
                FileUtils.deleteDirectory(getBackupDir().resolve(name).toFile());
                return true;
            } catch (SecurityException | IOException e) {
                return false;
            }
        } else return false;
    }

    public static void createBackupDir(Path path) {
        if (!path.toFile().exists()) path.toFile().mkdirs();
    }

    public static void startSchedule(ServerCommandSource commandSource) {
        String nextBackupTimeString = "";
        try {
            switch (Config.INSTANCE.getScheduleMode()) {
                case "cron" -> nextBackupTimeString = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false);
                case "interval" -> nextBackupTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + Config.INSTANCE.getScheduleInrerval() * 1000L);
            }
            buildScheduler();
            Config.TEMP_CONFIG.scheduler.start();
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.success", nextBackupTimeString)));
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.fail", e)));
        }
    }

    public static int switchScheduleMode(ServerCommandSource commandSource, String mode) {
        try {
            if (Config.INSTANCE.getScheduleBackup()) {
                if (Config.TEMP_CONFIG.scheduler.isStarted()) Config.TEMP_CONFIG.scheduler.shutdown();
                startSchedule(commandSource);
            }
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.switch.fail", e)));
            return 0;
        }
        Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.switch.set", mode)));
        return 1;
    }

    public static int disableSchedule(ServerCommandSource commandSource) {
        try {
            Config.TEMP_CONFIG.scheduler.shutdown();
            Config.INSTANCE.setScheduleBackup(false);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.disable.success")));
            return 1;
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.disable.fail", e)));
            return 0;
        }
    }

    public static int setScheduleCron(ServerCommandSource commandSource, String value) throws SchedulerException {
        if (cronIsValid(value)) {
            if (Config.TEMP_CONFIG.scheduler != null) {
                if (Config.TEMP_CONFIG.scheduler.isStarted()) Config.TEMP_CONFIG.scheduler.shutdown();
            }
            Config.INSTANCE.setScheduleCron(value);
            if (Config.INSTANCE.getScheduleBackup()) {
                startSchedule(commandSource);
                if (Config.INSTANCE.getScheduleMode().equals("cron")) {
                    Messenger.sendMessage(commandSource,
                        Messenger.literal(tr("quickbackupmulti.schedule.cron.set_custom_success", getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false))));
                }
            } else {
                Messenger.sendMessage(commandSource,
                    Messenger.literal(tr("quickbackupmulti.schedule.cron.set_success_only")));
            }
        } else {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.cron.expression_error")));
            return 0;
        }
        return 1;
    }

    public static int setScheduleInterval(ServerCommandSource commandSource, int value, String type) throws SchedulerException {
        if (Config.TEMP_CONFIG.scheduler != null) {
            if (Config.TEMP_CONFIG.scheduler.isStarted()) Config.TEMP_CONFIG.scheduler.shutdown();
        }
        switch (type) {
            case "s" -> Config.INSTANCE.setScheduleInterval(value);
            case "m" -> Config.INSTANCE.setScheduleInterval(getSeconds(value, 0, 0));
            case "h" -> Config.INSTANCE.setScheduleInterval(getSeconds(0, value, 0));
            case "d" -> Config.INSTANCE.setScheduleInterval(getSeconds(0, 0, value));
        }
        if (Config.INSTANCE.getScheduleBackup()) {
            startSchedule(commandSource);
            if (Config.INSTANCE.getScheduleMode().equals("interval")) {
                Messenger.sendMessage(commandSource,
                    Messenger.literal(tr("quickbackupmulti.schedule.cron.set_success", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + Config.INSTANCE.getScheduleInrerval() * 1000L))));
            }
        } else {
            Messenger.sendMessage(commandSource,
                Messenger.literal(tr("quickbackupmulti.schedule.cron.set_success_only")));
        }
        return 1;
    }

    public static int setScheduleInterval(ServerCommandSource commandSource, int value) throws SchedulerException {
        return setScheduleInterval(commandSource, value, "s");
    }

    public static ConfigStorage verifyConfig(ConfigStorage c, PlayerEntity player) {
        ServerCommandSource commandSource = player.getCommandSource();

        // schedule enable
        if (c.scheduleBackup && !Config.INSTANCE.getScheduleBackup()) {
            startSchedule(commandSource);
        } else if (!c.scheduleBackup && Config.INSTANCE.getScheduleBackup()){
            disableSchedule(commandSource);
        }

        // schedule backup mode switch
        if (!c.scheduleMode.equals(Config.INSTANCE.getScheduleMode()))
            switchScheduleMode(commandSource, c.scheduleMode);

        // schedule set cron
        if (!c.scheduleCron.equals(Config.INSTANCE.getScheduleCron())) {
            try {
                setScheduleCron(commandSource, c.scheduleCron);
            } catch (SchedulerException e) {
                Messenger.sendMessage(commandSource,
                    Messenger.literal(tr("quickbackupmulti.schedule.cron.set_fail", e)));
            }
        }

        // schedule set interval
        if (!((Integer) c.scheduleInterval).equals(Config.INSTANCE.getScheduleInrerval())) {
            try {
                setScheduleInterval(commandSource, c.scheduleInterval);
            } catch (SchedulerException e) {
                Messenger.sendMessage(commandSource,
                    Messenger.literal(tr("quickbackupmulti.schedule.cron.set_fail", e)));
            }
        }

        // lang
        if (!c.lang.equals(Config.INSTANCE.getLang())) {
            if (!supportLanguage.contains(c.lang)) {
                Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.failed")));
                c.lang = Config.INSTANCE.getLang();
            } else {
                Translate.handleResourceReload(c.lang);
                Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.set", c.lang)));
            }
        }

        return c;
    }
}
