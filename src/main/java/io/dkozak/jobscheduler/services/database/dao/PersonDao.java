package io.dkozak.jobscheduler.services.database.dao;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.database.DatabaseConnector;
import lombok.extern.log4j.Log4j;
import lombok.val;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j
public class PersonDao implements CrudDao<Person, String> {

    @Inject
    private DatabaseConnector connector;

    private PreparedStatement insert;
    private PreparedStatement findAll;
    private PreparedStatement findOne;
    private PreparedStatement update;
    private PreparedStatement delete;


    @PostConstruct
    public void init() {
        log.info("Preparing statements");
        try {
            Connection connection = connector.getConnection();
            insert = connection.prepareStatement("INSERT INTO Person VALUES (?,?,?)");
            findAll = connection.prepareStatement("SELECT login,firstName,lastName FROM PERSON p");
            findOne = connection.prepareStatement("SELECT login,firtName,lastName FROM PERSON p WHERE p.login=?");
            update = connection.prepareStatement("UPDATE Person p SET firstName=?,lastName=? WHERE p.login=?");
            delete = connection.prepareStatement("DELETE FROM Person p WHERE p.login=?");
            log.info("PreparedStatements ready");
        } catch (SQLException ex) {
            // TODO show error
            log.error("Creation of prepared statements failed: " + ex.getMessage());
        }
    }

    public void createTable() throws SQLException {
        Connection connection = connector.getConnection();
        try (Statement statement = connection.createStatement();) {
            val createStatement = "CREATE TABLE Person(" +
                    "login VARCHAR(50) PRIMARY KEY," +
                    " firstName VARCHAR(50) NOT NULL , " +
                    "lastName VARCHAR(50) NOT NULL )";
            statement.executeUpdate(createStatement);

        }
    }

    @Override
    public void dropTable() throws SQLException {
        Connection connection = connector.getConnection();
        try (Statement statement = connection.createStatement()) {
            val sql = "DROP TABLE Person";
            statement.executeUpdate(sql);
        }
    }

    @Override
    public List<Person> findALl() throws SQLException {
        val result = new ArrayList<Person>();
        try (ResultSet resultSet = findAll.executeQuery()) {
            while (resultSet.next()) {
                Person person = getPersonFromResultSet(resultSet);
                result.add(person);
            }
            return result;
        }
    }

    @Override
    public Optional<Person> findOne(String pk) throws SQLException {
        findOne.setString(1, pk);
        try (ResultSet resultSet = findOne.executeQuery()) {
            if (resultSet.next()) {
                Person person = getPersonFromResultSet(resultSet);
                return Optional.of(person);
            } else
                return Optional.empty();
        }
    }

    @Override
    public void save(Person person) throws SQLException {
        insert.setString(1, person.getLogin());
        insert.setString(2, person.getFirstName());
        insert.setString(3, person.getLastName());

        if (insert.executeUpdate() != 1) {
            throw new SQLException("insert failed");
        }
    }

    @Override
    public void update(Person person) throws SQLException {
        update.setString(1, person.getFirstName());
        update.setString(2, person.getLastName());
        update.setString(3, person.getLogin());

        if (update.executeUpdate() != 1) {
            throw new SQLException("update failed");
        }
    }

    @Override
    public void delete(String pk) throws SQLException {
        delete.setString(1, pk);
        if (delete.executeUpdate() != 1) {
            throw new SQLException("delete failed");
        }
    }

    private Person getPersonFromResultSet(ResultSet resultSet) throws SQLException {
        String login = resultSet.getString("login");
        String firstName = resultSet.getString("firstName");
        String lastName = resultSet.getString("lastName");

        return new Person(login, firstName, lastName);
    }
}
