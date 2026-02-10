package cn.kuzuanpa.kubicdivers.network;

import cn.kuzuanpa.kubicdivers.client.gui.StrategemSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
public record S2COpenStrategemScreen() {
    public static void encode(S2COpenStrategemScreen msg, FriendlyByteBuf buf) {
    }

    public static S2COpenStrategemScreen decode(FriendlyByteBuf buf) {
        return new S2COpenStrategemScreen();
    }
    public static void handle(S2COpenStrategemScreen msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new StrategemSelectionScreen());
        });
        ctx.get().setPacketHandled(true);
    }
}