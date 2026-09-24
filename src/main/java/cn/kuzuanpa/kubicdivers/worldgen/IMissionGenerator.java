package cn.kuzuanpa.kubicdivers.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// 生成 Mod 需要实现这个接口并暴露给主 Mod
public interface IMissionGenerator {
    /**
     * 异步生成任务区域
     * 使用 CompletableFuture 是因为生成区块和放置结构需要时间，不能卡死主线程
     */
    CompletableFuture<MissionGenResult> generateMissionArea(MissionGenRequest request);
}