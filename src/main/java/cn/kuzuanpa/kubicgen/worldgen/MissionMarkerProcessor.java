package cn.kuzuanpa.kubicgen.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MissionMarkerProcessor extends StructureProcessor {
    /**
     * Codec 用于序列化/反序列化此处理器，StructureProcessor 子类必须提供。
     * MissionMarkerProcessor 无需持久化状态（标记收集是运行时行为），因此使用 unit codec。
     */
    public static final MapCodec<MissionMarkerProcessor> CODEC = MapCodec.unit(MissionMarkerProcessor::new);

    public static final StructureProcessorType<MissionMarkerProcessor> TYPE = MissionMarkerProcessor.CODEC::codec;

    // 记录：标记名称 -> 绝对坐标
    public record Marker(String type, BlockPos pos) {}
    private final List<Marker> foundMarkers = new ArrayList<>();

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level, BlockPos structurePos, BlockPos worldPos,
            StructureTemplate.StructureBlockInfo ignored,
            StructureTemplate.StructureBlockInfo blockInfo,
            StructurePlaceSettings settings) {

        // 拦截处于“数据模式”的结构方块
        if (blockInfo.state().is(Blocks.STRUCTURE_BLOCK)) {
            if (blockInfo.nbt() != null && blockInfo.nbt().contains("metadata")) {
                String metadata = blockInfo.nbt().getString("metadata");

                // 识别属于我们的任务标记
                if (metadata.startsWith("mission_target:")) {
                    // blockInfo.pos() 是经过旋转+偏移后的世界绝对坐标
                    // （Minecraft 在调用 processBlock 前已经计算好了）
                    // 注意：worldPos 参数只是固定的 structurePos，不是每个方块的位置！
                    BlockPos actualWorldPos = blockInfo.pos();
                    foundMarkers.add(new Marker(metadata, actualWorldPos));
                    // 核心：将其替换为空气，不让结构方块残留在地图中
                    return new StructureTemplate.StructureBlockInfo(actualWorldPos, Blocks.AIR.defaultBlockState(), null);
                }
            }
        }
        return blockInfo; // 其他方块正常放置
    }

    public List<Marker> getFoundMarkers() {
        return foundMarkers;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TYPE;
    }
}