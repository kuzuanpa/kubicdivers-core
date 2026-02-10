package cn.kuzuanpa.kubicdivers.common;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.block.HellpodControllerBlock;
import cn.kuzuanpa.kubicdivers.common.block.MissionTerminalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, KubicDiversMod.MOD_ID);

    public static final RegistryObject<Block> MISSION_TERMINAL = BLOCKS.register("mission_terminal",
            () -> new MissionTerminalBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 10.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> HELLPOD_CONTROLLER = BLOCKS.register("hellpod_controller",
            () -> new HellpodControllerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f).noOcclusion()));

}