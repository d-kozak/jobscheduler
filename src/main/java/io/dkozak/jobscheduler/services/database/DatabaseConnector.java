package io.dkozak.jobscheduler.services.database;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;

@Log4j
public class DatabaseConnector implements AutoCloseable {

    @Inject
    private String databaseUsername;

    @Inject
    private String databasePassword;

    @Inject
    private String databaseUrl;

    @Getter
    private Connection connection;

    @PostConstruct
    public void init() {
        connect();
    }

    public void connect() {
        log.info("Connecting to the database");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
            log.info("Connected successfully");
        } catch (Exception e) {
            log.error("Cannot connect");
            // TODO reflect in in the view somehow instead of throwing an exception
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws Exception {
        log.info("Closing the database connection");
        connection.close();
    }
}
