package mortis.core;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import mortis.modules.browser.*;
import mortis.modules.calendar.CalendarModule;
import mortis.modules.file.*;
import mortis.modules.memory.MemoryModule;
import mortis.modules.question.QuestionModule;
import mortis.modules.unknown.UnKnownModule;
import mortis.modules.user.UserFactsModule;
import mortis.speech.TtsBridge;
public class CommandRouter {
    private Map<String, Module> modules =  new HashMap<>();
    private TtsBridge ttsBridge;
    public CommandRouter(TtsBridge ttsBridge) throws IOException {
        modules.put("file", new FileModule());
        modules.put("browser", new BroswerModule());
        modules.put("question", new QuestionModule());
        modules.put("unknown", new UnKnownModule());
        modules.put("memory", new MemoryModule());
        modules.put("calendar", new CalendarModule());
        modules.put("user", new UserFactsModule());
        this.ttsBridge = ttsBridge;
    }

    public void Route(Command command) throws IOException, URISyntaxException {
        Module module = modules.get(command.getModule());
        if (module != null) {
            module.execute(command, ttsBridge);
        } else {
            System.out.println("unknown module");
        }
    }
}

