package cn.kuzuanpa.kubicdivers.common.mission;

import cn.kuzuanpa.kubicdivers.common.ModDimensions;
import cn.kuzuanpa.kubicdivers.network.UpdateLoadoutPacket;
import cn.kuzuanpa.kubicdivers.stratagem.common.StratagemPlayerManager;
import cn.kuzuanpa.kubicdivers.stratagem.common.entity.HellpodEntity;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.StratagemManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;


public class PlayerManager {
    private static final Map<String, UpdateLoadoutPacket> PLAYER_LOADOUTS = new HashMap<>();
    public static DiveMission currentMission;

    public static final boolean dev = false;
    public static void updateLoadout(String playerName, UpdateLoadoutPacket data) {
        PLAYER_LOADOUTS.put(playerName, data);
    }

    public static boolean checkReady(ServerPlayer player){
        ServerLevel level = player.server.getLevel(ModDimensions.DESTROYER_DIM);
        ServerLevel targetLevel = player.server.getLevel(Level.OVERWORLD);
        if(level == null)return false;
        if(PLAYER_LOADOUTS.size() == level.players().size() && PLAYER_LOADOUTS.values().stream().allMatch(UpdateLoadoutPacket::isReady)){
            initiateLaunch(level, targetLevel, level.players());
            return true;
        }
        return false;
    }
    public static List<UpdateLoadoutPacket> getDisplayTeammates(Player localPlayer) {
        List<UpdateLoadoutPacket> list = new ArrayList<>();

        for (Map.Entry<String, UpdateLoadoutPacket> entry : PLAYER_LOADOUTS.entrySet()) {
            if (dev || !entry.getKey().equals(localPlayer.getScoreboardName())) {
                list.add(entry.getValue());
            }
        }

        if (dev) {
            List<IStratagem> devStrats = new ArrayList<>();
            devStrats.add(StratagemManager.getStratagem("fire_barrage"));

            list.add(new UpdateLoadoutPacket("DEV",
                    (new ItemStack(Items.IRON_CHESTPLATE)),
            (new ItemStack(Items.GOLDEN_SWORD)),
            (new ItemStack(Items.SHIELD)),
            (new ItemStack(Items.POISONOUS_POTATO)),devStrats, true));
        }

        return list;
    }
    public static void setupPlayerLoadout(ServerPlayer player){
        player.getPersistentData().putBoolean("IsDeploying",true);
        List<IStratagem> availStratagems = PLAYER_LOADOUTS.get(player.getScoreboardName()).stratagems();
        availStratagems.addAll(currentMission.getMissionRequiredStratagems());
        StratagemPlayerManager.setAvailStratagems(player, availStratagems);

        StratagemPlayerManager.syncAvailStratagem(player);
    }
    public static void initiateLaunch(ServerLevel shipLevel, ServerLevel targetLevel, List<ServerPlayer> squad) {
        for (ServerPlayer player : squad) {
            PlayerManager.setupPlayerLoadout(player);

            BlockPos targetPos = targetLevel.getSharedSpawnPos();
            BlockPos spawnPos = targetLevel.getSharedSpawnPos().offset(0,500,0);
            player.teleportTo(targetLevel, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), player.getYRot(), player.getXRot());

            HellpodEntity pod = new HellpodEntity(targetLevel, Vec3.atCenterOf(spawnPos));

            pod.setTargetHeight(targetPos.getY());
            targetLevel.addFreshEntity(pod);
            player.startRiding(pod);
        }
    }
}