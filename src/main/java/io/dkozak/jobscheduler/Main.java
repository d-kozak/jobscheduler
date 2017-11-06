package io.dkozak.jobscheduler;

import com.airhacks.afterburner.injection.Injector;
import io.dkozak.jobscheduler.mainview.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import lombok.val;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Log4j
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.debug("start()");
        initInjector();
        val mainView = new MainView();
        val scene = new Scene(mainView.getView());
        primaryStage.setTitle("Byl jeden pan, ten kozla mel, velice si, s nim rozumel");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initInjector() {
        try {
            Map<Object, Object> map = loadGlobalConfiguration();
            // add objects for injection if needed
            // ...
            Injector.setConfigurationSource(map::get);
        } catch (IOException ex) {
            log.error("Could not load configuration file: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("Could not load configuration file: " + ex.getMessage());
        }
    }

    private Map<Object, Object> loadGlobalConfiguration() throws IOException {
        return loadGlobalConfiguration("application");
    }

    private Map<Object, Object> loadGlobalConfiguration(String fileName) throws IOException {
        try {
            val fileExtensions = ".properties";
            if (!fileName.endsWith(fileExtensions)) {
                fileName += ".properties";
            }
            val fileURI = getClass().getResource(fileName)
                                    .toURI();
            val lines = Files.lines(Paths.get(fileURI))
                             .map(line -> line.split("="))
                             .collect(toList());
            val map = new HashMap<Object, Object>();
            for (String[] line : lines) {
                if (line.length != 2) {
                    log.error("skipping invalid line format in " + fileName + ": " + Arrays.toString(line));
                    continue;
                }
                map.put(line[0], line[1]);
            }
            return map;
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI is generated from the file itself, it should be always correct");
        }
    }
}
