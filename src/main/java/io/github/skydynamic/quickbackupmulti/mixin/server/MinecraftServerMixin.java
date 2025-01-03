package io.github.skydynamic.quickbackupmulti.mixin.server;

//#if MC<11900
//$$ import com.mojang.authlib.GameProfileRepository;
//$$ import com.mojang.authlib.minecraft.MinecraftSessionService;
//$$ import net.minecraft.util.UserCache;
//#else
import net.minecraft.util.ApiServices;
//#endif
import com.mojang.datafixers.DataFixer;
import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.nio.file.Path;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.setDataStore;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.shutdownSchedule;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.startSchedule;

@Environment(EnvType.SERVER)
@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftServerMixin extends MinecraftServer {
    //#if MC>=11900
    public MinecraftServerMixin(
        Thread serverThread, LevelStorage.Session session,
        ResourcePackManager dataPackManager, SaveLoader saveLoader,
        Proxy proxy, DataFixer dataFixer, ApiServices apiServices,
        WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory
    ) {
        super(serverThread, session, dataPackManager, saveLoader, proxy, dataFixer, apiServices, worldGenerationProgressListenerFactory);
    }
    //#else
    //$$ public MinecraftServerMixin(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
    //$$     super(serverThread, session, dataPackManager, saveLoader, proxy, dataFixer, sessionService, gameProfileRepo, userCache, worldGenerationProgressListenerFactory);
    //$$ }
    //#endif

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setServer(CallbackInfo ci) {
        QuickBackupMulti.TEMP_CONFIG.setServerValue(this);
    }

    @Inject(
        method = "setupServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/dedicated/MinecraftDedicatedServer;loadWorld()V",
            shift = At.Shift.AFTER
        )
    )
    private void initQuickBackupMulti(CallbackInfoReturnable<Boolean> cir) {
        Path backupDir = Path.of(QbmConstant.pathGetter.getGamePath() + "/QuickBackupMulti/");
        QuickBackupMulti.TEMP_CONFIG.setWorldName("");
        QbmManager.savePath = this.getSavePath(WorldSavePath.ROOT);
        createBackupDir(backupDir);
        setDataStore("server");
        startSchedule();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void injectShutDown(CallbackInfo ci) {
        shutdownSchedule();
        if (!QuickBackupMulti.TEMP_CONFIG.isBackup) getDataBase().stopInternalMongoServer();
    }
}
