package itreact.tutorup.server.db;

import java.sql.SQLException;
import java.util.List;

public interface GradesDao {
    List<String> getGrades()throws SQLException;
}
