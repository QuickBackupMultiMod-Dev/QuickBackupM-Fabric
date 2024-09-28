package io.github.skydynamic.quickbackupmulti.command.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.skydynamic.quickbackupmulti.QbmConstant;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static io.github.skydynamic.quickbackupmulti.QbmConstant.gson;
import static io.github.skydynamic.quickbackupmulti.QbmConstant.permissionManager;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.getPlayerFromCommandSource;

public class PermissionManager {
    private static final Path configPath = QbmConstant.pathGetter.getConfigPath();
    private static final File config = configPath.resolve("QuickBackupMulti-Permission.json").toFile();
    private PermissionConfig permissionConfig;

    public PermissionManager() {
        if (!config.exists()) {
            initPermission();
        } else {
            loadPermissionByFile();
        }
    }

    public void setPermissionByPermissionLevelInt(int level, ServerPlayerEntity player) {
        this.permissionConfig.setByPermissionType(PermissionType.getByLevelInt(level), player);
    }

    public void setPermissionByPermissionType(PermissionType permission, ServerPlayerEntity player) {
        this.permissionConfig.setByPermissionType(permission, player);
    }

    public PermissionType getPlayerPermission(ServerPlayerEntity player) {
        return permissionConfig.perm.getOrDefault(player.getName().getString(), PermissionType.USER);
    }

    public int getPlayerPermissionLevel(ServerPlayerEntity player) {
        return getPlayerPermission(player).level;
    }

    private void loadPermissionByFile() {
        try {
            FileReader reader = new FileReader(config);
            this.permissionConfig = gson.fromJson(reader, PermissionConfig.class);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePermissionToFile() {
        try {
            if (config.exists()) config.delete();
            if (!config.exists()) config.createNewFile();
            FileWriter writer = new FileWriter(config);
            gson.toJson(this.permissionConfig, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadPermission() {
        loadPermissionByFile();
    }

    public void initPermission() {
        try {
            this.permissionConfig = new PermissionConfig();
            config.createNewFile();
            FileWriter writer = new FileWriter(config);
            gson.toJson(this.permissionConfig, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasPermission(
        @NotNull ServerCommandSource source,
        int mcPermission,
        PermissionType modPermission
    ) {
        ServerPlayerEntity player = getPlayerFromCommandSource(source);
        if (player != null) {
            if (checkLocalGamePermission(source)) {
                return true;
            } else {
                return source.hasPermissionLevel(mcPermission)
                    || permissionManager.getPlayerPermissionLevel(player) >= modPermission.level;
            }
        }
        return true;
    }

    public static boolean checkLocalGamePermission(@NotNull ServerCommandSource source) {
        try {
            return getPermission(source);
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    private static boolean getPermission(ServerCommandSource source) throws CommandSyntaxException {
        boolean flag = source.hasPermissionLevel(4);
        ServerPlayerEntity player;
        MinecraftServer server;
        if (!flag && (server = source.getServer()).isSingleplayer() && (player = source.getPlayer()) != null) {
            flag = server.isHost(player.getGameProfile());
        }
        return flag;
    }

    static class PermissionConfig {
        private final Map<String, PermissionType> perm = new HashMap<>();
        public void setByPermissionType(PermissionType type, ServerPlayerEntity player) {
            perm.put(player.getName().getString(), type);
            permissionManager.savePermissionToFile();
        }
    }
}
