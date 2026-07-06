package mortis.ai;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.HashMap;
import mortis.speech.*;
public class AIManager {
    private GroqClient client;
    private SttBridge sttBridge;
    private HashMap<String, String> recent;

    public AIManager() {
        this.client = new GroqClient();
        this.sttBridge = new SttBridge();
        this.recent = new HashMap<>();
    }

    public String getData() throws FileNotFoundException, Exception {
        List<String> knownFiles = List.of("google.txt", "report.docx", "notes.md");
        List<String> knownApps = List.of("discord");

        String transcript = sttBridge.listenOnce();
        System.out.println("transcript: " + transcript);

        if (transcript == null || transcript.isBlank() || transcript.equals("no input")) {
            System.out.println("No input detected, ending session.");
            return null;
        }

        return this.client.ask(transcript, knownFiles, knownApps, this.recent);
    }

    public void addRecentActivity(String activityName, String module, String filePath) {
        if ("file".equals(module) && filePath != null) {
            recent.put(activityName, filePath);
        } else {
            recent.put(activityName, module);
        }
    }

    public void addRecentActivity(String activityName, String module) {
        addRecentActivity(activityName, module, null);
    }
}