
package cn.kuzuanpa.kubicdivers.worldgen;

import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端任务管理器：维护当前所有活跃任务与维度的映射关系。
 * 每个 ServerLevel 最多绑定一个活跃任务。
 */
public class ServerMissionManager {

    private static final ServerMissionManager INSTANCE = new ServerMissionManager();

    /** 维度 ResourceKey -> 活跃任务 */
    private final Map<ResourceKey<Level>, DiveMission> activeMissions = new ConcurrentHashMap<>();

    private ServerMissionManager() {}

    public static ServerMissionManager getInstance() {
        return INSTANCE;
    }

    /** 获取指定维度上的活跃任务，没有则返回 null */
    public DiveMission getMissionForLevel(ServerLevel level) {
        return activeMissions.get(level.dimension());
    }

    /** 注册一个新任务到维度 */
    public void registerMission(ServerLevel level, DiveMission mission) {
        activeMissions.put(level.dimension(), mission);
    }

    /** 移除维度上的任务（任务完成或失败时调用） */
    public void removeMission(ServerLevel level) {
        activeMissions.remove(level.dimension());
    }

    /** 判断维度是否有活跃任务 */
    public boolean hasActiveMission(ServerLevel level) {
        return activeMissions.containsKey(level.dimension());
    }

    /** 获取所有活跃任务 */
    public Map<ResourceKey<Level>, DiveMission> getAllActiveMissions() {
        return Map.copyOf(activeMissions);
    }

    /** 清空所有任务（服务器停止时调用） */
    public void clearAll() {
        activeMissions.clear();
    }
}
