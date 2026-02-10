package cn.kuzuanpa.kubicdivers.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "helldivers");

    public static final RegistryObject<MobEffect> CONFUSION =
            EFFECTS.register("confusion", ConfusionEffect::new);
    public static final RegistryObject<MobEffect> SMOKE =
            EFFECTS.register("smoked", SmokeEffect::new);
}
