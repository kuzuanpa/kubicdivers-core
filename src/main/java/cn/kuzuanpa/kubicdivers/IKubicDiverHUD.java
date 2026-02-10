package cn.kuzuanpa.kubicdivers;


import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface IKubicDiverHUD {
    default boolean shouldShowHUD() { return true; }
    List<Component> getHUDText();
    default float getTextScale() {return 1.0F;}
    default @Nullable ResourceLocation getHUDIcon(){return null;};
    default int getHUDColor() { return 0xFFFFFFFF; }
}