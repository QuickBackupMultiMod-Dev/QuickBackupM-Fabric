package dev.skydynamic.quickbackupmulti.utils;

import dev.skydynamic.quickbackupmulti.config.Config;
import net.minecraft.server.command.ServerCommandSource;
import org.quartz.SchedulerException;

import java.text.SimpleDateFormat;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.*;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.getSeconds;

public class ScheduleUtils {

    public static void startSchedule(ServerCommandSource commandSource) {
        String nextBackupTimeString = "";
        try {
            // 照顾Java8
            switch (Config.INSTANCE.getScheduleMode()) {
                case "cron": {
                    nextBackupTimeString = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false);
                    break;
                }
                case "interval": {
                    nextBackupTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + Config.INSTANCE.getScheduleInrerval() * 1000L);
                    break;
                }
            }
            buildScheduler();
            Config.TEMP_CONFIG.scheduler.start();
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.success", nextBackupTimeString)));
        } catch (SchedulerException e) {
            LOGGER.error("Start schedule backup fail: ", e);
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
            LOGGER.error("Switch schedule mode backup fail: ", e);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.switch.fail", e.getMessage())));
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
            LOGGER.error("Close schedule backup fail: ", e);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.disable.fail", e.getMessage())));
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
            case "s" : {
                Config.INSTANCE.setScheduleInterval(value);
                break;
            }
            case "m" : {
                Config.INSTANCE.setScheduleInterval(getSeconds(value, 0, 0));
                break;
            }
            case "h" : {
                Config.INSTANCE.setScheduleInterval(getSeconds(0, value, 0));
                break;
            }
            case "d" : {
                Config.INSTANCE.setScheduleInterval(getSeconds(0, 0, value));
                break;
            }
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

}
