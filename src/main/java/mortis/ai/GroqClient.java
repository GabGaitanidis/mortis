package mortis.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import mortis.utils.Env;

public class GroqClient {

    private static final String API_KEY = Env.get("GROQ_API_KEY", "");
    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient client = HttpClient.newHttpClient();

    private final String systemPrompt =
            """
            You are Mortis, a command-generation engine for a Java desktop assistant.

            Your ONLY job is to convert user input into valid JSON.

            RULES:
            - Output ONLY JSON
            - No markdown, no text, no explanations
            - Must be valid JSON

            FORMAT:
            The model must always output a JSON array of operation objects (even for a single operation).
            Example output:
            [
              {
                "activityName": "create_main_py",
                "module": "file",
                "action": "create",
                "params": { "path": "python/main.py" }
              },
              {
                "activityName": "open_docs",
                "module": "browser",
                "action": "open",
                "params": { "url": "https://docs.python.org/3/" }
              }
            ]

            MODULES:
            - file: create, read, write, delete
            - browser: open
            - memory: store, recall
            - system: open_app, shutdown, volume
            - question: answer
            - unknown (when unclear) 
            MODULES:
            - file:
            - write: params MUST include "path" (string) and "content" (string, use "" if no content was specified, and if content == "" then use create action)
            - read: params MUST include "path"
            - delete: params MUST include "path"
            DISAMBIGUATION:
            If the user's target name matches an entry in KNOWN_FILES, use module "file".
            If it matches KNOWN_APPS, use the appropriate module.
            Otherwise, if it looks like a website, brand, or search term, use module "browser".
            For each call, provide a valid activityName describing what the user wants to do.

            DISAMBIGUATION
            If module is question, then return a JSON array containing one operation object with the final answer in params.answer.
            Do not add a separate question field. 
            - Do NOT escape single quotes with a backslash. Only use standard JSON escapes (\", \\, \n, \t). Never output \'.
            Example:
            [
              {
                "activityName": "answer_question",
                "module": "question",
                "action": "answer",
                "params": { "answer": "The answer is 121." }
              }
            ]
            
            FAIL SAFE:
            If unclear, return a single-element array with a memory store operation, for example:
            [ { "activityName": "none", "module": "unknown", "action": "none", "params": { "key": "unclassified_input", "value": "<user input>" } } ]
            """;

    public String ask(String userInput, List<String> knownFiles, List<String> knownApps) throws Exception {
      if (API_KEY == null || API_KEY.isBlank()) {
        System.out.println("No key");
        return fallbackResponse(userInput);
      }
        String context = "KNOWN_FILES: " + knownFiles
          + "\nKNOWN_APPS: " + knownApps;

        JsonObject payload = new JsonObject();
        payload.addProperty("model", "llama-3.3-70b-versatile");
        payload.addProperty("temperature", 0.2);

        JsonArray messages = new JsonArray();
        messages.add(message("system", systemPrompt + "\n" + context));
        messages.add(message("user", userInput));
        payload.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
          return fallbackResponse(userInput);
        }

        try {
          return extractContent(response.body());
        } catch (RuntimeException ex) {
          return fallbackResponse(userInput);
        }
    }

    private JsonObject message(String role, String content) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        m.addProperty("content", content);
        return m;
    }

    private String extractContent(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

        if (root.has("error")) {
            throw new RuntimeException("Groq API error: " + root.get("error"));
        }
        String result = root.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
        return result;
    }

    private String fallbackResponse(String userInput) {
      JsonObject root = new JsonObject();
      root.addProperty("activityName", "web_search");
      root.addProperty("module", "browser");
      root.addProperty("action", "open");
      JsonObject params = new JsonObject();
      String target = userInput == null || userInput.isBlank()
          ? "https://www.youtube.com"
          : userInput.trim();
      if (!target.startsWith("http://") && !target.startsWith("https://")) {
        target = "https://www.youtube.com/search?q=" + target.replace(" ", "+");
      }
      params.addProperty("url", target);
      root.add("params", params);
      JsonArray arr = new JsonArray();
      arr.add(root);
      return arr.toString();
    }
}