package io.dkozak.jobscheduler.services.database.dao;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.entity.Task;
import io.dkozak.jobscheduler.services.database.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j;
import lombok.val;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.*;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.dkozak.jobscheduler.services.database.dao.DaoController.daoExecutorService;

@Log4j
public class TaskDao implements CrudDao<Task, Integer> {


    @Inject
    private DatabaseConnector connector;

    @Inject
    private PersonDao personDao;

    private PreparedStatement insert;
    private PreparedStatement findAll;
    private PreparedStatement findOne;
    private PreparedStatement update;
    private PreparedStatement delete;

    @PostConstruct
    public void init() {
        log.info("preparing statements");

        try {
            Connection connection = connector.getConnection();
            insert = connection.prepareStatement("INSERT INTO Task VALUES (?,?,?,?)");
            findAll = connection.prepareStatement("SELECT id,name,description,assignedPerson FROM Task");
            findOne = connection.prepareStatement("SELECT id,name,description,assignedPerson FROM Task WHERE id=?");
            update = connection.prepareStatement("UPDATE Task SET name=?,description=?,assignedPerson=? WHERE id=?");
            delete = connection.prepareStatement("DELETE FROM Task WHERE id=?");
            log.info("preparedStatements ready");
        } catch (SQLException ex) {
            // TODO show error
            log.error("creation of prepared statements failed: " + ex.getMessage());
        }
    }


    public javafx.concurrent.Task<Void> createTable() {
        val task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("creating table");
                Connection connection = connector.getConnection();
                try (Statement statement = connection.createStatement()) {
                    val createStatement = "CREATE TABLE Task(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(50) NOT NULL , " +
                            "description VARCHAR(250) ," +
                            "assignedPerson VARCHAR(50) KEY REFERENCES Person(login) ON  DELETE CASCADE ) ";
                    statement.executeUpdate(createStatement);
                }
                return null;
            }
        };
        daoExecutorService.submit(task);
        return task;
    }

    @Override
    public javafx.concurrent.Task<Void> dropTable() {
        val task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("dropping table");
                Connection connection = connector.getConnection();
                try (Statement statement = connection.createStatement()) {
                    val sql = "DROP TABLE Task";
                    statement.executeUpdate(sql);
                }

                return null;
            }
        };
        daoExecutorService.submit(task);
        return task;
    }

    @Override
    public javafx.concurrent.Task<ObservableList<Task>> findALl() {
        val task = new javafx.concurrent.Task<ObservableList<Task>>() {
            @Override
            protected ObservableList<Task> call() throws Exception {
                log.info("finding all");
                val result = FXCollections.<Task>observableArrayList();
                try (ResultSet resultSet = findAll.executeQuery()) {
                    while (resultSet.next()) {
                        Task task1 = getTaskFromResultSet(resultSet);
                        result.add(task1);
                    }
                    return result;
                }
            }
        };
        daoExecutorService.submit(task);
        return task;
    }

    @Override
    public javafx.concurrent.Task<Optional<Task>> findOne(Integer pk) {
        val task = new javafx.concurrent.Task<Optional<Task>>() {
            @Override
            protected Optional<Task> call() throws Exception {
                log.info("finding task with id " + pk);
                findOne.setInt(1, pk);
                try (ResultSet resultSet = findOne.executeQuery()) {
                    if (resultSet.next()) {
                        Task task = getTaskFromResultSet(resultSet);
                        return Optional.of(task);
                    } else
                        return Optional.empty();
                }
            }
        };
        daoExecutorService.submit(task);
        return task;
    }

    @Override
    public javafx.concurrent.Task<Void> save(Task task) {
        val databaseTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("saving task " + task);
                insert.setString(1, task.getName());
                insert.setString(2, task.getDescription());
                insert.setString(3, task.getAssignedPerson()
                                        .getLogin());

                if (insert.executeUpdate() != 1) {
                    throw new SQLException("insert failed");
                }
                return null;
            }
        };
        daoExecutorService.submit(databaseTask);
        return databaseTask;
    }

    @Override
    public javafx.concurrent.Task<Void> update(Task task) {
        val databaseTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("updating " + task.getId() + " to " + task);
                update.setString(1, task.getName());
                update.setString(2, task.getDescription());
                update.setString(3, task.getAssignedPerson()
                                        .getLogin());
                update.setInt(3, task.getId());

                if (update.executeUpdate() != 1) {
                    throw new SQLException("update failed");
                }
                return null;
            }
        };
        daoExecutorService.submit(databaseTask);
        return databaseTask;
    }

    @Override
    public javafx.concurrent.Task<Void> delete(Integer pk) {
        val task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                log.info("deleting task " + pk);
                delete.setInt(1, pk);
                if (delete.executeUpdate() != 1) {
                    throw new SQLException("delete failed");
                }
                return null;
            }
        };
        daoExecutorService.submit(task);
        return task;
    }

    private Task getTaskFromResultSet(ResultSet resultSet) throws SQLException, InterruptedException, ExecutionException, TimeoutException {

        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");

        String login = resultSet.getString("assignedPerson");
        val task = personDao.findOne(login);

        Optional<Person> person = task.get(1, TimeUnit.SECONDS);
        if (!person.isPresent())
            throw new RuntimeException("Each task needs to have an assigned person");

        return new Task(id, name, description, person.get());
    }
}
