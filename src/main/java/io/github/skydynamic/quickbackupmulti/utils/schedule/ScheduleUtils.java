package io.github.skydynamic.quickbackupmulti.utils.schedule;

import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import org.quartz.SchedulerException;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;

public class ScheduleUtils {

    public static void startSchedule() {
        if (QuickBackupMulti.config.isScheduleBackup()) {
            try {
                CronUtil.buildScheduler();
                QuickBackupMulti.TEMP_CONFIG.scheduler.start();
                QuickBackupMulti.TEMP_CONFIG.setLatestScheduleExecuteTime(System.currentTimeMillis());
                LOGGER.info("QBM Schedule backup started.");
            } catch (SchedulerException e) {
                LOGGER.error("QBM schedule backup start error: " , e);
            }
        }
    }

    public static void shutdownSchedule() {
        try {
            if (QuickBackupMulti.TEMP_CONFIG.scheduler != null && QuickBackupMulti.TEMP_CONFIG.scheduler.isStarted()) QuickBackupMulti.TEMP_CONFIG.scheduler.shutdown();
        } catch (SchedulerException ignored) {
        }
    }

}
