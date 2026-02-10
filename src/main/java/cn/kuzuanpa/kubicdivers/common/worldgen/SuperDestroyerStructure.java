package cn.kuzuanpa.kubicdivers.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import cn.kuzuanpa.kubicdivers.common.ModStructures;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SuperDestroyerStructure extends Structure {
    public static final Codec<SuperDestroyerStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            settingsCodec(instance),
            ResourceLocation.CODEC.fieldOf("template").forGetter(s -> s.template),
            Codec.INT.fieldOf("y_offset").forGetter(s -> s.yOffset)
    ).apply(instance, SuperDestroyerStructure::new));

    private final ResourceLocation template;
    private final int yOffset;

    public SuperDestroyerStructure(StructureSettings settings, ResourceLocation template, int yOffset) {
        super(settings);
        this.template = template;
        this.yOffset = yOffset;
    }

    @Override
    protected @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos cp = context.chunkPos();
        if (cp.x != 0 || cp.z != 0) {
            return Optional.empty();
        }

        BlockPos generatePos = new BlockPos(-142, this.yOffset, -40);
        return Optional.of(new GenerationStub(generatePos, (builder) -> this.generate(builder, context, generatePos)        ));
    }

    private void generate(StructurePiecesBuilder builder, GenerationContext context, BlockPos pos) {
        builder.addPiece(new SuperDestroyerPiece(context.structureTemplateManager(), this.template, pos));
    }

    @Override
    public @NotNull StructureType<?> type() {
        return ModStructures.SUPER_DESTROYER_TYPE.get();
    }
}