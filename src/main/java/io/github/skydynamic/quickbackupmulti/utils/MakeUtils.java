package io.github.skydynamic.quickbackupmulti.utils;

import io.github.skydynamic.increment.storage.lib.database.index.type.StorageInfo;
import io.github.skydynamic.quickbackupmulti.config.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.ArrayList;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getStorager;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.*;
import static io.github.skydynamic.quickbackupmulti.utils.ScheduleUtils.startSchedule;

public class MakeUtils {
    public static int make(ServerCommandSource commandSource, String name, String desc) {
        long startTime = System.currentTimeMillis();
        if (getStorager().storageExists(name)) {
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

            StorageInfo storageInfo = new StorageInfo(name, desc, System.currentTimeMillis(), true, new ArrayList<>());

            getStorager().incrementalStorage(storageInfo, savePath, getBackupDir().resolve(name), fileFilter, null);

            long endTime = System.currentTimeMillis();
            double intervalTime = (endTime - startTime) / 1000.0;
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.success", intervalTime)));

            if (Config.INSTANCE.getScheduleBackup()) startSchedule(commandSource);

            for (ServerWorld serverWorld : server.getWorlds()) {
                if (serverWorld == null || !serverWorld.savingDisabled) continue;
                serverWorld.savingDisabled = false;
            }
        } catch (Exception e) {
            LOGGER.error("Make Backup Failed", e);
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.make.fail",  e.toString())));
            backupDir.resolve(name).toFile().deleteOnExit();
        }
        return 1;
    }

    public static boolean scheduleMake(ServerCommandSource commandSource, String name) {
        if (getStorager().storageExists(name)) return false;
        make(commandSource, name, "Scheduled Backup");
        return true;
    }
}
