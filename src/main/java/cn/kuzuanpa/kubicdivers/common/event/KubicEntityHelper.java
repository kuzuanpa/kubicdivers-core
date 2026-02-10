package cn.kuzuanpa.kubicdivers.common.event;

import cn.kuzuanpa.kubicdivers.api.entity.IKubicEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class KubicEntityHelper {
    protected static final HashMap<Class<? extends Entity>, KubicEntityCompactEntry> compactEntry = new HashMap<>();
    public static @Nullable IKubicEntity getKubicEntity(Entity e){
        if(e instanceof IKubicEntity)return ((IKubicEntity) e);
        return compactEntry.get(e.getClass());
    }
    public static void registerVanillaEntityCompact(){
        register(Endermite.class, 0);
        register(Silverfish.class, 0);
        register(Bee.class, 0);
        register(Vex.class, 1);
        register(Slime.class, 1);
        register(Wolf.class, 1);
        register(Pufferfish.class, 1);
        register(Ghast.class, 1);
        register(Zombie.class, 2);
        register(ZombieVillager.class, 2);
        register(ZombifiedPiglin.class, 2);
        register(WitherSkeleton.class, 2);
        register(PolarBear.class, 2);
        register(Stray.class, 2);
        register(Skeleton.class, 2);
        register(Spider.class, 2);
        register(Pillager.class, 2);
        register(Piglin.class, 2);
        register(Phantom.class, 2);
        register(MagmaCube.class, 2);
        register(Husk.class, 2);
        register(Blaze.class, 2);
        register(Drowned.class, 2);
        register(CaveSpider.class, 2);
        register(Witch.class, 3);
        register(Shulker.class, 3);
        register(Guardian.class, 3);
        register(Creeper.class, 3);
        register(EnderMan.class, 3);
        register(Zoglin.class, 3);
        register(Hoglin.class, 3);
        register(Vindicator.class, 4);
        register(Evoker.class, 4);
        register(ElderGuardian.class, 4);
        register(PiglinBrute.class, 4);
        register(Ravager.class, 5);
        register(Warden.class, 7);
        register(EnderDragon.class, 7);
        register(WitherBoss.class, 7);
    }
    public static KubicEntityCompactEntry register(Class<? extends Entity> entityClass, int level){
        KubicEntityCompactEntry entry = new KubicEntityCompactEntry(level);
        compactEntry.put(entityClass, entry);
        return entry;
    }
    public record KubicEntityCompactEntry(int level) implements IKubicEntity{
        @Override
        public int getLevel() {
            return level;
        }
    }
}
