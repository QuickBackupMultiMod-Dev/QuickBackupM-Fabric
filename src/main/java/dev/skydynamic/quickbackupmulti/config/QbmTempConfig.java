package dev.skydynamic.quickbackupmulti.config;

import dev.skydynamic.quickbackupmulti.utils.DataBase;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import org.quartz.Scheduler;

public class QbmTempConfig {
    public Boolean isBackup = false;
    public MinecraftServer server;
    public String backupSlot;
    public EnvType env;
    public String worldName;
    public Scheduler scheduler;
    public long latestScheduleExecuteTime;
    public DataBase dataBase;

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

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setLatestScheduleExecuteTime(long time) {
        this.latestScheduleExecuteTime = time;
    }

    public void setDataBase(DataBase dataBase) {
        this.dataBase = dataBase;
    }

}
