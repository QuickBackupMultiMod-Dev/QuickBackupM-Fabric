package dev.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
//#if MC<=11820
//$$ import com.mojang.brigadier.exceptions.CommandSyntaxException;
//#endif
import dev.skydynamic.quickbackupmulti.backup.RestoreTask;
import dev.skydynamic.quickbackupmulti.utils.Messenger;
import dev.skydynamic.quickbackupmulti.utils.config.Config;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.skydynamic.quickbackupmulti.command.MakeCommand.makeCommand;
import static dev.skydynamic.quickbackupmulti.command.SettingCommand.settingCommand;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;
import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static net.minecraft.server.command.CommandManager.literal;

public class QuickBackupMultiCommand {

    private static final Logger logger = LoggerFactory.getLogger("Command");

    public static void RegisterCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> QuickBackupMultiShortCommand = dispatcher.register(literal("qb")
            .then(literal("list").executes(it -> listSaveBackups(it.getSource())))

            .then(makeCommand)

            .then(literal("back").requires(me -> me.hasPermissionLevel(2))
                    .executes(it -> restoreSaveBackup(it.getSource(), 1))
                    .then(CommandManager.argument("slot", IntegerArgumentType.integer(1))
                            .executes(it -> restoreSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot")))))

            .then(literal("confirm").requires(me -> me.hasPermissionLevel(2))
                    .executes(it -> {
                        try {
                            executeRestore(it.getSource());
                        } catch (Exception e) {
                            logger.info(e.toString());
                        }
                        return 0;
                    }))

            .then(literal("cancel").requires(me -> me.hasPermissionLevel(2))
                    .executes(it -> cancelRestore(it.getSource())))

            .then(literal("delete").requires(me -> me.hasPermissionLevel(2))
                    .then(CommandManager.argument("slot", IntegerArgumentType.integer(1))
                            .executes(it -> deleteSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot")))))

            .then(settingCommand)
        );

        dispatcher.register(literal("quickbackupm").redirect(QuickBackupMultiShortCommand));
    }

    public static final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> QbDataHashMap = new ConcurrentHashMap<>();

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
                try {
                    server.session.close();
                } catch (IOException var4) {
                    LOGGER.error("Failed to unlock level {}", server.session.getDirectoryName(), var4);
                }
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
