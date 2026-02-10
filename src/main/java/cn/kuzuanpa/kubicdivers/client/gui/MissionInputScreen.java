package cn.kuzuanpa.kubicdivers.client.gui;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.block.MissionTerminalBlockEntity;
import cn.kuzuanpa.kubicdivers.common.mission.MissionSummary;
import cn.kuzuanpa.kubicdivers.network.C2SConfirmMissionPacket;
import cn.kuzuanpa.kubicdivers.network.C2STerminalUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class MissionInputScreen extends Screen {
    private final MissionTerminalBlockEntity te;
    private double lastMouseX, lastMouseY;

    public MissionInputScreen(MissionTerminalBlockEntity te) {
        super(Component.empty());
        this.te = te;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public void mouseMoved(double mouseX, double mouseY) {
        if (lastMouseX == 0 && lastMouseY == 0) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return;
        }

        if(te.prepareToLaunch || te.activeDetails != null)return;
        double dx = mouseX - lastMouseX;
        double dy = mouseY - lastMouseY;

        te.cursorYaw += (float) dx * 0.03f;
        te.cursorPitch += (float) dy * 0.03f;

        te.cursorPitch = Math.max(-1.4f, Math.min(1.4f, te.cursorPitch));

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        checkMissionSelection();

        sync();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(te.prepareToLaunch)return super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            handleConfirmation();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleConfirmation() {
        if (te.focusedMissionId != -1) {
            KubicDiversMod.NETWORK_CHANNEL.sendToServer(
                    new C2SConfirmMissionPacket(te.getBlockPos(), te.focusedMissionId, false)
            );

            if(te.activeDetails != null)onClose();
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2f)
            );
        }
    }
    private void checkMissionSelection() {
        int nearestId = -1;
        float maxDot = 0.99f;

        float cx = (float) (Math.sin(te.cursorYaw) * Math.cos(te.cursorPitch));
        float cy = (float) (Math.sin(-te.cursorPitch));
        float cz = (float) (Math.cos(te.cursorYaw) * Math.cos(te.cursorPitch));

        for (MissionSummary m : te.missions.values()) {
            float[] v = getMissionVector(m);

            float len = (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            float vx = v[0] / len;
            float vy = v[1] / len;
            float vz = v[2] / len;

            float dot = cx * vx + cy * vy + cz * vz;

            if (dot > maxDot) {
                nearestId = m.id();
                break;
            }
        }

        if (te.focusedMissionId != nearestId) {
            te.focusedMissionId = nearestId;
            sync();
        }
    }

    private float[] getMissionVector(MissionSummary m) {
        float x = m.u() * 2 - 1;
        float y = -(m.v() * 2 - 1);
        float z = 1.0f;

        return switch (m.face()) {
            case 0 -> new float[]{ x,  y,  z};
            case 1 -> new float[]{-x,  y, -z};
            case 2 -> new float[]{ x,  z, -y};
            case 3 -> new float[]{ x, -z,  y};
            case 4 -> new float[]{ z,  y, -x};
            case 5 -> new float[]{-z,  y,  x};
            default -> new float[]{0, 0, 1};
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) KubicDiversMod.NETWORK_CHANNEL.sendToServer(
                new C2SConfirmMissionPacket(te.getBlockPos(), te.focusedMissionId, true)
        );

        if(te.prepareToLaunch)return super.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == GLFW.GLFW_KEY_Q) te.difficulty = Math.max(1, te.difficulty - 1);
        if (keyCode == GLFW.GLFW_KEY_E) te.difficulty = Math.min(9, te.difficulty + 1);

        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) {
            handleConfirmation();
            return true;
        }
        sync();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void sync() {
        KubicDiversMod.NETWORK_CHANNEL.sendToServer(new C2STerminalUpdatePacket(te.getBlockPos(), te.cursorYaw, te.cursorPitch, te.difficulty, te.focusedMissionId));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(this.font, "[ESC] TO RELEASE CONTROLS", 20, 20, 0x00F0FF);
    }
}