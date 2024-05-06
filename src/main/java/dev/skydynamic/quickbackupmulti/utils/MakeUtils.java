package dev.skydynamic.quickbackupmulti.utils;

import dev.morphia.query.filters.Filters;
import dev.skydynamic.quickbackupmulti.utils.config.Config;
import dev.skydynamic.quickbackupmulti.utils.storage.BackupInfo;
import dev.skydynamic.quickbackupmulti.utils.storage.DimensionFormat;
import dev.skydynamic.quickbackupmulti.utils.storage.FileHashes;
import dev.skydynamic.quickbackupmulti.utils.storage.IndexFile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;
import static dev.skydynamic.quickbackupmulti.utils.ScheduleUtils.startSchedule;
import static dev.skydynamic.quickbackupmulti.utils.hash.HashUtils.compareFileHash;
import static dev.skydynamic.quickbackupmulti.utils.hash.HashUtils.getFileHash;

public class MakeUtils {
    public static void copyFileAndMakeDirs(File destDir, File file) throws IOException {
        if (file.isDirectory()) {
            if (!file.getParentFile().getName().equals(Config.TEMP_CONFIG.worldName)) new File(destDir, file.getName()).mkdirs();
        } else {
            if (!file.getParentFile().getName().equals(Config.TEMP_CONFIG.worldName)) {
                File targetDir = new File(destDir, file.getParentFile().getName());
                targetDir.mkdirs();
                FileUtils.copyFileToDirectory(file, targetDir);
            } else {
                FileUtils.copyFileToDirectory(file, destDir);
            }
        }
    }

    private static void writeBackupInfo(String name, String desc, List<String> indexBackupList) {
//        try {
//            ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
//            data.put("desc", desc);
//            data.put("timestamp", System.currentTimeMillis());
//            var writer = new FileWriter(getBackupDir().resolve(name + "_info.json").toFile());
//            gson.toJson(data, writer);
//            writer.close();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

//        dataBase.createBackupTable(name, Enums.Type.BACKUP_INFO);
//        dataBase.insertBackupInfoData(name, desc, System.currentTimeMillis());
        new BackupInfo(name, desc, System.currentTimeMillis(), indexBackupList).save();
    }

    private static String getLatestBackup() {
        List<String> backupList = getBackupsList();
        if (backupList.isEmpty()) {
            return "null";
        } else {
            long time = 0;
            String latestBackupName = null;
            for (String name : backupList) {
                BackupInfo info = getDataBase().getSlotInfo(name);
                long timestamp = info.getTimestamp();
                if (Math.max(time, timestamp) == timestamp) {
                    latestBackupName = name;
                    time = timestamp;
                }
            }
            return latestBackupName;
        }
    }

    private static <T extends HashMap<String, String>> HashMap<String, Object> compareAndIndex(
        boolean isFirstBackup,
        String latestBackupName,
        FileHashes fileHashes,
        IndexFile indexFile,
        File file,
        File destCopyDir,
        T hashMap,
        T indexMap,
        List<String> indexBackupList
    ) throws Exception {
        String fileHash = getFileHash(file.toPath());
        if (!isFirstBackup) {
            if (compareFileHash(fileHashes, file.getParentFile(), file, fileHash)) {
                String index = indexFile.isIndexAndGetIndex(file.getParentFile(), file);
                if (index != null) {
                    indexMap.put(file.getName(), index);
                    if (!indexBackupList.contains(index)) {
                        indexBackupList.add(index);
                    }
                } else {
                    indexMap.put(file.getName(), latestBackupName);
                }
            } else {
                copyFileAndMakeDirs(destCopyDir, file);
            }
        }
        hashMap.put(file.getName(), fileHash);
        HashMap<String, Object> returnMap = new HashMap<>();
        returnMap.put("hash", hashMap);
        returnMap.put("index", indexMap);
        returnMap.put("indexBackupList", indexBackupList);
        return returnMap;
    }

    public static int make(ServerCommandSource commandSource, String name, String desc) {
        long startTime = System.currentTimeMillis();
        if (checkSlotExist(name)) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.fail_exists")));
            return 0;
        }
        try {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.start")));
            MinecraftServer server = commandSource.getServer();
            //#if MC>11800
            server.saveAll(true, true, true);
            //#else
            //$$ server.save(true, true, true);
            //#endif
            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = true;
            }
            if (!getBackupDir().resolve(name).toFile().exists()) getBackupDir().resolve(name).toFile().mkdir();
            if (Objects.requireNonNull(getBackupDir().resolve(name).toFile().listFiles()).length > 0) FileUtils.deleteDirectory(getBackupDir().resolve(name).toFile());
            // FileUtils.copyDirectory(savePath.toFile(), getBackupDir().resolve(name).toFile(), fileFilter);
            File destDir = getBackupDir().resolve(name).toFile();
            File[] saveFiles = savePath.toFile().listFiles((FilenameFilter) fileFilter);

