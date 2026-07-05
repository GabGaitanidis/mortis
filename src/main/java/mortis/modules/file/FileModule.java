package mortis.modules.file;
import mortis.core.Module;

import java.io.IOException;

import mortis.core.*;
import mortis.utils.Env;
import java.nio.file.Path;

public class FileModule implements Module{

    @Override
    public void execute(Command command) throws IOException {
        Object path = command.get("path");
        if (path == "") {
            path = "Desktop";
        }
        String fileRoot = Env.get("MORTIS_FILES_ROOT", Path.of(System.getProperty("user.home"), "Desktop").toString());
        String answerPath = Env.get("MORTIS_ANSWER_TEXT_PATH", Path.of(System.getProperty("user.home"), "mortisAnswer.txt").toString());
        FileHandler handler = new FileHandler(fileRoot + "/" + command.get("path")); 
        FileHandler answerHandler = new FileHandler(answerPath);
        switch (command.getAction()) {
            case "create":
                try {
                    handler.createFile();
                    answerHandler.writeFile("File created");
                } catch(IOException e ) {
                    answerHandler.writeFile("I cannot create the file");
                }
                break;
            case "read":
                try {
                    String content = handler.readFile();
                    System.out.println(content);
                    answerHandler.writeFile("File contents: " + content);
                } catch (Exception e) {
                    answerHandler.writeFile("I cannot read the file");
                }
                break;

            case "write":
                try {
                    handler.writeFile((String) command.get("content"));
                    answerHandler.writeFile("File written successfully");
                } catch (Exception e) {
                    answerHandler.writeFile("I cannot write to the file");
                }
                break;

            case "delete":
                try {
                    handler.deleteFile();
                    answerHandler.writeFile("File deleted successfully");
                } catch (Exception e) {
                    answerHandler.writeFile("I cannot delete the file");
                }
                break;
        }
    }
}