package cn.kuzuanpa.kubicdivers.client.event;

import cn.kuzuanpa.kubicdivers.IKubicDiverHUD;
import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = KubicDiversMod.MOD_ID, value = Dist.CLIENT)
public class HUDRenderHandler {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        //To render on top of anything.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof IKubicDiverHUD hud && hud.shouldShowHUD()) {
                renderWorldHUD(poseStack, bufferSource, hud, entity, cameraPos, event.getPartialTick());
            }
        }
    }

    private static void renderWorldHUD(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                       IKubicDiverHUD hud, Entity entity, Vec3 cameraPos, float partialTick) {

        double entityX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double entityY = Mth.lerp(partialTick, entity.yOld, entity.getY()) + entity.getBbHeight() + 0.5;
        double entityZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        poseStack.pushPose();
        poseStack.last().pose().identity();

        poseStack.translate(entityX - cameraPos.x, entityY - cameraPos.y, entityZ - cameraPos.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        float scale = 0.025F * hud.getTextScale();
        poseStack.scale(-scale, -scale, scale);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = poseStack.last().pose();
        Font font = Minecraft.getInstance().font;

        int light = 15728880;
        float currentY = 0;

        if(hud.getHUDIcon() != null){
            poseStack.pushPose();
            poseStack.translate(-26, 8, 0);
            renderIcon(poseStack, bufferSource, hud.getHUDIcon(), light);
            poseStack.popPose();
        }


        for (net.minecraft.network.chat.Component line : hud.getHUDText()) {
            font.drawInBatch(line, -14, currentY, hud.getHUDColor(), false, matrix,
                    bufferSource, Font.DisplayMode.SEE_THROUGH, 0, light);
            currentY += 10;
        }

        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }
    private static void renderIcon(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int light) {
        VertexConsumer builder = bufferSource.getBuffer(RenderType.textSeeThrough(texture));

        Matrix4f matrix = poseStack.last().pose();

        float s = 8.0f;
        builder.vertex(matrix, -s, -s, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, -s, s, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, s, s, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, s, -s, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
    }
}