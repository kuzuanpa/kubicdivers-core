package cn.kuzuanpa.kubicdivers.worldgen.goal;

import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.worldgen.ServerMissionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = cn.kuzuanpa.kubicdivers.KubicDiversMod.MOD_ID)
public class MissionEventRouter {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        DiveMission mission = ServerMissionManager.getInstance().getMissionForLevel(level);
        if (mission == null) return;

        // 遍历所有任务目标，如果是破坏类任务，则通知它
        for (IMissionGoal goal : mission.getAllObjectives()) {
            if (goal instanceof GenericDestroyGoal destroyGoal) {
                destroyGoal.notifyBlockDestroyed(event.getPos());
                mission.syncToClients(level); // 状态更新，同步 HUD
            }
        }
    }
}