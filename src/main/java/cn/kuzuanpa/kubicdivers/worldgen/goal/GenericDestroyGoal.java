package cn.kuzuanpa.kubicdivers.worldgen.goal;

import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 通用摧毁目标：数据驱动的任务目标实现。
 *
 * 数据包作者通过 JSON 定义：
 * - marker_id: 结构方块中数据标记的名称（如 "mission_target:aa_gun"）
 * - target_block: 该标记位置应该放置什么方块（如 "minecraft:iron_block"）
 * - display_name: 目标的显示名称
 *
 * 完成条件：所有标记位置的方块被摧毁（变为空气）。
 * 检测方式：
 *   1. 玩家挖掘 -> MissionEventRouter 拦截 BlockEvent.BreakEvent -> notifyBlockDestroyed
 *   2. 爆炸等非挖掘行为 -> tickCheck 定期扫描方块是否消失
 */
public class GenericDestroyGoal implements IMissionGoal {
    private final String id;
    private final Component displayName;
    private final ResourceLocation targetBlockId;
    public final String markerId; // 用于匹配生成器发来的标记

    private final Set<BlockPos> targetPositions = new HashSet<>();
    private int initialCount = 0;
    private int currentCount = 0;

    // 由工厂传入 JSON 配置进行初始化
    public GenericDestroyGoal(JsonObject config) {
        this.markerId = config.get("marker_id").getAsString();
        this.id = "generic_destroy_" + markerId;
        this.displayName = Component.literal(config.get("display_name").getAsString());
        this.targetBlockId = new ResourceLocation(config.get("target_block").getAsString());
    }

    // 绑定坐标：将其转化为需要摧毁的方块并记录
    public void registerTarget(ServerLevel level, BlockPos pos) {
        targetPositions.add(pos);
        initialCount = targetPositions.size();

        // 在该坐标放置真正的实体方块
        level.setBlock(pos, ForgeRegistries.BLOCKS.getValue(targetBlockId).defaultBlockState(), 3);
    }

    // --- IMissionGoal 接口实现 ---

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

    // 统一暴露的事件接收口，由全局 MissionEventRouter 分发
    public void notifyBlockDestroyed(BlockPos pos) {
        if (targetPositions.remove(pos)) {
            currentCount++;
        }
    }

    /**
     * 定期检查方块是否被爆炸等非挖掘行为摧毁。
     * 实现 IMissionGoal.onTick，由 MissionManager.onServerTick 统一调用。
     */
    @Override
    public void onTick(DiveMission mission, ServerLevel level) {
        if (level.getGameTime() % 20 == 0) {
            int removed = 0;
            // 使用迭代器安全移除
            for (var it = targetPositions.iterator(); it.hasNext(); ) {
                BlockPos pos = it.next();
                if (level.getBlockState(pos).isAir()) {
                    it.remove();
                    removed++;
                }
            }
            if (removed > 0) {
                currentCount += removed;
                mission.syncToClients(level);
            }
        }
    }
}