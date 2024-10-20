package io.github.skydynamic.quickbackupmulti.mixin.client;

import io.github.skydynamic.quickbackupmulti.config.Config;
import io.github.skydynamic.quickbackupmulti.utils.Messenger;
import net.minecraft.client.gui.Element;

import net.minecraft.client.gui.screen.TitleScreen;
//#if MC>=11900
import net.minecraft.client.gui.tooltip.Tooltip;
//#endif
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "tick", at = @At("RETURN"))
    private void setButtonActive(CallbackInfo ci) {
        if (!Config.TEMP_CONFIG.isBackup) {
            TitleScreen screen = (TitleScreen) (Object) this;
            screen.children()
                .stream()
                .filter(e -> e instanceof ButtonWidget)
                //#if MC>=11900
                .filter(it -> ((ButtonWidget) it).getMessage().getContent() instanceof TranslatableTextContent)
                //#else
                //$$ .filter(it -> ((ClickableWidget) it).getMessage() instanceof TranslatableText)
                //#endif
                .filter(it -> ((ClickableWidget) it).getMessage().toString().contains("menu.singleplayer"))
                .forEach(e -> {
                    ((ButtonWidget) e).active = true;
                    //#if MC>=11900
                    ((ButtonWidget) e).setTooltip(null);
                    //#endif
                });
        }
    }
}
