package itreact.tutorup.server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestsDaoImpl implements RequestsDao {

    private ConnectionManager connectionManager;
    public RequestsDaoImpl(ConnectionManager connectionManager){this.connectionManager = connectionManager;}

    private final Logger log = LoggerFactory.getLogger(RequestsDaoImpl.class);
    private final String className = "[RequestsDaoImpl] ";

    private Map<String, Object> getById(Integer id)throws SQLException{
        Map<String, Object> result = new HashMap<>();
        log.debug(className+"Prepairing to get request informations...");
        String sql = "SELECT * FROM student_requests WHERE requestid = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,id);
            log.debug(className+"Executing getById query...");
            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    result = readRequestFromResultSet(rs);
                    log.debug(className+"Got request results!");
                }
            }
        }
        return result;
    }

    public void setRequest(Integer userId, String subject, String grade, String level, String meetingType, Date dateFrom, Date dateTo)throws SQLException{
        String sql = "INSERT INTO student_requests (studentid, subject, grade, teaching_level, meeting_type, datefrom, dateto) VALUES (?,?,?,?,?,?,?)";
        log.debug(className+"Establishing connection...");
        Map<String, Object> result = new HashMap<>();
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            log.debug(className+"Prepairing statements...");
            statement.setInt(1,userId);
            statement.setString(2,subject);
            statement.setString(3,grade);
            statement.setString(4,level);
            statement.setString(5,meetingType);
            statement.setDate(6,dateFrom);
            statement.setDate(7,dateTo);

            log.debug(className+"Executing the insert... {}", sql);
            statement.executeUpdate();

            /*
            try(ResultSet rs = statement.getGeneratedKeys()){
                if(rs.next()){
                    log.debug(className+"Getting inserted values...");
                    int id = rs.getInt("requestid");
                    result =  getById(id);
                    // ToDo: check why doesn't return the request informations
                    log.debug(className+"Returning...");
                }
            }
            */
        }
    }

    public List<Map<String,Object>> listRequestsThatMatch(Integer userId, String subject, String grade, String level)throws SQLException{
        String sql = "SELECT * FROM student_requests WHERE studentid = ? AND subject LIKE ? AND grade LIKE ? AND teaching_level LIKE ?";
        List<Map<String,Object>> result = new ArrayList<>();
        log.debug(className+"Prepairing to list requests that match parameters!");
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,userId);
            statement.setString(2, subject);
            statement.setString(3, grade);
            statement.setString(4,level);
            log.debug(className+"Prepaired statements!");

            try(ResultSet rs = statement.executeQuery()){
                log.debug(className+"Adding results from DB!");
                while(rs.next()){
                    result.add(readRequestFromResultSet(rs));
                }
            }
        }
        return result;
    }



    @Override
    public List<Map<String, Object>> search(String subject, String grade, String level, String meetingType, Date dateFrom, Date dateTo) throws SQLException {
        List<Map<String,Object>> results = new ArrayList<>();
        String sql = "SELECT * FROM student_requests WHERE subject LIKE ? OR grade LIKE ? OR teaching_level LIKE ? OR meeting_type LIKE ?";
        if(dateFrom != null && dateTo != null) sql += " AND datefrom <= ? AND dateto >= ?";
        sql += " AND status LIKE 'ACTIVE'";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1,subject);
            statement.setString(2,grade);
            statement.setString(3,level);
            statement.setString(4,meetingType);
            if(dateFrom != null && dateTo != null){
                statement.setDate(5,dateTo);
                statement.setDate(6,dateFrom);
            }

            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    results.add(readRequestFromResultSet(rs));
                }
            }
        }

        return results;
    }

    @Override
    public void deleteRequest(int id, String subject, String grade, String level) throws SQLException {
        String sql = "UPDATE student_requests SET status = ? WHERE studentid = ? AND subject LIKE ? AND grade LIKE ? AND teaching_level LIKE ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1,"INACTIVE");
            statement.setInt(2,id);
            statement.setString(3,subject);
            statement.setString(4,grade);
            statement.setString(5,level);

            statement.executeUpdate();
        }
    }

    @Override
    public List<Map<String, Object>> listRequestsFromUser(int userId) throws SQLException {
        List<Map<String,Object>> result = new ArrayList<>();
        String sql = "SELECT * FROM student_requests WHERE studentid = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setInt(1,userId);

            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    result.add(readRequestFromResultSet(rs));
                }
            }
        }
        return result;
    }

    private Map<String, Object> readRequestFromResultSet(ResultSet rs)throws SQLException{
        Map<String, Object> result = new HashMap<>();

        result.put("requestId",rs.getInt("requestid"));
        result.put("studentId", rs.getInt("studentid"));
        result.put("subject", rs.getString("subject"));
        result.put("grade", rs.getString("grade"));
        result.put("level", rs.getString("teaching_level"));
        result.put("meeting_type", rs.getString("meeting_type"));
        result.put("dateFrom", rs.getDate("datefrom"));
        result.put("dateTo", rs.getDate("dateto"));
        result.put("status", rs.getString("status"));
        result.put("acceptedOffer",rs.getInt("offerid_accepted"));


        return result;
    }
}
