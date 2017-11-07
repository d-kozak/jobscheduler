package io.dkozak.jobscheduler.personView;

import io.dkozak.jobscheduler.addpersonview.AddPersonView;
import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.EditedPersonService;
import io.dkozak.jobscheduler.services.EventBus;
import io.dkozak.jobscheduler.services.MessageService;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
import io.dkozak.jobscheduler.utils.NotifiablePresenter;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import static io.dkozak.jobscheduler.utils.Utils.openModalDialog;

@Log4j
public class PersonPresenter implements NotifiablePresenter, Initializable {

    @FXML
    private TableView<Person> tableView;

    @Inject
    private Stage primaryStage;

    @Inject
    private PersonDao personDao;

    @Inject
    private EditedPersonService editedPersonService;

    @Inject
    private MessageService messageService;

    @Inject
    private EventBus eventBus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initPersonTable();
    }

    private void onTableChanged(TableColumn.CellEditEvent<Person, String> event, BiConsumer<Person, String> setter) {
        Person person = event.getTableView()
                             .getItems()
                             .get(event.getTablePosition()
                                       .getRow());
        setter.accept(person, event.getNewValue());

        Task<Void> task = personDao.update(person);
        task.setOnSucceeded(event1 -> {
            showInfoMessage("Update finished");
        });
        task.exceptionProperty()
            .addListener((observable, oldValue, newValue) -> showErrorMessage("Cannot update: " + newValue.getMessage()));

    }

    private void initPersonTable() {
        tableView.setEditable(true);

        TableColumn<Person, String> loginColumn = new TableColumn<>("Login");
        loginColumn.setEditable(false);
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

        Task<ObservableList<Person>> task = personDao.findAll();
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
        editedPersonService.unsetEditedPerson();
        openModalDialog(this.primaryStage, "Add new person", new AddPersonView());
        // reload the table
        loadDataIntoTable(false);
    }

    @FXML
    public void onEdit(ActionEvent event) {
        Person selectedPerson = tableView.getSelectionModel()
                                         .getSelectedItem();
        if (selectedPerson == null) {
            showErrorMessage("Please select person first");
            return;
        }

        editedPersonService.setEditedPerson(selectedPerson);
        openModalDialog(this.primaryStage, "Edit person " + selectedPerson.getLogin(), new AddPersonView());
        // reload the table
        loadDataIntoTable(false);
    }

    @FXML
    public void onDelete(ActionEvent event) {
        Person selectedPerson = tableView.getSelectionModel()
                                         .getSelectedItem();

        if (selectedPerson == null) {
            showErrorMessage("Please select person first");
            return;
        }

        Task<Void> task = personDao.delete(selectedPerson.getLogin());
        task.setOnSucceeded(event1 -> {
            showInfoMessage("Deleting " + selectedPerson.getLogin() + " finished");
            loadDataIntoTable(false);
            eventBus.sendMessage("refresh");
        });

        task.exceptionProperty()
            .addListener((observable, oldValue, newValue) -> showErrorMessage("Cannot delete person " + selectedPerson.getLogin() + ", reason: " + newValue.getMessage()));
    }

    @Override
    public void showInfoMessage(String message) {
        log.info("passing info message to event bus:" + message);
        messageService.infoMessage(message);
    }

    @Override
    public void showErrorMessage(String message) {
        log.error("passing error message to event bus:" + message);
        messageService.errorMessage(message);
    }
}
