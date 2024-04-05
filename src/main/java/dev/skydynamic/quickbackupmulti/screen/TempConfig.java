package dev.skydynamic.quickbackupmulti.screen;

import dev.skydynamic.quickbackupmulti.utils.config.ConfigStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TempConfig {
    public ConfigStorage config;
    public static TempConfig tempConfig = new TempConfig();
    public void setConfig(ConfigStorage config) {
        this.config = config;
    }
}
