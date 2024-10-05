package io.github.skydynamic.quickbackupmulti.utils.schedule;

import io.github.skydynamic.increment.storage.lib.database.index.type.StorageInfo;
import io.github.skydynamic.quickbackupmulti.utils.Messenger;
import io.github.skydynamic.quickbackupmulti.config.Config;
import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static io.github.skydynamic.quickbackupmulti.utils.MakeUtils.scheduleMake;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.CronUtil.getNextExecutionTime;

public class ScheduleBackup implements Job {
    public static String generateName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return "ScheduleBackup-" + dateFormat.format(System.currentTimeMillis());
    }

    private static void checkAndDeleteOldScheduleBackup() {
        List<StorageInfo> scheduleList = QbmManager.getScheduleBackupList();
        if (scheduleList.size() >= Config.INSTANCE.getScheduleMaxBackup()) {
            StorageInfo oldest;
            Optional<StorageInfo> oldestOpt = scheduleList.stream().min(
                Comparator.comparingLong(StorageInfo::getTimestamp)
            );
            if (oldestOpt.isPresent()) {
                oldest = oldestOpt.get();
                QbmManager.delete(oldest.getName());
            }
        }
    }

    @Override
    public void execute(JobExecutionContext context) {
        if (Config.TEMP_CONFIG.server != null) {
            MinecraftServer server = Config.TEMP_CONFIG.server;
            checkAndDeleteOldScheduleBackup();
            if (scheduleMake(server.getCommandSource(), generateName())) {
                List<ServerPlayerEntity> finalPlayerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                Config.TEMP_CONFIG.setLatestScheduleExecuteTime(System.currentTimeMillis());
                String nextExecuteTime = "";
                switch (Config.INSTANCE.getScheduleMode()) {
                    case "interval" : {
                        nextExecuteTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(System.currentTimeMillis() + Config.INSTANCE.getScheduleInterval() * 1000L);
                        break;
                    }
                    case "cron" : {
                        nextExecuteTime = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), true);
                        break;
                    }
                }
                for (ServerPlayerEntity player : finalPlayerList) {
                    player.sendMessage(Messenger.literal(tr("quickbackupmulti.schedule.execute.finish", nextExecuteTime)), false);
                }
            }
        }
    }
}
