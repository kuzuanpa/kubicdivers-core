package cn.kuzuanpa.kubicgen.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "kubicdivers_core", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MissionTemplateManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    // 存储加载进内存的模板
    private static final Map<String, MissionTemplate> TEMPLATES = new HashMap<>();

    /**
     * 任务模板：定义任务类型 -> 结构映射 + 目标定义。
     *
     * goal_definitions 是数据包作者的核心入口：
     * 通过 JSON 声明 "结构方块标记 marker_id 对应的方块应该是什么"，
     * 以及 "完成条件是什么"，无需编写 Java 代码即可创建新任务目标。
     *
     * 示例 goal_definition:
     * {
     *   "marker_id": "mission_target:aa_gun",
     *   "handler": "kubicdivers_core:generic_destroy",
     *   "config": {
     *     "display_name": "Destroy AA Gun",
     *     "target_block": "minecraft:iron_block",
     *     "marker_id": "mission_target:aa_gun"
     *   }
     * }
     */
    public record MissionTemplate(
            String environmentType,
            List<ResourceLocation> mains,
            List<ResourceLocation> subs,
            List<ResourceLocation> fillers,
            List<GoalDefinition> goalDefinitions
    ) {}

    /**
     * 单个目标定义：描述结构中某个标记对应什么任务目标。
     * 数据包作者通过此定义，无需 Java 代码即可创建新任务。
     */
    public record GoalDefinition(
            String markerId,        // 匹配结构方块中的 metadata，如 "mission_target:aa_gun"
            ResourceLocation handler, // 目标处理器工厂 ID，如 "kubicdivers_core:generic_destroy"
            JsonObject config       // 传给工厂的配置，如 { "display_name": "...", "target_block": "..." }
    ) {}

    public MissionTemplateManager() {
        super(GSON, "mission_templates"); // 对应 data/<modid>/mission_templates 文件夹
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, ResourceManager manager, ProfilerFiller profiler) {
        TEMPLATES.clear();
        objectMap.forEach((rl, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                String type = json.get("mission_type").getAsString();

                String envType = json.has("environment_type") ? json.get("environment_type").getAsString() : "";
                List<ResourceLocation> mains = parseList(json, "main_structures");
                List<ResourceLocation> subs = parseList(json, "sub_structures");
                List<ResourceLocation> fillers = parseList(json, "terrain_fillers");
                List<GoalDefinition> goalDefs = parseGoalDefinitions(json);

                TEMPLATES.put(type, new MissionTemplate(envType, mains, subs, fillers, goalDefs));
            } catch (Exception e) {
                System.err.println("Failed to parse mission template: " + rl);
                e.printStackTrace();
            }
        });
        System.out.println("Loaded " + TEMPLATES.size() + " mission templates.");
    }

    private List<ResourceLocation> parseList(JsonObject json, String key) {
        List<ResourceLocation> list = new ArrayList<>();
        if (json.has(key)) {
            json.getAsJsonArray(key).forEach(e -> list.add(new ResourceLocation(e.getAsString())));
        }
        return list;
    }

    private List<GoalDefinition> parseGoalDefinitions(JsonObject json) {
        List<GoalDefinition> list = new ArrayList<>();
        if (json.has("goal_definitions")) {
            json.getAsJsonArray("goal_definitions").forEach(e -> {
                JsonObject def = e.getAsJsonObject();
                String markerId = def.get("marker_id").getAsString();
                ResourceLocation handler = new ResourceLocation(def.get("handler").getAsString());
                JsonObject config = def.has("config") ? def.getAsJsonObject("config") : new JsonObject();
                list.add(new GoalDefinition(markerId, handler, config));
            });
        }
        return list;
    }

    public static MissionTemplate getTemplate(String missionType) {
        return TEMPLATES.getOrDefault(missionType, new MissionTemplate("", List.of(), List.of(), List.of(), List.of()));
    }

    /** 获取所有已加载的任务模板 */
    public static Map<String, MissionTemplate> getAllTemplates() {
        return java.util.Collections.unmodifiableMap(TEMPLATES);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MissionTemplateManager());
    }
}