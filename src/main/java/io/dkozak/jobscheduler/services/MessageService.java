package io.dkozak.jobscheduler.services;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class MessageService {

    private final StringProperty message;
    private Text infoText;

    public MessageService() {
        this.message = new SimpleStringProperty();
    }

    public void setMessageDestination(Text infoText) {
        this.infoText = infoText;
        this.infoText.textProperty()
                     .bindBidirectional(message);
    }

    public void infoMessage(String message) {
        Platform.runLater(() -> {
            if (infoText == null) {
                throw new RuntimeException("Message service not initialized");
            }
            infoText.setFill(Color.BLACK);
            this.message.set(message);
        });

    }

    public void errorMessage(String message) {
        Platform.runLater(() -> {
            if (infoText == null) {
                throw new RuntimeException("Message service not initialized");
            }
            infoText.setFill(Color.RED);
            this.message.set(message);
        });
    }
}
