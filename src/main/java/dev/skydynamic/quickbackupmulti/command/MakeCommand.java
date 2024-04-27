package dev.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.text.SimpleDateFormat;

import static dev.skydynamic.quickbackupmulti.utils.MakeUtils.make;
import static net.minecraft.server.command.CommandManager.literal;

public class MakeCommand {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    public static LiteralArgumentBuilder<ServerCommandSource> makeCommand = literal("make").requires(me -> me.hasPermissionLevel(2))
        .executes(it -> makeSaveBackup(it.getSource(), dateFormat.format(System.currentTimeMillis()), ""))
        .then(CommandManager.argument("name", StringArgumentType.string())
            .executes(it -> makeSaveBackup(it.getSource(), StringArgumentType.getString(it, "name"), ""))
            .then(CommandManager.argument("desc", StringArgumentType.string())
                .executes(it -> makeSaveBackup(it.getSource(), StringArgumentType.getString(it, "name"), StringArgumentType.getString(it, "desc"))))
        );
//        .then(CommandManager.argument("desc", StringArgumentType.string())
//            .executes(it -> makeSaveBackup(it.getSource(), String.valueOf(System.currentTimeMillis()), StringArgumentType.getString(it, "desc"))));

    private static int makeSaveBackup(ServerCommandSource commandSource, String name, String desc) {
        return make(commandSource, name, desc);
    }
}
