package cn.kuzuanpa.kubicdivers.common.mission;

import cn.kuzuanpa.kubicdivers.common.mission.types.IDiveMissionType;

public record MissionDetails(
        IDiveMissionType type,
        int difficulty
) {}