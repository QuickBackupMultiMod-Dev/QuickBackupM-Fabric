package io.github.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionManager;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.text.SimpleDateFormat;

import static io.github.skydynamic.quickbackupmulti.utils.MakeUtils.make;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
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
            long l = System.currentTimeMillis();
            LOGGER.info("Make Backup thread started...");
            make(commandSource, name, desc);
            LOGGER.info("Make Backup thread close => {}ms", System.currentTimeMillis() - l);
        }
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    public static LiteralArgumentBuilder<ServerCommandSource> makeCommand = literal("make")
        .requires(it -> PermissionManager.hasPermission(it, 4, PermissionType.HELPER))
        .executes(it -> makeSaveBackup(it.getSource(), dateFormat.format(System.currentTimeMillis()), ""))
        .then(CommandManager.argument("name", StringArgumentType.string())
            .executes(it -> makeSaveBackup(it.getSource(), StringArgumentType.getString(it, "name"), ""))
            .then(CommandManager.argument("desc", StringArgumentType.string())
                .executes(it -> makeSaveBackup(
                    it.getSource(),
                    StringArgumentType.getString(it, "name"),
                    StringArgumentType.getString(it, "desc")
                )))
        );
//        .then(CommandManager.argument("desc", StringArgumentType.string())
//            .executes(it -> makeSaveBackup(it.getSource(), String.valueOf(System.currentTimeMillis()), StringArgumentType.getString(it, "desc"))));

    private static int makeSaveBackup(ServerCommandSource commandSource, String name, String desc) {
        new Thread(new makeRunnable(commandSource, name, desc)).start();
        return 1;
    }
}
