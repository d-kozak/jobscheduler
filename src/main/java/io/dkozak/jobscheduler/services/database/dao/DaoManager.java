package io.dkozak.jobscheduler.services.database.dao;

import io.dkozak.jobscheduler.entity.Person;
import io.dkozak.jobscheduler.services.database.DatabaseConnector;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j
public class DaoManager {

    @Inject
    private PersonDao personDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private DatabaseConnector databaseConnector;

    @Inject
    private String createTables;

    @Getter
    private Task<Void> prepareDatabaseTask;

    public static final ExecutorService daoExecutorService = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void init() {
        if ("true".equals(createTables)) {
            log.info("creating new tables");
            prepareDatabaseTask = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    try {
                        dropTables();
                    } catch (SQLException ex) {
                        handleSQLException(ex);
                    }

                    createTables();
                    loadDemoData();
                    return null;
                }

                private void handleSQLException(SQLException cause) throws ExecutionException {
                    if (1051 == cause.getErrorCode()) {
                        // unknown table, may be the first run of the app
                        log.error("Cannot drop tables, is this the first run? Cause: " + cause.getMessage());
                    } else throw new ExecutionException(cause);
                }

                private void loadDemoData() throws ExecutionException, InterruptedException {
                    Person xkozak15 = new Person("xkozak15", "David", "Kozak");
                    Person xrobot = new Person("xrobot02", "robot", "rychly");
                    Person xabrah = new Person("xabrah15", "Lukas", "Abraham");
                    personDao.save(xkozak15)
                             .get();
                    personDao.save(xabrah)
                             .get();
                    personDao.save(new Person("xmendel1", "Vetrelec", "z Mendelky"))
                             .get();
                    personDao.save(new Person("xrobot01", "robot", "pomaly"))
                             .get();
                    personDao.save(xrobot)
                             .get();


                    taskDao.save(new io.dkozak.jobscheduler.entity.Task("Web", "Code the web", xabrah));
                    taskDao.save(new io.dkozak.jobscheduler.entity.Task("Back end", "Develop the back end", xkozak15));
                    taskDao.save(new io.dkozak.jobscheduler.entity.Task("Automate", "Write automation scripts", xrobot));
                }

                private void createTables() throws ExecutionException, InterruptedException {
                    log.info("creating tables...");
                    personDao.createTable()
                             .get();

                    taskDao.createTable()
                           .get();
                }

                private void dropTables() throws SQLException {
                    log.info("dropping tables...");
                    try (Statement statement = databaseConnector.getConnection()
                                                                .createStatement()) {
                        statement.executeUpdate("DROP TABLE IF EXISTS Task,Person");
                    }

                }
            };
            daoExecutorService.submit(prepareDatabaseTask);
        }
    }
}
