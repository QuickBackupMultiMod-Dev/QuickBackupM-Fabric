package io.github.skydynamic.quickbackupmulti;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.skydynamic.quickbackupmulti.command.permission.PermissionManager;
import io.github.skydynamic.quickbackupmulti.utils.ServerPathUtils;
import net.minecraft.util.Identifier;

public final class QbmConstant {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final ServerPathUtils pathGetter = new ServerPathUtils();
    public static final PermissionManager permissionManager = new PermissionManager();
}
