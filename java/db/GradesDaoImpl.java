package itreact.tutorup.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradesDaoImpl implements GradesDao {
    private ConnectionManager connectionManager;
    public GradesDaoImpl(ConnectionManager connectionManager){this.connectionManager = connectionManager;}

    @Override
    public List<String> getGrades() throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT * FROM grades";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    result.add(rs.getString("grade"));
                }
            }
        }
        return result;
    }
}
