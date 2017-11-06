package io.dkozak.jobscheduler.services.database.dao;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.database.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.extern.log4j.Log4j;
import lombok.val;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j
public class PersonDao implements CrudDao<Person, String> {

    @Inject
    private DatabaseConnector connector;

    private PreparedStatement insert;
    private PreparedStatement findAll;
    private PreparedStatement findOne;
    private PreparedStatement update;
    private PreparedStatement delete;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);


    @PostConstruct
    public void init() {
        log.info("preparing statements");
        try {
            Connection connection = connector.getConnection();
            insert = connection.prepareStatement("INSERT INTO Person VALUES (?,?,?)");
            findAll = connection.prepareStatement("SELECT login,firstName,lastName FROM Person p");
            findOne = connection.prepareStatement("SELECT login,firstName,lastName FROM Person p WHERE p.login=?");
            update = connection.prepareStatement("UPDATE Person p SET firstName=?,lastName=? WHERE p.login=?");
            delete = connection.prepareStatement("DELETE FROM Person WHERE login=?");
            log.info("preparedStatements ready");
        } catch (SQLException ex) {
            // TODO show error
            log.error("creation of prepared statements failed: " + ex.getMessage());
        }
    }

    public Task<Void> createTable() {
        val task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("creating table");
                Connection connection = connector.getConnection();
                try (Statement statement = connection.createStatement()) {
                    val createStatement = "CREATE TABLE Person(" +
                            "login VARCHAR(50) PRIMARY KEY," +
                            " firstName VARCHAR(50) NOT NULL , " +
                            "lastName VARCHAR(50) NOT NULL )";
                    statement.executeUpdate(createStatement);

                }
                return null;
            }
        };
        executorService.submit(task);
        return task;
    }

    @Override
    public Task<Void> dropTable() {
        val task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("dropping table");
                Connection connection = connector.getConnection();
                try (Statement statement = connection.createStatement()) {
                    val sql = "DROP TABLE Person";
                    statement.executeUpdate(sql);
                }

                return null;
            }
        };
        executorService.submit(task);
        return task;
    }

    @Override
    public Task<ObservableList<Person>> findALl() {
        val task = new Task<ObservableList<Person>>() {
            @Override
            protected ObservableList<Person> call() throws Exception {
                log.info("finding all");
                val result = FXCollections.<Person>observableArrayList();
                try (ResultSet resultSet = findAll.executeQuery()) {
                    while (resultSet.next()) {
                        Person person = getPersonFromResultSet(resultSet);
                        result.add(person);
                    }
                    return result;
                }
            }
        };
        executorService.submit(task);
        return task;
    }

    @Override
    public Task<Optional<Person>> findOne(String pk) {
        val task = new Task<Optional<Person>>() {
            @Override
            protected Optional<Person> call() throws Exception {
                log.info("finding person with login " + pk);
                findOne.setString(1, pk);
                try (ResultSet resultSet = findOne.executeQuery()) {
                    if (resultSet.next()) {
                        Person person = getPersonFromResultSet(resultSet);
                        return Optional.of(person);
                    } else
                        return Optional.empty();
                }
            }
        };
        executorService.submit(task);
        return task;
    }

    @Override
    public Task<Void> save(Person person) {
        val task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("saving person " + person);
                insert.setString(1, person.getLogin());
                insert.setString(2, person.getFirstName());
                insert.setString(3, person.getLastName());

                if (insert.executeUpdate() != 1) {
                    throw new SQLException("insert failed");
                }
                return null;
            }
        };
        executorService.submit(task);
        return task;
    }

    @Override
    public Task<Void> update(Person person) {
        val task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("updating " + person.getLogin() + " to " + person);
                update.setString(1, person.getFirstName());
                update.setString(2, person.getLastName());
                update.setString(3, person.getLogin());

                if (update.executeUpdate() != 1) {
                    throw new SQLException("update failed");
                }
                return null;
            }
        };
        executorService.submit(task);
        return task;
    }

    @Override
    public Task<Void> delete(String pk) {
        val task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("deleting person " + pk);
                delete.setString(1, pk);
                if (delete.executeUpdate() != 1) {
                    throw new SQLException("delete failed");
                }
                return null;
            }
        };
        executorService.submit(task);
        return task;
    }

    private Person getPersonFromResultSet(ResultSet resultSet) throws SQLException {
        String login = resultSet.getString("login");
        String firstName = resultSet.getString("firstName");
        String lastName = resultSet.getString("lastName");

        return new Person(login, firstName, lastName);
    }
}
