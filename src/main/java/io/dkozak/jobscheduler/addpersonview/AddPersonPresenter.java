package io.dkozak.jobscheduler.addpersonview;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.EditedPersonService;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
import io.dkozak.jobscheduler.utils.NotifiablePresenter;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Log4j
public class AddPersonPresenter implements Initializable, NotifiablePresenter {
    @FXML
    private TextField login;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;

    @FXML
    private Text infoText;

    @FXML
    private Button addEditButton;

    @Inject
    private PersonDao personDao;

    @Inject
    private EditedPersonService editedPersonService;

    private boolean isEdit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Optional<Person> personOptional = editedPersonService.getEditedPerson();
        if (personOptional.isPresent()) {
            // editing mode
            Person person = personOptional.get();
            login.setText(person.getLogin());
            login.setEditable(false);
            firstName.setText(person.getFirstName());
            lastName.setText(person.getLastName());
            addEditButton.setText("Save");
            isEdit = true;
        } else {
            isEdit = false;
        }
    }

    @FXML
    public void onClick(ActionEvent event) {
        String login = this.login.getText();
        String firstName = this.firstName.getText();
        String lastName = this.lastName.getText();
        if (login.isEmpty()) {
            showEmptyFieldErrorMessageFor("Login");
            return;
        } else if (firstName.isEmpty()) {
            showEmptyFieldErrorMessageFor("First name");
            return;
        } else if (lastName.isEmpty()) {
            showEmptyFieldErrorMessageFor("last name");
            return;
        }


        Person person = new Person(login, firstName, lastName);
        Task<Void> task;
        if (isEdit)
            task = personDao.update(person);
        else
            task = personDao.save(person);

        task.setOnSucceeded(workerStateEvent -> {
            log.info("Task finished, window will be closed soon");
            closeWindow(event);
        });
        task.exceptionProperty()
            .addListener((observable, oldValue, newValue) -> showErrorMessage("Operation failed: " + newValue.getMessage()));

    }

    private void showEmptyFieldErrorMessageFor(String field) {
        showErrorMessage(String.format("Please fill in the '%s' field", field));
    }

    @FXML
    public void onCancel(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        ((Node) event.getSource()).getScene()
                                  .getWindow()
                                  .hide();
    }

    @Override
    public void showInfoMessage(String message) {
        log.info("Info message: " + message);
        infoText.setFill(Color.BLACK);
        infoText.setText(message);
    }

    @Override
    public void showErrorMessage(String message) {
        log.error("Error message: " + message);
        infoText.setFill(Color.RED);
        infoText.setText(message);
    }
}
