package dev.skydynamic.quickbackupmulti.mixin;

import dev.skydynamic.quickbackupmulti.utils.config.Config;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import org.quartz.SchedulerException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static dev.skydynamic.quickbackupmulti.utils.schedule.CronUtil.buildScheduler;

@Environment(EnvType.SERVER)
@Mixin(MinecraftServer.class)
public class MinecraftServer_ServerMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setServer(CallbackInfo ci) {
        Config.TEMP_CONFIG.setServerValue((MinecraftServer)(Object)this);
    }

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMulti(CallbackInfo ci) {
        Path backupDir = Path.of(System.getProperty("user.dir") + "/QuickBackupMulti/");
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

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void stopSchedule(CallbackInfo ci) {
        try {
            if (Config.TEMP_CONFIG.scheduler.isStarted()) Config.TEMP_CONFIG.scheduler.shutdown();
        } catch (SchedulerException ignored) {
        }
    }

}
