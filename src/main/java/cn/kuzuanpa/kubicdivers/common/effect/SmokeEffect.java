package cn.kuzuanpa.kubicdivers.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class SmokeEffect extends MobEffect {
    public SmokeEffect() {
        super(MobEffectCategory.HARMFUL, 0x999999);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Mob mob) {
            if(mob.getTarget() != null && mob.getTarget().position().distanceTo(mob.position()) <= 4)return;
            mob.setTarget(null);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration  % (20 /(amplifier + 1)) <= 0;
    }
}