package mortis;

import java.io.FileNotFoundException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mortis.ai.AIManager;
import mortis.core.Command;
import mortis.core.CommandRouter;
import mortis.core.ConvertToJson;
import mortis.utils.*;
public class Mortis {
    public static void main(String args[]) throws FileNotFoundException, Exception {
        System.out.println("Hello sir, I'm listening!"); 
        JPython.welcomeMortis();
        AIManager manager = new AIManager();
        CommandRouter router = new CommandRouter();
        String result = JPython.getInputFromUser();
        while (result != null) {
                String data = manager.getData();
                JsonNode jsonData = ConvertToJson.convertToJson(data);
                ObjectMapper mapper = new ObjectMapper(); 
                System.out.println(jsonData);

                if (jsonData == null) {
                    // nothing to do
                } else if (jsonData.isArray()) {
                    for (JsonNode item : jsonData) {
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
                        JPython.mortisAnswer();
                    }
                } else {
                    Map<String, Object> paramsMap = mapper.convertValue(
                        jsonData.get("params"), 
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                    );
                    router.Route(new Command(
                        jsonData.get("module").asText(),
                        jsonData.get("action").asText(),
                        paramsMap
                    ));
                    String module = jsonData.get("module").asText();
                    String activityName = jsonData.get("activityName").asText();
                    if ("file".equals(module) && jsonData.get("params").has("path")) {
                        String filePath = jsonData.get("params").get("path").asText();
                        manager.addRecentActivity(activityName, module, filePath);
                    } else {
                        manager.addRecentActivity(activityName, module);
                    }
                    JPython.mortisAnswer();
                }
            result = JPython.getInputFromUser();

        }
      
    }
}