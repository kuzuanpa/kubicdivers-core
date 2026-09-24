package cn.kuzuanpa.kubicdivers.api.goal;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GoalFactoryRegistry {
    private static final Map<ResourceLocation, IGoalFactory> FACTORIES = new HashMap<>();

    public static void register(ResourceLocation id, IGoalFactory factory) {
        FACTORIES.put(id, factory);
    }

    public static IGoalFactory get(ResourceLocation id) {
        return FACTORIES.get(id);
    }
}