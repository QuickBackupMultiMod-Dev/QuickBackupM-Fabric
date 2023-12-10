package dev.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.skydynamic.quickbackupmulti.utils.LangSuggestionProvider;
import dev.skydynamic.quickbackupmulti.utils.Translate;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import dev.skydynamic.quickbackupmulti.utils.config.Config;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;
import static dev.skydynamic.quickbackupmulti.utils.Translate.tr;
import static net.minecraft.server.command.CommandManager.literal;

public class QuickBackupMultiCommandManager {
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
                .executes(it -> executeRestore(it.getSource())))

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

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> QbDataHashMap = new ConcurrentHashMap<>();

    private static int getLang(ServerCommandSource commandSource) {
        commandSource.sendMessage(Text.of(String.format(tr("quickbackupmulti.lang.get"), Config.INSTANCE.getLang())));
        return 1;
    }

    private static int setLang(ServerCommandSource commandSource, String lang) {
        commandSource.sendMessage(Text.of(String.format(tr("quickbackupmulti.lang.set"), lang)));
        Translate.handleResourceReload(lang);
        Config.INSTANCE.setLang(lang);
        return 1;
    }

    private static int makeSaveBackup(ServerCommandSource commandSource, int slot, String desc) {
        return make(commandSource, slot, desc);
    }

    private static int deleteSaveBackup(ServerCommandSource commandSource, int slot) {
        if (delete(slot)) commandSource.sendMessage(Text.of(String.format(tr("quickbackupmulti.delete.success"), slot)));
        else commandSource.sendMessage(Text.of(String.format(tr("quickbackupmulti.delete.fail"), slot)));
        return 1;
    }

    private static int restoreSaveBackup(ServerCommandSource commandSource, int slot) {
        if (!backupDir.resolve("Slot" + slot + "_info.json").toFile().exists()) {
            commandSource.sendMessage(Text.of(tr("quickbackupmulti.restore.fail")));
            return 0;
        }
        ConcurrentHashMap<String, Object> restoreDataHashMap = new ConcurrentHashMap<>();
        restoreDataHashMap.put("Slot", slot);
        restoreDataHashMap.put("Timer", new Timer());
        restoreDataHashMap.put("Countdown", Executors.newSingleThreadScheduledExecutor());
        synchronized (QbDataHashMap) {
            QbDataHashMap.put("QBM", restoreDataHashMap);
            commandSource.sendMessage(Text.of(tr("quickbackupmulti.restore.confirm_hint")));
            return 1;
        }
    }

    private static int executeRestore(ServerCommandSource commandSource) {
        synchronized (QbDataHashMap) {
            if (QbDataHashMap.containsKey("QBM")) {
                if (!backupDir.resolve("Slot" + QbDataHashMap.get("QBM").get("Slot") + "_info.json").toFile().exists()) {
                    commandSource.sendMessage(Text.of(tr("quickbackupmulti.restore.fail")));
                    QbDataHashMap.clear();
                    return 0;
                }
                EnvType env = FabricLoader.getInstance().getEnvironmentType();
                String executePlayerName;
                if (commandSource.isExecutedByPlayer()) {
                    executePlayerName = commandSource.getPlayer().getGameProfile().getName();
                } else {
                    executePlayerName = "Console";
                }
                commandSource.sendMessage(Text.of(tr("quickbackupmulti.restore.abort_hint")));
                MinecraftServer server = commandSource.getServer();
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(Text.of(String.format(tr("quickbackupmulti.restore.countdown.intro"), executePlayerName)));
                }
                int slot = (int) QbDataHashMap.get("QBM").get("Slot");
                Config.TEMPCONFIG.setBackupSlot(slot);
                Timer timer = (Timer) QbDataHashMap.get("QBM").get("Timer");
                ScheduledExecutorService countdown = (ScheduledExecutorService) QbDataHashMap.get("QBM").get("Countdown");
                AtomicInteger countDown = new AtomicInteger(11);
                final List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
                countdown.scheduleAtFixedRate(() -> {
                    int remaining = countDown.decrementAndGet();
                    if (remaining >= 1) {
                        for (ServerPlayerEntity player : playerList) {
                            MutableText sendText = Text.literal(String.format(tr("quickbackupmulti.restore.countdown.text"), remaining, slot))
                                .append(Text.literal(tr("quickbackupmulti.restore.countdown.hover"))
                                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qb cancel")))
                                    .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tr("quickbackupmulti.restore.countdown.hover"))))));
                            player.sendMessage(sendText);
                        }
                    } else {
                        countdown.shutdown();
                    }
                }, 0, 1, TimeUnit.SECONDS);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        QbDataHashMap.clear();
                        if (env == EnvType.SERVER) {
                            for (ServerPlayerEntity player : playerList) {
                                player.networkHandler.disconnect(Text.of("Server restore backup"));
                            }
                            Config.TEMPCONFIG.setIsBackupValue(true);
                            Config.TEMPCONFIG.server.stop(true);
                        } else {
                            MinecraftClient minecraftClient = MinecraftClient.getInstance();
                            minecraftClient.execute(() -> {
                                Config.TEMPCONFIG.setIsBackupValue(true);
                                minecraftClient.world.disconnect();
                                minecraftClient.disconnect(new MessageScreen(Text.of("Restore backup")));
                                CompletableFuture.runAsync(() -> {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    minecraftClient.execute(() -> {
                                        minecraftClient.setScreen(null);
                                        restoreClient(slot);
                                        Config.TEMPCONFIG.setIsBackupValue(false);
                                        Text title = Text.of(tr("quickbackupmulti.toast.end_title"));
                                        Text content = Text.of(tr("quickbackupmulti.toast.end_content"));
                                        SystemToast.show(minecraftClient.toastManager, SystemToast.Type.PERIODIC_NOTIFICATION, title, content);
                                    });
                                });
                            });

                        }
                    }
                }, 10000);
            } else {
                commandSource.sendMessage(Text.of(tr("quickbackupmulti.confirm_restore.nothing_to_confirm")));
            }
            return 1;
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
                Config.TEMPCONFIG.setIsBackupValue(false);
                commandSource.sendMessage(Text.of(tr("quickbackupmulti.restore.abort")));
            }
        } else {
            commandSource.sendMessage(Text.of(tr("quickbackupmulti.confirm_restore.nothing_to_confirm")));
        }
        return 1;
    }

    private static int listSaveBackups(ServerCommandSource commandSource) {
        MutableText resultText = list();
        commandSource.sendMessage(resultText);
        return 1;
    }
}
