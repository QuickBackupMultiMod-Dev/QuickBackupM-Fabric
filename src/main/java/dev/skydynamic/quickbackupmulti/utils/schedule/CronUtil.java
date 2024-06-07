package dev.skydynamic.quickbackupmulti.utils.schedule;

import dev.skydynamic.quickbackupmulti.config.Config;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;

public class CronUtil {

    public static Trigger buildTrigger() {
        try {
            if (Config.INSTANCE.getScheduleMode().equals("cron")) {
                return TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(Config.INSTANCE.getScheduleCron()))
                    .startAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false)))
                    .build();
            } else {
                return TriggerBuilder.newTrigger()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(Config.INSTANCE.getScheduleInrerval()).repeatForever())
                    .startAt(new Date(System.currentTimeMillis() + Config.INSTANCE.getScheduleInrerval() * 1000L))
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
            Config.TEMP_CONFIG.setScheduler(sf.getScheduler());
            Config.TEMP_CONFIG.scheduler.scheduleJob(jb, t);
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
                return simpleDateFormat.format(cronExpression.getNextValidTimeAfter(new Date(Config.TEMP_CONFIG.latestScheduleExecuteTime)));
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
