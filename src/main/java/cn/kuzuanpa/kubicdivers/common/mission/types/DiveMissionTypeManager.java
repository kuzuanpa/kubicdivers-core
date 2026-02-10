package cn.kuzuanpa.kubicdivers.common.mission.types;

import java.util.concurrent.ConcurrentHashMap;

public class DiveMissionTypeManager {

    private static final ConcurrentHashMap<Integer, IDiveMissionType> REGISTRY = new ConcurrentHashMap<>();
    public static IDiveMissionType getFromID(int id){
        return REGISTRY.get(id);
    }
    public static void register(IDiveMissionType missionType){
        REGISTRY.put(missionType.getID(), missionType);
    }
    public static void registerDefaultMissionTypes(){
        register(new DiveMissionTypeVanillaRecoverRedstone());
    }
}
