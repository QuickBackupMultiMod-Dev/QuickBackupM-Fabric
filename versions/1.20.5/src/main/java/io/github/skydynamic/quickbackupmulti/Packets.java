package io.github.skydynamic.quickbackupmulti;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import static io.github.skydynamic.quickbackupmulti.QbmConstant.OPEN_CONFIG_GUI_PACKET_ID;
import static io.github.skydynamic.quickbackupmulti.QbmConstant.REQUEST_OPEN_CONFIG_GUI_PACKET_ID;
import static io.github.skydynamic.quickbackupmulti.QbmConstant.SAVE_CONFIG_PACKET_ID;

public class Packets {
    public static void registerPacketCodec() {
        PayloadTypeRegistry.playC2S().register(RequestOpenConfigGuiPacket.ID, RequestOpenConfigGuiPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(OpenConfigGuiPacket.ID, OpenConfigGuiPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SaveConfigPacket.ID, SaveConfigPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenConfigGuiPacket.ID, OpenConfigGuiPacket.PACKET_CODEC);
    }

    public record RequestOpenConfigGuiPacket(String config) implements CustomPayload {
        public static final Id<RequestOpenConfigGuiPacket> ID =
            new Id<>(REQUEST_OPEN_CONFIG_GUI_PACKET_ID);
        public static final PacketCodec<PacketByteBuf, RequestOpenConfigGuiPacket> PACKET_CODEC =
            CustomPayload.codecOf(RequestOpenConfigGuiPacket::write, RequestOpenConfigGuiPacket::new);

        private RequestOpenConfigGuiPacket(PacketByteBuf buf) {
            this(buf.readString());
        }

        public RequestOpenConfigGuiPacket(String config) {
            this.config = config;
        }

        private void write(PacketByteBuf buf) {
            buf.writeString(this.config);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        public String config() {
            return config;
        }
    }

    public record OpenConfigGuiPacket(String config) implements CustomPayload {
        public static final Id<OpenConfigGuiPacket> ID =
            new Id<>(OPEN_CONFIG_GUI_PACKET_ID);
        public static final PacketCodec<PacketByteBuf, OpenConfigGuiPacket> PACKET_CODEC =
            CustomPayload.codecOf(OpenConfigGuiPacket::write, OpenConfigGuiPacket::new);

        private OpenConfigGuiPacket(PacketByteBuf buf) {
            this(buf.readString());
        }

        public OpenConfigGuiPacket(String config) {
            this.config = config;
        }

        private void write(PacketByteBuf buf) {
            buf.writeString(this.config);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        public String config() {
            return config;
        }
    }

    public record SaveConfigPacket(String config) implements CustomPayload {
        public static final Id<SaveConfigPacket> ID = new Id<>(SAVE_CONFIG_PACKET_ID);
        public static final PacketCodec<PacketByteBuf, SaveConfigPacket> PACKET_CODEC =
            CustomPayload.codecOf(SaveConfigPacket::write, SaveConfigPacket::new);

        private SaveConfigPacket(PacketByteBuf buf) {
            this(buf.readString());
        }

        public SaveConfigPacket(String config) {
            this.config = config;
        }

        private void write(PacketByteBuf buf) {
            buf.writeString(this.config);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        public String config() {
            return config;
        }
    }
}
