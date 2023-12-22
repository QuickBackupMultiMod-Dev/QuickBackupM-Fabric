package dev.skydynamic.quickbackupmulti.mixin;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static dev.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;

@Environment(EnvType.SERVER)
@Mixin(MinecraftServer.class)
public abstract class MinecraftServer_ServerMixin {
    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMulti(CallbackInfo ci) {
        Path backupDir = Path.of(System.getProperty("user.dir") + "/QuickBackupMulti/");
        createBackupDir(backupDir);
    }

}
