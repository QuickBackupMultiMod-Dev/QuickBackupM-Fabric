package dev.skydynamic.quickbackupmulti.mixin;

import dev.skydynamic.quickbackupmulti.QbmConstant;
import dev.skydynamic.quickbackupmulti.utils.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static dev.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.*;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftServer.class)
public class MinecraftServer_ClientMixin {

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMultiClient(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Config.TEMP_CONFIG.setServerValue(server);
        Path saveDirectoryPath = server.getSavePath(WorldSavePath.ROOT);
        String worldName = saveDirectoryPath.getParent().getFileName().toString();
        Config.TEMP_CONFIG.setWorldName(worldName);
        Path backupDir = Path.of(QbmConstant.gameDir + "/QuickBackupMulti/").resolve(worldName);
        createBackupDir(backupDir);
        startSchedule();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void stopSchedule(CallbackInfo ci) {
        shutdownSchedule();
    }
}
