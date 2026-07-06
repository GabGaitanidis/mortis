package mortis.modules.unknown;

import mortis.core.Command;
import mortis.core.Module;

public class UnKnownModule implements Module{
    @Override
    public void execute(Command command) {
        speak("Sorry I didnt catch that");
    }
}
