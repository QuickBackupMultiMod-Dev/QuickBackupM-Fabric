package io.github.skydynamic.quickbackupmulti;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.skydynamic.quickbackupmulti.command.PermissionManager;
import io.github.skydynamic.quickbackupmulti.utils.ServerPathUtils;
import net.minecraft.util.Identifier;

public final class QbmConstant {
    public static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    public static final ServerPathUtils pathGetter = new ServerPathUtils();
    public static final PermissionManager permissionManager = new PermissionManager();
    public static final Identifier REQUEST_OPEN_CONFIG_GUI_PACKET_ID = Identifier.tryParse("quickbackupmulti:request_open_config_gui");
    public static final Identifier OPEN_CONFIG_GUI_PACKET_ID = Identifier.tryParse("quickbackupmulti:open_config_gui");
    public static final Identifier SAVE_CONFIG_PACKET_ID = Identifier.tryParse("quickbackupmulti:save_config");
}
