package io.dkozak.jobscheduler.addpersonview;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.EditedPersonService;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
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
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

@Log4j
public class AddPersonPresenter implements Initializable {
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
            showEmptyMessageFor("Login");
            return;
        } else if (firstName.isEmpty()) {
            showEmptyMessageFor("First name");
            return;
        } else if (lastName.isEmpty()) {
            showEmptyMessageFor("last name");
            return;
        }

        try {
            Person person = new Person(login, firstName, lastName);
            if (isEdit)
                personDao.update(person);
            else
                personDao.save(person);
            closeWindow(event);
        } catch (SQLException e) {
            infoText.setFill(Color.RED);
            infoText.setText(String.format("Cannot save new person, because: %d %s ", e.getErrorCode(), e.getMessage()));
        }
    }

    private void showEmptyMessageFor(String field) {
        infoText.setFill(Color.RED);
        infoText.setText(String.format("Please fill in the '%s' field", field));
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
}
