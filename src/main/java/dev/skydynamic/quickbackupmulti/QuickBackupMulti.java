package dev.skydynamic.quickbackupmulti;

import dev.skydynamic.quickbackupmulti.i18n.Translate;
import dev.skydynamic.quickbackupmulti.utils.config.Config;

import dev.skydynamic.quickbackupmulti.utils.schedule.ScheduleBackup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
//#if MC>=11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.restore;
import static dev.skydynamic.quickbackupmulti.command.QuickBackupMultiCommand.RegisterCommand;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.buildScheduler;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.buildTrigger;

public final class QuickBackupMulti implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("QuickBackupMulti");

	EnvType env = FabricLoader.getInstance().getEnvironmentType();

	@Override
	public void onInitialize() {
		Config.INSTANCE.load();
		Translate.handleResourceReload(Config.INSTANCE.getLang());

		buildScheduler();

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
			Config.TEMP_CONFIG.server = null;
		});
	}
}