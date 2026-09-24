package cn.kuzuanpa.kubicdivers.common.event;

import cn.kuzuanpa.kubicdivers.common.mission.MissionManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cn.kuzuanpa.kubicdivers.KubicDiversMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModTickEvents {
    @SubscribeEvent
    public static void onServerUpdate(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) MissionManager.onServerTick(event);
    }
}
