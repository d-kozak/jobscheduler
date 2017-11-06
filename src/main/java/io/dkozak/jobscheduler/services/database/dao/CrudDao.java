package io.dkozak.jobscheduler.services.database.dao;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.Optional;

public interface CrudDao<T, Key> {
    Task<ObservableList<T>> findALl();

    Task<Optional<T>> findOne(Key key);

    Task<Void> save(T t);

    Task<Void> update(T t);

    Task<Void> delete(Key key);

    Task<Void> createTable();

    Task<Void> dropTable();
}
