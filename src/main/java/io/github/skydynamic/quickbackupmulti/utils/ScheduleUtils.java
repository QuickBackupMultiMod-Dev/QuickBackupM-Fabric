package io.github.skydynamic.quickbackupmulti.utils;

import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.i18n.Translate;
import io.github.skydynamic.quickbackupmulti.utils.schedule.CronUtil;
import net.minecraft.server.command.ServerCommandSource;
import org.quartz.SchedulerException;

import java.text.SimpleDateFormat;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;

public class ScheduleUtils {

    public static void startSchedule(ServerCommandSource commandSource) {
        String nextBackupTimeString = "";
        try {
            QuickBackupMulti.TEMP_CONFIG.scheduler.shutdown();
            // 照顾Java8
            switch (QuickBackupMulti.config.getScheduleMode()) {
                case "cron": {
                    nextBackupTimeString = CronUtil.getNextExecutionTime(QuickBackupMulti.config.getScheduleCron(), false);
                    break;
                }
                case "interval": {
                    nextBackupTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + QuickBackupMulti.config.getScheduleInterval() * 1000L);
                    break;
                }
            }
            CronUtil.buildScheduler();
            QuickBackupMulti.TEMP_CONFIG.scheduler.start();
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.success", nextBackupTimeString)));
        } catch (SchedulerException e) {
            LOGGER.error("Start schedule backup fail: ", e);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.fail", e.toString())));
        }
    }

    public static int switchScheduleMode(ServerCommandSource commandSource, String mode) {
        try {
            if (QuickBackupMulti.config.isScheduleBackup()) {
                if (QuickBackupMulti.TEMP_CONFIG.scheduler.isStarted()) QuickBackupMulti.TEMP_CONFIG.scheduler.shutdown();
                startSchedule(commandSource);
            }
        } catch (SchedulerException e) {
            LOGGER.error("Switch schedule mode backup fail: ", e);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.switch.fail", e.toString())));
            return 0;
        }
        Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.switch.set", mode)));
        return 1;
    }

    public static int disableSchedule(ServerCommandSource commandSource) {
        try {
            QuickBackupMulti.TEMP_CONFIG.scheduler.shutdown();
            QuickBackupMulti.config.setScheduleBackup(false);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.disable.success")));
            return 1;
        } catch (SchedulerException e) {
            LOGGER.error("Close schedule backup fail: ", e);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.disable.fail", e.toString())));
            return 0;
        }
    }

    public static int setScheduleCron(ServerCommandSource commandSource, String value) throws SchedulerException {
        if (CronUtil.cronIsValid(value)) {
            if (QuickBackupMulti.TEMP_CONFIG.scheduler != null) {
                if (QuickBackupMulti.TEMP_CONFIG.scheduler.isStarted()) QuickBackupMulti.TEMP_CONFIG.scheduler.shutdown();
            }
            QuickBackupMulti.config.setScheduleCron(value);
            if (QuickBackupMulti.config.isScheduleBackup()) {
                startSchedule(commandSource);
                if (QuickBackupMulti.config.getScheduleMode().equals("cron")) {
                    Messenger.sendMessage(commandSource,
                        Messenger.literal(Translate.tr("quickbackupmulti.schedule.cron.set_custom_success", CronUtil.getNextExecutionTime(QuickBackupMulti.config.getScheduleCron(), false))));
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
        if (QuickBackupMulti.TEMP_CONFIG.scheduler != null) {
            if (QuickBackupMulti.TEMP_CONFIG.scheduler.isStarted()) QuickBackupMulti.TEMP_CONFIG.scheduler.shutdown();
        }
        switch (type) {
            case "s" : {
                QuickBackupMulti.config.setScheduleInterval(value);
                break;
            }
            case "m" : {
                QuickBackupMulti.config.setScheduleInterval(CronUtil.getSeconds(value, 0, 0));
                break;
            }
            case "h" : {
                QuickBackupMulti.config.setScheduleInterval(CronUtil.getSeconds(0, value, 0));
                break;
            }
            case "d" : {
                QuickBackupMulti.config.setScheduleInterval(CronUtil.getSeconds(0, 0, value));
                break;
            }
        }
        if (QuickBackupMulti.config.isScheduleBackup()) {
            startSchedule(commandSource);
            if (QuickBackupMulti.config.getScheduleMode().equals("interval")) {
                Messenger.sendMessage(commandSource,
                    Messenger.literal(tr("quickbackupmulti.schedule.cron.set_success",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(System.currentTimeMillis() + QuickBackupMulti.config.getScheduleInterval() * 1000L))
                    )
                );
            }
        } else {
            Messenger.sendMessage(commandSource,
                Messenger.literal(tr("quickbackupmulti.schedule.cron.set_success_only")));
        }
        return 1;
    }

    public static void setScheduleInterval(ServerCommandSource commandSource, int value) throws SchedulerException {
        setScheduleInterval(commandSource, value, "s");
    }

}
