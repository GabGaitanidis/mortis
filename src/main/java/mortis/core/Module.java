package mortis.core;
import java.io.IOException;
import java.net.URISyntaxException;

import mortis.speech.TtsBridge;

public interface Module  {
    void execute(Command command, TtsBridge ttsBridge) throws IOException, URISyntaxException, InterruptedException;
    default void speak(String text, TtsBridge ttsBridge) {
        try {
            ttsBridge.speak(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
