package cn.kuzuanpa.kubicdivers.common;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.block.HellpodControllerBlockEntity;
import cn.kuzuanpa.kubicdivers.common.block.MissionTerminalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static cn.kuzuanpa.kubicdivers.common.ModBlocks.HELLPOD_CONTROLLER;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, KubicDiversMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<MissionTerminalBlockEntity>> MISSION_TERMINAL =
            BLOCK_ENTITIES.register("mission_terminal",
                    () -> BlockEntityType.Builder.of(MissionTerminalBlockEntity::new, ModBlocks.MISSION_TERMINAL.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<HellpodControllerBlockEntity>> HELLPOD_CONTROLLER_BE =
            BLOCK_ENTITIES.register("hellpod_controller", () -> BlockEntityType.Builder.of(HellpodControllerBlockEntity::new, HELLPOD_CONTROLLER.get()).build(null));
}