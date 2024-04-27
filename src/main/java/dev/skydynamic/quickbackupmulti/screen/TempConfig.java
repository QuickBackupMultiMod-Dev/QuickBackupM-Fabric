package dev.skydynamic.quickbackupmulti.screen;

import dev.skydynamic.quickbackupmulti.utils.config.ConfigStorage;
import dev.skydynamic.quickbackupmulti.utils.storage.BackupInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class TempConfig {
    public ConfigStorage config;
    public HashMap<String, BackupInfo> backupsData = new HashMap<>();
    public static TempConfig tempConfig = new TempConfig();
    public void setConfig(ConfigStorage config) {
        this.config = config;
    }

    public void setBackupsData(HashMap<String, BackupInfo> backupsData) {
        this.backupsData = backupsData;
    }
}
