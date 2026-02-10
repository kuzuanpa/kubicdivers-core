package cn.kuzuanpa.kubicdivers.common;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KubicDiversMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GameFlowHandler {


    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
//Todo: if in game, if new, spawn join effects, else, call teammate to reinforce.

        ServerLevel destroyerLevel = player.server.getLevel(ModDimensions.DESTROYER_DIM);
        if (destroyerLevel == null) return;

        player.teleportTo(destroyerLevel, 0.5, 92.0, 0.5, 0, 0);
    }
    @SubscribeEvent
    public static void onDimensionChange(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean isFromDestroyer = player.level().dimension().equals(ModDimensions.DESTROYER_DIM);

        if (!isFromDestroyer) return;

        if (!player.getPersistentData().getBoolean("IsDeploying")) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("Select mission first"));
        }
    }
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = player.server.getLevel(ModDimensions.DESTROYER_DIM);
        if(level == null)return;

        BlockPos respawnPos = new BlockPos(0, 92, 0);
        float respawnAngle = level.getSharedSpawnAngle();

        player.teleportTo(level,
                respawnPos.getX() + 0.5,
                respawnPos.getY(),
                respawnPos.getZ() + 0.5,
                respawnAngle, 0.0F);
    }
}