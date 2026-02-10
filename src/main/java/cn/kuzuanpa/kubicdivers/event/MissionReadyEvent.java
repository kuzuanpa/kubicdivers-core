package cn.kuzuanpa.kubicdivers.event;

import cn.kuzuanpa.kubicdivers.common.mission.MissionDetails;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class MissionReadyEvent extends Event {
    public final Level level;
    public final BlockPos terminalPos;
    public final MissionDetails details;
    public final boolean cancelMission;

    public MissionReadyEvent(Level level, BlockPos terminalPos, MissionDetails details, boolean cancelMission) {
        this.level = level;
        this.terminalPos = terminalPos;
        this.details = details;
        this.cancelMission = cancelMission;
    }
}