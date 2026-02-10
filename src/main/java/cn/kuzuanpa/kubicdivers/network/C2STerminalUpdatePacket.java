package cn.kuzuanpa.kubicdivers.network;

import cn.kuzuanpa.kubicdivers.common.block.MissionTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2STerminalUpdatePacket(BlockPos pos, float rx, float ry, int diff, int selectedID) {
    public static void encode(C2STerminalUpdatePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeFloat(msg.rx);
        buf.writeFloat(msg.ry);
        buf.writeInt(msg.diff);
        buf.writeInt(msg.selectedID);
    }

    public static C2STerminalUpdatePacket decode(FriendlyByteBuf buf) {
        return new C2STerminalUpdatePacket(buf.readBlockPos(), buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level().getBlockEntity(pos) instanceof MissionTerminalBlockEntity terminal) {
                terminal.syncState(rx, ry, diff, selectedID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}