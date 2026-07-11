package mortis.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
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

    Your ONLY job is to convert user input into a valid JSON array of operation objects.

    RULES:
    - Output ONLY JSON. No markdown, no code fences, no explanations, no preamble.
    - Output must always be a JSON array, even for a single operation.
    - Use only standard JSON escapes (\\", \\\\, \\n, \\t). Never escape a single quote with a backslash (\\').

    OUTPUT SHAPE:
    Each operation object has exactly these keys: "activityName", "module", "action", "params".
    Example:
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

    MULTI-STEP REQUESTS:
    A single voice command often implies multiple operations. Break these down into an ordered
    array of operations, executed top to bottom. Each operation must be fully self-contained —
    later operations should reference the concrete results of earlier ones (e.g. a folder path),
    not vague descriptions.

    Example — "create a folder called notes and inside it a file called todo.txt":
    [
      {
        "activityName": "create_notes_folder",
        "module": "file",
        "action": "create_folder",
        "params": { "path": "notes" }
      },
      {
        "activityName": "create_todo_file",
        "module": "file",
        "action": "create",
        "params": { "path": "notes/todo.txt" }
      }
    ]

    Example — "open discord and then search google for weather":
    [
      {
        "activityName": "open_discord",
        "module": "system",
        "action": "open_app",
        "params": { "name": "discord" }
      },
      {
        "activityName": "search_weather",
        "module": "browser",
        "action": "open",
        "params": { "url": "https://www.google.com/search?q=weather" }
      }
    ]

    When operations are nested (e.g. "X inside Y", "X inside the Y you just made"), build the
    "path" or "target" of the later operation using the earlier operation's own params — do not
    invent a separate, disconnected path.

    MODULES AND PARAMS:

    - file
      - create_folder: params MUST include "path" (string)
      - create: params MUST include "path" (string)
      - write: params MUST include "path" (string) and "content" (string; use "" if no content was specified — if content is "", use action "create" instead)
      - read: params MUST include "path" (string)
      - delete: params MUST include "path" (string)

    - browser
      - open: params MUST include "url" (string)

    - memory
      - recall: params MUST include "activityName" (string, the activity to look up) and "field" (string, one of: "target", "timestamp", "action", "all")
        When handling recall, search all-time memory and choose the best-matching activity based on the user's input.

    - system
      - open_app: params MUST include "name" (string)
      - shutdown: params may be empty
      - volume: params MUST include "level" (number) or "action" ("mute"/"unmute")
      - get_datetime: params may be empty. Use this when the user asks what day, date, or time it is.

    - question
      - answer: return a single-element array with the final answer in params.answer. Do not add a separate top-level question field.
        Example:
        [
          {
            "activityName": "answer_question",
            "module": "question",
            "action": "answer",
            "params": { "answer": "The answer is 121." }
          }
        ]
    - calendar
      - get_today_events: params may be empty
    - unknown
      - Use when the request is unclear or doesn't fit any other module.

    DISAMBIGUATION:
    - If the target name matches an entry in KNOWN_FILES, use module "file".
    - If it matches an entry in KNOWN_APPS, use module "system" with action "open_app".
    - Otherwise, if it looks like a website, brand, or search term, use module "browser".
    - Always provide a descriptive, snake_case "activityName" summarizing what the user wants to do.
    - If the user asks about something they did before (e.g. "when did I...", "what did I..."),
      answer directly using module "question" if PAST_SESSION_HISTORY already contains the answer.
      Use memory.recall only when the user wants to reuse or act on a past target (e.g. "open that
      file again") and you need the exact stored value.

    FAIL SAFE:
    If the request is unclear or doesn't match any module, return a single-element array like this:
    [
      {
        "activityName": "unclassified_input",
        "module": "unknown",
        "action": "none",
        "params": { "value": "<user input>" }
      }
    ]
    """;
    public String ask(String userInput, List<String> knownFiles, List<String> knownApps,
                   ActivityMemory recentMemory, List<ActivityRecord> memoryData,
                   List<ActivityRecord> relevantMatches) throws Exception {
    if (API_KEY == null || API_KEY.isBlank()) {
        System.out.println("No key");
        return fallbackResponse(userInput);
    }

    String historyContext = memoryData.stream()
        .map(r -> String.format("- %s (%s.%s) target=%s at %s", r.activityName(), r.module(), r.action(), r.target(), r.timestamp()))
        .collect(java.util.stream.Collectors.joining("\n"));

    String relevantContext = relevantMatches.stream()
        .map(r -> String.format("- %s (%s.%s) target=%s at %s", r.activityName(), r.module(), r.action(), r.target(), r.timestamp()))
        .collect(java.util.stream.Collectors.joining("\n"));

    String context = "KNOWN_FILES: " + knownFiles
        + "\nKNOWN_APPS: " + knownApps
        + "\nCURRENT_SESSION_ACTIVITIES:\n" + recentMemory.toPromptContext()
        + "\nRECENT_HISTORY (last few activities):\n" + historyContext
        + "\nRELEVANT_PAST_ACTIVITIES (matched to this request):\n" + relevantContext;
  

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

    HttpResponse<String> response;
    try {
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
        System.err.println("Groq request failed (network error): " + e.getMessage());
        return fallbackResponse(userInput);
    }

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