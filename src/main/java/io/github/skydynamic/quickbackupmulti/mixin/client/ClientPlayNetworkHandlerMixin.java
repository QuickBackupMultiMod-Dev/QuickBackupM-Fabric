package io.github.skydynamic.quickbackupmulti.mixin.client;

import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.utils.Messenger;
import io.github.skydynamic.quickbackupmulti.utils.UpdateChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.skydynamic.quickbackupmulti.i18n.Translate.tr;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(
        method = "onGameJoin",
        at = @At("TAIL")
    )
    private void showUpdateMsg(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) {
			return;
		}
        UpdateChecker checker = QuickBackupMulti.updateChecker;
        if (checker.needUpdate) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            MutableText updateText = Messenger.literal(
                tr("quickbackupmulti.check_update.on_player_join", checker.latestVersion)
            );
            updateText.styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, checker.latestVersionHtmUrl))
                .withHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, Messenger.literal(tr("quickbackupmulti.check_update.click_tooltip"))
                ))
            );
            player.sendMessage(updateText, false);
        }
    }
}
