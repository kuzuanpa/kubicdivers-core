package cn.kuzuanpa.kubicdivers.worldgen.goal;

import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockTransformGoal implements IMissionGoal {
    private final String id;
    private final Component displayName;
    public final String markerId;

    private final ResourceLocation initialBlockId;
    private final ResourceLocation targetBlockId; // 核心泛化：目标状态

    private final Set<BlockPos> targetPositions = new HashSet<>();
    private int initialCount = 0;
    private int currentCount = 0;

    public BlockTransformGoal(JsonObject config) {
        this.markerId = config.get("marker_id").getAsString();
        this.id = "block_transform_" + markerId;
        this.displayName = Component.literal(config.get("display_name").getAsString());
        this.initialBlockId = new ResourceLocation(config.get("initial_block").getAsString());

        // 如果数据包没写 target_block，默认退化为破坏任务 (变成空气)
        if (config.has("target_block")) {
            this.targetBlockId = new ResourceLocation(config.get("target_block").getAsString());
        } else {
            this.targetBlockId = new ResourceLocation("minecraft:air");
        }
    }

    public void registerTarget(ServerLevel level, BlockPos pos) {
        targetPositions.add(pos);
        initialCount = targetPositions.size();

        // 放置初始方块
        level.setBlock(pos, ForgeRegistries.BLOCKS.getValue(initialBlockId).defaultBlockState(), 3);
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return displayName.getString(); }

    @Override
    public String getDescription() { return displayName.getString(); }

    @Override
    public boolean isCompleted() { return initialCount > 0 && currentCount >= initialCount; }

    @Override
    public List<IStratagem> getRequiredStratagems() { return List.of(); }

    @Override
    public String getProgressText() { return currentCount + "/" + initialCount; }

    @Override
    public void onTick(DiveMission mission, ServerLevel level) {
        // 每秒检测一次（降低性能开销）
        if (level.getGameTime() % 20 == 0) {
            int completed = 0;
            for (var it = targetPositions.iterator(); it.hasNext(); ) {
                BlockPos pos = it.next();
                BlockState currentState = level.getBlockState(pos);
                ResourceLocation currentId = ForgeRegistries.BLOCKS.getKey(currentState.getBlock());

                // 核心逻辑：当前方块 ID 是否等于数据包定义的目标 ID？
                // （如果目标是 air，则方块被破坏也算完成）
                if (currentId != null && currentId.equals(targetBlockId)) {
                    it.remove();
                    completed++;
                }
            }

            if (completed > 0) {
                currentCount += completed;
                mission.syncToClients(level); // 同步给玩家右上角 HUD
            }
        }
    }
}