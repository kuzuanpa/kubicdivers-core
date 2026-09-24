package cn.kuzuanpa.kubicdivers.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

// 任务生成结果包
public record MissionGenResult(
        ResourceKey<Level> dimension,
        BlockPos landingZone // 玩家空投落地的中心点
) {}
