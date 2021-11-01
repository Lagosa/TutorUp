package itreact.tutorup.server.service;

import itreact.tutorup.server.db.DatabaseFactory;
import itreact.tutorup.server.db.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class InteractionsManager {
    private static InteractionsManager ourInstance = new InteractionsManager();
    public static InteractionsManager getInstance(){return ourInstance;}
    private static Logger log = LoggerFactory.getLogger(InteractionsManager.class);
    private final String className = "[InteractionsManager] ";

    public void requestInteraction(Integer requesterId, String receiverUsername, String description)throws SQLException {
        log.debug(className+"Getting receiverId...");
        int receiverId = (int) DatabaseFactory.getUserDao().findByUsername(receiverUsername).get("id");

        if(description.isEmpty() || description == null){
            description = "";
        }

        log.debug(className+"Saving interaction...");
        DatabaseFactory.getInteractionsDao().saveInteraction(requesterId,receiverId,description);

        log.debug(className+"Sending out notification...");
        NotificationsManager.getInstance().save(receiverId,(String)DatabaseFactory.getUserDao().findById(requesterId).get("username"),"New interaction requested!", "You have a new interaction invite pending. If you accept it " +
                "the sender will be able to send messages, initiate calls and to rate you.","/acceptOrDecline","popUpPage");

    }

    public void updateStatus(String senderToken, Integer receiverId, String status)throws SQLException{
        Map<String,Object> senderUser = DatabaseFactory.getUserDao().findByToken(senderToken);

        DatabaseFactory.getInteractionsDao().updateStatus((int)senderUser.get("id"),receiverId,status);

        log.debug(className+"Sending out notification");
        String username = (String)DatabaseFactory.getUserDao().findById(receiverId).get("username");
        NotificationsManager.getInstance().save(receiverId,username,username+" accepted your interaction request!","Now you can chat, call or rate the user!","/chat","chat");
    }

    public boolean hadInteraction(Integer senderId, Integer receiverId)throws SQLException{
        log.debug(className+"Checking if they had interaction...");
        return DatabaseFactory.getInteractionsDao().checkInteractionStatus(senderId,receiverId,"ACCEPTED");
    }

    public List<Map<String,Object>> getInteractions(int userId)throws SQLException{
        List<Map<String,Object>> interactions =  DatabaseFactory.getInteractionsDao().getInteractions(userId);
        for(Map<String,Object> interaction : interactions){
            interaction.put("senderUser",DatabaseFactory.getUserDao().findById((int)interaction.get("senderId")));
        }
        return interactions;
    }
}
