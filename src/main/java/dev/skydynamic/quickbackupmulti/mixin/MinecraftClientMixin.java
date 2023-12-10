package dev.skydynamic.quickbackupmulti.mixin;

import dev.skydynamic.quickbackupmulti.utils.config.Config;
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

import static dev.skydynamic.quickbackupmulti.utils.Translate.tr;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow @Final private ToastManager toastManager;

    @Inject(method = "setScreen", at = @At("RETURN"))
    void inj(CallbackInfo ci) {
        if (Config.TEMPCONFIG.isBackup) {
            Text title = Text.of(tr("quickbackupmulti.toast.start_title"));
            Text content = Text.of(tr("quickbackupmulti.toast.start_content"));
            SystemToast.show(this.toastManager, SystemToast.Type.PERIODIC_NOTIFICATION, title, content);
        }
    }

}
