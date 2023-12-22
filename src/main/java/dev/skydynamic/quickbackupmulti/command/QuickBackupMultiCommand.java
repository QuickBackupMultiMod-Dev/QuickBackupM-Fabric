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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
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

                .then(literal("lang")
                        .then(literal("get").executes(it -> getLang(it.getSource())))
                        .then(literal("set").requires(me -> me.hasPermissionLevel(2))
                                .then(CommandManager.argument("lang", StringArgumentType.string())
                                        .suggests(new LangSuggestionProvider())
                                        .executes(it -> setLang(it.getSource(), StringArgumentType.getString(it, "lang")))))));

        dispatcher.register(literal("quickbackupm").redirect(QuickBackupMultiShortCommand));
    }

    public static final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> QbDataHashMap = new ConcurrentHashMap<>();

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
