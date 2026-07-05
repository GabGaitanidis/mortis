package mortis.modules.browser;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
public class BrowserHandler {
    private String url;

    public BrowserHandler(String url) {
        this.url = url;
    }

    public void openWindow() throws IOException, URISyntaxException{
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(this.url));
        }
    }
    
}
