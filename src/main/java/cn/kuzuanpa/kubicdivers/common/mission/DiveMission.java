package cn.kuzuanpa.kubicdivers.common.mission;

import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.common.mission.types.IDiveMissionType;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DiveMission {
    public final IDiveMissionType type;
    public final int difficulty;
    public final Level missionLevel;

    public final List<IMissionGoal> mainGoal = new ArrayList<>();
    public final List<IMissionGoal> subGoal = new ArrayList<>();

    public DiveMission(IDiveMissionType type, Level missionLevel, int difficulty) {
        this.type = type;
        this.difficulty = difficulty;
        this.missionLevel = missionLevel;
        this.init();
    }

    private void init() {
        this.mainGoal.addAll(type.genMainTargets());
        int subTargetCount = (int) Math.ceil(difficulty / 2.0);
        this.subGoal.addAll(type.genSubTargets(subTargetCount));
    }

    public List<IStratagem> getMissionRequiredStratagems(){
        List<IStratagem> list = new ArrayList<>();
        mainGoal.forEach(goal-> list.addAll(goal.getRequiredStratagems()));
        subGoal.forEach(goal-> list.addAll(goal.getRequiredStratagems()));
        return list;
    }

    public MissionStatus checkStatus() {
        boolean mainDone = mainGoal.stream().allMatch(IMissionGoal::isCompleted);
        if (mainDone) return MissionStatus.SUCCESS;

        return MissionStatus.ACTIVE;
    }
    public enum MissionStatus {
        PENDING,
        AVAILABLE,
        ACTIVE,
        SUCCESS,
        FAILED
    }
}
