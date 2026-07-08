package mortis.modules.unknown;

import mortis.core.Command;
import mortis.core.Module;
import mortis.speech.TtsBridge;

public class UnKnownModule implements Module{
    @Override
    public void execute(Command command, TtsBridge ttsBridge) {
        speak("Sorry I didnt catch that", ttsBridge);
    }
}
