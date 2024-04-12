package dev.skydynamic.quickbackupmulti;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

public final class QbmConstant {
    public static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    public static final Path configDir = FabricLoader.getInstance().getConfigDir();
    public static final Path gameDir = FabricLoader.getInstance().getGameDir();
    public static final Identifier REQUEST_OPEN_CONFIG_GUI_PACKET_ID = new Identifier("quickbackupmulti", "request_open_config_gui");
    public static final Identifier OPEN_CONFIG_GUI_PACKET_ID = new Identifier("quickbackupmulti", "open_config_gui");
    public static final Identifier SAVE_CONFIG_PACKET_ID = new Identifier("quickbackupmulti", "save_config");
    public static final Identifier GET_BACKUP_LIST_PACKET_ID = new Identifier("quickbackupmulti", "get_backup_list");
}
