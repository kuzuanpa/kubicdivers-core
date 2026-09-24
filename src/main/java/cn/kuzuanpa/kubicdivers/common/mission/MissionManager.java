package cn.kuzuanpa.kubicdivers.common.mission;

import cn.kuzuanpa.kubicdivers.common.ModDimensions;
import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.network.UpdateLoadoutPacket;
import cn.kuzuanpa.kubicdivers.stratagem.common.StratagemPlayerManager;
import cn.kuzuanpa.kubicdivers.stratagem.common.entity.HellpodEntity;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.StratagemManager;
import cn.kuzuanpa.kubicdivers.worldgen.IMissionGenerator;
import cn.kuzuanpa.kubicdivers.worldgen.MissionGenRequest;
import cn.kuzuanpa.kubicdivers.worldgen.MissionGenResult;
import cn.kuzuanpa.kubicdivers.worldgen.ServerMissionManager;
import cn.kuzuanpa.kubicgen.PlanetGeneratorService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MissionManager {
    private static final Map<String, UpdateLoadoutPacket> PLAYER_LOADOUTS = new HashMap<>();
    public static DiveMission currentMission;
    public static DiveMission.MissionStatus currentMissionStatus;
    private static int tickCounter = 0;
    public static final boolean dev = false;
    public static void updateLoadout(String playerName, UpdateLoadoutPacket data) {
        PLAYER_LOADOUTS.put(playerName, data);
    }

    public static boolean checkReady(ServerPlayer player){
        ServerLevel level = player.server.getLevel(ModDimensions.DESTROYER_DIM);
        ServerLevel targetLevel = player.server.getLevel(Level.OVERWORLD);
        if(level == null)return false;
        if(PLAYER_LOADOUTS.size() == level.players().size() && PLAYER_LOADOUTS.values().stream().allMatch(UpdateLoadoutPacket::isReady)){
            initiateLaunch(level, targetLevel, level.players());
            return true;
        }
        return false;
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            // 遍历 ServerMissionManager 中所有活跃任务，调用每个目标的 onTick
            for (var entry : ServerMissionManager.getInstance().getAllActiveMissions().entrySet()) {
                net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimKey = entry.getKey();
                DiveMission mission = entry.getValue();
                net.minecraft.server.level.ServerLevel level = event.getServer().getLevel(dimKey);
                if (level == null) continue;

                // 调用所有目标的 onTick（定期检测方块状态等）
                for (var goal : mission.getAllObjectives()) {
                    goal.onTick(mission, level);
                }
            }

            // 检查 currentMission 状态
            if (currentMission == null || tickCounter % 10 != 0) return;

            currentMissionStatus = currentMission.checkStatus();
            if (currentMissionStatus == DiveMission.MissionStatus.SUCCESS) {
                handleMissionSuccess();
            } else if (currentMissionStatus == DiveMission.MissionStatus.FAILED) {
                handleMissionFailure();
            }
        }
    }
    private static void handleMissionSuccess() {
        // TODO: 触发撤离点开启逻辑，播放全局成功音效，发放报酬
        System.out.println("MISSION SUCCESS: " );
        cleanupMission();
    }

    private static void handleMissionFailure() {
        System.out.println("MISSION FAILED: " );
        cleanupMission();
    }

    /**
     * 任务结束后的通用清理：清除 IsDeploying 标记、清理 loadout 缓存、
     * 从 ServerMissionManager 移除任务、重置 currentMission。
     */
    private static void cleanupMission() {
        if (currentMission != null && currentMission.missionLevel instanceof ServerLevel sLevel) {
            // 清除所有玩家的 IsDeploying 标记
            for (ServerPlayer player : sLevel.getServer().getPlayerList().getPlayers()) {
                player.getPersistentData().remove("IsDeploying");
            }
            ServerMissionManager.getInstance().removeMission(sLevel);
        }
        currentMission = null;
        currentMissionStatus = null;
        PLAYER_LOADOUTS.clear();
    }

    public static List<UpdateLoadoutPacket> getDisplayTeammates(Player localPlayer) {
        List<UpdateLoadoutPacket> list = new ArrayList<>();

        for (Map.Entry<String, UpdateLoadoutPacket> entry : PLAYER_LOADOUTS.entrySet()) {
            if (dev || !entry.getKey().equals(localPlayer.getScoreboardName())) {
                list.add(entry.getValue());
            }
        }

        if (dev) {
            List<IStratagem> devStrats = new ArrayList<>();
            devStrats.add(StratagemManager.getStratagem("fire_barrage"));

            list.add(new UpdateLoadoutPacket("DEV",
                    (new ItemStack(Items.IRON_CHESTPLATE)),
            (new ItemStack(Items.GOLDEN_SWORD)),
            (new ItemStack(Items.SHIELD)),
            (new ItemStack(Items.POISONOUS_POTATO)),devStrats, true));
        }

        return list;
    }
    /**
     * 为玩家配置装备和战备，使用指定的 mission 实例获取 requiredStratagems。
     * 创建新列表避免修改 PLAYER_LOADOUTS 中的原始数据。
     */
    public static void setupPlayerLoadout(ServerPlayer player, DiveMission mission){
        player.getPersistentData().putBoolean("IsDeploying", true);
        // 创建新列表，避免修改 PLAYER_LOADOUTS 中存储的原始 stratagems 列表
        List<IStratagem> availStratagems = new ArrayList<>(PLAYER_LOADOUTS.get(player.getScoreboardName()).stratagems());
        availStratagems.addAll(mission.getMissionRequiredStratagems());
        StratagemPlayerManager.setAvailStratagems(player, availStratagems);

        StratagemPlayerManager.syncAvailStratagem(player);
    }
    /**
     * 发起空降：构建生成请求 -> 调用 PlanetGeneratorService 生成任务区域 -> 传送玩家。
     *
     * 注意：goal 绑定逻辑（扫描标记 -> 创建 IMissionGoal -> 绑定坐标）
     * 已移至 PlanetGeneratorService.generateMissionArea() 中统一处理。
     *
     * 生成完成后，从 ServerMissionManager 获取新创建的 DiveMission 实例并赋值给 currentMission，
     * 确保后续 onServerTick 状态检测和 setupPlayerLoadout 使用的是同一个 mission。
     * 所有玩家传送/实体操作通过 server.execute() 调度回主线程执行，避免并发问题。
     */
    public static void initiateLaunch(ServerLevel shipLevel, ServerLevel targetLevel, List<ServerPlayer> playerList) {
        // 防御性拷贝：squad 可能来自 level.players() 的可变内部列表，
        // 异步回调期间玩家加入/离开会导致 ConcurrentModificationException
        List<ServerPlayer> squad = new ArrayList<>(playerList);

        // 1. 构建生成请求
        // 使用 activeDetails.missionType（模板 ID，如 "destroy_hive"）作为 mainObjectiveIds，
        // 这样 PlanetGeneratorService 才能通过 MissionTemplateManager.getTemplate 找到正确的模板
        String missionType = !currentMission.missionType.isEmpty()
                ? currentMission.missionType : "";
        List<String> mainGoalIds = missionType.isEmpty()
                ? currentMission.mainGoal.stream().map(IMissionGoal::getId).toList()
                : List.of(missionType);
        // 次要目标数量由模板的 sub_structures 决定，这里传递数量信息
        List<String> subGoalIds = List.of("sub_1", "sub_2");

        MissionGenRequest request = new MissionGenRequest(
                "terminid_desert", // 环境类型（回退值，模板中的 environment_type 优先）
                currentMission.difficulty,
                mainGoalIds,
                subGoalIds
        );

        // 2. 获取生成服务并同步执行世界生成（已在主线程）
        // PlanetGeneratorService.generateMissionArea 内部会：
        //   - 放置结构 -> 扫描标记 -> 通过 GoalFactoryRegistry 创建 goal -> 绑定坐标
        //   - 创建 DiveMission 并注册到 ServerMissionManager
        IMissionGenerator generator = PlanetGeneratorService.INSTANCE;
        MissionGenResult result = generator.generateMissionArea(request).join();

        // 3. 生成完成后直接执行玩家传送（已在主线程）
        ServerLevel missionLevel = shipLevel.getServer().getLevel(result.dimension());
        if (missionLevel == null) return;

        // 从 ServerMissionManager 获取新生成的 mission 实例，替换 currentMission
        DiveMission newMission = ServerMissionManager.getInstance().getMissionForLevel(missionLevel);
        if (newMission != null) {
            currentMission = newMission;
        }

        for (ServerPlayer player : squad) {
            setupPlayerLoadout(player, currentMission);

            BlockPos landingZone = result.landingZone();
            BlockPos spawnPos = landingZone.offset(0, 500, 0);
            player.teleportTo(missionLevel, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), player.getYRot(), player.getXRot());

            HellpodEntity pod = new HellpodEntity(missionLevel, Vec3.atCenterOf(spawnPos));
            pod.setTargetHeight(landingZone.getY());
            missionLevel.addFreshEntity(pod);
            player.startRiding(pod);
        }

        // 初始同步任务状态到所有客户端
        currentMission.syncToClients(missionLevel);
    }
}