package dev.skydynamic.quickbackupmulti.mixin;

import dev.skydynamic.quickbackupmulti.utils.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import org.quartz.SchedulerException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.buildScheduler;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftServer.class)
public class MinecraftServer_ClientMixin {

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMultiClient(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Config.TEMP_CONFIG.setServerValue(server);
        String worldName = server.getSaveProperties().getLevelName();
        Config.TEMP_CONFIG.setWorldName(worldName);
        Path backupDir = Path.of(System.getProperty("user.dir") + "/QuickBackupMulti/").resolve(worldName);
        createBackupDir(backupDir);
        if (Config.INSTANCE.getScheduleBackup()) {
            try {
                buildScheduler();
                Config.TEMP_CONFIG.scheduler.start();
                Config.TEMP_CONFIG.setLatestScheduleExecuteTime(System.currentTimeMillis());
                LOGGER.info("QBM Schedule backup started.");
            } catch (SchedulerException e) {
                LOGGER.error("QBM schedule backup start error: " + e);
            }
        }
    }
}
