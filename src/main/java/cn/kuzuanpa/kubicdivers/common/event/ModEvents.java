package cn.kuzuanpa.kubicdivers.common.event;


import cn.kuzuanpa.kubicdivers.common.ModEntities;
import cn.kuzuanpa.kubicdivers.common.entity.DistractionEntity;
import cn.kuzuanpa.kubicdivers.common.mission.MissionManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cn.kuzuanpa.kubicdivers.KubicDiversMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DISTRACTION.get(), DistractionEntity.createAttributes().build());
    }
}
