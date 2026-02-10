package cn.kuzuanpa.kubicdivers.client;

import cn.kuzuanpa.kubicdivers.common.mission.MissionSummary;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import java.util.List;

public class MissionSelectorLogic {
    public float rotX = 0, rotY = 0;
    private final List<MissionSummary> missions;
    private MissionSummary focusedMission;

    public MissionSelectorLogic(List<MissionSummary> missions) { this.missions = missions; }

    public void rotate(float dx, float dy) {
        this.rotY += dx * 0.2f;
        this.rotX += dy * 0.2f;
        updateFocus();
    }

    private void updateFocus() {
        focusedMission = null;
        float bestDot = 0.98f;

        Matrix4f rotationMatrix = new Matrix4f().rotateX(-rotX).rotateY(-rotY);

        for (MissionSummary m : missions) {
            Vector3f pos = getCubePos(m.face(), m.u(), m.v());
            pos.mulPosition(rotationMatrix);

            float dot = pos.dot(0, 0, 1);
            if (dot > bestDot) {
                bestDot = dot;
                focusedMission = m;
            }
        }
    }

    private Vector3f getCubePos(int face, float u, float v) {
        float x = u * 2 - 1, y = v * 2 - 1;
        return switch (face) {
            case 0 -> new Vector3f(x, y, 1);
            case 1 -> new Vector3f(x, y, -1);
            case 2 -> new Vector3f(x, 1, y);
            case 3 -> new Vector3f(x, -1, y);
            case 4 -> new Vector3f(1, x, y);
            case 5 -> new Vector3f(-1, x, y);
            default -> new Vector3f(0);
        };
    }

    public MissionSummary getFocusedMission() { return focusedMission; }
}