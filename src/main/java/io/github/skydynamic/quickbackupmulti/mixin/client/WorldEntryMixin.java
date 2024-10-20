package io.github.skydynamic.quickbackupmulti.mixin.client;

import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldListWidget.WorldEntry.class)
public class WorldEntryMixin {
    @Shadow @Final private LevelSummary level;

    @Inject(
        method = "delete",
        at = @At(
            value = "RETURN"
        )
    )
    private void onDelete(CallbackInfo ci)
    {
        String savePathName = this.level.getName();
        QbmManager.deleteWorld(savePathName);
    }
}
