package io.dkozak.jobscheduler;

import io.dkozak.jobscheduler.mainview.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import lombok.val;

@Log4j
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.debug("start()");
        val mainView = new MainView();
        val scene = new Scene(mainView.getView());

        primaryStage.setTitle("Byl jeden pan, ten kozla mel, velice si, s nim rozumel");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
