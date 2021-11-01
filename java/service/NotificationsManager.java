package itreact.tutorup.server.service;

import itreact.tutorup.server.db.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsManager {
    public static NotificationsManager ourInstance = new NotificationsManager();
    public static NotificationsManager getInstance(){return ourInstance;}

    private final Logger log = LoggerFactory.getLogger(RequestsManager.class);
    private final String className = "[NotificationManager] ";

    public void save(int userid, String from, String title, String description, String link, String page)throws SQLException{
        DatabaseFactory.getNotificationsDao().saveNotification(userid,from, title,description,link, page);
    }

    public List<Map<String, Object>> listNotificationsFromUser(int userId)throws SQLException{
        List<Map<String,Object>> notifications = DatabaseFactory.getNotificationsDao().listNotificationsFromUser(userId);

        return notifications;
    }
    
    public List<Map<String, Object>> listNotificationsFromUser(String userName)throws SQLException{
        List<Map<String,Object>> notifications = DatabaseFactory.getNotificationsDao().listNotificationsFromUser(userName);
        if (notifications.size() < 10) {
        	notifications.addAll(DatabaseFactory.getNotificationsDao().listNotificationsWithStatusSentFromUser(userName, (10 - notifications.size())));
        }
        return notifications;
    }

    public void markNotificationSent(int id)throws SQLException{
        DatabaseFactory.getNotificationsDao().changeStatus(id, "SENT");
    }

    public List<Map<String,Object>> listAMAResults( int requestId, int nrResults)throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String,Object> aMAResult ;
        log.debug(className+"Filling list with AMA results...");
        for(int i=0;i<nrResults;i++){
            log.debug(className+"Getting ids from db for requestid: {} and offer number {}",requestId,i);
            aMAResult = DatabaseFactory.getNotificationsDao().listAMAResultsIds(requestId,i);
            log.debug(className+"Getting offer for id: {}",aMAResult.get("offerId"));
            results.add(OffersManager.getInstance().findOfferById((int)aMAResult.get("offerId")));
            log.debug(className+"");
            markNotificationSent((int)aMAResult.get("notificationId"));
        }
        results = SearchManager.getInstance().attachUsers(results,"tutorId");

        return results;
    }

    public List<Map<String, Object>> getNotificationsFromUser(int id)throws SQLException {
        List<Map<String, Object>> result = listNotificationsFromUser(id);

        for(Map<String,Object> notification: result){
            markNotificationSent((int)notification.get("id"));
        }

        return result;
    }
}
