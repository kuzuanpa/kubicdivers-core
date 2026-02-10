package cn.kuzuanpa.kubicdivers;

import cn.kuzuanpa.kubicdivers.client.renderer.MissionTerminalRenderer;
import cn.kuzuanpa.kubicdivers.common.*;
import cn.kuzuanpa.kubicdivers.common.effect.ModEffects;
import cn.kuzuanpa.kubicdivers.common.event.KubicEntityHelper;
import cn.kuzuanpa.kubicdivers.common.mission.types.DiveMissionTypeManager;
import cn.kuzuanpa.kubicdivers.network.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraft.resources.ResourceLocation;

@Mod(KubicDiversMod.MOD_ID)
public class KubicDiversMod {
    public static final String MOD_ID = "kubicdivers_core";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public KubicDiversMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.PIECE_TYPES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        DiveMissionTypeManager.registerDefaultMissionTypes();
        KubicEntityHelper.registerVanillaEntityCompact();
        MinecraftForge.EVENT_BUS.register(this);


        LOGGER.info("kubicdivers initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

            NETWORK_CHANNEL.registerMessage(0,
                    S2COpenMissionScreen.class,
                    S2COpenMissionScreen::encode,
                    S2COpenMissionScreen::decode,
                    S2COpenMissionScreen::handle
            );
            NETWORK_CHANNEL.registerMessage(1,
                    C2STerminalUpdatePacket.class,
                    C2STerminalUpdatePacket::encode,
                    C2STerminalUpdatePacket::decode,
                    C2STerminalUpdatePacket::handle
            );

            NETWORK_CHANNEL.registerMessage(2,
                    C2SConfirmMissionPacket.class,
                    C2SConfirmMissionPacket::encode,
                    C2SConfirmMissionPacket::decode,
                    C2SConfirmMissionPacket::handle);
            NETWORK_CHANNEL.registerMessage(3,
                    C2SExitHellpodPacket.class,
                    C2SExitHellpodPacket::encode,
                    C2SExitHellpodPacket::decode,
                    C2SExitHellpodPacket::handle);
            NETWORK_CHANNEL.registerMessage(4,
                    S2COpenStrategemScreen.class,
                    S2COpenStrategemScreen::encode,
                    S2COpenStrategemScreen::decode,
                    S2COpenStrategemScreen::handle);
            NETWORK_CHANNEL.registerMessage(5,
                    UpdateLoadoutPacket.class,
                    UpdateLoadoutPacket::encode,
                    UpdateLoadoutPacket::decode,
                    UpdateLoadoutPacket::handle);
            NETWORK_CHANNEL.registerMessage(6,
                    S2CSyncMissionPacket.class,
                    S2CSyncMissionPacket::encode,
                    S2CSyncMissionPacket::decode,
                    S2CSyncMissionPacket::handle);
            LOGGER.info("Common setup completed");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Client setup completed");

            BlockEntityRenderers.register(ModBlockEntities.MISSION_TERMINAL.get(), MissionTerminalRenderer::new);
        });
    }

}