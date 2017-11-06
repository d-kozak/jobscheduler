package io.dkozak.jobscheduler.services.database.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CrudDao<T, Key> {
    List<T> findALl() throws SQLException;

    Optional<T> findOne(Key key) throws SQLException;

    void save(T t) throws SQLException;

    void update(T t) throws SQLException;

    void delete(Key key) throws SQLException;

    void createTable() throws SQLException;

    void dropTable() throws SQLException;
}
