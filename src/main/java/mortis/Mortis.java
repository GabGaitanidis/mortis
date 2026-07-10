package mortis;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mortis.ai.AIManager;
import mortis.ai.ActivityMemory;
import mortis.ai.ActivityRecord;
import mortis.ai.Memory;
import mortis.core.Command;
import mortis.core.CommandRouter;
import mortis.core.ConvertToJson;
import mortis.speech.SttBridge;
import mortis.speech.TtsBridge;
import mortis.speech.WaitWakeWord;

public class Mortis {
    private static volatile boolean shuttingDown = false;

    public static void main(String[] args) throws FileNotFoundException, Exception {
        WaitWakeWord waitWake = new WaitWakeWord();
        waitWake.start();
        TtsBridge ttsBridge = new TtsBridge();
        ttsBridge.start();
        SttBridge sttBridge = new SttBridge();
        Memory memory = new Memory();
        List<ActivityRecord> memoryData = memory.getRecentRecords();
        AIManager manager = new AIManager(sttBridge, ttsBridge);
        CommandRouter router = new CommandRouter(ttsBridge);
        ObjectMapper mapper = new ObjectMapper();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("shutdown");
            shuttingDown = true;
            try { waitWake.shutdown(); } catch (Exception e) { System.err.println("wake shutdown failed: " + e.getMessage()); }
            try { ttsBridge.shutdown(); } catch (Exception e) { System.err.println("tts shutdown failed: " + e.getMessage()); }
            try { sttBridge.shutdown(); } catch (Exception e) { System.err.println("stt shutdown failed: " + e.getMessage()); }
        }));

        while (!shuttingDown) {
            System.out.println("Waiting for wake word...");
            try {
                waitWake.waitForWakeWord();
            } catch (Exception e) {
                if (shuttingDown) break;
                throw e;
            }
            if (shuttingDown) break;

            ttsBridge.speak("Hello sir! What can I do for you?");
            start(manager, router, mapper, ttsBridge, memoryData, memory);
        }
    }

    private static void start(AIManager manager, CommandRouter router, ObjectMapper mapper, TtsBridge ttsBridge, List<ActivityRecord> memoryData, Memory memory) throws Exception {
        ActivityMemory recentMemory = new ActivityMemory();
        String data = manager.getData(recentMemory, memoryData, memory);
        while (data != null && !shuttingDown) {
            JsonNode jsonData = ConvertToJson.convertToJson(data);
            System.out.println(jsonData);
            if (jsonData == null) {
                return;
            } else if (jsonData.isArray()) {
                for (JsonNode item : jsonData) {
                    try {
                        handleItem(item, router, manager, mapper, recentMemory, memory);
                    } catch (Exception e) {
                        System.err.println("Command failed: " + e.getMessage());
                        ttsBridge.speak("Sorry, something went wrong with that.");
                    }
                }
            } else {
                try {
                    handleItem(jsonData, router, manager, mapper, recentMemory, memory);
                } catch (Exception e) {
                    System.err.println("Command failed: " + e.getMessage());
                    ttsBridge.speak("Sorry, something went wrong with that.");
                }
            }
            if (shuttingDown) return;
            data = manager.getData(recentMemory, memoryData, memory);
        }
    }

    private static void handleItem(JsonNode item, CommandRouter router, AIManager manager, ObjectMapper mapper, ActivityMemory recentMemory, Memory memory) throws Exception {
        Map<String, Object> paramsMap = mapper.convertValue(
            item.get("params"),
            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
        );
        router.Route(new Command(
            item.get("module").asText(),
            item.get("action").asText(),
            paramsMap
        ));
        String module = item.get("module").asText();
        if (!"memory".equals(module)) {
            ActivityRecord record = convertToActivityRecord(item);
            recentMemory.add(record);
            memory.add(record);
        }
    }

    private static ActivityRecord convertToActivityRecord(JsonNode item) {
        String activityName = item.get("activityName").asText();
        String module = item.get("module").asText();
        String action = item.get("action").asText();
        JsonNode params = item.get("params");

        String target = extractTarget(module, params);
        String timestamp = java.time.LocalDateTime.now().toString();

        return new ActivityRecord(activityName, module, action, target, timestamp);
    }

    private static String extractTarget(String module, JsonNode params) {
        if (params == null || params.isNull()) {
            return null;
        }
        if (params.has("path")) {
            return params.get("path").asText();
        }
        if (params.has("url")) {
            return params.get("url").asText();
        }
        if (params.has("name")) {
            return params.get("name").asText();
        }
        if (params.has("answer")) {
            return params.get("answer").asText();
        }
        return params.toString();
    }
}