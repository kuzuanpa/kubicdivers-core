package cn.kuzuanpa.kubicdivers.client.renderer;

import cn.kuzuanpa.kubicdivers.common.entity.DistractionEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class NoRenders {
    public static class DistractionEntityRenderer extends EntityRenderer<DistractionEntity> {
        public DistractionEntityRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull DistractionEntity entity) {
            return TextureAtlas.LOCATION_BLOCKS;
        }

    }
}
