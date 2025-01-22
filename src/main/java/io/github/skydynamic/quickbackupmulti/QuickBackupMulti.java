package io.github.skydynamic.quickbackupmulti;

import io.github.skydynamic.increment.storage.lib.util.IndexUtil;
import io.github.skydynamic.increment.storage.lib.util.Storager;
import io.github.skydynamic.increment.storage.lib.database.DataBase;
import io.github.skydynamic.quickbackupmulti.config.QbmTempConfig;
import io.github.skydynamic.quickbackupmulti.config.QuickBackupMultiConfig;
import io.github.skydynamic.quickbackupmulti.i18n.Translate;
import io.github.skydynamic.quickbackupmulti.command.QuickBackupMultiCommand;
import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
import io.github.skydynamic.quickbackupmulti.utils.UpdateChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
//#if MC>=11900
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//#else
//$$ import org.apache.logging.log4j.Logger;
//#endif
import java.nio.file.Path;


public class QuickBackupMulti implements ModInitializer {

	//#if MC>=11900
	public static final Logger LOGGER = LoggerFactory.getLogger(QuickBackupMulti.class);
	//#else
	//$$ public static final Logger LOGGER = LogManager.getLogger(QuickBackupMulti.class);
	//#endif

	public static final UpdateChecker updateChecker = new UpdateChecker();

	public static final String modName = "QuickBackupMulti";
	public static final String modId = "quickbackupmulti";
	public static QbmTempConfig TEMP_CONFIG = new QbmTempConfig();

	EnvType env = FabricLoader.getInstance().getEnvironmentType();

	private static DataBase dataBase;
	private static Storager storager;

	public static final QuickBackupMultiConfig config = new QuickBackupMultiConfig(QbmConstant.pathGetter.getConfigPath().resolve(modName + ".json"));

	@Override
	public void onInitialize() {
		config.load();
		config.save();

		FabricLoader.getInstance().getModContainer(modId).ifPresent(modContainer ->
			TEMP_CONFIG.setModVersion(modContainer.getMetadata().getVersion().getFriendlyString()));

		final JavaUtilLog4jFilter filter = new JavaUtilLog4jFilter();
		java.util.logging.Logger.getLogger("").setFilter(filter);
		((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(filter);

		Translate.handleResourceReload(config.getLang());

		initDataBase();

		//#if MC>=11900
		CommandRegistrationCallback.EVENT.register(
			(dispatcher, registryAccess, environment) -> QuickBackupMultiCommand.RegisterCommand(dispatcher)
		);
		//#else
		//$$ CommandRegistrationCallback.EVENT.register(
		//$$ 	(dispatcher, registryAccess) -> QuickBackupMultiCommand.RegisterCommand(dispatcher)
		//$$ );
		//#endif

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			TEMP_CONFIG.setServerValue(server);
			TEMP_CONFIG.setEnv(env);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			if (env == EnvType.SERVER) {
				if (TEMP_CONFIG.isBackup) {
					QbmManager.restore(TEMP_CONFIG.backupSlot);
					TEMP_CONFIG.setIsBackupValue(false);
					switch (config.getAutoRestartMode()) {
						case DISABLE -> {}
						case DEFAULT -> {
							TEMP_CONFIG.server.stopped = false;
							TEMP_CONFIG.server.running = true;
							TEMP_CONFIG.server.runServer();
						}
						case MCSM -> new Thread(() -> System.exit(-4000)).start();
					}
				} else {
					getDataBase().stopInternalMongoServer();
				}
			}
			TEMP_CONFIG.server = null;
		});

		if (config.isCheckUpdate()) updateChecker.start();
	}

	public void initDataBase() {
		DataBaseManager dataBaseManager = new DataBaseManager(
			"QuickBackupMulti",
			Path.of(config.getStoragePath())
		);

		dataBase = new DataBase(dataBaseManager, config);

		IndexUtil.setConfig(config);
		IndexUtil.setDataBase(dataBase);
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

	public static void setDataStore(String worldName) {
		storager = new Storager(dataBase);
		dataBase.newDataStore(modName + "-" + worldName);
	}

	public static void deleteDataStore(String worldName) {
		dataBase.deleteCollection(modName + "-" + worldName);
	}
}