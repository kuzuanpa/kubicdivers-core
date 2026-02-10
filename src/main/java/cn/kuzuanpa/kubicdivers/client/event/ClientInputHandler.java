package cn.kuzuanpa.kubicdivers.client.event;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.block.HellpodControllerBlockEntity;
import cn.kuzuanpa.kubicdivers.network.C2SExitHellpodPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cn.kuzuanpa.kubicdivers.KubicDiversMod.NETWORK_CHANNEL;

@Mod.EventBusSubscriber(modid = KubicDiversMod.MOD_ID, value = Dist.CLIENT)
public class ClientInputHandler {
    @SubscribeEvent
    public static void onKeyInput(TickEvent.ClientTickEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && event.phase == TickEvent.Phase.END) {
            if ((player.isShiftKeyDown()) && player.level().getBlockEntity(player.blockPosition().above(2)) instanceof HellpodControllerBlockEntity) {
                NETWORK_CHANNEL.sendToServer(new C2SExitHellpodPacket());
            }
        }
    }
}