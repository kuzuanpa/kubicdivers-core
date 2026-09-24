package cn.kuzuanpa.kubicdivers.common.mission;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.common.mission.types.IDiveMissionType;
import cn.kuzuanpa.kubicdivers.network.S2CSyncMissionPacket;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DiveMission {
    public final IDiveMissionType type;
    public final int difficulty;
    public final Level missionLevel;
    /** 数据驱动模板 ID（如 "destroy_hive"），用于 MissionTemplateManager 查找模板 */
    public final String missionType;

    public final List<IMissionGoal> mainGoal = new ArrayList<>();
    public final List<IMissionGoal> subGoal = new ArrayList<>();

    public DiveMission(IDiveMissionType type, Level missionLevel, int difficulty) {
        this(type, missionLevel, difficulty, "");
    }

    public DiveMission(IDiveMissionType type, Level missionLevel, int difficulty, String missionType) {
        this.type = type;
        this.difficulty = difficulty;
        this.missionLevel = missionLevel;
        this.missionType = missionType != null ? missionType : "";
        this.init();
    }

    private void init() {
        // 如果有数据驱动模板，goal 将由 PlanetGeneratorService 动态添加，不需要从 type 生成
        if (!missionType.isEmpty()) return;
        this.mainGoal.addAll(type.genMainTargets());
        int subTargetCount = (int) Math.ceil(difficulty / 2.0);
        this.subGoal.addAll(type.genSubTargets(subTargetCount));
    }

    public List<IStratagem> getMissionRequiredStratagems(){
        List<IStratagem> list = new ArrayList<>();
        mainGoal.forEach(goal-> list.addAll(goal.getRequiredStratagems()));
        subGoal.forEach(goal-> list.addAll(goal.getRequiredStratagems()));
        return list;
    }

    public MissionStatus checkStatus() {
        boolean failed = mainGoal.stream().anyMatch(IMissionGoal::isFailed);
        if(failed)return MissionStatus.FAILED;

        boolean mainDone = mainGoal.stream().allMatch(IMissionGoal::isCompleted);
        if (mainDone) return MissionStatus.SUCCESS;

        return MissionStatus.ACTIVE;
    }
    /**
     * 获取所有目标（主目标 + 次要目标）的合并列表。
     * 用于 MissionEventRouter 遍历所有目标分发事件。
     */
    public List<IMissionGoal> getAllObjectives() {
        List<IMissionGoal> all = new ArrayList<>(mainGoal);
        all.addAll(subGoal);
        return all;
    }

    /**
     * 动态添加主目标（由数据驱动生成器在运行时调用）。
     */
    public void addMainObjective(IMissionGoal goal) {
        mainGoal.add(goal);
    }

    /**
     * 动态添加次要目标。
     */
    public void addSubObjective(IMissionGoal goal) {
        subGoal.add(goal);
    }

    /**
     * 将任务状态同步到所有客户端玩家。
     * 由目标状态变更时调用（如方块被摧毁），确保 HUD 实时更新。
     */
    public void syncToClients(ServerLevel level) {
        S2CSyncMissionPacket packet = S2CSyncMissionPacket.fromMission(this);
        for (ServerPlayer player : level.players()) {
            KubicDiversMod.NETWORK_CHANNEL.sendTo(packet, player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public enum MissionStatus {
        PENDING,
        AVAILABLE,
        ACTIVE,
        SUCCESS,
        FAILED
    }
}
