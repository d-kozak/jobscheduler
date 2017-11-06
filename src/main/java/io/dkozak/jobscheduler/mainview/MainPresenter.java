package io.dkozak.jobscheduler.mainview;

import com.airhacks.afterburner.views.FXMLView;
import io.dkozak.jobscheduler.addpersonview.AddPersonView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Log4j
public class MainPresenter {

    @FXML
    private TableView tableView;

    @Inject
    private Stage primaryStage;

    @PostConstruct
    public void init() {
        log.info("init()");

    }

    @FXML
    public void onAddNewPerson(ActionEvent event) {
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
    }
}
