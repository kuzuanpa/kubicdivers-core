package cn.kuzuanpa.kubicdivers.common.mission.goal;

import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public interface IMissionGoal {
    String getId();
    String getName();
    String  getDescription();
    boolean isCompleted();
    List<IStratagem> getRequiredStratagems();
    default boolean isFailed(){return false;}

    /**
     * 每服务端 tick 调用，用于定期检测目标完成状态（如方块被爆炸摧毁、方块被替换等）。
     * 默认空实现，需要定期检测的目标应覆盖此方法。
     *
     * @param mission 当前任务实例，用于状态变更时调用 syncToClients
     * @param level   任务所在的服务端维度
     */
    default void onTick(DiveMission mission, ServerLevel level) {}

    /**
     * 获取进度文本，供 HUD 显示（如 "2/5"）。
     * 默认返回 null，表示无进度信息。
     */
    default String getProgressText() { return null; }
}