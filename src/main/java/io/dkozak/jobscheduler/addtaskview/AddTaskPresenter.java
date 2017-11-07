package io.dkozak.jobscheduler.addtaskview;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.entity.Task;
import io.dkozak.jobscheduler.services.EditedTaskService;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
import io.dkozak.jobscheduler.services.database.dao.TaskDao;
import io.dkozak.jobscheduler.utils.NotifiablePresenter;
import io.dkozak.jobscheduler.utils.PersonStringConverter;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j;
import lombok.val;

import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static io.dkozak.jobscheduler.utils.Utils.closeWindow;

@Log4j
public class AddTaskPresenter implements Initializable, NotifiablePresenter {
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField nameTextField;
    @FXML
    private ChoiceBox<Person> assignedPersonChoiceBox;
    @FXML
    private Text infoText;

    @Inject
    private EditedTaskService editedTaskService;

    @Inject
    private PersonDao personDao;

    @Inject
    private TaskDao taskDao;

    private Task backingTask;

    private boolean isEdit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assignedPersonChoiceBox.converterProperty()
                               .setValue(new PersonStringConverter());

        Optional<Task> editedTask = editedTaskService.getEditedTask();
        if (editedTask.isPresent()) {
            backingTask = editedTask.get();
            isEdit = true;
        } else {
            backingTask = new Task();
            isEdit = false;
        }

        javafx.concurrent.Task<ObservableList<Person>> allPeople = personDao.findAll();

        allPeople.setOnSucceeded(event -> {
            val people = allPeople.getValue();
            log.info("loaded list of people: " + people);
            assignedPersonChoiceBox.setItems(people);
            if (isEdit)
                assignedPersonChoiceBox.getSelectionModel()
                                       .select(backingTask.getAssignedPerson());
        });
        allPeople.setOnFailed(event -> {
            Throwable cause = allPeople.exceptionProperty()
                                       .get();
            val msg = "Cannot load list of people, cause: " + cause.getMessage();
            log.error(msg);
            showErrorMessage(msg);
        });


        nameTextField.textProperty()
                     .bindBidirectional(backingTask.nameProperty());
        descriptionTextArea.textProperty()
                           .bindBidirectional(backingTask.descriptionProperty());
        assignedPersonChoiceBox.valueProperty()
                               .bindBidirectional(backingTask.assignedPersonProperty());
    }

    @FXML
    public void onAdd(ActionEvent event) {
        if (backingTask.getName() == null || backingTask.getName()
                                                        .isEmpty()) {
            showErrorMessage("Please fill in the name");
            return;
        } else if (backingTask.getAssignedPerson() == null) {
            showErrorMessage("Please select assigned person");
            return;
        }


        javafx.concurrent.Task<Void> task;
        if (isEdit) {
            task = taskDao.update(this.backingTask);
        } else {
            task = taskDao.save(this.backingTask);
        }
        task.setOnSucceeded(event1 -> {
            log.info("Operation successful, window will be closed soon");
            closeWindow(event);
        });
        task.setOnFailed(event1 -> {
            showErrorMessage("Cannot save task, reason: " + task.exceptionProperty()
                                                                .get()
                                                                .getMessage());
        });
    }

    @FXML
    public void onCancel(ActionEvent event) {
        closeWindow(event);
    }

    @Override
    public void showInfoMessage(String message) {
        log.info("showing info message: " + message);
        infoText.setFill(Color.BLACK);
        infoText.setText(message);
    }

    @Override
    public void showErrorMessage(String message) {
        log.error("showing error message: " + message);
        infoText.setFill(Color.RED);
        infoText.setText(message);
    }
}
