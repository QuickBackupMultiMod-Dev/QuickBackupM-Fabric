package dev.skydynamic.quickbackupmulti;

import dev.skydynamic.quickbackupmulti.i18n.Translate;
import dev.skydynamic.quickbackupmulti.utils.config.Config;

import dev.skydynamic.quickbackupmulti.utils.config.ConfigStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
//#if MC>=11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.network.PacketByteBuf;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.skydynamic.quickbackupmulti.QbmConstant.REQUEST_OPEN_CONFIG_GUI_PACKET_ID;
import static dev.skydynamic.quickbackupmulti.QbmConstant.gson;
import static dev.skydynamic.quickbackupmulti.command.QuickBackupMultiCommand.RegisterCommand;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;

public final class QuickBackupMulti implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("QuickBackupMulti");

	EnvType env = FabricLoader.getInstance().getEnvironmentType();

	@Override
	public void onInitialize() {
		Config.INSTANCE.load();
		Translate.handleResourceReload(Config.INSTANCE.getLang());

		//#if MC>=11900
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> RegisterCommand(dispatcher));
		//#else
		//$$ CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> RegisterCommand(dispatcher));
		//#endif
		registerPacketHandler();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Config.TEMP_CONFIG.setServerValue(server);
			Config.TEMP_CONFIG.setEnv(env);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			if (Config.TEMP_CONFIG.isBackup) {
				if (env == EnvType.SERVER) {
					restore(Config.TEMP_CONFIG.backupSlot);
					Config.TEMP_CONFIG.setIsBackupValue(false);
					Config.TEMP_CONFIG.server.stopped = false;
					Config.TEMP_CONFIG.server.running = true;
					Config.TEMP_CONFIG.server.runServer();
				}
			}
			Config.TEMP_CONFIG.server = null;
		});
	}

	public static void registerPacketHandler() {
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_OPEN_CONFIG_GUI_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			if (player.hasPermissionLevel(2)) {
				PacketByteBuf sendBuf = PacketByteBufs.create();
				sendBuf.writeString(Config.INSTANCE.getConfigStorage());
				ServerPlayNetworking.send(player, QbmConstant.OPEN_CONFIG_GUI_PACKET_ID, sendBuf);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(QbmConstant.SAVE_CONFIG_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			if (player.hasPermissionLevel(2)) {
				String configStorage = buf.readString();
				ConfigStorage c = gson.fromJson(configStorage, ConfigStorage.class);
				// Verify config
				ConfigStorage result = verifyConfig(c, player);
				Config.INSTANCE.setConfigStorage(result);
			}
		});
	}
}