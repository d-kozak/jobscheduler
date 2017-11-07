package io.dkozak.jobscheduler.services.database.dao;

import io.dkozak.jobscheduler.entity.Person;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j
public class DaoController {

    @Inject
    private PersonDao personDao;

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
                    dropTables();
                    createTables();
                    loadDemoData();
                    return null;
                }

                private void loadDemoData() throws ExecutionException, InterruptedException {
                    personDao.save(new Person("xkozak15", "David", "Kozak"))
                             .get();
                    personDao.save(new Person("xabrah15", "Lukas", "Abraham"))
                             .get();
                    personDao.save(new Person("xmendel1", "Vetrelec", "z Mendelky"))
                             .get();
                    personDao.save(new Person("xrobot01", "robot", "pomaly"))
                             .get();
                    personDao.save(new Person("xrobot02", "robot", "rychly"))
                             .get();
                }

                private void createTables() throws ExecutionException, InterruptedException {
                    log.info("creating tables...");
                    personDao.createTable()
                             .get();
                }

                private void dropTables() throws ExecutionException, InterruptedException {
                    log.info("dropping tables...");
                    personDao.dropTable()
                             .get();
                    //        if (1051 == ex.getErrorCode()) {
                    //            // unknown table, may be the first run of the app
                    //            log.info("Cannot drop the table Person, is this the first run?");
                    //        } else throw new SQLException(ex);
                }
            };
            daoExecutorService.submit(prepareDatabaseTask);
        }
    }
}
