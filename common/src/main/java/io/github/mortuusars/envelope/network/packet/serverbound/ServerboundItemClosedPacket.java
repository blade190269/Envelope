package io.github.mortuusars.envelope.network.packet.serverbound;

import io.github.mortuusars.envelope.Envelope;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ServerboundItemClosedPacket(int slot) implements Packet {
    public static final ResourceLocation ID = Envelope.resource("item_closed");
    public static final Type<ServerboundItemClosedPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundItemClosedPacket> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, ServerboundItemClosedPacket::slot,
          ServerboundItemClosedPacket::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        if (slot != -1 && player.getInventory().getItem(slot).remove(Envelope.DataComponents.OPEN) != null) {
            player.resetAttackStrengthTicker();
        }
        return true;
    }
}
