package dev.skydynamic.quickbackupmulti;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class QbmConstant {

    public static Path configDir = FabricLoader.getInstance().getConfigDir();
    public static Path gameDir = FabricLoader.getInstance().getGameDir();

}
