package io.github.skydynamic.quickbackupmulti.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionManager;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionType;
import io.github.skydynamic.quickbackupmulti.utils.Messenger;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

import static io.github.skydynamic.quickbackupmulti.QbmConstant.permissionManager;
import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static net.minecraft.server.command.CommandManager.literal;

public class PermissionCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> permissionCommand = literal("permission")
        .requires(it -> PermissionManager.hasPermission(it, 4, PermissionType.ADMIN))
        .then(literal("set")
            .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 2))
                    .executes(it -> setPermission(
                            it.getSource(),
                            GameProfileArgumentType.getProfileArgument(it, "player"),
                            IntegerArgumentType.getInteger(it, "level")
                    ))
                )
            )
        )
        .then(literal("get")
            .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                .executes(it -> getPermission(
                    it.getSource(),
                    GameProfileArgumentType.getProfileArgument(it, "player")
                ))
            )
        )
        .then(literal("reload")
            .executes(it -> reloadPermission(it.getSource()))
        );

    private static int setPermission(ServerCommandSource commandSource, Collection<GameProfile> players, int level) {
        players.forEach(player -> {
            permissionManager.setPermissionByPermissionLevelInt(level, player.getName());
            Messenger.sendMessage(commandSource,
            Messenger.literal(
                tr("quickbackupmulti.permission.set",
                    player.getName(),
                    PermissionType.getByLevelInt(level).name()))
        );
        });
        return 1;
    }

    private static int getPermission(ServerCommandSource commandSource, Collection<GameProfile> players) {
        players.forEach(player -> {
            Messenger.sendMessage(commandSource,
                Messenger.literal(
                    tr("quickbackupmulti.permission.get",
                        player.getName(),
                        permissionManager.getPlayerPermission(player.getName()).name()))
            );
        });
        return 1;
    }

    private static int reloadPermission(ServerCommandSource commandSource) {
        permissionManager.reloadPermission();
        Messenger.sendMessage(commandSource, Messenger.literal(tr("quickbackupmulti.permission.reload")));
        return 1;
    }
}
