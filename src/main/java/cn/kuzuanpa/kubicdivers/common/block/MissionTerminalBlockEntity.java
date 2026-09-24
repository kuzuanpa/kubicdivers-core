package cn.kuzuanpa.kubicdivers.common.block;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.ModBlockEntities;
import cn.kuzuanpa.kubicdivers.common.mission.DiveMission;
import cn.kuzuanpa.kubicdivers.common.mission.MissionDetails;
import cn.kuzuanpa.kubicdivers.common.mission.MissionManager;
import cn.kuzuanpa.kubicdivers.common.mission.MissionSummary;
import cn.kuzuanpa.kubicdivers.common.mission.types.DiveMissionTypeManager;
import cn.kuzuanpa.kubicdivers.event.MissionReadyEvent;
import cn.kuzuanpa.kubicdivers.network.S2CSyncMissionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MissionTerminalBlockEntity extends BlockEntity {
    public float cursorYaw, cursorPitch;
    public int difficulty = 4,  focusedMissionId = -1;
    public boolean prepareToLaunch = false;
    public final HashMap<Integer, MissionSummary> missions = new HashMap<>();
    public MissionDetails activeDetails = null;

    public MissionTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MISSION_TERMINAL.get(), pos, state);
    }
    public void syncState(float yaw, float pitch, int diff, int mid) {
        cursorYaw = yaw;

        cursorPitch = Math.max(-1.4f, Math.min(1.4f, pitch));

        difficulty = diff;

        // 当选中的任务发生变化时，自动加载该任务的详情
        if (focusedMissionId != mid && mid != -1 && !prepareToLaunch) {
            focusedMissionId = mid;
            loadMissionDetails(mid);
        } else {
            focusedMissionId = mid;
        }

        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void generateMissions() {
        missions.clear();
        RandomSource random = level != null ? level.random : RandomSource.create();

        // 从数据驱动的模板中获取可用任务类型
        var templates = cn.kuzuanpa.kubicgen.data.MissionTemplateManager.getAllTemplates();
        List<String> templateKeys = new ArrayList<>(templates.keySet());

        if (templateKeys.isEmpty()) {
            // 没有模板时回退到默认
            for (int i = 0; i < 5; i++) {
                missions.put(i, new MissionSummary(
                        i, random.nextInt(6), random.nextFloat(), random.nextFloat(),
                        DiveMissionTypeManager.getFromID(0), ""
                ));
            }
        } else {
            // 每个任务槽位随机选一个模板
            for (int i = 0; i < 5; i++) {
                String selectedKey = templateKeys.get(random.nextInt(templateKeys.size()));
                missions.put(i, new MissionSummary(
                        i, random.nextInt(6), random.nextFloat(), random.nextFloat(),
                        DiveMissionTypeManager.getFromID(0), selectedKey
                ));
            }
        }

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void cancelMission(){
        prepareToLaunch = false;
        activeDetails = null;
        focusedMissionId = -1;
        MinecraftForge.EVENT_BUS.post(new MissionReadyEvent(level, worldPosition, null, true));
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void confirmMission(int missionId, boolean isCanceled) {
        if(isCanceled){
            cancelMission();
            return;
        }
        if (activeDetails == null) {
            loadMissionDetails(missionId);
        }
        else {
            MinecraftForge.EVENT_BUS.post(new MissionReadyEvent(level, worldPosition, activeDetails, false));

            MissionManager.currentMission = new DiveMission(activeDetails.type(), level, difficulty, activeDetails.missionType());
            KubicDiversMod.NETWORK_CHANNEL.send(PacketDistributor.ALL.noArg(), S2CSyncMissionPacket.fromMission(MissionManager.currentMission));
            prepareToLaunch = true;
            triggerLaunchSequence();
        }
    }
    private void triggerLaunchSequence() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public void loadMissionDetails(int missionId) {
        boolean exists = missions.containsKey(missionId);
        if (!exists) return;

        focusedMissionId = missionId;
        MissionSummary summary = missions.get(missionId);

        // 使用选中任务的模板 ID 构建详情
        activeDetails = new MissionDetails(
                DiveMissionTypeManager.getFromID(0),
                difficulty,
                summary.missionType() != null ? summary.missionType() : ""
        );

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("cursorYaw", cursorYaw);
        tag.putFloat("cursorPitch", cursorPitch);
        tag.putInt("difficulty", difficulty);
        tag.putInt("focusedMissionId", focusedMissionId);
        tag.putBoolean("prepareToLaunch", prepareToLaunch);

        if(activeDetails != null){
            CompoundTag mTag = new CompoundTag();
            mTag.putInt("typeID", activeDetails.type().getID());
            mTag.putInt("diff", activeDetails.difficulty());
            mTag.putString("missionType", activeDetails.missionType() != null ? activeDetails.missionType() : "");
            tag.put("activeDetails", mTag);
        }
        ListTag missionList = new ListTag();
        for (MissionSummary m : missions.values()) {
            CompoundTag mTag = new CompoundTag();
            mTag.putInt("id", m.id());
            mTag.putInt("face", m.face());
            mTag.putFloat("u", m.u());
            mTag.putFloat("v", m.v());
            mTag.putInt("type", m.type().getID());
            mTag.putString("missionType", m.missionType() != null ? m.missionType() : "");
            missionList.add(mTag);
        }
        tag.put("missions", missionList);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        cursorYaw = tag.getFloat("cursorYaw");
        cursorPitch = tag.getFloat("cursorPitch");
        difficulty = tag.getInt("difficulty");
        focusedMissionId = tag.getInt("focusedMissionId");
        prepareToLaunch = tag.getBoolean("prepareToLaunch");

        missions.clear();
        if (tag.contains("activeDetails", Tag.TAG_COMPOUND)) {
            CompoundTag mTag = tag.getCompound("activeDetails");
            activeDetails = new MissionDetails(
                    DiveMissionTypeManager.getFromID(mTag.getInt("typeID")),
                    mTag.getInt("diff"),
                    mTag.contains("missionType") ? mTag.getString("missionType") : "");

        }else activeDetails = null;
        if (tag.contains("missions", Tag.TAG_LIST)) {
            ListTag missionList = tag.getList("missions", Tag.TAG_COMPOUND);
            for (int i = 0; i < missionList.size(); i++) {
                CompoundTag mTag = missionList.getCompound(i);
                missions.put(mTag.getInt("id"), new MissionSummary(
                        mTag.getInt("id"),
                        mTag.getInt("face"),
                        mTag.getFloat("u"),
                        mTag.getFloat("v"),
                        DiveMissionTypeManager.getFromID(mTag.getInt("type")),
                        mTag.contains("missionType") ? mTag.getString("missionType") : ""
                ));
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}