package mortis;

import java.io.FileNotFoundException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mortis.ai.AIManager;
import mortis.core.Command;
import mortis.core.CommandRouter;
import mortis.core.ConvertToJson;
import mortis.speech.SttBridge;
import mortis.speech.TtsBridge;
import mortis.speech.WaitWakeWord;

public class Mortis {
    public static void main(String[] args) throws FileNotFoundException, Exception {
        WaitWakeWord waitWake = new WaitWakeWord();
        waitWake.start();
        TtsBridge ttsBridge = new TtsBridge();
        ttsBridge.start();
        SttBridge sttBridge = new SttBridge();
        AIManager manager = new AIManager(sttBridge, ttsBridge);
        CommandRouter router = new CommandRouter(ttsBridge);
        ObjectMapper mapper = new ObjectMapper();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("shutdown");
            try { waitWake.shutdown(); } catch (Exception e) { System.err.println("wake shutdown failed: " + e.getMessage()); }
            try { ttsBridge.shutdown(); } catch (Exception e) { System.err.println("tts shutdown failed: " + e.getMessage()); }
            try { sttBridge.shutdown(); } catch (Exception e) { System.err.println("stt shutdown failed: " + e.getMessage()); }
        }));

        
        while (true) {
            System.out.println("Waiting for wake word...");
            waitWake.waitForWakeWord();

            ttsBridge.speak("Hello sir! I'm listening");
            start(manager, router, mapper, ttsBridge);
        }
    }

    private static void start(AIManager manager, CommandRouter router, ObjectMapper mapper, TtsBridge ttsBridge) throws Exception {
        String data = manager.getData();

        while (data != null) {
            JsonNode jsonData = ConvertToJson.convertToJson(data);
            System.out.println(jsonData);

            if (jsonData == null) {
                return;
            } else if (jsonData.isArray()) {
                for (JsonNode item : jsonData) {
                    handleItem(item, router, manager, mapper);
                }
            } else {
                try {
                    handleItem(jsonData, router, manager, mapper);
                } catch (Exception e) {
                    
                    System.err.println("Command failed: " + e.getMessage());
                    ttsBridge.speak("Sorry, something went wrong with that.");
                }
            }
            data = manager.getData();
        }
    }

    private static void handleItem(JsonNode item, CommandRouter router, AIManager manager, ObjectMapper mapper) throws Exception {
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
        String activityName = item.get("activityName").asText();
        if ("file".equals(module) && item.get("params").has("path")) {
            String filePath = item.get("params").get("path").asText();
            manager.addRecentActivity(activityName, module, filePath);
        } else {
            manager.addRecentActivity(activityName, module);
        }
    }
}