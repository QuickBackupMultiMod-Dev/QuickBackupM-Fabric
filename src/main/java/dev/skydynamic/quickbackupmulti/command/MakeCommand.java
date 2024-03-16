package dev.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.make;
import static net.minecraft.server.command.CommandManager.literal;

public class MakeCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> makeCommand = literal("make").requires(me -> me.hasPermissionLevel(2))
        .executes(it -> makeSaveBackup(it.getSource(), -1, ""))
        .then(CommandManager.argument("slot", IntegerArgumentType.integer(1))
            .executes(it -> makeSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot"), ""))
            .then(CommandManager.argument("desc", StringArgumentType.string())
                .executes(it -> makeSaveBackup(it.getSource(), IntegerArgumentType.getInteger(it, "slot"), StringArgumentType.getString(it, "desc"))))
        )
        .then(CommandManager.argument("desc", StringArgumentType.string())
            .executes(it -> makeSaveBackup(it.getSource(), -1, StringArgumentType.getString(it, "desc"))));

    private static int makeSaveBackup(ServerCommandSource commandSource, int slot, String desc) {
        return make(commandSource, slot, desc);
    }
}
