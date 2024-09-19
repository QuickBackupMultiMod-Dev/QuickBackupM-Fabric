package io.github.skydynamic.quickbackupmulti.backup;

import io.github.skydynamic.quickbackupmulti.config.Config;
import io.github.skydynamic.quickbackupmulti.i18n.Translate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.restoreClient;

@Environment(EnvType.CLIENT)
public class ClientRestoreDelegate {

    private final List<ServerPlayerEntity> playerList;
    private final String slot;

    public ClientRestoreDelegate(List<ServerPlayerEntity> playerList, String slot) {
        this.playerList = playerList;
        this.slot = slot;
    }

    public void run() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.execute(() -> {
            minecraftClient.world.disconnect();
            minecraftClient.disconnect(new MessageScreen(Text.of("Restore backup")));
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                minecraftClient.execute(() -> minecraftClient.setScreen(null));
                restoreClient(slot);
                Config.TEMP_CONFIG.setIsBackupValue(false);
                getDataBase().stopInternalMongoServer();
                minecraftClient.execute(() -> {
                    Text title = Text.of(Translate.tr("quickbackupmulti.toast.end_title"));
                    Text content = Text.of(Translate.tr("quickbackupmulti.toast.end_content"));
                    //#if MC>=11800
                    SystemToast.show(minecraftClient.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, title, content);
                    //#else
                    //$$ SystemToast.show(minecraftClient.getToastManager(), SystemToast.Type.WORLD_BACKUP, title, content);
                    //#endif
                });
            });
        });
    }
}
