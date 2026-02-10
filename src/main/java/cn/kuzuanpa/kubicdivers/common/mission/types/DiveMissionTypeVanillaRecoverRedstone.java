package cn.kuzuanpa.kubicdivers.common.mission.types;

import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.common.mission.goal.RecoverLight;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;

public class DiveMissionTypeVanillaRecoverRedstone implements IDiveMissionType {
    Random rand = new Random();
    @Override
    public int getID() {
        return 0;
    }

    @Override
    public String getDisplayName() {
        return "Vanilla: Recover Redstone Machine";
    }

    @Override
    public ResourceLocation getIcon() {
        return null;
    }

    @Override
    public int getRewardMedals() {
        return 10;
    }

    @Override
    public List<IMissionGoal> genMainTargets() {
        return List.of(new RecoverLight(new BlockPos(rand.nextInt(120), rand.nextInt(120), rand.nextInt(120))));
    }

    @Override
    public List<IMissionGoal> genSubTargets(int amount) {
        return List.of();
    }
}
