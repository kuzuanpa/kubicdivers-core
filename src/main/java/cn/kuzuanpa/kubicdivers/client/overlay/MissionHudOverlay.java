package cn.kuzuanpa.kubicdivers.client.overlay;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.mission.ClientMissionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KubicDiversMod.MOD_ID, value = Dist.CLIENT)
public class MissionHudOverlay {
    private static final int COLOR_MAIN = 0xFFE800;
    private static final int COLOR_SUB = 0x00FFFF;
    private static final int COLOR_DONE = 0x888888;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (Minecraft.getInstance().screen != null) return;

        GuiGraphics gui = event.getGuiGraphics();
        Font font = Minecraft.getInstance().font;
        int width = event.getWindow().getGuiScaledWidth();

        int x = width - 150;
        int y = 10;

        gui.drawString(font, " // " + ClientMissionCache.missionTitle, x, y, 0xFFFFFF);
        y += 15;

        for (ClientMissionCache.ObjectiveSummary goal : ClientMissionCache.mainObjectives) {
            renderGoal(gui, font, x + 5, y, goal, COLOR_MAIN);
            y += 12;
        }

        if (!ClientMissionCache.subObjectives.isEmpty()) {
            y += 5;
            gui.fill(x, y, width - 5, y + 1, 0x44FFFFFF);
            y += 5;
            for (ClientMissionCache.ObjectiveSummary goal : ClientMissionCache.subObjectives) {
                renderGoal(gui, font, x + 5, y, goal, COLOR_SUB);
                y += 12;
            }
        }
    }

    private static void renderGoal(GuiGraphics gui, Font font, int x, int y, ClientMissionCache.ObjectiveSummary goal, int color) {
        String prefix = goal.completed() ? "☑ " : "□ ";
        int renderColor = goal.completed() ? COLOR_DONE : color;

        String text = prefix + goal.description();
        if (goal.progressText() != null) text += " (" + goal.progressText() + ")";

        gui.drawString(font, text, x, y, renderColor);

        if (goal.completed()) {
            int textWidth = font.width(text);
            gui.fill(x + 10, y + 4, x + textWidth, y + 5, 0x88000000);
        }
    }
}