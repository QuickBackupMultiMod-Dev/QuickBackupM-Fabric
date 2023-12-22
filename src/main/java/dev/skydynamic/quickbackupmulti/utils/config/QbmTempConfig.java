package dev.skydynamic.quickbackupmulti.utils.config;

import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;

public class QbmTempConfig {
    public Boolean isBackup = false;
    public MinecraftServer server;
    public int backupSlot;
    public EnvType env;
    public String worldName;

    public void setIsBackupValue(Boolean value) {
        this.isBackup = value;
    }

    public void setServerValue(MinecraftServer server) {
        this.server = server;
    }

    public void setBackupSlot(int slot) {
        this.backupSlot = slot;
    }

    public void setEnv(EnvType env) {
        this.env = env;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

}
