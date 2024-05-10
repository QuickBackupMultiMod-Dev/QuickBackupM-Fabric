package dev.skydynamic.quickbackupmulti.mixin;

import dev.skydynamic.quickbackupmulti.config.Config;
import dev.skydynamic.quickbackupmulti.utils.Messenger;
import net.minecraft.client.gui.Element;

import net.minecraft.client.gui.screen.TitleScreen;
//#if MC>=11900
import net.minecraft.client.gui.tooltip.Tooltip;
//#endif
import net.minecraft.client.gui.widget.ButtonWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @ModifyArg(
        method = "initWidgetsNormal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
            ordinal = 0
        )
    )
    private Element setSinglePlayerButton(Element element) {
        if (Config.TEMP_CONFIG.isBackup) {
            //#if MC>=11900
            ((ButtonWidget) element).setTooltip(Tooltip.of(Messenger.literal("Restore now...")));
            //#endif
            ((ButtonWidget) element).active = false;
        }
        return element;
    }
}
