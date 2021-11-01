package itreact.tutorup.server.db;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface RequestsDao {
    void setRequest(Integer userId, String subject, String grade, String level, String meetingType, Date dateFrom, Date dateTo)throws SQLException;
    List<Map<String,Object>> listRequestsThatMatch(Integer userId, String subject, String grade, String level)throws SQLException;

    List<Map<String,Object>> search(String subject, String grade, String level, String meeting_type, Date dateFrom, Date dateTo)throws SQLException;

    void deleteRequest(int id, String subject, String grade, String level)throws SQLException;

    List<Map<String,Object>> listRequestsFromUser(int userId)throws SQLException;
}
