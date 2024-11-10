package io.github.skydynamic.quickbackupmulti.backup;

import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.command.QuickBackupMultiCommand;
import net.fabricmc.api.EnvType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.TimerTask;

public class RestoreTask extends TimerTask {

    private final EnvType env;
    private final List<ServerPlayerEntity> playerList;
    private final String slot;

    public RestoreTask(EnvType env, List<ServerPlayerEntity> playerList, String slot) {
        this.env = env;
        this.playerList = playerList;
        this.slot = slot;
    }

    @Override
    public void run() {
        QuickBackupMultiCommand.QbDataHashMap.clear();
        QuickBackupMulti.TEMP_CONFIG.setIsBackupValue(true);
        if (env == EnvType.SERVER) {
            for (ServerPlayerEntity player : playerList) {
                player.networkHandler.disconnect(Text.of("Server restore backup"));
            }
            QuickBackupMulti.TEMP_CONFIG.setIsBackupValue(true);
            QuickBackupMulti.TEMP_CONFIG.server.stop(true);
        } else {
            //不分到另一个class中执行 会找不到Screen然后炸（
            new ClientRestoreDelegate(playerList, slot).run();
        }
    }
}
