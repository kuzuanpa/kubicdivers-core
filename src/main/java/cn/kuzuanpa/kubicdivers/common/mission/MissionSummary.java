package cn.kuzuanpa.kubicdivers.common.mission;

import cn.kuzuanpa.kubicdivers.common.mission.types.DiveMissionTypeManager;
import cn.kuzuanpa.kubicdivers.common.mission.types.IDiveMissionType;
import net.minecraft.network.FriendlyByteBuf;

public record MissionSummary(int id, int face, float u, float v, IDiveMissionType type, String missionType) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(face);
        buf.writeFloat(u);
        buf.writeFloat(v);
        buf.writeInt(type.getID());
        buf.writeUtf(missionType != null ? missionType : "");
    }

    public static MissionSummary decode(FriendlyByteBuf buf) {
        return new MissionSummary(buf.readInt(), buf.readInt(), buf.readFloat(), buf.readFloat(), DiveMissionTypeManager.getFromID(buf.readInt()), buf.readUtf());
    }
}
