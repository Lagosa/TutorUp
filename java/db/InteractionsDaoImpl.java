package itreact.tutorup.server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InteractionsDaoImpl implements InteractionsDao {
    private ConnectionManager connectionManager;
    public InteractionsDaoImpl(ConnectionManager connectionManager){
        this.connectionManager = connectionManager;
    }
    private static Logger log = LoggerFactory.getLogger(InteractionsDaoImpl.class);
    private final String className = "[InteractionsWS] ";

    @Override
    public void saveInteraction(Integer requesterId, Integer receiverId, String description)throws SQLException{
        String sql = "INSERT INTO interactions (senderid, receiverid, description) VALUES (?,?,?)";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug(className+"Preparing statement...");
            statement.setInt(1,requesterId);
            statement.setInt(2,receiverId);
            statement.setString(3,description);
            log.debug(className+"Executing insert...");
            statement.executeUpdate();
        }
    }

    @Override
    public void updateStatus(Integer requesterId, Integer receiverId, String status) throws SQLException {
        String sql = "UPDATE interactions SET status = ? WHERE senderid = ? AND receiverid = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            log.debug(className+"Preparing statement,,,");
            statement.setString(1,status);
            statement.setInt(2,requesterId);
            statement.setInt(3,receiverId);

            log.debug(className+"Executing update...");
            statement.executeUpdate();
        }
    }

    @Override
    public boolean checkInteractionStatus(Integer senderId, Integer receiverId, String status) throws SQLException {
        boolean result=false;
        String sql = "SELECT id FROM interactions WHERE senderid = ? AND receiverid = ? AND status LIKE ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            log.debug(className+"Preparing statement...");
            statement.setInt(1,senderId);
            statement.setInt(2,receiverId);
            statement.setString(3,status);

            log.debug(className+"Running query...");
            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getInteractions(int userId) throws SQLException {
        String sql = "SELECT * FROM interactions WHERE receiverid = ? and status LIKE 'PENDING'";
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String,Object> interaction = new HashMap<>();
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);){
            statement.setInt(1,userId);

            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    interaction = new HashMap<>();
                    interaction.put("senderId", rs.getInt("senderid"));
                    interaction.put("description",rs.getString("description"));

                    result.add(interaction);
                }
            }
        }
        return result;
    }
}
