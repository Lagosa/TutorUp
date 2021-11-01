package itreact.tutorup.server.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This interface provides connections to database.
 *
 */
public interface ConnectionManager {
    /**
     * Gets a {@link Connection} to database
     *
     * @return the connection to DB
     */
    Connection getConnection() throws SQLException;

    /**
     * Gets the {@link DataSource}.
     *
     * @return the DataSource we use
     */
    DataSource getDataSource();
}