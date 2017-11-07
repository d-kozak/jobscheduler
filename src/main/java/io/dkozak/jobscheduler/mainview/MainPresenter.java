package io.dkozak.jobscheduler.mainview;

import io.dkozak.jobscheduler.personView.PersonView;
import io.dkozak.jobscheduler.services.MessageService;
import io.dkozak.jobscheduler.services.database.dao.DaoManager;
import io.dkozak.jobscheduler.taskview.TaskView;
import io.dkozak.jobscheduler.utils.NotifiablePresenter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;


@Log4j
public class MainPresenter implements Initializable, NotifiablePresenter {

    @FXML
    private Tab peopleTab;

    @FXML
    private Tab tasksTab;

    @FXML
    private Text infoText;

    @Inject
    private DaoManager daoManager;

    @Inject
    private MessageService messageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("initializing main view");

        daoManager.getPrepareDatabaseTask()
                  .setOnSucceeded(event -> showInfoMessage("Database initialized successfully"));

        daoManager.getPrepareDatabaseTask()
                  .exceptionProperty()
                  .addListener((observable, oldValue, newValue) -> showErrorMessage("Database initialization failed :" + newValue.getMessage()));

        PersonView personView = new PersonView();
        peopleTab.setContent(personView.getView());
        TaskView taskView = new TaskView();
        tasksTab.setContent(taskView.getView());

        messageService.setMessageDestination(infoText);
    }

    @Override
    public void showInfoMessage(String message) {
        log.info("Info message: " + message);
        messageService.infoMessage(message);
    }

    @Override
    public void showErrorMessage(String message) {
        log.error("Error message: " + message);
        messageService.errorMessage(message);
    }
}
