
package cn.kuzuanpa.kubicgen;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicgen.worldgen.MissionMarkerProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * kubicgen 模块的注册表。
 * 虽然逻辑上 kubicgen 是独立模块，但由于打包在同一个 jar 中，
 * 所有注册都挂载到主 mod kubicdivers_core 下。
 */
public class KubicGenRegistries {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, KubicDiversMod.MOD_ID);

    public static final RegistryObject<StructureProcessorType<MissionMarkerProcessor>> MISSION_MARKER_PROCESSOR =
            STRUCTURE_PROCESSOR_TYPES.register("mission_marker", () -> MissionMarkerProcessor.TYPE);
}
