package dev.skydynamic.quickbackupmulti.mixin;

import dev.skydynamic.quickbackupmulti.QbmConstant;
import dev.skydynamic.quickbackupmulti.config.Config;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.setDataBase;
import static dev.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static dev.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.startSchedule;

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
        createBackupDir(backupDir);
        setDataBase("server");
        startSchedule();
    }

}
