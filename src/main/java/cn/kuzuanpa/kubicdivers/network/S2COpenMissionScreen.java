package cn.kuzuanpa.kubicdivers.network;

import cn.kuzuanpa.kubicdivers.client.gui.MissionInputScreen;
import cn.kuzuanpa.kubicdivers.common.block.MissionTerminalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2COpenMissionScreen(BlockPos pos) {
    public static void encode(S2COpenMissionScreen msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static S2COpenMissionScreen decode(FriendlyByteBuf buf) {
        return new S2COpenMissionScreen(buf.readBlockPos());
    }

    public static void handle(S2COpenMissionScreen msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.getBlockEntity(msg.pos) instanceof MissionTerminalBlockEntity te) {
                mc.setScreen(new MissionInputScreen(te));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}