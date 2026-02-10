package cn.kuzuanpa.kubicdivers.common.mission.goal;

import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;

public interface ITickingCheckGoals extends IMissionGoal{
    void onTick(DiveMission mission);
}
