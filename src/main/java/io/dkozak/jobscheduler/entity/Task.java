package io.dkozak.jobscheduler.entity;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.ToString;

@ToString
public class Task {
    private int id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty description;
    private final SimpleObjectProperty<Person> assignedPerson;

    public Task(String name, String description, Person assignedPerson) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.assignedPerson = new SimpleObjectProperty<>(assignedPerson);
    }

    public Task(int id, String name, String description, Person assignedPerson) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.assignedPerson = new SimpleObjectProperty<>(assignedPerson);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Person getAssignedPerson() {
        return assignedPerson.get();
    }

    public SimpleObjectProperty<Person> assignedPersonProperty() {
        return assignedPerson;
    }

    public void setAssignedPerson(Person assignedPerson) {
        this.assignedPerson.set(assignedPerson);
    }
}
