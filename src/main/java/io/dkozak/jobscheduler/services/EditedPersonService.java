package io.dkozak.jobscheduler.services;


import io.dkozak.jobscheduler.entity.Person;

import java.util.Optional;

public class EditedPersonService {

    private Person editedPerson;

    public Optional<Person> getEditedPerson() {
        return Optional.ofNullable(editedPerson);
    }

    public void unsetEditedPerson() {
        editedPerson = null;
    }

    public void setEditedPerson(Person editedPerson) {
        this.editedPerson = editedPerson;
    }
}
