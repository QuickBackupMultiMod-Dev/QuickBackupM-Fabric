package dev.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.skydynamic.quickbackupmulti.i18n.LangSuggestionProvider;
import dev.skydynamic.quickbackupmulti.i18n.Translate;
import dev.skydynamic.quickbackupmulti.utils.Messenger;
import dev.skydynamic.quickbackupmulti.utils.config.Config;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.quartz.SchedulerException;

import java.text.SimpleDateFormat;

import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.startSchedule;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.*;
import static net.minecraft.server.command.CommandManager.literal;

public class SettingCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> settingCommand = literal("setting").requires(me -> me.hasPermissionLevel(2))
        .then(literal("lang")
            .then(literal("get").executes(it -> getLang(it.getSource())))
            .then(literal("set").requires(me -> me.hasPermissionLevel(2))
                .then(CommandManager.argument("lang", StringArgumentType.string())
                    .suggests(new LangSuggestionProvider())
                    .executes(it -> setLang(it.getSource(), StringArgumentType.getString(it, "lang"))))))
        .then(literal("schedule")
            .then(literal("enable").executes(it -> enableScheduleBackup(it.getSource())))
            .then(literal("disable").executes(it -> disableScheduleBackup(it.getSource())))
            .then(literal("set")
                .then(literal("interval")
                    .then(literal("second")
                        .then(CommandManager.argument("second", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(it.getSource(), IntegerArgumentType.getInteger(it, "second"), "s"))
                        )
                    ).then(literal("minute")
                        .then(CommandManager.argument("minute", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(it.getSource(), IntegerArgumentType.getInteger(it, "minute"), "m"))
                        )
                    ).then(literal("hour")
                        .then(CommandManager.argument("hour", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(it.getSource(), IntegerArgumentType.getInteger(it, "hour"), "h"))
                        )
                    ).then(literal("day")
                        .then(CommandManager.argument("day", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(it.getSource(), IntegerArgumentType.getInteger(it, "day"), "d"))
                        )
                    )
                )
                .then(literal("cron")
                    .then(CommandManager.argument("cron", StringArgumentType.string())
                        .executes(it -> setScheduleCron(it.getSource(), StringArgumentType.getString(it, "cron")))))
                .then(literal("mode")
                    .then(literal("sset")
                        .then(literal("interval")
                            .executes(it -> switchMode(it.getSource(), "interval")))
                        .then(literal("cron")
                            .executes(it -> switchMode(it.getSource(), "cron"))))
                    .then(literal("get").executes(it -> getScheduleMode(it.getSource()))))
            )
            .then(literal("get")
                .executes(it -> getNextBackupTime(it.getSource()))));

    private static int getLang(ServerCommandSource commandSource) {
        Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.get", Config.INSTANCE.getLang())));
        return 1;
    }

    private static int setLang(ServerCommandSource commandSource, String lang) {
        Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.set", lang)));
        Translate.handleResourceReload(lang);
        Config.INSTANCE.setLang(lang);
        return 1;
    }

    private static int switchMode(ServerCommandSource commandSource, String mode) {
        Config.INSTANCE.setScheduleMode(mode);
        try {
            if (Config.INSTANCE.getScheduleBackup()) {
                if (Config.TEMP_CONFIG.scheduler.isStarted()) Config.TEMP_CONFIG.scheduler.shutdown();
                startSchedule(commandSource);
            }
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.switch.fail", e)));
            return 0;
        }
        Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.switch.set", mode)));
        return 1;
    }

    private static int setScheduleCron(ServerCommandSource commandSource, String value) {
        try {
            if (cronIsValid(value)) {
                if (Config.TEMP_CONFIG.scheduler.isStarted()) Config.TEMP_CONFIG.scheduler.shutdown();
                if (Config.INSTANCE.getScheduleBackup()) {
                    Config.INSTANCE.setScheduleCron(value);
                    startSchedule(commandSource);
                    if (Config.INSTANCE.getScheduleMode().equals("cron")) {
                        Messenger.sendMessage(commandSource,
                            Messenger.literal(tr("quickbackupmulti.schedule.cron.set_custom_success", getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false))));
                    }
                } else {
                    Config.INSTANCE.setScheduleCron(value);
                    Messenger.sendMessage(commandSource,
                        Messenger.literal(tr("quickbackupmulti.schedule.cron.set_custom_success_only")));
                }
            } else {
                Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.cron.expression_error")));
                return 0;
            }
            return 1;
        } catch (SchedulerException e) {
            return 0;
        }
    }

    private static int setScheduleInterval(ServerCommandSource commandSource, int value, String type) {
        try {
            Config.TEMP_CONFIG.scheduler.shutdown();
            switch (type) {
                case "s" -> Config.INSTANCE.setScheduleInterval(value);
                case "m" -> Config.INSTANCE.setScheduleInterval(getSeconds(value, 0, 0));
                case "h" -> Config.INSTANCE.setScheduleInterval(getSeconds(0, value, 0));
                case "d" -> Config.INSTANCE.setScheduleInterval(getSeconds(0, 0, value));
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
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource,
                Messenger.literal(tr("quickbackupmulti.schedule.cron.set_fail", e)));
            return 0;
        }
    }

    private static int disableScheduleBackup(ServerCommandSource commandSource) {
        try {
            Config.TEMP_CONFIG.scheduler.shutdown();
            Config.INSTANCE.setScheduleBackup(false);
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.disable.success")));
            return 1;
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.disable.fail", e)));
            return 0;
        }
    }

    private static int enableScheduleBackup(ServerCommandSource commandSource) {
        try {
            Config.INSTANCE.setScheduleBackup(true);
            if (Config.TEMP_CONFIG.scheduler != null) Config.TEMP_CONFIG.scheduler.shutdown();
            startSchedule(commandSource);
            return 1;
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.enable.fail", e)));
            return 0;
        }
    }

    public static int getScheduleMode(ServerCommandSource commandSource) {
        Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.schedule.mode.get", Config.INSTANCE.getScheduleMode())));
        return 1;
    }

    public static int getNextBackupTime(ServerCommandSource commandSource) {
        if (Config.INSTANCE.getScheduleBackup()) {
            String nextBackupTimeString = "";
            switch (Config.INSTANCE.getScheduleMode()) {
                case "cron" -> nextBackupTimeString = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false);
                case "interval" -> nextBackupTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Config.TEMP_CONFIG.latestScheduleExecuteTime + Config.INSTANCE.getScheduleInrerval() * 1000L);
            }
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.get", nextBackupTimeString)));
            return 1;
        } else {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.get_fail")));
            return 0;
        }
    }

}
