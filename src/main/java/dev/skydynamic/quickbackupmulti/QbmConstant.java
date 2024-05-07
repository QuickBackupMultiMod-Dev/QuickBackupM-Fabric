package dev.skydynamic.quickbackupmulti;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.skydynamic.quickbackupmulti.utils.ServerPathUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

public final class QbmConstant {
    public static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    public static final ServerPathUtils pathGetter = new ServerPathUtils();
    public static final Identifier REQUEST_OPEN_CONFIG_GUI_PACKET_ID = new Identifier("quickbackupmulti", "request_open_config_gui");
    public static final Identifier OPEN_CONFIG_GUI_PACKET_ID = new Identifier("quickbackupmulti", "open_config_gui");
    public static final Identifier SAVE_CONFIG_PACKET_ID = new Identifier("quickbackupmulti", "save_config");
}
