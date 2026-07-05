package mortis.core;
import java.io.IOException;
import java.net.URISyntaxException;

public interface Module  {
    void execute(Command command) throws IOException, URISyntaxException;
} 
