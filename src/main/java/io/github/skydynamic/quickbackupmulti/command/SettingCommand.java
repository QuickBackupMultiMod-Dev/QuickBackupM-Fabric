package io.github.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionManager;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionType;
import io.github.skydynamic.quickbackupmulti.command.suggestion.AutoRestartModeSuggestionProvider;
import io.github.skydynamic.quickbackupmulti.command.suggestion.LangSuggestionProvider;
import io.github.skydynamic.quickbackupmulti.i18n.Translate;
import io.github.skydynamic.quickbackupmulti.utils.Messenger;
import io.github.skydynamic.quickbackupmulti.utils.ScheduleUtils;
import io.github.skydynamic.quickbackupmulti.config.Config;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.quartz.SchedulerException;

import java.text.SimpleDateFormat;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.setDataBase;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.supportLanguage;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static io.github.skydynamic.quickbackupmulti.utils.ScheduleUtils.disableSchedule;
import static io.github.skydynamic.quickbackupmulti.utils.ScheduleUtils.startSchedule;
import static io.github.skydynamic.quickbackupmulti.utils.ScheduleUtils.switchScheduleMode;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.CronUtil.getNextExecutionTime;
import static net.minecraft.server.command.CommandManager.literal;

public class SettingCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> settingCommand = literal("setting")
        .requires(it -> PermissionManager.hasPermission(it, 2, PermissionType.HELPER))
        .then(literal("lang")
            .then(literal("get").executes(it -> getLang(it.getSource())))
            .then(literal("set")
                .requires(it -> PermissionManager.hasPermission(it, 2, PermissionType.HELPER))
                .then(CommandManager.argument("lang", StringArgumentType.string())
                    .suggests(LangSuggestionProvider.lang())
                    .executes(it -> setLang(it.getSource(), StringArgumentType.getString(it, "lang"))))))
        .then(literal("schedule")
            .then(literal("enable").executes(it -> enableScheduleBackup(it.getSource())))
            .then(literal("disable").executes(it -> disableScheduleBackup(it.getSource())))
            .then(literal("set")
                .then(literal("interval")
                    .then(literal("second")
                        .then(CommandManager.argument("second", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(
                                it.getSource(),
                                IntegerArgumentType.getInteger(it, "second"), "s")
                            )
                        )
                    ).then(literal("minute")
                        .then(CommandManager.argument("minute", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(
                                it.getSource(),
                                IntegerArgumentType.getInteger(it, "minute"), "m")
                            )
                        )
                    ).then(literal("hour")
                        .then(CommandManager.argument("hour", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(
                                it.getSource(),
                                IntegerArgumentType.getInteger(it, "hour"), "h")
                            )
                        )
                    ).then(literal("day")
                        .then(CommandManager.argument("day", IntegerArgumentType.integer(1))
                            .executes(it -> setScheduleInterval(
                                it.getSource(),
                                IntegerArgumentType.getInteger(it, "day"), "d")
                            )
                        )
                    )
                )
                .then(literal("cron")
                    .then(CommandManager.argument("cron", StringArgumentType.string())
                        .executes(it -> setScheduleCron(it.getSource(), StringArgumentType.getString(it, "cron")))
                    )
                )

                .then(literal("mode")
                    .then(literal("set")
                        .then(literal("interval")
                            .executes(it -> switchMode(it.getSource(), "interval")))
                        .then(literal("cron")
                            .executes(it -> switchMode(it.getSource(), "cron"))))
                    .then(literal("get").executes(it -> getScheduleMode(it.getSource()))))
            )
            .then(literal("get")
                .executes(it -> getNextBackupTime(it.getSource()))
            )
        )
        .then(literal("dataBase")
            .requires(it -> PermissionManager.hasPermission(it, 2, PermissionType.HELPER))
            .then(literal("useInternalDataBase")
                .then(literal("set")
                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(it -> setUseInternalDataBase(it.getSource(), BoolArgumentType.getBool(it, "value")))
                    )
                )
            )
        )
        .then(literal("restartMode")
            .requires(it -> PermissionManager.hasPermission(it, 4, PermissionType.ADMIN))
            .then(literal("set")
                .then(CommandManager.argument("mode", StringArgumentType.string())
                    .suggests(AutoRestartModeSuggestionProvider.mode())
                    .executes(it -> setAutoRestartMode(it.getSource(), StringArgumentType.getString(it, "mode")))
                )
            )
            .then(literal("get").executes(it -> getAutoRestartMode(it.getSource())))
        );

    private static int getAutoRestartMode(ServerCommandSource commandSource) {
        Messenger.sendMessage(commandSource,
            Messenger.literal(tr("quickbackupmulti.restartmode.get", Config.INSTANCE.getAutoRestartMode().name())));
        return 1;
    }

    private static int setAutoRestartMode(ServerCommandSource commandSource, String mode) {
        Config.AutoRestartMode newMode = Config.AutoRestartMode.valueOf(mode.toUpperCase());
        Config.INSTANCE.setAutoRestartMode(newMode);
        Messenger.sendMessage(commandSource,
            Messenger.literal(tr("quickbackupmulti.restartmode.switch", newMode.name())));
        return 1;
    }

    private static int setUseInternalDataBase(ServerCommandSource commandSource, Boolean value) {
        if (value != Config.INSTANCE.getUseInternalDataBase()) {
            Config.INSTANCE.setUseInternalDataBase(value);
            try {
                if (value) {
                    setDataBase(Config.TEMP_CONFIG.worldName);
                } else {
                    getDataBase().stopInternalMongoServer();
                    setDataBase(Config.TEMP_CONFIG.worldName);
                }
                Messenger.sendMessage(commandSource,
                    Messenger.literal(tr("quickbackupmulti.database.set_success")));
                return 1;
            } catch (Exception e) {
                Messenger.sendMessage(commandSource,
                    Messenger.literal(tr("quickbackupmulti.database.set_success_but", e.getMessage())));
                return 0;
            }
        } else {
            Messenger.sendMessage(
                commandSource,
                Messenger.literal(
                    tr("quickbackupmulti.database.set_fail", tr("quickbackupmulti.database.value_equal_config", value)))
            );
            return 0;
        }

    }

    private static int getLang(ServerCommandSource commandSource) {
        Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.get", Config.INSTANCE.getLang())));
        return 1;
    }

    private static int setLang(ServerCommandSource commandSource, String lang) {
        if (!supportLanguage.contains(lang)) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.failed")));
            return 0;
        }
        Translate.handleResourceReload(lang);
        Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.lang.set", lang)));
        Config.INSTANCE.setLang(lang);
        return 1;
    }

    private static int switchMode(ServerCommandSource commandSource, String mode) {
        Config.INSTANCE.setScheduleMode(mode);
        return switchScheduleMode(commandSource, mode);
    }

    private static int setScheduleCron(ServerCommandSource commandSource, String value) {
        try {
            return ScheduleUtils.setScheduleCron(commandSource, value);
        } catch (SchedulerException e) {
            return 0;
        }
    }

    private static int setScheduleInterval(ServerCommandSource commandSource, int value, String type) {
        try {
            return ScheduleUtils.setScheduleInterval(commandSource, value, type);
        } catch (SchedulerException e) {
            Messenger.sendMessage(commandSource,
                Messenger.literal(tr("quickbackupmulti.schedule.cron.set_fail", e)));
            return 0;
        }
    }

    private static int disableScheduleBackup(ServerCommandSource commandSource) {
        return disableSchedule(commandSource);
    }

    private static int enableScheduleBackup(ServerCommandSource commandSource) {
        try {
            Config.INSTANCE.setScheduleBackup(true);
            if (Config.TEMP_CONFIG.scheduler != null) Config.TEMP_CONFIG.scheduler.shutdown();
            startSchedule(commandSource);
            return 1;
        } catch (SchedulerException e) {
            Messenger.sendMessage(
                commandSource,
                Messenger.literal(tr("quickbackupmulti.schedule.enable.fail", e.toString()))
                );
            return 0;
        }
    }

    public static int getScheduleMode(ServerCommandSource commandSource) {
        Messenger.sendMessage(
            commandSource,
            Text.of(tr("quickbackupmulti.schedule.mode.get", Config.INSTANCE.getScheduleMode()))
        );
        return 1;
    }

    public static int getNextBackupTime(ServerCommandSource commandSource) {
        if (Config.INSTANCE.getScheduleBackup()) {
            String nextBackupTimeString = "";
            switch (Config.INSTANCE.getScheduleMode()) {
                case "cron" : {
                    nextBackupTimeString = getNextExecutionTime(Config.INSTANCE.getScheduleCron(), false);
                    break;
                }
                case "interval" : {
                    nextBackupTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(
                            Config.TEMP_CONFIG.latestScheduleExecuteTime + Config.INSTANCE.getScheduleInterval() * 1000L
                        );
                    break;
                }
            }
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.get", nextBackupTimeString)));
            return 1;
        } else {
            Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.schedule.get_fail")));
            return 0;
        }
    }

}
