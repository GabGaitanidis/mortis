package mortis.core;
import java.io.IOException;
import java.net.URISyntaxException;

import mortis.speech.TtsBridge;

public interface Module  {
    void execute(Command command) throws IOException, URISyntaxException;
    default void speak(String text) {
        try {
            new TtsBridge().speakOnce(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
