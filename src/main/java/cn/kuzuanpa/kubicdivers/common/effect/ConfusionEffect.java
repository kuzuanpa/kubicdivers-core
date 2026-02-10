package cn.kuzuanpa.kubicdivers.common.effect;

import cn.kuzuanpa.kubicdivers.common.entity.DistractionEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class ConfusionEffect extends MobEffect {
    public ConfusionEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A2BE2);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Mob mob) {
            if (entity.getRandom().nextFloat() < 0.9f) {
                DistractionEntity distraction = new DistractionEntity(entity.level(),
                        entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 16,
                        entity.getY() + 1,
                        entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16
                );
                entity.level().addFreshEntity(distraction);
                mob.setTarget(distraction);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration  % (20 /(amplifier + 1)) <= 0;
    }
}