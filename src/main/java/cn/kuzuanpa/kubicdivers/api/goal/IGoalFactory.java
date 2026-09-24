package cn.kuzuanpa.kubicdivers.api.goal;

import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import com.google.gson.JsonObject;

public interface IGoalFactory {
    IMissionGoal create(JsonObject config);
}