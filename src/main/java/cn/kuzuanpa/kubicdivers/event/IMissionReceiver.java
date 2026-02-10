package cn.kuzuanpa.kubicdivers.event;

public interface IMissionReceiver {
    void onMissionReady(MissionReadyEvent event);
}