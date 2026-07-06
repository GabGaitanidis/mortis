package mortis.modules.browser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import mortis.core.Module;
import mortis.core.*;
import mortis.speech.TtsBridge;
public class BroswerModule implements Module{
     @Override
     public void execute(Command command) throws IOException, URISyntaxException {
        BrowserHandler handler = new BrowserHandler((String) command.get("url"));

        switch (command.getAction()) {
            case "open":
                try {
                    handler.openWindow();
                    String host = URI.create(command.get("url").toString()).getHost();
                    if (host.startsWith("www.")) host = host.substring(4);
                    int dot = host.lastIndexOf('.');
                    if (dot > 0) host = host.substring(0, dot);
                    speak("Opening " + host);
                } catch (Exception e) {
                    speak("I cannot open that URL");
                }
                break;
        
            default:
                break;
        }
     }

    
     
}
