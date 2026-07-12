package mortis.modules.user;

import java.io.IOException;

import mortis.core.Command;
import mortis.core.Module;
import mortis.speech.TtsBridge;

public class UserFactsModule implements Module {
    private final UserFactsHandler handler;

    public UserFactsModule() throws IOException {
        this.handler = new UserFactsHandler();
    }

    @Override
    public void execute(Command command, TtsBridge ttsBridge) throws IOException {
        switch (command.getAction()) {
            case "get_fact":
                try {
                    String value = handler.get(command.get("key").toString());
                    if (value == null) {
                        speak("I don't have that stored.", ttsBridge);
                        return;
                    }
                    speak(value, ttsBridge);
                } catch (Exception e) {
                    speak("I cannot find that right now", ttsBridge);
                }
                break;

            case "set_fact":
                try {
                    handler.set(command.get("key").toString(), command.get("value").toString());
                    speak("Looks like I learned something", ttsBridge);
                } catch (Exception e) {
                    speak("Something came up check logs", ttsBridge);
                }
                break;

            case "forget_fact":
                try {
                    handler.forget(command.get("key").toString());
                    speak("I've forgotten that.", ttsBridge);
                } catch (Exception e) {
                    speak("Something came up check logs", ttsBridge);
                }
                break;

            default:
                speak("I don't know how to do that with your info.", ttsBridge);
                break;
        }
    }

    
}