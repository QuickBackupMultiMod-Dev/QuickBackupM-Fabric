package dev.skydynamic.quickbackupmulti;

import dev.skydynamic.quickbackupmulti.screen.ConfigScreen;
import dev.skydynamic.quickbackupmulti.screen.TempConfig;
import dev.skydynamic.quickbackupmulti.utils.QbmManager;
import dev.skydynamic.quickbackupmulti.utils.config.ConfigStorage;

import net.fabricmc.api.ClientModInitializer;
//#if MC>=11900
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
//$$ import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
//$$ import com.mojang.brigadier.CommandDispatcher;
//#endif
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.HashMap;

import static dev.skydynamic.quickbackupmulti.QbmConstant.*;

public class QuickBackupMultiClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        //#if MC>=11900
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("open-qb-config-screen").executes(
                context -> {
                    ClientPlayNetworking.send(REQUEST_OPEN_CONFIG_GUI_PACKET_ID, PacketByteBufs.empty());
                    return 1;
                }
            ));
        });
        //#else
        //$$ CommandDispatcher<FabricClientCommandSource> dispatcher = ClientCommandManager.DISPATCHER;
        //$$ dispatcher.register(ClientCommandManager.literal("open-qb-config-screen").executes(
        //$$     context -> {
        //$$        ClientPlayNetworking.send(REQUEST_OPEN_CONFIG_GUI_PACKET_ID, PacketByteBufs.empty());
        //$$        return 1;
        //$$     }));
        //#endif

        registerPacketHandler();
    }

    public static void registerPacketHandler() {
        ClientPlayNetworking.registerGlobalReceiver(OPEN_CONFIG_GUI_PACKET_ID, (client, handler, buf, responseSender) -> {
            String config = buf.readString();
            ConfigStorage c = gson.fromJson(config, ConfigStorage.class);
            client.execute(() -> client.setScreen(new ConfigScreen(client.currentScreen, c)));
        });

        ClientPlayNetworking.registerGlobalReceiver(GET_BACKUP_LIST_PACKET_ID, (client, handler, buf, responseSender) -> {
            String Backups = buf.readString();
            HashMap<String, QbmManager.SlotInfoStorage> BackupsData = gson.fromJson(Backups, HashMap.class);
            TempConfig.tempConfig.setBackupsData(BackupsData);
        });
    }
}
