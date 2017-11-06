package io.dkozak.jobscheduler.mainview;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import lombok.extern.log4j.Log4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Log4j
public class MainPresenter {
    @FXML
    public Text text;

    @Inject
    private String localMessage;

    @Inject
    private String globalMessage;

    @PostConstruct
    public void init() {
        log.info("init()");
        System.out.println("global message: " + globalMessage);
        System.out.println("local message: " + localMessage);
    }

}
