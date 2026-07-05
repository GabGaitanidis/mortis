package mortis.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class mainWindow extends Application {
    
    @Override
    public void start(Stage stage) throws IOException { 
        TextField textField = new TextField();
        Label label = new Label("Mortis listening");
            
    }

    public static void main(String[] args) {
        launch(args);
    }
}

