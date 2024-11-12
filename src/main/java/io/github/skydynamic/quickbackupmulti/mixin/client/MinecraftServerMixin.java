package io.github.skydynamic.quickbackupmulti.mixin.client;

import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.setDataStore;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.shutdownSchedule;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.startSchedule;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMultiClient(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        QuickBackupMulti.TEMP_CONFIG.setServerValue(server);
        Path saveDirectoryPath = server.getSavePath(WorldSavePath.ROOT);
        String worldName = saveDirectoryPath.getParent().getFileName().toString();
        QuickBackupMulti.TEMP_CONFIG.setWorldName(worldName);
        Path backupDir = Path.of(QbmConstant.pathGetter.getGamePath() + "/QuickBackupMulti/").resolve(worldName);
        QbmManager.savePath = saveDirectoryPath;
        createBackupDir(backupDir);
        setDataStore(worldName);
        startSchedule();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void stopSchedule(CallbackInfo ci) {
        shutdownSchedule();
    }
}
