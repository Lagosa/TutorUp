package itreact.tutorup.server.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface NotificationsDao {
    void saveNotification(int userId, String from, String title, String description, String link, String additionalInformation)throws SQLException;
    List<Map<String,Object>> listNotificationsFromUser(int userId)throws SQLException;
    List<Map<String,Object>> listNotificationsFromUser(String userName)throws SQLException;
    void changeStatus(int id, String status)throws SQLException;

    Map<String,Object> listAMAResultsIds(int requestId, int i)throws SQLException;
	List<Map<String, Object>> listNotificationsWithStatusSentFromUser(String userName, int maxRows) throws SQLException;
}
