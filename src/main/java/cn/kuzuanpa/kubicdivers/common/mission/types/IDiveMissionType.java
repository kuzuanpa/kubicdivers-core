package cn.kuzuanpa.kubicdivers.common.mission.types;

import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IDiveMissionType {
    int getID();
    String  getDisplayName();
    ResourceLocation getIcon();
    int getRewardMedals();
    List<IMissionGoal> genMainTargets();
    List<IMissionGoal> genSubTargets(int amount);
}