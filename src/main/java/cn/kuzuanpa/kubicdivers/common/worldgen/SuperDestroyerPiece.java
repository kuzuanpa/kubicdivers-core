package cn.kuzuanpa.kubicdivers.common.worldgen;

import cn.kuzuanpa.kubicdivers.common.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

public class SuperDestroyerPiece extends TemplateStructurePiece {
    public SuperDestroyerPiece(StructureTemplateManager manager, ResourceLocation template, BlockPos pos) {
        super(ModStructures.SUPER_DESTROYER_PIECE.get(), 0, manager, template, template.toString(), makeSettings(), pos);
    }

    public SuperDestroyerPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.SUPER_DESTROYER_PIECE.get(), tag, context.structureTemplateManager(), (rl) -> makeSettings());
    }

    private static StructurePlaceSettings makeSettings() {
        return new StructurePlaceSettings().setRotation(Rotation.NONE).setIgnoreEntities(false);
    }

    @Override
    protected void handleDataMarker(@NotNull String name, @NotNull BlockPos pos, @NotNull ServerLevelAccessor level, @NotNull RandomSource random, @NotNull BoundingBox box) {
    }

}