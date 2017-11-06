package io.dkozak.jobscheduler.entity;


import javafx.beans.property.SimpleStringProperty;
import lombok.ToString;

@ToString
public class Person {
    private final SimpleStringProperty login;
    private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;


    public Person() {
        this.login = new SimpleStringProperty();
        this.firstName = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();
    }

    public Person(String login, String fName, String lName) {
        this.login = new SimpleStringProperty(login);
        this.firstName = new SimpleStringProperty(fName);
        this.lastName = new SimpleStringProperty(lName);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String fName) {
        firstName.set(fName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String fName) {
        lastName.set(fName);
    }

    public String getLogin() {
        return login.get();
    }

    public void setLogin(String fName) {
        login.set(fName);
    }
}
