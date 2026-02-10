package cn.kuzuanpa.kubicdivers.common.mission.goal;

import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;

import java.util.List;

public interface IMissionGoal {
    String getId();
    String getName();
    String  getDescription();
    boolean isCompleted();
    List<IStratagem> getRequiredStratagems();
    default boolean isFailed(){return false;}
}