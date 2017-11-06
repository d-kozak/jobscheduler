package io.dkozak.jobscheduler.services.database.dao;

import lombok.extern.log4j.Log4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.SQLException;

@Log4j
public class DaoController {

    @Inject
    private PersonDao personDao;

    @Inject
    private String createTables;

    @PostConstruct
    public void init() {
        if ("true".equals(createTables)) {
            log.info("creating new tables");
            try {
                dropTables();
                createTables();
                log.info("finished");
            } catch (SQLException ex) {
                // TODO show exception in view
                log.error("creation of new tables failed");
                log.error("SQL error : " + ex.getErrorCode());
                throw new RuntimeException(ex);
            }
        }
    }

    public void createTables() throws SQLException {
        log.info("creating tables...");
        personDao.createTable();
    }

    public void dropTables() throws SQLException {
        log.info("dropping tables...");
        try {
            personDao.dropTable();
        } catch (SQLException ex) {
            if (1051 == ex.getErrorCode()) {
                // unknown table, may be the first run of the app
                log.info("Cannot drop the table Person, is this the first run?");
            } else throw new SQLException(ex);
        }

    }
}