            String latestBackupName = getLatestBackup();

            FileHashes fileHashedDocument = getDataBase().getDatastore().find(FileHashes.class).filter(Filters.eq("name", latestBackupName)).first();
            IndexFile indexFileDocument = getDataBase().getDatastore().find(IndexFile.class).filter(Filters.eq("name", latestBackupName)).first();

            boolean firstBackup = latestBackupName.equals("null");
            if (firstBackup) FileUtils.copyDirectory(savePath.toFile(), getBackupDir().resolve(name).toFile(), fileFilter);

            FileHashes fileHashes = new FileHashes(name);
            IndexFile indexFile = new IndexFile(name);
            HashMap<String, String> rootHashMap = new HashMap<>();
            HashMap<String, String> rootIndexMap = new HashMap<>();
            List<String> indexBackupList = new ArrayList<>();
            if (!firstBackup) indexBackupList.add(latestBackupName);
            for (File file : saveFiles) {
                if (file.isDirectory()) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    HashMap<String, String> indexMap = new HashMap<>();
                    DimensionFormat dimHashData = new DimensionFormat();
                    DimensionFormat dimIndexData = new DimensionFormat();
                    for (File dirFile : file.listFiles()) {
                        if (file.getName().equals("DIM1") || file.getName().equals("DIM-1")) {
                            if (dirFile.isDirectory()) {
                                hashMap = new HashMap<>();
                                indexMap = new HashMap<>();
                                for (File dirFile1 : dirFile.listFiles()) {
                                    HashMap<String, Object> resultMap = compareAndIndex(firstBackup, latestBackupName, fileHashedDocument, indexFileDocument, dirFile1, destDir, hashMap, indexMap, indexBackupList);
                                    hashMap = (HashMap<String, String>) resultMap.get("hash");
                                    indexMap = (HashMap<String, String>) resultMap.get("index");
                                    indexBackupList = (List<String>) resultMap.get("indexBackupList");
                                }
                                dimHashData.set(dirFile.getName(), hashMap);
                                dimIndexData.set(dirFile.getName(), indexMap);
                            }
                            fileHashes.setDim(file.getName(), dimHashData);
                            indexFile.setDim(file.getName(), dimIndexData);
                        } else {
                            if (file.getName().equals("datapacks")) {
                                if(dirFile.isDirectory()) {
                                    continue;
                                }
                            }
                            HashMap<String, Object> resultMap = compareAndIndex(firstBackup, latestBackupName, fileHashedDocument, indexFileDocument, dirFile, destDir, hashMap, indexMap, indexBackupList);
                            hashMap = (HashMap<String, String>) resultMap.get("hash");
                            indexMap = (HashMap<String, String>) resultMap.get("index");
                            indexBackupList = (List<String>) resultMap.get("indexBackupList");
                        }
                    }
                    fileHashes.set(file.getName(), hashMap);
                    indexFile.set(file.getName(), indexMap);
                } else {
                    HashMap<String, Object> resultMap = compareAndIndex(firstBackup, latestBackupName, fileHashedDocument, indexFileDocument, file, destDir, rootHashMap, rootIndexMap, indexBackupList);
                    rootHashMap = (HashMap<String, String>) resultMap.get("hash");
                    rootIndexMap = (HashMap<String, String>) resultMap.get("index");
                    indexBackupList = (List<String>) resultMap.get("indexBackupList");
                }
            }
            fileHashes.set("root", rootHashMap);
            indexFile.set("root", rootIndexMap);
            fileHashes.save();
            indexFile.save();

            FileUtils.deleteDirectory(getBackupDir().resolve(name).resolve(Config.TEMP_CONFIG.worldName).toFile());

            long endTime = System.currentTimeMillis();
            double intervalTime = (endTime - startTime) / 1000.0;
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.success", intervalTime)));

            writeBackupInfo(name, desc, indexBackupList);

            if (Config.INSTANCE.getScheduleBackup()) startSchedule(commandSource);

            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || !serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = false;
            }
        } catch (Exception e) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.fail", e.getMessage())));
            backupDir.resolve(name).toFile().deleteOnExit();
        }
        return 1;
    }

    public static boolean scheduleMake(ServerCommandSource commandSource, String name) {
        if (checkSlotExist(name)) return false;
        make(commandSource, name, "Scheduled Backup");
        return true;
    }

}
