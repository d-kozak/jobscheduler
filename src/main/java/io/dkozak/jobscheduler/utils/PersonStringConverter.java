package io.dkozak.jobscheduler.utils;

import io.dkozak.jobscheduler.entity.Person;

public class PersonStringConverter extends javafx.util.StringConverter<io.dkozak.jobscheduler.entity.Person> {

    @Override
    public String toString(Person object) {
        return object.getLogin();
    }

    @Override
    public Person fromString(String string) {
        throw new RuntimeException("This method should never be called");
    }

}
