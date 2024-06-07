package dev.skydynamic.quickbackupmulti;

import dev.skydynamic.quickbackupmulti.screen.ConfigScreen;
import dev.skydynamic.quickbackupmulti.config.ConfigStorage;

import net.fabricmc.api.ClientModInitializer;
//#if MC>=12005
//$$ import net.minecraft.client.MinecraftClient;
//#endif
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
                    //#if MC>=12005
                    //$$ ClientPlayNetworking.send(new Packets.RequestOpenConfigGuiPacket(""));
                    //#else
                    ClientPlayNetworking.send(REQUEST_OPEN_CONFIG_GUI_PACKET_ID, PacketByteBufs.empty());
                    //#endif
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
        //#if MC<12005
        ClientPlayNetworking.registerGlobalReceiver(OPEN_CONFIG_GUI_PACKET_ID, (client, handler, buf, responseSender) -> {
            String config = buf.readString();
            ConfigStorage c = gson.fromJson(config, ConfigStorage.class);
            client.execute(() -> client.setScreen(new ConfigScreen(client.currentScreen, c)));
        });
        //#else
        //$$ ClientPlayNetworking.registerGlobalReceiver(Packets.OpenConfigGuiPacket.PACKET_ID, (payload, context) -> {
        //$$     String config = payload.config();
        //$$     ConfigStorage c = gson.fromJson(config, ConfigStorage.class);
        //$$     MinecraftClient client = context.client();
        //$$     client.execute(() -> client.setScreen(new ConfigScreen(client.currentScreen, c)));
        //$$ });
        //#endif
    }
}
