package io.github.skydynamic.quickbackupmulti;

import io.github.skydynamic.increment.storage.lib.util.IndexUtil;
import io.github.skydynamic.increment.storage.lib.util.Storager;
import io.github.skydynamic.increment.storage.lib.database.DataBase;
import io.github.skydynamic.quickbackupmulti.i18n.Translate;
import io.github.skydynamic.quickbackupmulti.config.Config;
import io.github.skydynamic.quickbackupmulti.config.ConfigStorage;
import io.github.skydynamic.quickbackupmulti.command.QuickBackupMultiCommand;
import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
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

//#if MC>=12005
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//#endif
import net.minecraft.network.PacketByteBuf;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
//#if MC>=11900
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//#else
//$$ import org.apache.logging.log4j.Logger;
//#endif
import java.nio.file.Path;

import static io.github.skydynamic.quickbackupmulti.QbmConstant.gson;


public class QuickBackupMulti implements ModInitializer {

	//#if MC>=11900
	public static final Logger LOGGER = LoggerFactory.getLogger(QuickBackupMulti.class);
	//#else
	//$$ public static final Logger LOGGER = LogManager.getLogger(QuickBackupMulti.class);
	//#endif
	public static final String modName = "QuickBackupMulti";

	EnvType env = FabricLoader.getInstance().getEnvironmentType();

	private static DataBase dataBase;
	private static Storager storager;

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
		CommandRegistrationCallback.EVENT.register(
			(dispatcher, registryAccess, environment) -> QuickBackupMultiCommand.RegisterCommand(dispatcher)
		);
		//#else
		//$$ CommandRegistrationCallback.EVENT.register(
		//$$ 	(dispatcher, registryAccess) -> QuickBackupMultiCommand.RegisterCommand(dispatcher)
		//$$ );
		//#endif
		registerPacketHandler();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Config.TEMP_CONFIG.setServerValue(server);
			Config.TEMP_CONFIG.setEnv(env);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			if (env == EnvType.SERVER) {
				if (Config.TEMP_CONFIG.isBackup) {
					QbmManager.restore(Config.TEMP_CONFIG.backupSlot);
					getDataBase().stopInternalMongoServer();
					Config.TEMP_CONFIG.setIsBackupValue(false);
					switch (Config.INSTANCE.getAutoRestartMode()) {
						case DISABLE -> {}
						case DEFAULT -> {
							Config.TEMP_CONFIG.server.stopped = false;
							Config.TEMP_CONFIG.server.running = true;
							Config.TEMP_CONFIG.server.runServer();
						}
						case MCSM -> new Thread(() -> System.exit(-4000)).start();
					}
				} else {
					getDataBase().stopInternalMongoServer();
				}
			}
			Config.TEMP_CONFIG.server = null;
		});
	}

	public static void registerPacketHandler() {
		//#if MC>=12005
		//$$ ServerPlayNetworking.registerGlobalReceiver(
		//$$ Packets.RequestOpenConfigGuiPacket.PACKET_ID, (payload, context) -> {
		//#else
		ServerPlayNetworking.registerGlobalReceiver(
			QbmConstant.REQUEST_OPEN_CONFIG_GUI_PACKET_ID, (server, player, handler, buf, responseSender) -> {
		//#endif
			//#if MC>=12005
			//$$ ServerPlayerEntity player = context.player();
			//$$ if (player.hasPermissionLevel(2)) ServerPlayNetworking.send(
			//$$ 	player, new Packets.OpenConfigGuiPacket(gson.toJson(Config.INSTANCE.getConfigStorage()))
			//$$ );
			//#else
			if (player.hasPermissionLevel(2)) {
				PacketByteBuf sendBuf = PacketByteBufs.create();
				sendBuf.writeString(gson.toJson(Config.INSTANCE.getConfigStorage()));
				ServerPlayNetworking.send(player, QbmConstant.OPEN_CONFIG_GUI_PACKET_ID, sendBuf);
			}
			//#endif
		});

		//#if MC>=12005
		//$$ ServerPlayNetworking.registerGlobalReceiver(Packets.SaveConfigPacket.PACKET_ID, (payload, context) -> {
		//#else
		ServerPlayNetworking.registerGlobalReceiver(
			QbmConstant.SAVE_CONFIG_PACKET_ID, (server, player, handler, buf, responseSender) -> {
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
				ConfigStorage c = QbmConstant.gson.fromJson(configStorage, ConfigStorage.class);
				// Verify config
				ConfigStorage result = QbmManager.verifyConfig(c, player);
				Config.INSTANCE.setConfigStorage(result);
			}
		});
	}

	public static boolean shouldFilterMessage(Level level, String packetName) {
		// 仅过滤INFO，Debug / ERROR不过滤
		if (level == Level.INFO) {
			return packetName.contains("de.bwaldvogel.mongo")
				|| packetName.contains("org.mongodb.driver")
				|| packetName.contains("org.quartz");
		}
		return false;
	}

	public static DataBase getDataBase() {
		return dataBase;
	}

	public static Storager getStorager() {
		return storager;
	}

	public static void setDataBase(String worldName) {
		DataBaseManager dataBaseManager = new DataBaseManager(
			"QuickBackupMulti",
			modName + "-" + worldName,
            Path.of(Config.INSTANCE.getStoragePath())
		);
		dataBase = new DataBase(dataBaseManager, Config.INSTANCE.getConfigStorage());
		storager = new Storager(dataBase);
		IndexUtil.setDataBase(dataBase);
	}
}