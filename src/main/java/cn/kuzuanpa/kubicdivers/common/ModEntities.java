package cn.kuzuanpa.kubicdivers.common;

import cn.kuzuanpa.kubicdivers.common.entity.DistractionEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static cn.kuzuanpa.kubicdivers.KubicDiversMod.MOD_ID;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);


    public static final RegistryObject<EntityType<DistractionEntity>> DISTRACTION =
            ENTITIES.register("distraction",
                    () -> EntityType.Builder.<DistractionEntity>of(DistractionEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("distraction"));
}