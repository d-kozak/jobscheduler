package io.dkozak.jobscheduler.mainview;

import com.airhacks.afterburner.views.FXMLView;
import io.dkozak.jobscheduler.addpersonview.AddPersonView;
import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.EditedPersonService;
import io.dkozak.jobscheduler.services.database.dao.PersonDao;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


@Log4j
public class MainPresenter implements Initializable {

    @FXML
    private TableView<Person> tableView;

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

    private void initTable() {
        tableView.setEditable(true);


        TableColumn<Person, String> loginColumn = new TableColumn<>("Login");
        loginColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("login"));
        loginColumn.setCellFactory(TextFieldTableCell.forTableColumn());


        TableColumn<Person, String> firstNameColumn = new TableColumn<>("First name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        firstNameColumn.setOnEditCommit((EventHandler<TableColumn.CellEditEvent<Person, String>>) event -> {
            Person person = event.getTableView()
                                 .getItems()
                                 .get(event.getTablePosition()
                                           .getRow());
            person.setFirstName(event.getNewValue());
            try {
                personDao.update(person);
            } catch (SQLException e) {
                // TODO handle error properly
                throw new RuntimeException(e);
            }
        });
        firstNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));
        lastNameColumn.setOnEditCommit((EventHandler<TableColumn.CellEditEvent<Person, String>>) event -> {
            Person person = event.getTableView()
                                 .getItems()
                                 .get(event.getTablePosition()
                                           .getRow());
            person.setLastName(event.getNewValue());
            try {
                personDao.update(person);
            } catch (SQLException e) {
                // TODO handle error properly~
                throw new RuntimeException(e);
            }
        });
        lastNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        //noinspection unchecked
        tableView.getColumns()
                 .addAll(loginColumn, firstNameColumn, lastNameColumn);

        loadDataIntoTable();
    }

    private void loadDataIntoTable() {
        log.info("loading data into the table");
        try {
            ObservableList<Person> people = personDao.findALl();
            tableView.setItems(people);
        } catch (SQLException e) {
            // TODO handle error properly
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
        loadDataIntoTable();
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
        try {
            personDao.delete(selectedPerson.getLogin());
            loadDataIntoTable();
        } catch (SQLException e) {
            // TODO handle error properly
            throw new RuntimeException(e);
        }
    }
}
