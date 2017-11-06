package io.dkozak.jobscheduler.mainview;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j;

import javax.annotation.PostConstruct;

@Log4j
public class MainPresenter {
    @FXML
    public Text text;

    @PostConstruct
    public void init() {
        log.debug("init()");
    }

}
