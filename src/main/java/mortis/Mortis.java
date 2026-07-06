package mortis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mortis.ai.AIManager;
import mortis.core.Command;
import mortis.core.CommandRouter;
import mortis.core.ConvertToJson;
import mortis.speech.TtsBridge;
import mortis.utils.WaitWakeWord;

public class Mortis {
    public static void main(String[] args) throws FileNotFoundException, Exception {
        WaitWakeWord wakeWord = new WaitWakeWord();
        TtsBridge ttsBridge = new TtsBridge();
        AIManager manager = new AIManager();
        CommandRouter router = new CommandRouter();
        ObjectMapper mapper = new ObjectMapper();
        
        
        while (true) {
            System.out.println("Waiting for wake word...");
            wakeWord.waitForWakeWord();

            ttsBridge.speakOnce("Hello sir! I'm listening");
            start(manager, router, mapper);
        }
    }

    private static void start(AIManager manager, CommandRouter router, ObjectMapper mapper) throws Exception {
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
                    TtsBridge ttsBridge = new TtsBridge();
                    System.err.println("Command failed: " + e.getMessage());
                    ttsBridge.speakOnce("Sorry, something went wrong with that.");
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