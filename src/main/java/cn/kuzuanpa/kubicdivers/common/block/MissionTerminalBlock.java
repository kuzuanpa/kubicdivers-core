package cn.kuzuanpa.kubicdivers.common.block;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.network.S2COpenMissionScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class MissionTerminalBlock extends Block implements EntityBlock {
    public MissionTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

        player.setXRot(1.0F);
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MissionTerminalBlockEntity terminal))return InteractionResult.PASS;

        if (terminal.missions.isEmpty()) {
            terminal.generateMissions();
        }
        KubicDiversMod.NETWORK_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new S2COpenMissionScreen(pos)
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new MissionTerminalBlockEntity(pos, state);
    }
}