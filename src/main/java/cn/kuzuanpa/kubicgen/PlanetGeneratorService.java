package cn.kuzuanpa.kubicgen;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.api.goal.GoalFactoryRegistry;
import cn.kuzuanpa.kubicdivers.api.goal.IGoalFactory;
import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import cn.kuzuanpa.kubicdivers.common.mission.goal.IMissionGoal;
import cn.kuzuanpa.kubicdivers.common.mission.types.DiveMissionTypeManager;
import cn.kuzuanpa.kubicdivers.worldgen.IMissionGenerator;
import cn.kuzuanpa.kubicdivers.worldgen.MissionGenRequest;
import cn.kuzuanpa.kubicdivers.worldgen.MissionGenResult;
import cn.kuzuanpa.kubicdivers.worldgen.ServerMissionManager;
import cn.kuzuanpa.kubicdivers.worldgen.goal.BlockTransformGoal;
import cn.kuzuanpa.kubicdivers.worldgen.goal.GenericDestroyGoal;
import cn.kuzuanpa.kubicgen.data.MissionTemplateManager;
import cn.kuzuanpa.kubicgen.worldgen.MissionMarkerProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 星球生成服务：负责在超平坦维度上放置结构、扫描标记、绑定任务目标。
 *
 * 生成流程：
 * 1. 根据 environmentType 获取目标超平坦维度
 * 2. 计算隔离坐标（每个任务相隔 10000 格）
 * 3. 从 MissionTemplateManager 获取模板，放置结构（主目标/次目标/填充物）
 * 4. 放置时通过 MissionMarkerProcessor 扫描结构方块数据标记
 * 5. 根据 goalDefinitions 将标记绑定到对应的 IMissionGoal 实例
 * 6. 注册任务到 ServerMissionManager
 */
public class PlanetGeneratorService implements IMissionGenerator {

    public static final PlanetGeneratorService INSTANCE = new PlanetGeneratorService();
    private final AtomicInteger missionCounter = new AtomicInteger(1);
    private MinecraftServer server;

    /** 维度类型 -> 维度 ResourceKey 的映射，由数据包定义或硬编码注册 */
    private final java.util.Map<String, ResourceKey<Level>> dimensionMap = new java.util.HashMap<>();

