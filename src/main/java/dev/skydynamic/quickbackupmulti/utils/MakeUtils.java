package dev.skydynamic.quickbackupmulti.utils;

import dev.morphia.query.filters.Filters;
import dev.skydynamic.quickbackupmulti.config.Config;
import dev.skydynamic.quickbackupmulti.storage.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;
import static dev.skydynamic.quickbackupmulti.utils.ScheduleUtils.startSchedule;
import static dev.skydynamic.quickbackupmulti.utils.hash.HashUtils.compareFileHash;
import static dev.skydynamic.quickbackupmulti.utils.hash.HashUtils.getFileHash;

public class MakeUtils {
    private static final List<String> notFilterFolderList = Arrays.asList("playerdata", "stats", "advancements", "DIM1", "DIM-1", "data", "region", "poi", "entities");

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
        boolean isFirstBackup,       // 是否是第一次备份
        String latestBackupName,     // 最新备份的名字
        FileHashes fileHashes,       // 最新备份的哈希存储类
        IndexFile indexFile,         // 最新备份的索引存储类
        File file,                   // 目标文件
        File destCopyDir,            // 复制的目标文件夹
        T hashMap,                   // 需要改动的HashMap
        T indexMap,                  // 需要改动的IndexMap
        List<String> indexBackupList // 索引列表
    ) throws Exception {
        // 获取文件Hash
        String fileHash = getFileHash(file.toPath());
        // 如果不是第一次备份
        if (!isFirstBackup) {
            // 对比文件Hash
            if (compareFileHash(fileHashes, file.getParentFile(), file, fileHash)) {
                // 获取索引
                String index = indexFile.isIndexAndGetIndex(file.getParentFile(), file);
                // 如果最新备份中该文件没有索引到别的备份中, 则索引到最新存档中的文件
                if (index == null) index = latestBackupName;
                indexMap.put(file.getName(), index);
                // 如果索引的备份列表中没有索引文件的出处，则添加到索引列表
                if (!indexBackupList.contains(index)) {
                    indexBackupList.add(index);
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

    @SuppressWarnings("unchecked")
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
            // if (!firstBackup) indexBackupList.add(latestBackupName);
            // 开始循环存档文件夹
            for (File file : saveFiles) {
                // 判断是否是文件夹，并过滤mod创建的文件夹
                if (file.isDirectory()) {
                    // 过滤掉非原版文件夹，不备份
                    if (!notFilterFolderList.contains(file.getName())) continue;
                    // 新建一些表来临时用
                    HashMap<String, String> hashMap = new HashMap<>(); // hashMap     文件哈希临时存储Map
                    HashMap<String, String> indexMap = new HashMap<>(); // indexMap   文件索引临时存储Map
                    DimensionFormat dimHashData = new DimensionFormat(); // DIM Hash  哈希类
                    DimensionFormat dimIndexData = new DimensionFormat();// DIM Index 索引类
                    for (File dirFile : file.listFiles()) {
                        // DIM1和DIM-1文件格式有点特殊，单独区别
                        if (file.getName().equals("DIM1") || file.getName().equals("DIM-1")) {
                            // 如果是文件夹（原版情况下一般只有文件夹了xwx, 但还是要判断）
                            if (dirFile.isDirectory()) {
                                // 初始化
                                hashMap = new HashMap<>();
                                indexMap = new HashMap<>();
                                for (File dirFile1 : dirFile.listFiles()) {
                                    // 该处同root与别的文件夹
                                    HashMap<String, Object> resultMap = compareAndIndex(firstBackup, latestBackupName, fileHashedDocument, indexFileDocument, dirFile1, destDir, hashMap, indexMap, indexBackupList);
                                    hashMap = (HashMap<String, String>) resultMap.get("hash");
                                    indexMap = (HashMap<String, String>) resultMap.get("index");
                                    indexBackupList = (List<String>) resultMap.get("indexBackupList");
                                }
                                // 设置
                                dimHashData.set(dirFile.getName(), hashMap);
                                dimIndexData.set(dirFile.getName(), indexMap);
                            }
                            fileHashes.setDim(file.getName(), dimHashData);
                            indexFile.setDim(file.getName(), dimIndexData);
                        } else {
                            HashMap<String, Object> resultMap = compareAndIndex(firstBackup, latestBackupName, fileHashedDocument, indexFileDocument, dirFile, destDir, hashMap, indexMap, indexBackupList);
                            hashMap = (HashMap<String, String>) resultMap.get("hash");
                            indexMap = (HashMap<String, String>) resultMap.get("index");
                            indexBackupList = (List<String>) resultMap.get("indexBackupList");
                        }
                    }
                    fileHashes.set(file.getName(), hashMap);
                    indexFile.set(file.getName(), indexMap);
                } else {
                    // 不是文件夹则为root下的，不进行继续迭代
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

            // FileUtils.deleteDirectory(getBackupDir().resolve(name).resolve(Config.TEMP_CONFIG.worldName).toFile());

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
            LOGGER.error(e.toString());
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.fail",  e.toString())));
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
