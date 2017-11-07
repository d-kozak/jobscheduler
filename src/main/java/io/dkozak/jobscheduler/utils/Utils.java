package io.dkozak.jobscheduler.utils;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Utils {

    private Utils() {
    }

    public static void openModalDialog(Stage primaryStage, String title, FXMLView fxmlView) {
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlView.getView());
        stage.setTitle(title);
        stage.setScene(scene);

        // make the dialog modal
        stage.initOwner(primaryStage.getOwner());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
