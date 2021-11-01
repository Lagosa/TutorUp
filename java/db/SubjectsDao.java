package itreact.tutorup.server.db;

import java.sql.SQLException;
import java.util.List;

public interface SubjectsDao {
    List<String> getSubjects()throws SQLException;
}
