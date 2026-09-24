package cn.kuzuanpa.kubicdivers.common.mission;

import java.util.ArrayList;
import java.util.List;

public class ClientMissionCache {
    public static List<ObjectiveSummary> mainObjectives = new ArrayList<>();
    public static List<ObjectiveSummary> subObjectives = new ArrayList<>();
    public static String missionTitle = "UNKNOWN";

    public record ObjectiveSummary(String description, boolean completed, String progressText) {}

    public static void update(String title, List<ObjectiveSummary> mains, List<ObjectiveSummary> subs) {
        missionTitle = title;
        mainObjectives = mains;
        subObjectives = subs;
    }
}
