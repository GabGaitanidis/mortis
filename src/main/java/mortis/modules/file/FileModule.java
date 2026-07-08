package mortis.modules.file;
import mortis.core.Module;

import java.io.IOException;

import mortis.core.*;
import java.nio.file.Path;
import mortis.speech.TtsBridge;

public class FileModule implements Module{

    @Override
    public void execute(Command command, TtsBridge ttsBridge) throws IOException {
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
                    speak("File created", ttsBridge);
                } catch(IOException e ) {
                    speak("I cannot create the file", ttsBridge);
                }
                break;
            case "read":
                try {
                    String content = handler.readFile();
                    System.out.println(content);
                    speak("File contents: " + content, ttsBridge );
                } catch (Exception e) {
                    speak("I cannot read the file", ttsBridge);
                }
                break;

            case "write":
                try {
                    handler.writeFile((String) command.get("content"));
                    speak("File written successfully", ttsBridge);
                } catch (Exception e) {
                    speak("I cannot write to the file", ttsBridge);
                }
                break;

            case "delete":
                try {
                    handler.deleteFile();
                    speak("File deleted successfully", ttsBridge);
                } catch (Exception e) {
                    speak("I cannot delete the file", ttsBridge);
                }
                break;
        }
    }

}