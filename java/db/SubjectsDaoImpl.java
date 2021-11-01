package itreact.tutorup.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubjectsDaoImpl implements SubjectsDao {
    private ConnectionManager connectionManager;
    public SubjectsDaoImpl(ConnectionManager connectionManager){this.connectionManager = connectionManager;}

    @Override
    public List<String> getSubjects() throws SQLException {
        List<String> result = new ArrayList<>();
        String sql = "SELECT * FROM subjects";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            try(ResultSet rs = statement.executeQuery()) {
                while(rs.next()){
                    result.add(rs.getString("subject"));
                }
            }
        }
        return result;
    }
}
