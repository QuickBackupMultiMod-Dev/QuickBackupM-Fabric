package dev.skydynamic.quickbackupmulti;

import dev.skydynamic.quickbackupmulti.i18n.Translate;
import dev.skydynamic.quickbackupmulti.utils.config.Config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.ModContainer;
//#if MC>=11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.restore;
import static dev.skydynamic.quickbackupmulti.command.QuickBackupMultiCommand.RegisterCommand;

public final class QuickBackupMulti implements ModInitializer {
	public static final String MODID = "quickbackupmulti";

    public static final Logger LOGGER = LoggerFactory.getLogger("QuickBackupMulti");

	EnvType env = FabricLoader.getInstance().getEnvironmentType();

	@Override
	public void onInitialize() {
		ModContainer qbm = FabricLoader.getInstance().getModContainer(MODID)
			.orElseThrow(() -> new IllegalStateException("Couldn't find the mod container for QuickBackupMulti"));

		Config.INSTANCE.load();
		Translate.handleResourceReload(Config.INSTANCE.getLang());

		//#if MC>=11900
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> RegisterCommand(dispatcher));
		//#else
		//$$ CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> RegisterCommand(dispatcher));
		//#endif
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
		});
	}
}