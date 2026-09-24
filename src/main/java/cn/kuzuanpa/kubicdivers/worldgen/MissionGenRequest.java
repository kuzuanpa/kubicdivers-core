package cn.kuzuanpa.kubicdivers.worldgen;

import java.util.List;

// 任务生成请求包
public record MissionGenRequest(
        String environmentType, // 例如 "terminid_desert"
        int difficulty,
        List<String> mainObjectiveIds,
        List<String> subObjectiveIds
) {}
