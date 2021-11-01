package itreact.tutorup.server.service;

import itreact.tutorup.server.db.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RequestsManager {
    public static RequestsManager ourInstance = new RequestsManager();
    public static RequestsManager getInstance(){return ourInstance;}

    private final Logger log = LoggerFactory.getLogger(RequestsManager.class);
    private final String className = "[RequestsManager] ";

    public Boolean setRequest(Integer userId, String subject, String grade, String level, String meetingType, String date)throws SQLException {
        log.debug(className+"Extracting dateFrom and dateTo...");
        Map<String, Date> dateMap = OffersManager.getInstance().stringToDate(date);

        if(subject != null && !subject.isEmpty() ) subject = subject.toLowerCase();
        if(grade != null && !grade.isEmpty())grade = grade.toLowerCase();
        if(level != null && !level.isEmpty())level = level.toLowerCase();
        if(meetingType != null && !meetingType.isEmpty())meetingType = meetingType.toLowerCase();

        List<Map<String,Object>> existingRequests = DatabaseFactory.getRequestsDao().listRequestsThatMatch(userId,subject,grade,level);
        log.debug(className+"Checking if there are existing requests!");
        if(!existingRequests.isEmpty()){
            log.error(className+"Found existing requests!");
            return false;
        }

        log.debug(className+"Sending data to DB...");
        DatabaseFactory.getRequestsDao().setRequest(userId,subject,grade,level,meetingType,dateMap.get("from"), dateMap.get("to"));

        NotificationsManager.getInstance().save(userId,"server","Request saved!", "Your request has been saved, your will be notified when someone is interested in it. Hope to find your dream mentor!","","searchoffers");

        log.debug(className+"Triggering Automatic Matching Algorithm");
        AutomaticMatchingAlgorithm.getInstance().triggerMatching(userId,subject,grade,level,dateMap.get("from"),dateMap.get("to"));

        return true;
    }

    public void deleteRequest(int id, String subject, String grade, String level)throws SQLException {
        subject = subject.toLowerCase();
        grade = grade.toLowerCase();
        level = level.toLowerCase();
        DatabaseFactory.getRequestsDao().deleteRequest(id,subject,grade,level);
    }
}
