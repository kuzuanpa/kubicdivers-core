package cn.kuzuanpa.kubicdivers.common.mission;

import cn.kuzuanpa.kubicdivers.common.mission.types.IDiveMissionType;

public record MissionDetails(
        IDiveMissionType type,
        int difficulty,
        String missionType
) {
    /** 兼容旧构造（无模板 ID） */
    public MissionDetails(IDiveMissionType type, int difficulty) {
        this(type, difficulty, "");
    }
}