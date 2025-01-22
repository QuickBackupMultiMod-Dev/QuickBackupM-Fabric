package io.github.skydynamic.quickbackupmulti.utils;

import io.github.skydynamic.increment.storage.lib.database.index.type.StorageInfo;
import io.github.skydynamic.increment.storage.lib.util.IndexUtil;
import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import net.fabricmc.api.EnvType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.deleteDataStore;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getStorager;

public class QbmManager {
    public static Path backupDir = Path.of(QbmConstant.pathGetter.getGamePath() + "/QuickBackupMulti/");
    public static Path savePath;
    public static IOFileFilter folderFilter = new NotFileFilter(new NameFileFilter(QuickBackupMulti.config.getIgnoredFolders()));
    public static IOFileFilter fileFilter = new NotFileFilter(new NameFileFilter(QuickBackupMulti.config.getIgnoredFiles()));

    public static Path getRootBackupDir() {
        return backupDir;
    }

    public static Path getBackupDir() {
        if (QuickBackupMulti.TEMP_CONFIG.env == EnvType.SERVER) {
            return getRootBackupDir();
        } else {
            return getRootBackupDir().resolve(QuickBackupMulti.TEMP_CONFIG.worldName);
        }
    }

    public static void restore(String slot) {
        File targetBackupSlot = getBackupDir().resolve(slot).toFile();
        try {
            for (File file : FileUtils.listFiles(savePath.toFile(), fileFilter, folderFilter)) {
                if (file.equals(savePath.toFile())) continue;
                FileUtils.forceDelete(file);
            }

            FileUtils.copyDirectory(targetBackupSlot, savePath.toFile());
            IndexUtil.copyIndexFile(
                slot,
                Path.of(QuickBackupMulti.config.getStoragePath()).resolve(QuickBackupMulti.TEMP_CONFIG.worldName),
                savePath.toFile()
            );
        } catch (IOException e) {
            LOGGER.error("Restore Failed", e);
        }
    }

    public static List<String> getBackupsList() {
        List<String> backupsDirList = new ArrayList<>();
        for (File file : Objects.requireNonNull(getBackupDir().toFile().listFiles())) {
            if (file.isDirectory() && getStorager().storageExists(file.getName())) {
                backupsDirList.add(file.getName());
            }
        }
        return backupsDirList;
    }

    public static boolean delete(String name) {
        if (getStorager().storageExists(name)) {
            try {
                IndexUtil.reIndex(name, QuickBackupMulti.TEMP_CONFIG.worldName);
                getStorager().deleteStorage(name);
                FileUtils.deleteDirectory(getBackupDir().resolve(name).toFile());
                return true;
            } catch (SecurityException | IOException e) {
                LOGGER.error("Delete Backup Failed", e);
                return false;
            }
        } else return false;
    }

    public static void deleteWorld(String worldName) {
        try {
            FileUtils.deleteDirectory(getRootBackupDir().resolve(worldName).toFile());
            deleteDataStore(worldName);
        } catch (IOException e) {
            LOGGER.error("Delete World Backup Data Failed", e);
        }
    }

    public static void createBackupDir(Path path) {
        if (!path.toFile().exists()) path.toFile().mkdirs();
    }

    public static List<StorageInfo> getScheduleBackupList() {
        Stream<StorageInfo> backupStream = getDataBase().getDatastore().find(StorageInfo.class).stream();
        return backupStream.filter(it -> it.getName().startsWith("ScheduleBackup-")).toList();
    }

    public static ServerPlayerEntity getPlayerFromCommandSource(ServerCommandSource source) {
        //#if MC<11900
        //$$ try {
        //$$     return source.getPlayer();
        //$$ } catch (Exception e) {
        //$$     throw new RuntimeException("Cannot get ServerPlayerEntity from ServerCommandSource");
        //$$ }
        //#else
        return source.getPlayer();
        //#endif
    }
}
