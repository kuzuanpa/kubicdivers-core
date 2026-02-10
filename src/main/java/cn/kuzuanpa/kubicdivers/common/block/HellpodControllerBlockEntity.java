package cn.kuzuanpa.kubicdivers.common.block;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.event.IMissionReceiver;
import cn.kuzuanpa.kubicdivers.event.MissionReadyEvent;
import cn.kuzuanpa.kubicdivers.common.ModBlockEntities;
import cn.kuzuanpa.kubicdivers.event.MissionSystemManager;
import cn.kuzuanpa.kubicdivers.network.S2COpenStrategemScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

public class HellpodControllerBlockEntity extends BlockEntity implements IMissionReceiver {
    private UUID playerUUID = null;
    public HellpodControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HELLPOD_CONTROLLER_BE.get(), pos, state);
    }

    @Override
    public void onMissionReady(MissionReadyEvent event) {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            if (state.hasProperty(HellpodControllerBlock.OPEN)) {
                this.level.setBlock(this.worldPosition, state.setValue(HellpodControllerBlock.OPEN, !event.cancelMission), 3);
            }
        }
    }

    public void tryPlayerEntry(Player player) {
        if (playerUUID != null) return;
        this.playerUUID = player.getUUID();
        player.teleportTo(worldPosition.getX() + 0.5, worldPosition.getY() - 2.0, worldPosition.getZ() + 0.5);

        checkAllPlayersReady();
    }

    private void checkAllPlayersReady() {
        if (level == null) return;

        List<? extends Player> allPlayers = level.players();

        long readyCount = MissionSystemManager.getOccupiedPodCount(level);
        int totalPlayers = allPlayers.size();

        if (readyCount >= totalPlayers) {
            dispatchStrategemScreenToAll(allPlayers);
        } else {
            Component msg = Component.literal("Awaiting Helldivers... (" + readyCount + "/" + totalPlayers + ")")
                    .withStyle(ChatFormatting.YELLOW);
            for (Player p : allPlayers) {
                p.displayClientMessage(msg, true);
            }
        }
    }

    private void dispatchStrategemScreenToAll(List<? extends Player> players) {
        for (Player p : players) {
             KubicDiversMod.NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) p), new S2COpenStrategemScreen());
        }
    }
    public void releasePlayer(Player player) {
        if (player.getUUID().equals(playerUUID)) {
            this.playerUUID = null;

            player.teleportTo(worldPosition.getX() + 1.5, worldPosition.getY(), worldPosition.getZ() + 0.5);

            this.checkAllPlayersReady();

            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public boolean isReady(){
        return playerUUID != null;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            MissionSystemManager.register(level, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            MissionSystemManager.unregister(level, worldPosition);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            MissionSystemManager.unregister(level, worldPosition);
        }
    }
}