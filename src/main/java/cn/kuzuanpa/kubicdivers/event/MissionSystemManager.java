package cn.kuzuanpa.kubicdivers.event;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.block.HellpodControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber(modid = KubicDiversMod.MOD_ID)
public class MissionSystemManager {
    private static final Set<ReceiverKey> RECEIVERS = new HashSet<>();

    public record ReceiverKey(ResourceKey<Level> dimension, BlockPos pos) {}

    public static void register(Level level, BlockPos pos) {
        RECEIVERS.add(new ReceiverKey(level.dimension(), pos.immutable()));
    }

    public static void unregister(Level level, BlockPos pos) {
        RECEIVERS.remove(new ReceiverKey(level.dimension(), pos));
    }
    public static long getOccupiedPodCount(Level level) {
        return RECEIVERS.stream()
                .filter(key -> key.dimension().equals(level.dimension()))
                .map(key -> level.getBlockEntity(key.pos()))
                .filter(be -> be instanceof HellpodControllerBlockEntity pod && pod.isReady())
                .count();
    }
    public static Optional<HellpodControllerBlockEntity> findPodOccupiedBy(Player player) {
        return RECEIVERS.stream()
                .map(key -> player.level().getBlockEntity(key.pos()))
                .filter(be -> be instanceof HellpodControllerBlockEntity pod && player.getUUID().equals(pod.getPlayerUUID()))
                .map(be -> (HellpodControllerBlockEntity) be)
                .findFirst();
    }

    @SubscribeEvent
    public static void onMissionReady(MissionReadyEvent event) {
        RECEIVERS.removeIf(key -> {
            if (!key.dimension().equals(event.level.dimension())) return false;

            BlockEntity be = event.level.getBlockEntity(key.pos());
            if (be instanceof IMissionReceiver receiver) {
                receiver.onMissionReady(event);
                return false;
            }
            return true;
        });
    }
}