package dev.skydynamic.quickbackupmulti;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class QbmConstant {

    public static final Path configDir = FabricLoader.getInstance().getConfigDir();
    public static final Path gameDir = FabricLoader.getInstance().getGameDir();

}
