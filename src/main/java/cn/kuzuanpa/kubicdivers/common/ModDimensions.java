package cn.kuzuanpa.kubicdivers.common;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensions {
    public static final ResourceKey<Level> DESTROYER_DIM = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(KubicDiversMod.MOD_ID, "super_destroyer_dim")
    );

    public static final ResourceKey<DimensionType> DESTROYER_DIM_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(KubicDiversMod.MOD_ID, "super_destroyer_type")
    );
}