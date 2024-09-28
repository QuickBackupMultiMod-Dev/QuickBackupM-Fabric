package io.github.skydynamic.quickbackupmulti.utils;

import io.github.skydynamic.increment.storage.lib.util.IndexUtil;
import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.config.Config;
import io.github.skydynamic.quickbackupmulti.config.ConfigStorage;
import io.github.skydynamic.quickbackupmulti.i18n.Translate;
import net.fabricmc.api.EnvType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getStorager;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.supportLanguage;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;

public class QbmManager {
    public static Path backupDir = Path.of(QbmConstant.pathGetter.getGamePath() + "/QuickBackupMulti/");
    public static Path savePath;
    public static IOFileFilter fileFilter = new NotFileFilter(new NameFileFilter(Config.INSTANCE.getIgnoredFiles()));
    // public static IOFileFilter dirFilter = new NonRecursiveDirFilter();

    public static Path getBackupDir() {
        if (Config.TEMP_CONFIG.env == EnvType.SERVER) {
            return backupDir;
        } else {
            return backupDir.resolve(Config.TEMP_CONFIG.worldName);
        }
    }

    public static void restoreClient(String slot) {
        File targetBackupSlot = getBackupDir().resolve(slot).toFile();
        try {
            final Iterator<Path> iterator = Files.walk(savePath)
                    .sorted(Comparator.reverseOrder())
                    .iterator();
            while (iterator.hasNext()) {
                Path p = iterator.next();
                if (p.equals(savePath)) continue;
                Files.delete(p);
            }
            //FileUtils.deleteDirectory(savePath.toFile());
            FileUtils.copyDirectory(targetBackupSlot, savePath.toFile());
            IndexUtil.copyIndexFile(
                    slot,
                    Path.of(Config.INSTANCE.getStoragePath()).resolve(Config.TEMP_CONFIG.worldName),
                    savePath.toFile()
            );
        } catch (IOException e) {
            LOGGER.error("Restore Failed", e);
        }
    }

    public static void restore(String slot) {
        File targetBackupSlot = getBackupDir().resolve(slot).toFile();
        try {
            for (File file : Objects.requireNonNull(savePath.toFile().listFiles((FilenameFilter) fileFilter))) {
                FileUtils.forceDelete(file);
            }
            FileUtils.copyDirectory(targetBackupSlot, savePath.toFile());
            IndexUtil.copyIndexFile(
                    slot,
                    Path.of(Config.INSTANCE.getStoragePath()).resolve(Config.TEMP_CONFIG.worldName),
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
                IndexUtil.reIndex(name, Config.TEMP_CONFIG.worldName);
                getStorager().deleteStorage(name);
                FileUtils.deleteDirectory(getBackupDir().resolve(name).toFile());
                return true;
            } catch (SecurityException | IOException e) {
                LOGGER.error("Delete Backup Failed", e);
                return false;
            }
        } else return false;
    }

    public static void createBackupDir(Path path) {
        if (!path.toFile().exists()) path.toFile().mkdirs();
    }

    public static ConfigStorage verifyConfig(ConfigStorage c, PlayerEntity player) {
        ServerCommandSource commandSource = player.getCommandSource();

        // schedule enable
        if (c.isScheduleBackup() && !Config.INSTANCE.getScheduleBackup()) {
            ScheduleUtils.startSchedule(commandSource);
        } else if (!c.isScheduleBackup() && Config.INSTANCE.getScheduleBackup()) {
            ScheduleUtils.disableSchedule(commandSource);
        }

        // schedule backup mode switch
        if (!c.getScheduleMode().equals(Config.INSTANCE.getScheduleMode()))
            ScheduleUtils.switchScheduleMode(commandSource, c.getScheduleMode());

        // schedule set cron
        if (!c.getScheduleCron().equals(Config.INSTANCE.getScheduleCron())) {
            try {
                ScheduleUtils.setScheduleCron(commandSource, c.getScheduleCron());
            } catch (SchedulerException e) {
                Messenger.sendMessage(commandSource,
                        Messenger.literal(tr("quickbackupmulti.schedule.cron.set_fail", e)));
            }
        }

        // schedule set interval
        if (!((Integer) c.getScheduleInterval()).equals(Config.INSTANCE.getScheduleInterval())) {
            try {
                ScheduleUtils.setScheduleInterval(commandSource, c.getScheduleInterval());
            } catch (SchedulerException e) {
                Messenger.sendMessage(commandSource,
                        Messenger.literal(tr("quickbackupmulti.schedule.cron.set_fail", e)));
            }
        }

        // lang
        if (!c.getLang().equals(Config.INSTANCE.getLang())) {
            if (!supportLanguage.contains(c.getLang())) {
                Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.failed")));
                c.setLang(Config.INSTANCE.getLang());
            } else {
                Translate.handleResourceReload(c.getLang());
                Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.set", c.getLang())));
            }
        }

        return c;
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
