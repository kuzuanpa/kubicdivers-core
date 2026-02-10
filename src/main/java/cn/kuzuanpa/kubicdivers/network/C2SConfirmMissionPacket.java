package cn.kuzuanpa.kubicdivers.network;

import cn.kuzuanpa.kubicdivers.common.block.MissionTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SConfirmMissionPacket(BlockPos pos, int missionId, boolean isCanceled) {
    public static void encode(C2SConfirmMissionPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.missionId);
        buf.writeBoolean(msg.isCanceled);
    }

    public static C2SConfirmMissionPacket decode(FriendlyByteBuf buf) {
        return new C2SConfirmMissionPacket(buf.readBlockPos(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(C2SConfirmMissionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level().getBlockEntity(msg.pos) instanceof MissionTerminalBlockEntity te) {
                te.confirmMission(msg.missionId, msg.isCanceled);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}