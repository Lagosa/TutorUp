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

public class NotificationsDaoImpl implements NotificationsDao {
    private ConnectionManager connectionManager;
    public NotificationsDaoImpl(ConnectionManager connectionManager){this.connectionManager = connectionManager;}
    private final Logger log = LoggerFactory.getLogger(OffersDaoImpl.class);
    private String className = "[NotificationsDaoImpl] ";


    @Override
    public void saveNotification(int userId, String from,String title, String description, String link, String additionalInformation) throws SQLException {
        String sql = "INSERT INTO notifications (userid, usernamefrom, title, description, linktoopen, pagetoopen, datewhensent) VALUES (?,?,?,?,?,?,now())";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            log.debug(className+"Preparing statement...");
            statement.setInt(1, userId);
            statement.setString(2,from);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.setString(5,link);
            statement.setString(6,additionalInformation);

            log.debug(className+"Executing update...{},{},{},{},{},{}",userId,from,title,description,link,additionalInformation);
            statement.executeUpdate();
            log.debug(className+"Update executed!");
        }
    }

    @Override
    public List<Map<String, Object>> listNotificationsFromUser(int userId) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE userid = ? AND status = 'ACTIVE'";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setInt(1,userId);
            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    result.add(readNotificationsFromResultSet(rs));
                }
            }
        }
        return result;
    }
    
    @Override
    public List<Map<String, Object>> listNotificationsFromUser(String userName) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        
        String sql = "SELECT n.* FROM notifications n INNER JOIN tutorup_user u ON n.userid=u.id WHERE u.username=? AND n.status = 'ACTIVE'";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1,userName);
            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    result.add(readNotificationsFromResultSet(rs));
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listNotificationsWithStatusSentFromUser(String userName, int maxRows) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        
        String sql = "SELECT n.* FROM notifications n INNER JOIN tutorup_user u ON n.userid=u.id WHERE u.username=? AND n.status = 'SENT' order by n.datewhensent desc, id desc";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1,userName);
            //return only the newest max rows
            statement.setMaxRows(maxRows);
            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    result.add(readNotificationsFromResultSet(rs));
                }
            }
        }
        return result;
    }
    
    @Override
    public void changeStatus(int id, String status) throws SQLException {
        String sql = "UPDATE notifications SET status = ? WHERE id = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1,status);
            statement.setInt(2,id);

            statement.executeUpdate();
        }
    }

    @Override
    public Map<String,Object> listAMAResultsIds(int requestId, int i) throws SQLException {
        Map<String,Object> result = new HashMap<>();
        String sql = "SELECT * FROM notifications WHERE title LIKE ? AND description LIKE ? AND linktoopen LIKE 'AMA'";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            log.debug(className+"Preparing statement...");
            statement.setString(1,requestId+"");
            statement.setString(2,i+"");

            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    log.debug(className+"Getting offerid and notificationId...");
                    result.put("offerId",rs.getInt("userid"));
                    result.put("notificationId",rs.getInt("id"));
                }
            }
        }
        return result;
    }

    private Map<String, Object> readNotificationsFromResultSet(ResultSet rs)throws SQLException{
        Map<String, Object> result = new HashMap<>();

        result.put("id",rs.getInt("id"));
        result.put("from",rs.getString("usernamefrom"));
        result.put("title",rs.getString("title"));
        result.put("description", rs.getString("description"));
        result.put("link", rs.getString("linktoopen"));
        result.put("page", rs.getString("pagetoopen"));
        result.put("date",rs.getTimestamp("dateWhenSent"));
        result.put("status", rs.getString("status"));
        return result;
    }
}
