package mortis.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import mortis.Mortis;
import mortis.ai.ActivityMemory;
import mortis.ai.ActivityRecord;
import mortis.ai.AIManager;
import mortis.ai.Memory;
import mortis.core.CommandRouter;
import mortis.speech.TtsBridge;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class TextModeWindow extends Application {

    private static AIManager manager;
    private static CommandRouter router;
    private static ObjectMapper mapper;
    private static TtsBridge ttsBridge;
    private static List<ActivityRecord> memoryData;
    private static Memory memory;

    public static void launchWindow(AIManager m, CommandRouter r, ObjectMapper map,
                                     TtsBridge tts, List<ActivityRecord> data, Memory mem) {
        manager = m;
        router = r;
        mapper = map;
        ttsBridge = tts;
        memoryData = data;
        memory = mem;
        Application.launch(TextModeWindow.class);
    }

    @Override
    public void start(Stage stage) {
        ActivityMemory sessionMemory = new ActivityMemory();

        TextField input = new TextField();
        input.setPromptText("Mortis is listening");

        Button send = new Button("Send");
        Label status = new Label("");

        Runnable submit = () -> {
            String text = input.getText().trim();
            if (text.isBlank()) return;
            input.clear();
            status.setText("Thinking...");

            new Thread(() -> {
                try {
                    String result = Mortis.processTextInput(text, manager, router, mapper, sessionMemory, memoryData, memory);
                    Platform.runLater(() -> status.setText(result != null ? result : "No response"));
                } catch (Exception ex) {
                    Platform.runLater(() -> status.setText("Error: " + ex.getMessage()));
                }
            }).start();
        };

        send.setOnAction(e -> submit.run());
        input.setOnAction(e -> submit.run());

        VBox root = new VBox(10, input, send, status);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 500, 300);
        stage.setTitle("Mortis - Text Mode");
        stage.setScene(scene);
        stage.show();
    }
}