package io.github.skydynamic.quickbackupmulti.mixin.client;

import com.mojang.datafixers.DataFixer;
import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ApiServices;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.nio.file.Path;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.setDataStore;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.shutdownSchedule;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.startSchedule;

@Environment(EnvType.CLIENT)
@Mixin(IntegratedServer.class)
public abstract class MinecraftServerMixin extends MinecraftServer {
    public MinecraftServerMixin(
        Thread serverThread, LevelStorage.Session session,
        ResourcePackManager dataPackManager, SaveLoader saveLoader,
        Proxy proxy, DataFixer dataFixer, ApiServices apiServices,
        WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory
    ) {
        super(serverThread, session, dataPackManager, saveLoader, proxy, dataFixer, apiServices, worldGenerationProgressListenerFactory);
    }

    @Inject(
        method = "setupServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/integrated/IntegratedServer;loadWorld()V",
            shift = At.Shift.AFTER
        )
    )
    private void initQuickBackupMultiClient(CallbackInfoReturnable<Boolean> cir) {
        QuickBackupMulti.TEMP_CONFIG.setServerValue(this);
        Path saveDirectoryPath = this.getSavePath(WorldSavePath.ROOT);
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
