package cn.kuzuanpa.kubicdivers.common;

import cn.kuzuanpa.kubicdivers.common.worldgen.SuperDestroyerPiece;
import cn.kuzuanpa.kubicdivers.common.worldgen.SuperDestroyerStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, KubicDiversMod.MOD_ID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, KubicDiversMod.MOD_ID);

    public static final RegistryObject<StructureType<SuperDestroyerStructure>> SUPER_DESTROYER_TYPE =
            STRUCTURE_TYPES.register("super_destroyer", () -> () -> SuperDestroyerStructure.CODEC);

    public static final RegistryObject<StructurePieceType> SUPER_DESTROYER_PIECE =
            PIECE_TYPES.register("super_destroyer_piece", () -> SuperDestroyerPiece::new);

    public static final ResourceLocation SUPER_DESTROYER = ResourceLocation.fromNamespaceAndPath(KubicDiversMod.MOD_ID, "super_destroyer");
}