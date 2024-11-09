package io.github.skydynamic.quickbackupmulti.config;

import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.Scheduler;

public class QbmTempConfig {
    public Boolean isBackup = false;
    public MinecraftServer server;
    public String backupSlot;
    public EnvType env;
    public String worldName;
    public @Nullable Scheduler scheduler;
    public long latestScheduleExecuteTime;
    public String modVersion;

    public void setIsBackupValue(Boolean value) {
        this.isBackup = value;
    }

    public void setServerValue(MinecraftServer server) {
        this.server = server;
    }

    public void setBackupSlot(String slot) {
        this.backupSlot = slot;
    }

    public void setEnv(EnvType env) {
        this.env = env;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public void setScheduler(@NotNull Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setLatestScheduleExecuteTime(long time) {
        this.latestScheduleExecuteTime = time;
    }

    public void setModVersion(String modVersion) {
        this.modVersion = modVersion;
    }
}
