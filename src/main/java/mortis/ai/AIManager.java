package mortis.ai;
import java.io.FileNotFoundException;
import java.util.List;
import mortis.speech.*;
public class AIManager {
    private GroqClient client;
    private SttBridge sttBridge;
    private TtsBridge ttsBridge;

    public AIManager(SttBridge sttBridge, TtsBridge ttsBridge) {
        this.client = new GroqClient();
        this.sttBridge = sttBridge;
        this.ttsBridge = ttsBridge;
    }

    public String getData(ActivityMemory recentMemory, List<ActivityRecord> memoryData, Memory memory) throws FileNotFoundException, Exception {
        List<String> knownFiles = List.of("google.txt", "report.docx", "notes.md");
        List<String> knownApps = List.of("discord");

        String transcript = sttBridge.listenOnce();
        System.out.println("transcript: " + transcript);

        if (transcript == null || transcript.isBlank() || transcript.equals("no input")) {
            System.out.println("No input detected, ending session.");
            try {
                ttsBridge.speak("Call me again if you need anything");
            } catch (Exception e) {
            }
            return null;
        }
        List<ActivityRecord> relevantMatches = memory.searchRelevant(transcript, 5); 

        return this.client.ask(transcript, knownFiles, knownApps, recentMemory, memoryData, relevantMatches);
    }

    public String getData(ActivityMemory recentMemory, List<ActivityRecord> memoryData, Memory memory, String transcript) throws Exception {
        List<String> knownFiles = List.of("google.txt", "report.docx", "notes.md");
        List<String> knownApps = List.of("discord");
        System.out.println("transcript: " + transcript);

        if (transcript == null || transcript.isBlank() || transcript.equals("no input")) {
            System.out.println("No input detected, ending session.");
            try {
                ttsBridge.speak("Call me again if you need anything");
            } catch (Exception e) {
            }
            return null;
        }
        List<ActivityRecord> relevantMatches = memory.searchRelevant(transcript, 5); 

        return this.client.ask(transcript, knownFiles, knownApps, recentMemory, memoryData, relevantMatches);
    }
}