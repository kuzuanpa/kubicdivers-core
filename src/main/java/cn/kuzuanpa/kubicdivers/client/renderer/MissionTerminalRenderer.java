package cn.kuzuanpa.kubicdivers.client.renderer;

import cn.kuzuanpa.kubicdivers.common.block.MissionTerminalBlockEntity;
import cn.kuzuanpa.kubicdivers.common.mission.MissionSummary;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class MissionTerminalRenderer implements BlockEntityRenderer<MissionTerminalBlockEntity> {
    static final ResourceLocation HOLOGRID = ResourceLocation.fromNamespaceAndPath("kubicdivers_core", "textures/misc/hologram_grid.png");
    static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("kubicdivers_core", "textures/gui/mission_icon.png");
    static final ResourceLocation RETICLE = ResourceLocation.fromNamespaceAndPath("kubicdivers_core", "textures/gui/terminal_cursor.png");
    private float smoothYaw, smoothPitch;
    public MissionTerminalRenderer(BlockEntityRendererProvider.Context context) {}
    @Override
    public void render(@NotNull MissionTerminalBlockEntity be, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        smoothYaw = lerp(smoothYaw, be.cursorYaw, 0.15f);
        smoothPitch = lerp(smoothPitch, be.cursorPitch, 0.15f);

        poseStack.pushPose();
        poseStack.translate(0.5, 2.5, -0.5);
        poseStack.pushPose();

        poseStack.mulPose(new Quaternionf().rotateX(-smoothPitch));
        poseStack.mulPose(new Quaternionf().rotateY(-smoothYaw));
        float cubeSize = 0.9f;
        renderHologramCube(poseStack, buffer, cubeSize);

        if(!be.missions.isEmpty())for (MissionSummary m : be.missions.values()) {
            boolean isSelected = be.focusedMissionId != -1 && be.activeDetails != null && be.missions.get(be.focusedMissionId) != null && be.missions.get(be.focusedMissionId).type() == be.activeDetails.type();
            renderMissionIcon(poseStack, buffer, m, cubeSize, be.focusedMissionId == m.id(), isSelected);
        }
        poseStack.popPose();

        renderFixedReticle(poseStack, buffer, cubeSize, be.cursorYaw - smoothYaw, be.cursorPitch - smoothPitch);

        renderPhysicalUI(be, poseStack, buffer);
        poseStack.popPose();
    }
    private void renderFixedReticle(PoseStack poseStack, MultiBufferSource buffer, float cubeSize, float swayYaw, float swayPitch) {
        VertexConsumer builder = buffer.getBuffer(RenderType.beaconBeam(RETICLE, false));

        float offsetX = swayYaw * 0.5F;
        float offsetY = -swayPitch* 0.5F;
        float zPos = cubeSize + 0.25f;
        float s = 0.04f;

        Matrix4f mat = poseStack.last().pose();
        builder.vertex(mat, -s + offsetX, -s + offsetY, zPos).color(255, 255, 255, 220).uv(0, 1).overlayCoords(0).uv2(15728880).normal(0,0,1).endVertex();
        builder.vertex(mat,  s + offsetX, -s + offsetY, zPos).color(255, 255, 255, 220).uv(1, 1).overlayCoords(0).uv2(15728880).normal(0,0,1).endVertex();
        builder.vertex(mat,  s + offsetX,  s + offsetY, zPos).color(255, 255, 255, 220).uv(1, 0).overlayCoords(0).uv2(15728880).normal(0,0,1).endVertex();
        builder.vertex(mat, -s + offsetX,  s + offsetY, zPos).color(255, 255, 255, 220).uv(0, 0).overlayCoords(0).uv2(15728880).normal(0,0,1).endVertex();
}

    private float lerp(float start, float end, float pct) {
        return start + (end - start) * pct;
    }
    private void renderHologramCube(PoseStack poseStack, MultiBufferSource buffer, float cubeSize) {
        int color = 0xFFFFFFFF;
        VertexConsumer cubeBuilder = buffer.getBuffer(RenderType.beaconBeam(HOLOGRID, false));

        // Front (+Z)
        renderFace(poseStack, cubeBuilder, cubeSize, 0, 0, 0, color);
        // Back (-Z)
        renderFace(poseStack, cubeBuilder, cubeSize, 0, (float)Math.PI, 0, color);
        // Up (+Y)
        renderFace(poseStack, cubeBuilder, cubeSize, (float)-Math.PI/2, 0, 0, color);
        // Down (-Y)
        renderFace(poseStack, cubeBuilder, cubeSize, (float)Math.PI/2, 0, 0, color);
        // Right (+X)
        renderFace(poseStack, cubeBuilder, cubeSize, 0, (float)Math.PI/2, 0, color);
        // Left (-X)
        renderFace(poseStack, cubeBuilder, cubeSize, 0, (float)-Math.PI/2, 0, color);

    }

    /**
     * 通用平面渲染：将当前面旋转到正面并绘制
     */
    private void renderFace(PoseStack poseStack, VertexConsumer builder, float size, float rotX, float rotY, float rotZ, int color) {
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateX(rotX).rotateY(rotY).rotateZ(rotZ));
        poseStack.translate(0, 0, size);

        Matrix4f mat = poseStack.last().pose();
        builder.vertex(mat, -size, -size, 0).color(color).uv(0, 1).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
        builder.vertex(mat,  size, -size, 0).color(color).uv(1, 1).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
        builder.vertex(mat,  size,  size, 0).color(color).uv(1, 0).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
        builder.vertex(mat, -size,  size, 0).color(color).uv(0, 0).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();

        poseStack.popPose();
    }

    private void renderMissionIcon(PoseStack poseStack, MultiBufferSource buffer, MissionSummary m, float cubeSize, boolean isFocused, boolean isSelected) {
        poseStack.pushPose();

        switch (m.face()) {
            case 0 -> {} // front
            case 1 -> poseStack.mulPose(new Quaternionf().rotateY((float)Math.PI)); // back
            case 2 -> poseStack.mulPose(new Quaternionf().rotateX((float)-Math.PI/2)); // up
            case 3 -> poseStack.mulPose(new Quaternionf().rotateX((float)Math.PI/2)); // down
            case 4 -> poseStack.mulPose(new Quaternionf().rotateY((float)Math.PI/2)); // right
            case 5 -> poseStack.mulPose(new Quaternionf().rotateY((float)-Math.PI/2)); // left
        }

        float x = (m.u() * 2 - 1) * cubeSize;
        float y = -(m.v() * 2 - 1) * cubeSize;
        poseStack.translate(x, y, cubeSize + 0.01f);

        VertexConsumer iconBuilder = buffer.getBuffer(RenderType.beaconBeam(ICON, false));
        Matrix4f mat = poseStack.last().pose();
        float s = isFocused ? 0.06f : 0.05f;
        int color = 0xFFFFFFFF;

        iconBuilder.vertex(mat, -s, -s, 0).color(color).uv(0, 1).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
        iconBuilder.vertex(mat,  s, -s, 0).color(color).uv(1, 1).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
        iconBuilder.vertex(mat,  s,  s, 0).color(color).uv(1, 0).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
        iconBuilder.vertex(mat, -s,  s, 0).color(color).uv(0, 0).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();

        if (isFocused && isSelected) {
            poseStack.pushPose();
            float time = System.currentTimeMillis() % 360000 / 1000.0f;
            poseStack.mulPose(new Quaternionf().rotateZ(time));

            VertexConsumer ringBuilder = buffer.getBuffer(RenderType.beaconBeam(ICON, false));
            Matrix4f matrix = poseStack.last().pose();
            float size = 0.09f ;


            ringBuilder.vertex(matrix, -size, -size, 0).color(0xFFFFFFFF).uv(0, 1).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
            ringBuilder.vertex(matrix,  size, -size, 0).color(0xFFFFFFFF).uv(1, 1).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
            ringBuilder.vertex(matrix,  size,  size, 0).color(0xFFFFFFFF).uv(1, 0).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();
            ringBuilder.vertex(matrix, -size,  size, 0).color(0xFFFFFFFF).uv(0, 0).overlayCoords(0).uv2(15728880).normal(0, 0, 1).endVertex();

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderPhysicalUI(MissionTerminalBlockEntity be, PoseStack poseStack, MultiBufferSource buffer) {
        if (be.activeDetails == null) return;

        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();

        poseStack.translate(0.6, 0.4, 0.1);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-20)));
        poseStack.scale(0.02f, -0.02f, 0.02f);

        int yOffset = 0;
        // 优先显示模板名称（如 "Destroy Hive"），否则回退到 type 的显示名
        String displayName = be.activeDetails.missionType();
        if (displayName == null || displayName.isEmpty()) {
            displayName = be.activeDetails.type().getDisplayName();
        } else {
            // 将 mission_type (如 "destroy_hive") 转为可读标题 (如 "Destroy Hive")
            displayName = formatMissionType(displayName);
        }
        font.drawInBatch(displayName, 0, yOffset, 0xFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, 15728880);

        // 显示难度
        yOffset += 12;
        font.drawInBatch("Difficulty: " + be.activeDetails.difficulty(), 0, yOffset, 0xAAAAFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, 15728880);

        poseStack.popPose();
    }

    /** 将 snake_case 的 mission_type 转为 Title Case 显示名 */
    private static String formatMissionType(String type) {
        StringBuilder sb = new StringBuilder();
        for (String word : type.split("_")) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}