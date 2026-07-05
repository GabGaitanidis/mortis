package mortis.modules.question;

import mortis.core.Command;
import mortis.core.Module;
import mortis.modules.file.FileHandler;
import mortis.utils.Env;
import java.nio.file.Path;

public class QuestionModule implements Module {
    @Override
    public void execute(Command command) {
        FileHandler fileHandler = new FileHandler(Env.get("MORTIS_ANSWER_TEXT_PATH", Path.of(System.getProperty("user.home"), "mortisAnswer.txt").toString()));
        Object answer = command.get("answer");
        Object question = command.get("question");

        if (answer != null) {
            fileHandler.writeFile(answer.toString());
        } else if (question != null) {
            fileHandler.writeFile("I don't know the answer to: " + question.toString());
        } else {
            fileHandler.writeFile("I cannot answer that question");
        }
    }
}