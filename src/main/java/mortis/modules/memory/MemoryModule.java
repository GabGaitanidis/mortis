package mortis.modules.memory;

import java.io.IOException;
import java.util.Map;

import mortis.ai.ActivityRecord;
import mortis.core.Command;
import mortis.core.Module;
import mortis.speech.TtsBridge;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MemoryModule implements Module{
    private final MemoryHandler handler;
    public MemoryModule() {
        this.handler = new MemoryHandler();
    }
    private void recall(Map<String, Object> params, TtsBridge ttsBridge) throws IOException {
        String activityName = params.get("activityName").toString();

        String field = params.getOrDefault("field", "all").toString();
        if (activityName == null || activityName.isBlank()) {
            ttsBridge.speak("I need to know what you're asking about.");
            return;
        }
        ActivityRecord record = handler.searchActivity(activityName);
        if (record == null) {
            ttsBridge.speak("I don't have anything on that.");
            return;
        }
        String answer = formatField(record, field);
        speak(answer, ttsBridge);
    }

     private String formatField(ActivityRecord record, String field) {
        return switch (field) {
            case "target" -> record.target() != null ? record.target() : "There's no target stored for that.";
            case "timestamp" -> "That happened at " + formatTimestamp(record);
            case "action" -> "The action was " + record.action();
            case "all" -> String.format("%s: %s.%s targeting %s at %s",
                    record.activityName(), record.module(), record.action(), record.target(), formatTimestamp(record));
            default -> "I'm not sure what to tell you about that.";
        };
    }

    private String formatTimestamp(ActivityRecord record) {
        LocalDateTime dt = LocalDateTime.parse(record.timestamp());
        return DateTimeFormatter.ofPattern("MMMM d 'at' h:mm a").format(dt);
    }
    @Override
    public void execute(Command command, TtsBridge ttsBridge) throws IOException {
        switch (command.getAction()) {
            case "recall":
                recall(command.getParams(), ttsBridge);
                break;
            case "store":
                speak("I will remember that", ttsBridge);
                break;
            default:
                break;
        }
    }
}
