package io.github.skydynamic.quickbackupmulti.utils.schedule;

import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;

public class CronUtil {

    public static Trigger buildTrigger() {
        try {
            if (QuickBackupMulti.config.getScheduleMode().equals("cron")) {
                return TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(QuickBackupMulti.config.getScheduleCron()))
                    .startAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getNextExecutionTime(QuickBackupMulti.config.getScheduleCron(), false)))
                    .build();
            } else {
                return TriggerBuilder.newTrigger()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(QuickBackupMulti.config.getScheduleInterval()).repeatForever())
                    .startAt(new Date(System.currentTimeMillis() + QuickBackupMulti.config.getScheduleInterval() * 1000L))
                    .build();
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void buildScheduler() {
        try {
            JobDetail jb = JobBuilder.newJob(ScheduleBackup.class).withIdentity("ScheduleBackup").build();
            Trigger t = buildTrigger();
            StdSchedulerFactory sf = new StdSchedulerFactory();
            QuickBackupMulti.TEMP_CONFIG.setScheduler(sf.getScheduler());
            QuickBackupMulti.TEMP_CONFIG.scheduler.scheduleJob(jb, t);
        } catch (SchedulerException e) {
            LOGGER.error(e.toString());
        }
    }

    public static int getSeconds(int minute, int hour, int day) {
        return minute*60 + hour*3600 + day*3600*24;
    }

    public static String getNextExecutionTime(String cronExpress, boolean get) {
        CronExpression cronExpression;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cronExpression = new CronExpression(cronExpress);
            if (get) {
                return simpleDateFormat.format(cronExpression.getNextValidTimeAfter(new Date(QuickBackupMulti.TEMP_CONFIG.latestScheduleExecuteTime)));
            }
            Date nextValidTime = cronExpression.getNextValidTimeAfter(new Date());
            return simpleDateFormat.format(nextValidTime);
        } catch (ParseException e) {
            return simpleDateFormat.format(new Date());
        }
    }

    public static boolean cronIsValid(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
