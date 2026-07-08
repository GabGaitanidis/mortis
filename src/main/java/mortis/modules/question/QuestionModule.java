package mortis.modules.question;

import mortis.core.Command;
import mortis.core.Module;
import mortis.speech.TtsBridge;

public class QuestionModule implements Module {
    @Override
    public void execute(Command command, TtsBridge ttsBridge) {
        Object answer = command.get("answer");
        Object question = command.get("question");

        if (answer != null) {
            speak(answer.toString(), ttsBridge);
        } else if (question != null) {
            speak("I don't know the answer to: " + question.toString(), ttsBridge);
        } else {
            speak("I cannot answer that question", ttsBridge);
        }
    }

    
}