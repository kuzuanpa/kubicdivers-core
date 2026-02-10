package cn.kuzuanpa.kubicdivers.network;

import cn.kuzuanpa.kubicdivers.event.MissionSystemManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SExitHellpodPacket() {
    public static void handle(C2SExitHellpodPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            MissionSystemManager.findPodOccupiedBy(player).ifPresent(pod -> {
                pod.releasePlayer(player);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void encode(C2SExitHellpodPacket msg, FriendlyByteBuf buf) {
    }

    public static C2SExitHellpodPacket decode(FriendlyByteBuf buf) {
        return new C2SExitHellpodPacket();
    }
}