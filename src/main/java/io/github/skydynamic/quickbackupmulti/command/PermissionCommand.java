package io.github.skydynamic.quickbackupmulti.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionManager;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionType;
import io.github.skydynamic.quickbackupmulti.utils.Messenger;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static io.github.skydynamic.quickbackupmulti.QbmConstant.permissionManager;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static net.minecraft.server.command.CommandManager.literal;

public class PermissionCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> permissionCommand = literal("permission")
        .requires(it -> PermissionManager.hasPermission(it, 4, PermissionType.ADMIN))
        .then(literal("set")
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 2))
                    .executes(it -> setPermission(
                            it.getSource(),
                            EntityArgumentType.getPlayer(it, "player"),
                            IntegerArgumentType.getInteger(it, "level")
                    ))
                )
            )
        )
        .then(literal("get")
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .executes(it -> getPermission(it.getSource(), EntityArgumentType.getPlayer(it, "player")))
            )
        )
        .then(literal("reload")
            .executes(it -> reloadPermission(it.getSource()))
        );

    private static int setPermission(ServerCommandSource commandSource, ServerPlayerEntity player, int level) {
        permissionManager.setPermissionByPermissionLevelInt(level, player);
        Messenger.sendMessage(commandSource,
            Messenger.literal(
                tr("quickbackupmulti.permission.set",
                    player.getName().getString(),
                    PermissionType.getByLevelInt(level).name()))
        );
        return 1;
    }

    private static int getPermission(ServerCommandSource commandSource, ServerPlayerEntity player) {
        Messenger.sendMessage(commandSource,
            Messenger.literal(
                tr("quickbackupmulti.permission.get",
                    player.getName().getString(),
                    permissionManager.getPlayerPermission(player).name()))
        );
        return 1;
    }

    private static int reloadPermission(ServerCommandSource commandSource) {
        permissionManager.reloadPermission();
        Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.permission.reload")));
        return 1;
    }
}
