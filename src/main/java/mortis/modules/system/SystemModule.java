package mortis.modules.system;

import java.io.IOException;

import mortis.core.Command;
import mortis.core.Module;
import mortis.speech.TtsBridge;

public class SystemModule implements Module{
    @Override 
    public void execute(Command command, TtsBridge ttsBridge) throws IOException, InterruptedException {
        switch (command.getAction()) {
            case "open_app":
                SystemHandler.executeCommand(command.get("command").toString());
                break;
        
            default:
                break;
        }
    }
}
