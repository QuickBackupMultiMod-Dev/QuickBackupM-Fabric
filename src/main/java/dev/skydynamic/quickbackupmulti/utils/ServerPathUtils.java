package dev.skydynamic.quickbackupmulti.utils;

import dev.skydynamic.quickbackupmulti.api.ServerPathGetter;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ServerPathUtils implements ServerPathGetter {
    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getGamePath() {
        return FabricLoader.getInstance().getGameDir();
    }
}
