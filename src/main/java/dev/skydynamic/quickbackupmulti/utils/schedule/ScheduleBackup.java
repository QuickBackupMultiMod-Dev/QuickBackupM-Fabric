package dev.skydynamic.quickbackupmulti.utils.schedule;

import dev.skydynamic.quickbackupmulti.utils.Messenger;
import dev.skydynamic.quickbackupmulti.utils.config.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.text.SimpleDateFormat;
import java.util.Collection;

import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.scheduleMake;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.getNextExecutionTime;

public class ScheduleBackup implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        if (Config.TEMP_CONFIG.server != null) {
            MinecraftServer server = Config.TEMP_CONFIG.server;
            if (scheduleMake(server.getCommandSource(), -1)) {
                final Collection<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
                String nextExecuteTime = "";
                switch (Config.INSTANCE.getScheduleMode()) {
                    case "interval" -> nextExecuteTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + Config.INSTANCE.getScheduleInrerval() * 1000L);
                    case "cron" -> nextExecuteTime = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), true);
                }
                for (ServerPlayerEntity player : playerList) {
                    player.sendMessage(Messenger.literal(tr("quickbackupmulti.schedule.execute.finish", nextExecuteTime)), false);
                }
            }
        }
    }
}
