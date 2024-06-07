package dev.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.text.SimpleDateFormat;

import static dev.skydynamic.quickbackupmulti.utils.MakeUtils.make;
import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static net.minecraft.server.command.CommandManager.literal;

public class MakeCommand {

    static class makeRunnable implements Runnable {
        ServerCommandSource commandSource;
        String name;
        String desc;

        makeRunnable(ServerCommandSource commandSource, String name, String desc) {
            this.commandSource = commandSource;
            this.name = name;
            this.desc = desc;
        }

        @Override
        public void run() {
            LOGGER.info("Make Backup thread started...");
            make(commandSource, name, desc);
            LOGGER.info("Make Backup thread close");
        }
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    public static LiteralArgumentBuilder<ServerCommandSource> makeCommand = literal("make").requires(QuickBackupMultiCommand::checkPermission)
        .executes(it -> makeSaveBackup(it.getSource(), dateFormat.format(System.currentTimeMillis()), ""))
        .then(CommandManager.argument("name", StringArgumentType.string())
            .executes(it -> makeSaveBackup(it.getSource(), StringArgumentType.getString(it, "name"), ""))
            .then(CommandManager.argument("desc", StringArgumentType.string())
                .executes(it -> makeSaveBackup(it.getSource(), StringArgumentType.getString(it, "name"), StringArgumentType.getString(it, "desc"))))
        );
//        .then(CommandManager.argument("desc", StringArgumentType.string())
//            .executes(it -> makeSaveBackup(it.getSource(), String.valueOf(System.currentTimeMillis()), StringArgumentType.getString(it, "desc"))));

    private static int makeSaveBackup(ServerCommandSource commandSource, String name, String desc) {
        new makeRunnable(commandSource, name, desc).run();
        return 1;
    }
}