    public PlanetGeneratorService() {
        // 注册默认维度映射
        registerDimensionType("terminid_desert", ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath("kubicgen", "terminid_desert")));
    }

    /** 注册环境类型到维度的映射，供数据包或其他 mod 扩展 */
    public void registerDimensionType(String environmentType, ResourceKey<Level> dimKey) {
        dimensionMap.put(environmentType, dimKey);
    }

    /** 根据环境类型获取对应的维度 ResourceKey */
    public ResourceKey<Level> getDimensionForType(String environmentType) {
        ResourceKey<Level> key = dimensionMap.get(environmentType);
        if (key == null) {
            throw new IllegalArgumentException("Unknown environment type: " + environmentType
                    + ". Register it via PlanetGeneratorService.INSTANCE.registerDimensionType()");
        }
        return key;
    }

    @Mod.EventBusSubscriber(modid = KubicDiversMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            INSTANCE.server = event.getServer();
        }
    }

    @Override
    public CompletableFuture<MissionGenResult> generateMissionArea(MissionGenRequest request) {
        // 整个生成流程在主线程执行，避免跨线程区块加载和方块操作问题
        String mainType = request.mainObjectiveIds().isEmpty() ? "" : request.mainObjectiveIds().get(0);
        MissionTemplateManager.MissionTemplate template = MissionTemplateManager.getTemplate(mainType);

        // 优先使用模板中定义的 environment_type，若为空则回退到 request 中的值
        String envType = !template.environmentType().isEmpty() ? template.environmentType() : request.environmentType();
        ResourceKey<Level> dimKey = getDimensionForType(envType);
        ServerLevel targetLevel = server.getLevel(dimKey);
        if (targetLevel == null) {
            KubicDiversMod.LOGGER.error("Mission dim not found for type: {}", envType);
            throw new IllegalArgumentException("Mission dim not found for type: " + envType);
        }

        // 计算隔离坐标 (每个任务相隔 10000 格，绝对互不干扰)
        int index = missionCounter.getAndIncrement();
        int centerX = index * 10000;
        int centerZ = index * 10000;

        // 强制加载目标区块 (超平坦维度在远处坐标可能没有区块)
        net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos(
                new BlockPos(centerX, 0, centerZ));
        targetLevel.getChunkSource().getChunk(chunkPos.x, chunkPos.z, true);

        // 寻找地表高度
        int surfaceY = targetLevel.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                new BlockPos(centerX, 0, centerZ)).getY();
        BlockPos landingZone = new BlockPos(centerX, surfaceY, centerZ);

        KubicDiversMod.LOGGER.info("Generating mission area at {} (envType={}, template={})", landingZone, envType, mainType);

        // 放置结构并扫描标记
        List<MissionMarkerProcessor.Marker> allMarkers = placeObjectives(targetLevel, landingZone, request);

        KubicDiversMod.LOGGER.info("Placed objectives, found {} markers", allMarkers.size());

        // 根据 goalDefinitions 将扫描到的标记绑定到 IMissionGoal 实例
        List<IMissionGoal> boundGoals = bindGoalsFromMarkers(template.goalDefinitions(), allMarkers, targetLevel);

        KubicDiversMod.LOGGER.info("Bound {} goals from markers", boundGoals.size());

        // 创建 DiveMission 并注册到 ServerMissionManager
        DiveMission mission = new DiveMission(
                DiveMissionTypeManager.getFromID(0), targetLevel, request.difficulty(),
                mainType);
        // 将数据驱动创建的 goal 追加到任务中
        for (IMissionGoal goal : boundGoals) {
            mission.addMainObjective(goal);
        }
        ServerMissionManager.getInstance().registerMission(targetLevel, mission);

        return CompletableFuture.completedFuture(new MissionGenResult(dimKey, landingZone));
    }

    /**
     * 放置所有结构并收集扫描到的标记。
     *
     * @return 所有结构中扫描到的 MissionMarkerProcessor.Marker 列表
     */
    private List<MissionMarkerProcessor.Marker> placeObjectives(ServerLevel level, BlockPos center, MissionGenRequest request) {
        Random random = new Random();
        List<MissionMarkerProcessor.Marker> allMarkers = new ArrayList<>();

        // 获取数据驱动加载的模板信息
        String mainType = request.mainObjectiveIds().isEmpty() ? "" : request.mainObjectiveIds().get(0);
        MissionTemplateManager.MissionTemplate templateData = MissionTemplateManager.getTemplate(mainType);

        // 1. 放置主要目标 (在降落区附近)
        BlockPos mainObjPos = center.offset(0, 0, -100);
        if (!templateData.mains().isEmpty()) {
            ResourceLocation mainStructRl = templateData.mains().get(random.nextInt(templateData.mains().size()));
            allMarkers.addAll(placeAndScanStructure(level, mainStructRl, mainObjPos, random));
        }

        // 2. 放置次要目标 (在中心半径 150 格内随机散布)
        int subCount = request.subObjectiveIds().size();
        for (int i = 0; i < subCount; i++) {
            if (templateData.subs().isEmpty()) break;

            ResourceLocation subStructRl = templateData.subs().get(random.nextInt(templateData.subs().size()));

            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 50 + random.nextDouble() * 100;
            BlockPos subPos = center.offset((int) (Math.cos(angle) * radius), 0, (int) (Math.sin(angle) * radius));

            allMarkers.addAll(placeAndScanStructure(level, subStructRl, subPos, random));
        }

        // 3. 放置环境填充物 (岩石、小陨石坑等，掩盖超平坦的突兀感)
        int fillerCount = 15 + random.nextInt(10);
        for (int i = 0; i < fillerCount; i++) {
            if (templateData.fillers().isEmpty()) break;

            ResourceLocation fillerRl = templateData.fillers().get(random.nextInt(templateData.fillers().size()));
            BlockPos fillerPos = center.offset(random.nextInt(300) - 150, 0, random.nextInt(300) - 150);

            // 填充物不需要扫描标记，使用不带 processor 的方式放置
            placeStructureWithoutScan(level, fillerRl, fillerPos, random);
        }

        return allMarkers;
    }

    /**
     * 核心：放置结构并扫描其中的数据标记。
     * 使用 MissionMarkerProcessor 拦截结构方块中的 metadata，
     * 将 "mission_target:xxx" 标记替换为空气并记录其坐标。
     *
     * @return 扫描到的标记列表
     */
    public List<MissionMarkerProcessor.Marker> placeAndScanStructure(ServerLevel level, ResourceLocation nbtPath, BlockPos pos, Random random) {
        MissionMarkerProcessor processor = new MissionMarkerProcessor();

        level.getStructureManager().get(nbtPath).ifPresentOrElse(template -> {
            Rotation[] rotations = Rotation.values();
            Rotation randomRot = rotations[random.nextInt(rotations.length)];

            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(randomRot)
                    .addProcessor(processor) // 挂载标记扫描处理器
                    .setIgnoreEntities(false);

            BlockPos offset = new BlockPos(-template.getSize(randomRot).getX() / 2, 0, -template.getSize(randomRot).getZ() / 2);
            BlockPos placePos = pos.offset(offset);
            KubicDiversMod.LOGGER.info("Placing structure {} at {} (size={})", nbtPath, placePos, template.getSize(randomRot));
            template.placeInWorld(level, placePos, placePos, settings, RandomSource.create(), 2);
            KubicDiversMod.LOGGER.info("Structure {} placed, markers found: {}", nbtPath, processor.getFoundMarkers().size());
        }, () -> {
            KubicDiversMod.LOGGER.warn("Structure not found: {}", nbtPath);
        });

        return processor.getFoundMarkers();
    }

    /**
     * 放置结构但不扫描标记（用于环境填充物等不需要任务标记的结构）。
     */
    private void placeStructureWithoutScan(ServerLevel level, ResourceLocation nbtPath, BlockPos pos, Random random) {
        level.getStructureManager().get(nbtPath).ifPresent(template -> {
            Rotation[] rotations = Rotation.values();
            Rotation randomRot = rotations[random.nextInt(rotations.length)];

            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(randomRot)
                    .setIgnoreEntities(false);

            BlockPos offset = new BlockPos(-template.getSize(randomRot).getX() / 2, 0, -template.getSize(randomRot).getZ() / 2);
            template.placeInWorld(level, pos.offset(offset), pos.offset(offset), settings, RandomSource.create(), 2);
        });
    }

    /**
     * 根据 goalDefinitions 将扫描到的标记绑定到 IMissionGoal 实例。
     *
     * 这是数据驱动 API 的核心：数据包作者通过 JSON 定义 marker_id + handler + config，
     * 此方法将结构中的标记与对应的 Goal 工厂匹配，创建并注册目标实例。
     *
     * @param goalDefinitions 来自 MissionTemplate 的目标定义列表
     * @param markers         从结构中扫描到的标记列表
     * @param level           目标维度
     * @return 创建并绑定好坐标的 IMissionGoal 列表
     */
    private List<IMissionGoal> bindGoalsFromMarkers(
            List<MissionTemplateManager.GoalDefinition> goalDefinitions,
            List<MissionMarkerProcessor.Marker> markers,
            ServerLevel level) {

        List<IMissionGoal> goals = new ArrayList<>();

        for (MissionTemplateManager.GoalDefinition def : goalDefinitions) {
            // 通过工厂 ID 获取目标工厂
            IGoalFactory factory = GoalFactoryRegistry.get(def.handler());
            if (factory == null) {
                KubicDiversMod.LOGGER.warn("Unknown goal handler: {}. Skipping goal definition for marker: {}",
                        def.handler(), def.markerId());
                continue;
            }

            // 使用工厂创建目标实例
            IMissionGoal goal = factory.create(def.config());

            // 将扫描到的匹配标记绑定到目标上
            for (MissionMarkerProcessor.Marker marker : markers) {
                if (marker.type().equals(def.markerId())) {
                    if (goal instanceof GenericDestroyGoal destroyGoal) {
                        destroyGoal.registerTarget(level, marker.pos());
                    } else if (goal instanceof BlockTransformGoal transformGoal) {
                        transformGoal.registerTarget(level, marker.pos());
                    }
                    // 其他类型的目标可在此扩展
                }
            }

            goals.add(goal);
        }

        return goals;
    }
}