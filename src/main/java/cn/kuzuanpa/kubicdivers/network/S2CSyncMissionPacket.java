package cn.kuzuanpa.kubicdivers.network;

import cn.kuzuanpa.kubicdivers.common.mission.ClientMissionCache;
import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CSyncMissionPacket {
    private final String missionTitle;
    private final boolean failed;
    private final List<ClientMissionCache.ObjectiveSummary> mainObjectives;
    private final List<ClientMissionCache.ObjectiveSummary> subObjectives;

    public S2CSyncMissionPacket(String title, List<ClientMissionCache.ObjectiveSummary> mains, List<ClientMissionCache.ObjectiveSummary> subs, boolean failed) {
        this.missionTitle = title;
        this.failed = failed;
        this.mainObjectives = mains;
        this.subObjectives = subs;
    }

    public S2CSyncMissionPacket(String title, List<ClientMissionCache.ObjectiveSummary> mains, List<ClientMissionCache.ObjectiveSummary> subs) {
        this(title,mains,subs,false);
    }
    public static S2CSyncMissionPacket fromMission(DiveMission mission) {
        return new S2CSyncMissionPacket(
                mission.type.getDisplayName(),
                mission.mainGoal.stream().map(goal -> new ClientMissionCache.ObjectiveSummary(goal.getDescription(), goal.isCompleted(), goal.getProgressText())).toList(),
                mission.subGoal.stream().map(goal -> new ClientMissionCache.ObjectiveSummary(goal.getDescription(), goal.isCompleted(), goal.getProgressText())).toList());
    }

    public static void encode(S2CSyncMissionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.missionTitle);
        buf.writeBoolean(msg.failed);
        buf.writeCollection(msg.mainObjectives, (b, obj) -> {
            b.writeUtf(obj.description());
            b.writeBoolean(obj.completed());
            b.writeUtf(obj.progressText() != null ? obj.progressText() : "");
        });
        buf.writeCollection(msg.subObjectives, (b, obj) -> {
            b.writeUtf(obj.description());
            b.writeBoolean(obj.completed());
            b.writeUtf(obj.progressText() != null ? obj.progressText() : "");
        });
    }

    public static S2CSyncMissionPacket decode(FriendlyByteBuf buf) {
        String title = buf.readUtf();
        boolean failed = buf.readBoolean();
        List<ClientMissionCache.ObjectiveSummary> mains = buf.readCollection(ArrayList::new, b ->
                new ClientMissionCache.ObjectiveSummary(b.readUtf(), b.readBoolean(), b.readUtf()));
        List<ClientMissionCache.ObjectiveSummary> subs = buf.readCollection(ArrayList::new, b ->
                new ClientMissionCache.ObjectiveSummary(b.readUtf(), b.readBoolean(), b.readUtf()));
        return new S2CSyncMissionPacket(title, mains, subs, failed);
    }

    public static void handle(S2CSyncMissionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientMissionCache.update(
                    msg.missionTitle,
                    msg.mainObjectives,
                    msg.subObjectives
            );
        })
        );
        ctx.get().setPacketHandled(true);
    }
}