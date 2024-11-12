package io.github.skydynamic.quickbackupmulti.mixin.client;

import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Final
    public ToastManager toastManager;

    @Inject(method = "setScreen", at = @At("RETURN"))
    private void inj(CallbackInfo ci) {
        if (QuickBackupMulti.TEMP_CONFIG.isBackup) {
            Text title = Text.of(tr("quickbackupmulti.toast.start_title"));
            Text content = Text.of(tr("quickbackupmulti.toast.start_content"));
            //#if MC>=11800
            SystemToast.show(this.toastManager, SystemToast.Type.PERIODIC_NOTIFICATION, title, content);
            //#else
            //$$ SystemToast.show(this.toastManager, SystemToast.Type.WORLD_BACKUP, title, content);
            //#endif
        }
    }
}
