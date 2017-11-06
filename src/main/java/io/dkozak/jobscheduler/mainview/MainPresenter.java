package io.dkozak.jobscheduler.mainview;

import com.airhacks.afterburner.views.FXMLView;
import io.dkozak.jobscheduler.addpersonview.AddPersonView;
import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.EditedPersonService;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;


@Log4j
public class MainPresenter implements Initializable {

    @FXML
    private TableView<Person> tableView;

    @FXML
    private Text infoText;

    @Inject
    private Stage primaryStage;

    @Inject
    private PersonDao personDao;

    @Inject
    private EditedPersonService editedPersonService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("initializing main view");

        initTable();
    }

    private void onTableChanged(TableColumn.CellEditEvent<Person, String> event, BiConsumer<Person, String> setter) {
        Person person = event.getTableView()
                             .getItems()
                             .get(event.getTablePosition()
                                       .getRow());
        setter.accept(person, event.getNewValue());
        person.setFirstName(event.getNewValue());


        Task<Void> task = personDao.update(person);
        task.setOnSucceeded(event1 -> {
            showInfoMessage("Update finished");
        });
        task.exceptionProperty()
            .addListener((observable, oldValue, newValue) -> showErrorMessage("Cannot update: " + newValue.getMessage()));

    }

    private void initTable() {
        tableView.setEditable(true);


        TableColumn<Person, String> loginColumn = new TableColumn<>("Login");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        loginColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Person, String> firstNameColumn = new TableColumn<>("First name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameColumn.setOnEditCommit(event -> {
            onTableChanged(event, Person::setFirstName);
        });
        firstNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameColumn.setOnEditCommit(event -> {
            onTableChanged(event, Person::setLastName);
        });
        lastNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        //noinspection unchecked
        tableView.getColumns()
                 .addAll(loginColumn, firstNameColumn, lastNameColumn);

        loadDataIntoTable(true);
    }

    private void loadDataIntoTable(boolean showMessageOnFinished) {
        log.info("loading data into the table");

        Task<ObservableList<Person>> task = personDao.findALl();
        task.setOnSucceeded(event -> {
            ObservableList<Person> loadedList = task.getValue();
            log.info("Loaded" + loadedList);
            tableView.setItems(loadedList);
            if (showMessageOnFinished)
                showInfoMessage("Table data loaded successfully");
        });
        task.exceptionProperty()
            .addListener(((observable, oldValue, newValue) -> showErrorMessage("Cannot load data from database: " + newValue.getMessage())));

    }

    @FXML
    public void onAdd(ActionEvent event) {
        editedPersonService.unsetPerson();
        openModalDialog("Add new person", new AddPersonView());
    }

    private void openModalDialog(String title, FXMLView fxmlView) {
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlView.getView());
        stage.setTitle(title);
        stage.setScene(scene);

        // make the dialog modal
        stage.initOwner(this.primaryStage.getOwner());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        // reload the table
        loadDataIntoTable(true);
    }

    @FXML
    public void onEdit(ActionEvent event) {
        Person selectedPerson = tableView.getSelectionModel()
                                         .getSelectedItem();
        editedPersonService.setEditedPerson(selectedPerson);
        openModalDialog("Edit person " + selectedPerson.getLogin(), new AddPersonView());
    }

    @FXML
    public void onDelete(ActionEvent event) {
        Person selectedPerson = tableView.getSelectionModel()
                                         .getSelectedItem();

        Task<Void> task = personDao.delete(selectedPerson.getLogin());
        task.setOnSucceeded(event1 -> {
            showInfoMessage("Deleting " + selectedPerson.getLogin() + " finished");
            loadDataIntoTable(false);
        });

        task.exceptionProperty()
            .addListener((observable, oldValue, newValue) -> showErrorMessage("Cannot delete person " + selectedPerson.getLogin() + ", reason: " + newValue.getMessage()));
    }

    private void showInfoMessage(String message) {
        infoText.setFill(Color.BLACK);
        infoText.setText(message);
    }

    private void showErrorMessage(String message) {
        infoText.setFill(Color.RED);
        infoText.setText(message);
    }
}
