package io.dkozak.jobscheduler;

import com.airhacks.afterburner.injection.Injector;
import io.dkozak.jobscheduler.mainview.MainView;
import io.dkozak.jobscheduler.services.database.DatabaseConnector;
import io.dkozak.jobscheduler.services.database.dao.DaoController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.val;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private static Logger log = initLog4j();

    @Inject
    private DatabaseConnector databaseConnector;

    // small hack to perform DI in daoController
    @Inject
    private DaoController daoController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Application starting");
        initInjector(primaryStage);
        initLog4j();
        val mainView = new MainView();
        val scene = new Scene(mainView.getView());
        primaryStage.setTitle("Job scheduler");
        primaryStage.setScene(scene);
        primaryStage.show();
        log.info("Stage shown");
    }

    /**
     * Makes sure that the logger is initialized before
     * '-Dlog4j.configuration=io/dkozak/jobscheduler/log4j.properties'
     * it is probably a better way, because then you can use @Log4j annotation
     * but I want to do this in code, otherwise '-D...' would be necessary when executing the jar
     *
     * @return logger for this class
     */
    private static Logger initLog4j() {
        val url = Main.class.getResource("log4j.properties");
        PropertyConfigurator.configure(url);
        return Logger.getLogger(Main.class);
    }

    @Override
    public void stop() throws Exception {
        log.info("preparing to close the app");

        databaseConnector.close();

        log.info("closing the dao executor service");

        DaoController.daoExecutorService.shutdown();
        try {
            DaoController.daoExecutorService.awaitTermination(1, TimeUnit.SECONDS);
            log.info("dao executor terminated successfully");
        } catch (InterruptedException ex) {
            log.error("interrupted while waiting for the dao executor to end, now it will be shut down with force");
            DaoController.daoExecutorService.shutdownNow();
        }
        log.info("shutting down the app... Goodbye :)");
    }

    private void initInjector(Stage primaryStage) {
        try {
            log.info("loading the configuration file for DI");
            Logger logger = Logger.getLogger(Injector.class);
            Injector.setLogger(logger::info);

            Map<Object, Object> map = loadGlobalConfiguration();
            // add other objects for injection if needed
            // ...
            map.put("primaryStage", primaryStage);
            Injector.setConfigurationSource(map::get);

            // inject fields in this class as well
            Injector.injectMembers(getClass(), this);


        } catch (IOException ex) {
            log.error("Could not load configuration file: " + ex.getMessage());
        }
    }

    private Map<Object, Object> loadGlobalConfiguration() throws IOException {
        return loadGlobalConfiguration("application");
    }

    private Map<Object, Object> loadGlobalConfiguration(String fileName) throws IOException {
        val map = new HashMap<Object, Object>();
        val fileExtensions = ".properties";
        if (!fileName.endsWith(fileExtensions)) {
            fileName += ".properties";
        }

        try (val reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                String[] split = line.split("=");
                if (split.length != 2) {
                    log.error("Line '" + line + "' is in incorrect format");
                    continue;
                }
                log.info("'" + split[0] + "' loaded for DI");
                map.put(split[0], split[1]);
            }
            return map;
        }
    }
}
