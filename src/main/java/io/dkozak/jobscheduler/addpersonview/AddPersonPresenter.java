package io.dkozak.jobscheduler.addpersonview;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.sql.SQLException;

public class AddPersonPresenter {
    @FXML
    private TextField login;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;

    @FXML
    private Text infoText;

    @Inject
    private PersonDao personDao;

    @FXML
    public void onAdd(ActionEvent event) {
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

        Person person = new Person(login, firstName, lastName);
        try {
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
