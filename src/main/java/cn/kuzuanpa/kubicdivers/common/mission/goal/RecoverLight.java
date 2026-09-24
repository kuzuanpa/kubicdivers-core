package cn.kuzuanpa.kubicdivers.common.mission.goal;

import cn.kuzuanpa.kubicdivers.common.mission.MissionManager;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class RecoverLight implements IMissionGoal{
    BlockPos pos;
    public RecoverLight(BlockPos pos){
        this.pos=pos;
    }
    @Override
    public String getId() {
        return "rec.light."+pos.getX()+"."+pos.getY()+"."+pos.getZ();
    }

    @Override
    public String getName() {
        return "Recover Light";
    }
    @Override
    public String getDescription() {
        return "Recover Lights in Village";
    }

    @Override
    public boolean isCompleted() {
        Level level = MissionManager.currentMission.missionLevel;
        return level.getBlockState(pos).equals(Blocks.GLOWSTONE.defaultBlockState());
    }

    @Override
    public List<IStratagem> getRequiredStratagems() {
        return List.of();
    }
}
