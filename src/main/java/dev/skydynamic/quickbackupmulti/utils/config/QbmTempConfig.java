package dev.skydynamic.quickbackupmulti.utils.config;

import net.minecraft.server.MinecraftServer;

public class QbmTempConfig {
    public Boolean isBackup = false;
    public MinecraftServer server;
    public int backupSlot;

    public void setIsBackupValue(Boolean value) {
        this.isBackup = value;
    }

    public void setServerValue(MinecraftServer server) {
        this.server = server;
    }

    public void setBackupSlot(int slot) {
        this.backupSlot = slot;
    }

}
