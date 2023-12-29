package dev.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.CommandDispatcher;
//#if MC<11900
//$$ import com.mojang.brigadier.exceptions.CommandSyntaxException;
//#endif
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.skydynamic.quickbackupmulti.backup.RestoreTask;
import dev.skydynamic.quickbackupmulti.i18n.LangSuggestionProvider;
import dev.skydynamic.quickbackupmulti.i18n.Translate;
import dev.skydynamic.quickbackupmulti.utils.Messenger;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import dev.skydynamic.quickbackupmulti.utils.config.Config;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.*;
import static net.minecraft.server.command.CommandManager.literal;

public class QuickBackupMultiCommand {

    private static final Logger logger = LoggerFactory.getLogger("Command");

    public static void RegisterCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> QuickBackupMultiShortCommand = dispatcher.register(literal("qb")
                .then(literal("list").executes(it -> listSaveBackups(it.getSource())))

                .then(literal("make").requires(me -> me.hasPermissionLevel(2))
                        .executes(it -> makeSaveBackup(it.getSource(), -1, ""))
                        .then(CommandManager.argument("slot", IntegerArgumentType.integer(1))
                                .executes(it -> makeSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot"), ""))
                                .then(CommandManager.argument("desc", StringArgumentType.string())
                                        .executes(it -> makeSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot"), StringArgumentType.getString(it, "desc"))))
                        )
                        .then(CommandManager.argument("desc", StringArgumentType.string())
                                .executes(it -> makeSaveBackup(it.getSource(), -1, StringArgumentType.getString(it, "desc"))))
                )

                .then(literal("back").requires(me -> me.hasPermissionLevel(2))
                        .executes(it -> restoreSaveBackup(it.getSource(), 1))
                        .then(CommandManager.argument("slot", IntegerArgumentType.integer(1))
                                .executes(it -> restoreSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot")))))

                .then(literal("confirm").requires(me -> me.hasPermissionLevel(2))
                        .executes(it -> {
                            try {
                                executeRestore(it.getSource());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }))

                .then(literal("cancel").requires(me -> me.hasPermissionLevel(2))
                        .executes(it -> cancelRestore(it.getSource())))

                .then(literal("delete").requires(me -> me.hasPermissionLevel(2))
                        .then(CommandManager.argument("slot", IntegerArgumentType.integer(1))
                                .executes(it -> deleteSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot")))))

                .then(literal("setting").requires(me -> me.hasPermissionLevel(2))
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
                                .then(literal("switch")
                                    .then(literal("interval")
                                        .executes(it -> switchMode(it.getSource(), "interval")))
                                    .then(literal("cron")
                                        .executes(it -> switchMode(it.getSource(), "cron"))))
                                .then(literal("get").executes(it -> getScheduleMode(it.getSource()))))
                        )
                        .then(literal("get")
                            .executes(it -> getNextBackupTime(it.getSource())))
                    )
                )
        );

        dispatcher.register(literal("quickbackupm").redirect(QuickBackupMultiShortCommand));
    }

    public static final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> QbDataHashMap = new ConcurrentHashMap<>();

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

    private static int makeSaveBackup(ServerCommandSource commandSource, int slot, String desc) {
        return make(commandSource, slot, desc);
    }

    private static int deleteSaveBackup(ServerCommandSource commandSource, int slot) {
        if (delete(slot)) Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.delete.success", slot)));
        else Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.delete.fail", slot)));
        return 1;
    }

    private static int restoreSaveBackup(ServerCommandSource commandSource, int slot) {
        if (!getBackupDir().resolve("Slot" + slot + "_info.json").toFile().exists()) {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.restore.fail")));
            return 0;
        }
        ConcurrentHashMap<String, Object> restoreDataHashMap = new ConcurrentHashMap<>();
        restoreDataHashMap.put("Slot", slot);
        restoreDataHashMap.put("Timer", new Timer());
        restoreDataHashMap.put("Countdown", Executors.newSingleThreadScheduledExecutor());
        synchronized (QbDataHashMap) {
            QbDataHashMap.put("QBM", restoreDataHashMap);
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.restore.confirm_hint")));
            return 1;
        }
    }

    //#if MC>11900
    private static void executeRestore(ServerCommandSource commandSource) {
    //#else
    //$$ private static void executeRestore(ServerCommandSource commandSource) throws CommandSyntaxException {
    //#endif
        synchronized (QbDataHashMap) {
            if (QbDataHashMap.containsKey("QBM")) {
                if (!getBackupDir().resolve("Slot" + QbDataHashMap.get("QBM").get("Slot") + "_info.json").toFile().exists()) {
                    Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.restore.fail")));
                    QbDataHashMap.clear();
                    return;
                }
                EnvType env = FabricLoader.getInstance().getEnvironmentType();
                String executePlayerName;
                if (commandSource.getPlayer() != null) {
                    executePlayerName = commandSource.getPlayer().getGameProfile().getName();
                } else {
                    executePlayerName = "Console";
                }
                Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.restore.abort_hint")));
                MinecraftServer server = commandSource.getServer();
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(Text.of(tr("quickbackupmulti.restore.countdown.intro", executePlayerName)), false);
                }
                int slot = (int) QbDataHashMap.get("QBM").get("Slot");
                Config.TEMP_CONFIG.setBackupSlot(slot);
                Timer timer = (Timer) QbDataHashMap.get("QBM").get("Timer");
                ScheduledExecutorService countdown = (ScheduledExecutorService) QbDataHashMap.get("QBM").get("Countdown");
                AtomicInteger countDown = new AtomicInteger(11);
                final List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
                countdown.scheduleAtFixedRate(() -> {
                    int remaining = countDown.decrementAndGet();
                    if (remaining >= 1) {
                        for (ServerPlayerEntity player : playerList) {
                            //#if MC>11900
                            MutableText content = Messenger.literal(tr("quickbackupmulti.restore.countdown.text", remaining, slot))
                            //#else
                            //$$ BaseText content = (BaseText) Messenger.literal(tr("quickbackupmulti.restore.countdown.text", remaining, slot))
                            //#endif
                                    .append(Messenger.literal(tr("quickbackupmulti.restore.countdown.hover"))
                                            .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qb cancel")))
                                            .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.restore.countdown.hover"))))));
                            player.sendMessage(content, false);
                            logger.info(content.getString());
                        }
                    } else {
                        countdown.shutdown();
                    }
                }, 0, 1, TimeUnit.SECONDS);

                timer.schedule(new RestoreTask(env, playerList, slot), 10000);
            } else {
                Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.confirm_restore.nothing_to_confirm")));
            }
        }
    }

    private static int cancelRestore(ServerCommandSource commandSource) {
        if (QbDataHashMap.containsKey("QBM")) {
            synchronized (QbDataHashMap) {
                Timer timer = (Timer) QbDataHashMap.get("QBM").get("Timer");
                ScheduledExecutorService countdown = (ScheduledExecutorService) QbDataHashMap.get("QBM").get("Countdown");
                timer.cancel();
                countdown.shutdown();
                QbDataHashMap.clear();
                Config.TEMP_CONFIG.setIsBackupValue(false);
                Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.restore.abort")));
            }
        } else {
            Messenger.sendMessage(commandSource, Text.of(tr("quickbackupmulti.confirm_restore.nothing_to_confirm")));
        }
        return 1;
    }

    private static int listSaveBackups(ServerCommandSource commandSource) {
        MutableText resultText = list();
        Messenger.sendMessage(commandSource, resultText);
        return 1;
    }
}
