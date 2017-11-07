package io.dkozak.jobscheduler.taskview;

import io.dkozak.jobscheduler.addtaskview.AddTaskView;
import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.EditedTaskService;
import io.dkozak.jobscheduler.services.MessageService;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
import io.dkozak.jobscheduler.services.database.dao.TaskDao;
import io.dkozak.jobscheduler.utils.NotifiablePresenter;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import static io.dkozak.jobscheduler.utils.Utils.openModalDialog;

@Log4j
public class TaskPresenter implements NotifiablePresenter, Initializable {

    @FXML
    private TableView<io.dkozak.jobscheduler.entity.Task> tableView;

    @Inject
    private Stage primaryStage;

    @Inject
    private PersonDao personDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private EditedTaskService editedTaskService;

    @Inject
    private MessageService messageService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTaskTable();
    }

    private void onTableChanged(TableColumn.CellEditEvent<io.dkozak.jobscheduler.entity.Task, String> event, BiConsumer<io.dkozak.jobscheduler.entity.Task, String> setter) {
        io.dkozak.jobscheduler.entity.Task task = event.getTableView()
                                                       .getItems()
                                                       .get(event.getTablePosition()
                                                                 .getRow());
        setter.accept(task, event.getNewValue());

        Task<Void> databaseTask = taskDao.update(task);
        databaseTask.setOnSucceeded(event1 -> {
            showInfoMessage("Update finished");
        });
        databaseTask.exceptionProperty()
                    .addListener((observable, oldValue, newValue) -> showErrorMessage("Cannot update: " + newValue.getMessage()));

    }

    private void initTaskTable() {
        tableView.setEditable(false);

        TableColumn<io.dkozak.jobscheduler.entity.Task, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            onTableChanged(event, io.dkozak.jobscheduler.entity.Task::setName);
        });

        TableColumn<io.dkozak.jobscheduler.entity.Task, String> firstNameColumn = new TableColumn<>("Description");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        firstNameColumn.setOnEditCommit(event -> {
            onTableChanged(event, io.dkozak.jobscheduler.entity.Task::setDescription);
        });
        firstNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<io.dkozak.jobscheduler.entity.Task, String> assignedPersonColumn = new TableColumn<>("Assigned person");
        assignedPersonColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()
                                                                                        .getAssignedPerson()
                                                                                        .getLogin()));
        assignedPersonColumn.setOnEditCommit(event -> {
            onTableChanged(event, this::setAssignedPersonToTask);
        });
        assignedPersonColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        //noinspection unchecked
        tableView.getColumns()
                 .addAll(nameColumn, firstNameColumn, assignedPersonColumn);

        loadDataIntoTable(true);
    }

    private void setAssignedPersonToTask(io.dkozak.jobscheduler.entity.Task task, String login) {
        Task<Optional<Person>> personTask = personDao.findOne(login);
        personTask.setOnSucceeded(event -> {
            Optional<Person> value = personTask.getValue();
            if (!value.isPresent()) {
                throw new RuntimeException("Task should always be assigned to someone");
            }
            task.setAssignedPerson(value.get());

            Task<Void> updateTask = taskDao.update(task);
            updateTask.setOnSucceeded(event1 -> {
                showInfoMessage("Task updated successfully");
            });
            updateTask.setOnFailed(event1 -> {
                showErrorMessage("Task updating failed");
            });
        });
        personTask.setOnFailed(event -> {
            showErrorMessage("Task updating failed");
        });
    }

    private void loadDataIntoTable(boolean showMessageOnFinished) {
        log.info("loading data into the table");

        Task<ObservableList<io.dkozak.jobscheduler.entity.Task>> task = taskDao.findAll();
        task.setOnSucceeded(event -> {
            ObservableList<io.dkozak.jobscheduler.entity.Task> loadedList = task.getValue();
            log.info("Loaded" + loadedList);
            tableView.getItems()
                     .clear();
            tableView.setItems(loadedList);
            if (showMessageOnFinished)
                showInfoMessage("Table data loaded successfully");
        });
        task.exceptionProperty()
            .addListener(((observable, oldValue, newValue) -> showErrorMessage("Cannot load data from database: " + newValue.getMessage())));

    }

    @FXML
    public void onAdd(ActionEvent event) {
        editedTaskService.unsetEditedTask();
        openModalDialog(this.primaryStage, "Add new person", new AddTaskView());
        // reload the table
        loadDataIntoTable(false);
    }

    @FXML
    public void onEdit(ActionEvent event) {
        io.dkozak.jobscheduler.entity.Task selectedTask = tableView.getSelectionModel()
                                                                   .getSelectedItem();
        if (selectedTask == null) {
            showErrorMessage("Please select task first");
            return;
        }
        editedTaskService.setEditedTask(selectedTask);
        openModalDialog(this.primaryStage, "Edit task " + selectedTask.getName(), new AddTaskView());
        // reload the table
        loadDataIntoTable(false);
    }

    @FXML
    public void onDelete(ActionEvent event) {
        io.dkozak.jobscheduler.entity.Task selectedTask = tableView.getSelectionModel()
                                                                   .getSelectedItem();

        if (selectedTask == null) {
            showErrorMessage("Please select task first");
            return;
        }
        Task<Void> task = taskDao.delete(selectedTask.getId());
        task.setOnSucceeded(event1 -> {
            showInfoMessage("Deleting " + selectedTask.getName() + " finished");
            loadDataIntoTable(false);
        });

        task.exceptionProperty()
            .addListener((observable, oldValue, newValue) -> showErrorMessage("Cannot delete task " + selectedTask.getName() + ", reason: " + newValue.getMessage()));
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
