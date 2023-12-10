package dev.skydynamic.quickbackupmulti.mixin;

import dev.skydynamic.quickbackupmulti.utils.config.Config;

import dev.skydynamic.quickbackupmulti.utils.Translate;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static dev.skydynamic.quickbackupmulti.QuickBackupMulti.LOGGER;
import static dev.skydynamic.quickbackupmulti.utils.Translate.tr;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onServerInit(CallbackInfo ci) {
        Config.INSTANCE.load();
        Translate.handleResourceReload(Config.INSTANCE.getLang());
    }

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMulti(CallbackInfo ci) {
        Path backupDir = Path.of(System.getProperty("user.dir") + "/QuickBackupMulti/");
        if (!backupDir.toFile().exists()) {
            LOGGER.info(tr("quickbackupmulti.init.start"));
            backupDir.toFile().mkdir();
            LOGGER.info(tr("quickbackupmulti.init.finish"));
        }
        for(int j=1; j<=Config.INSTANCE.getNumOfSlot(); j++) {
            if (!backupDir.resolve("Slot" + j).toFile().exists()) backupDir.resolve("Slot" + j).toFile().mkdir();
        }
    }

}
