package io.github.skydynamic.quickbackupmulti.utils.schedule;

import io.github.skydynamic.quickbackupmulti.config.Config;
import org.quartz.SchedulerException;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;

public class ScheduleUtils {

    public static void startSchedule() {
        if (Config.INSTANCE.getScheduleBackup()) {
            try {
                CronUtil.buildScheduler();
                Config.TEMP_CONFIG.scheduler.start();
                Config.TEMP_CONFIG.setLatestScheduleExecuteTime(System.currentTimeMillis());
                LOGGER.info("QBM Schedule backup started.");
            } catch (SchedulerException e) {
                LOGGER.error("QBM schedule backup start error: " + e);
            }
        }
    }

    public static void shutdownSchedule() {
        try {
            if (Config.TEMP_CONFIG.scheduler != null && Config.TEMP_CONFIG.scheduler.isStarted()) Config.TEMP_CONFIG.scheduler.shutdown();
        } catch (SchedulerException ignored) {
        }
    }

}
