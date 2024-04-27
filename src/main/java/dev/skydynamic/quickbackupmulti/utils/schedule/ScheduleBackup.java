package dev.skydynamic.quickbackupmulti.utils.schedule;

import dev.skydynamic.quickbackupmulti.utils.Messenger;
import dev.skydynamic.quickbackupmulti.utils.config.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.MakeUtils.scheduleMake;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.getNextExecutionTime;

public class ScheduleBackup implements Job {
    public static String generateName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return "ScheduleBackup-" + dateFormat.format(System.currentTimeMillis());
    }
    @Override
    public void execute(JobExecutionContext context) {
        if (Config.TEMP_CONFIG.server != null) {
            MinecraftServer server = Config.TEMP_CONFIG.server;
            if (scheduleMake(server.getCommandSource(), generateName())) {
                List<ServerPlayerEntity> finalPlayerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                Config.TEMP_CONFIG.setLatestScheduleExecuteTime(System.currentTimeMillis());
                String nextExecuteTime = "";
                switch (Config.INSTANCE.getScheduleMode()) {
                    case "interval" -> nextExecuteTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + Config.INSTANCE.getScheduleInrerval() * 1000L);
                    case "cron" -> nextExecuteTime = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), true);
                }
                for (ServerPlayerEntity player : finalPlayerList) {
                    player.sendMessage(Messenger.literal(tr("quickbackupmulti.schedule.execute.finish", nextExecuteTime)), false);
                }
            }
        }
    }
}
