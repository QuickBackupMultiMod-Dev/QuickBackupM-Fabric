package io.github.skydynamic.quickbackupmulti.mixin;

import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.config.Config;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.setDataBase;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.shutdownSchedule;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.startSchedule;

@Environment(EnvType.SERVER)
@Mixin(MinecraftServer.class)
public class MinecraftServer_ServerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void setServer(CallbackInfo ci) {
        Config.TEMP_CONFIG.setServerValue((MinecraftServer)(Object)this);
    }

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMulti(CallbackInfo ci) {
        Path backupDir = Path.of(QbmConstant.pathGetter.getGamePath() + "/QuickBackupMulti/");
        Config.TEMP_CONFIG.setWorldName("");
        createBackupDir(backupDir);
        setDataBase("server");
        startSchedule();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void injectShutDown(CallbackInfo ci) {
        shutdownSchedule();
        if (!Config.TEMP_CONFIG.isBackup) getDataBase().stopInternalMongoServer();
    }
}
