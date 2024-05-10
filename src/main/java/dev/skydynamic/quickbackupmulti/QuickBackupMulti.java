package dev.skydynamic.quickbackupmulti;

import dev.skydynamic.quickbackupmulti.i18n.Translate;
import dev.skydynamic.quickbackupmulti.utils.DataBase;
import dev.skydynamic.quickbackupmulti.config.Config;

import dev.skydynamic.quickbackupmulti.config.ConfigStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
//#if MC>=12005
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//#endif
//#if MC>=11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.network.PacketByteBuf;

import org.apache.logging.log4j.LogManager;
//#if MC>=11900
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//#else
//$$ import org.apache.logging.log4j.Logger;
//#endif

import static dev.skydynamic.quickbackupmulti.QbmConstant.REQUEST_OPEN_CONFIG_GUI_PACKET_ID;
import static dev.skydynamic.quickbackupmulti.QbmConstant.gson;
import static dev.skydynamic.quickbackupmulti.command.QuickBackupMultiCommand.RegisterCommand;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.*;

public class QuickBackupMulti implements ModInitializer {

	//#if MC>=11900
	public static final Logger LOGGER = LoggerFactory.getLogger(QuickBackupMulti.class);
	//#else
	//$$ public static final Logger LOGGER = LogManager.getLogger(QuickBackupMulti.class);
	//#endif

	EnvType env = FabricLoader.getInstance().getEnvironmentType();

	@Override
	public void onInitialize() {
		//#if MC>=12005
		//$$ Packets.registerPacketCodec();
		//#endif
		final JavaUtilLog4jFilter filter = new JavaUtilLog4jFilter();
		java.util.logging.Logger.getLogger("").setFilter(filter);
		((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(filter);

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
		//#if MC>=12005
		//$$ ServerPlayNetworking.registerGlobalReceiver(Packets.RequestOpenConfigGuiPacket.PACKET_ID, (payload, context) -> {
		//#else
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_OPEN_CONFIG_GUI_PACKET_ID, (server, player, handler, buf, responseSender) -> {
		//#endif
			//#if MC>=12005
			//$$ ServerPlayerEntity player = context.player();
			//$$ if (player.hasPermissionLevel(2)) ServerPlayNetworking.send(player, new Packets.OpenConfigGuiPacket(Config.INSTANCE.getConfigStorage()));
			//#else
			if (player.hasPermissionLevel(2)) {
				PacketByteBuf sendBuf = PacketByteBufs.create();
				sendBuf.writeString(Config.INSTANCE.getConfigStorage());
				ServerPlayNetworking.send(player, QbmConstant.OPEN_CONFIG_GUI_PACKET_ID, sendBuf);
			}
			//#endif
		});

		//#if MC>=12005
		//$$ ServerPlayNetworking.registerGlobalReceiver(Packets.SaveConfigPacket.PACKET_ID, (payload, context) -> {
		//#else
		ServerPlayNetworking.registerGlobalReceiver(QbmConstant.SAVE_CONFIG_PACKET_ID, (server, player, handler, buf, responseSender) -> {
		//#endif
			//#if MC>=12005
			//$$ ServerPlayerEntity player = context.player();
			//#endif
			if (player.hasPermissionLevel(2)) {
				//#if MC>=12005
				//$$ String configStorage = payload.config();
				//#else
				String configStorage = buf.readString();
				//#endif
				ConfigStorage c = gson.fromJson(configStorage, ConfigStorage.class);
				// Verify config
				ConfigStorage result = verifyConfig(c, player);
				Config.INSTANCE.setConfigStorage(result);
			}
		});
	}

	public static boolean shouldFilterMessage(String message) {
		// 仅过滤INFO，Debug / ERROR不过滤
		if (message.contains("INFO")) {
			return message.contains("Mongo") || message.contains("H2Backend") || message.contains("cluster");
		}
		return false;
	}

	public static DataBase getDataBase() {
		return Config.TEMP_CONFIG.dataBase;
	}

	public static void setDataBase(String worldName) {
		Config.TEMP_CONFIG.setDataBase(new DataBase(worldName));
	}
}