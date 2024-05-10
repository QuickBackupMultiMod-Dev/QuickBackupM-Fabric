package dev.skydynamic.quickbackupmulti;

 import io.netty.buffer.ByteBuf;
 import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
 import net.minecraft.network.codec.PacketCodec;
 import net.minecraft.network.codec.PacketCodecs;
 import net.minecraft.network.packet.CustomPayload;
 import static dev.skydynamic.quickbackupmulti.QbmConstant.OPEN_CONFIG_GUI_PACKET_ID;
 import static dev.skydynamic.quickbackupmulti.QbmConstant.REQUEST_OPEN_CONFIG_GUI_PACKET_ID;
 import static dev.skydynamic.quickbackupmulti.QbmConstant.SAVE_CONFIG_PACKET_ID;

public class Packets {
    public static void registerPacketCodec() {
        PayloadTypeRegistry.playC2S().register(Packets.RequestOpenConfigGuiPacket.PACKET_ID, Packets.RequestOpenConfigGuiPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(Packets.OpenConfigGuiPacket.PACKET_ID, Packets.OpenConfigGuiPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(Packets.SaveConfigPacket.PACKET_ID, Packets.SaveConfigPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(Packets.OpenConfigGuiPacket.PACKET_ID, Packets.OpenConfigGuiPacket.PACKET_CODEC);
    }
    public record RequestOpenConfigGuiPacket(String config) implements CustomPayload {
        public static final CustomPayload.Id<RequestOpenConfigGuiPacket> PACKET_ID = new CustomPayload.Id<>(REQUEST_OPEN_CONFIG_GUI_PACKET_ID);
        public static final PacketCodec<ByteBuf, RequestOpenConfigGuiPacket> PACKET_CODEC =
            PacketCodecs.STRING.xmap(RequestOpenConfigGuiPacket::new, RequestOpenConfigGuiPacket::config);

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public record OpenConfigGuiPacket(String config) implements CustomPayload {
        public static final CustomPayload.Id<OpenConfigGuiPacket> PACKET_ID = new CustomPayload.Id<>(OPEN_CONFIG_GUI_PACKET_ID);
        public static final PacketCodec<ByteBuf, OpenConfigGuiPacket> PACKET_CODEC =
            PacketCodecs.STRING.xmap(OpenConfigGuiPacket::new, OpenConfigGuiPacket::config);

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public record SaveConfigPacket(String config) implements CustomPayload {
        public static final CustomPayload.Id<SaveConfigPacket> PACKET_ID = new CustomPayload.Id<>(SAVE_CONFIG_PACKET_ID);
        public static final PacketCodec<ByteBuf, SaveConfigPacket> PACKET_CODEC =
            PacketCodecs.STRING.xmap(SaveConfigPacket::new, SaveConfigPacket::config);

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }
}
