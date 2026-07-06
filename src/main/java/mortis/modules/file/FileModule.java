package mortis.modules.file;
import mortis.core.Module;

import java.io.IOException;

import mortis.core.*;
import java.nio.file.Path;
import mortis.speech.TtsBridge;

public class FileModule implements Module{

    @Override
    public void execute(Command command) throws IOException {
        Object path = command.get("path");
        if (path == "") {
            path = "Desktop";
        }
        String fileRoot = Path.of(System.getProperty("user.home"), "Desktop").toString();
        FileHandler handler = new FileHandler(fileRoot + "/" + command.get("path")); 
        switch (command.getAction()) {
            case "create":
                try {
                    handler.createFile();
                    speak("File created");
                } catch(IOException e ) {
                    speak("I cannot create the file");
                }
                break;
            case "read":
                try {
                    String content = handler.readFile();
                    System.out.println(content);
                    speak("File contents: " + content);
                } catch (Exception e) {
                    speak("I cannot read the file");
                }
                break;

            case "write":
                try {
                    handler.writeFile((String) command.get("content"));
                    speak("File written successfully");
                } catch (Exception e) {
                    speak("I cannot write to the file");
                }
                break;

            case "delete":
                try {
                    handler.deleteFile();
                    speak("File deleted successfully");
                } catch (Exception e) {
                    speak("I cannot delete the file");
                }
                break;
        }
    }

}