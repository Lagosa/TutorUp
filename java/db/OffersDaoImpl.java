package itreact.tutorup.server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class OffersDaoImpl implements OffersDao {
    private ConnectionManager connectionManager;
    public OffersDaoImpl(ConnectionManager connectionManager){this.connectionManager = connectionManager;}
    private final Logger log = LoggerFactory.getLogger(OffersDaoImpl.class);
    private String className = "[OfferDaoImpl] ";

    public Map<String, Object> listOneOfferFromUser(Integer userId, String subject, String classnr, String level) throws SQLException{
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT * FROM tutor_offers WHERE tutorID = ? AND subject LIKE ? AND grade = ? AND teaching_level LIKE ? AND status LIKE ?";
        String sqlDates = "SELECT * FROM tutor_offers_dates WHERE offerid = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            PreparedStatement statement2 = connection.prepareStatement(sqlDates)){
            statement.setInt(1,userId);
            statement.setString(2,subject);
            statement.setString(3,classnr);
            statement.setString(4,level);
            statement.setString(5,"ACTIVE");

            try(ResultSet rs = statement.executeQuery()) {
                if (rs.next()){
                    Map<String, Object> oneResult = readOffersFromResultSet(rs,1);
                    statement2.setInt(1,rs.getInt("offerid"));
                    List<Map<String, Object>> dateList = new ArrayList<>();
                    Map<String,Object> dateFromAndTo = new HashMap<>();
                    try(ResultSet rs_dates = statement2.executeQuery()){
                        while(rs_dates.next()){
                            dateFromAndTo.put("datefrom",rs_dates.getDate("datefrom"));
                            dateFromAndTo.put("dateto",rs_dates.getDate("dateto"));
                            dateFromAndTo.put("hourFrom", rs_dates.getTime("hourfrom"));
                            dateFromAndTo.put("hourTo",rs_dates.getTime("hourto"));
                            dateList.add(dateFromAndTo);
                        }
                    }
                    oneResult.put("dateAndTime",dateList);
                    result = oneResult;
                }
            }
        }
        return result;
    }
    public Boolean publishOffer(Integer userId, String subject, String nrclass, Integer nrStudents, String level,
                             String location, Integer price, Boolean periodically, String meetingType) throws SQLException{
        String sql = "INSERT INTO tutor_offers (tutorID,subject,teaching_level,grade,number_of_students,meeting_type," +
                "location, price, periodically) VALUES (?,?,?,?,?,?,?,?,?)";
        log.debug(className+"Prepairing sql code to publish the offer...");
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,userId);
            statement.setString(2,subject);
            statement.setString(3,level);
            statement.setString(4,nrclass);
            statement.setInt(5,nrStudents);
            statement.setString(6,meetingType);
            statement.setString(7,location);
            statement.setInt(8,price);
            if(periodically) statement.setInt(9,1);
                else statement.setInt(9,0);
            log.debug(className + "Statement prepared!");

            if(statement.executeUpdate() != 1){
                log.debug(className+"Unable to save the offer!");
                return false;
            }else {
                log.debug(className+"Successfully saved the offer!");
                return true;
            }
        }
    }

    public Boolean setNewOfferDate(Integer userId, String subject, String nrClass, String level,Date dateFrom, Date dateTo, Time hourFrom, Time hourTo, Integer type) throws SQLException{
        String sql = "INSERT INTO tutor_offers_dates (offerId, dateFrom, dateTo, hourFrom, hourTo) VALUES (?,?,?,?,?)";
        int offerid=0;
        if(type == 1) {
            offerid = getOfferId(userId, subject, nrClass, level);
        }
        Boolean result = false;
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            if(type == 1){
                statement.setInt(1, offerid);
            }else if(type == 2){
                statement.setInt(1,userId);
            }
            statement.setDate(2,dateFrom);
            statement.setDate(3,dateTo);
            statement.setTime(4,hourFrom);
            statement.setTime(5,hourTo);
            log.debug(className+"Writing date into DB...");

            if(statement.executeUpdate() != 0) result = true;
                else result = false;
        }
        log.debug(className+"Date has been written into DB...");
        return result;
    }

    public Boolean deleteOffer(Integer userId, String subject, String classnr, String level)throws SQLException{
        String sql = "DELETE FROM tutor_offers WHERE tutorid = ? AND subject LIKE ? AND grade LIKE ? AND teaching_level LIKE ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1,userId);
            statement.setString(2,subject);
            statement.setString(3,classnr);
            statement.setString(4,level);

            int modifiedRows = statement.executeUpdate();

            if(modifiedRows != 0 ){
                return true;
            }else{
                return false;
            }
        }
    }

    public void deleteOfferDate(Integer dateId)throws SQLException{
        String sql = "DELETE FROM tutor_offers_dates WHERE dateid = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,dateId);

            statement.executeUpdate();
        }
    }

    public Boolean updateOffer(Integer userId, String subject, String nrClass, Integer price, String meetingType, String location,
                               Integer nrStudents, Boolean periodically, String status, String level)throws SQLException{
        String sql = "UPDATE tutor_offers SET ";
        boolean result=false;
        log.debug("[OfferDaoImpl] Selecting sent information...");
        if(price != null && price != 0){
            sql += "price = ?, ";
        }
        if(!isNullorBlank(meetingType)){
            sql += " meeting_type = ?,";
        }
        if(!isNullorBlank(location)){
            sql += " location = ?,";
        }
        if(nrStudents != null && nrStudents != 0){
            sql += " number_of_students = ?,";
        }
        if(periodically != null){
            sql += " periodically = ?";
        }
        if(!isNullorBlank(status)){
            sql+= " status = ?";
        }

        if (sql.endsWith(",")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        sql += " WHERE tutorid = ? AND subject = ? AND grade = ? AND teaching_level LIKE ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            int db=0;
            log.debug("[OfferDaoImpl] Prepairing statement...");
            if(price!=null && price !=0){
                db++;
                statement.setInt(db,price);
            }
            if(!isNullorBlank(meetingType)){
                db++;
                statement.setString(db,meetingType);
            }
            if(!isNullorBlank(location)){
                db++;
                statement.setString(db,location);
            }
            if(nrStudents != null && nrStudents != 0){
                db++;
                statement.setInt(db,nrStudents);
            }
            if(periodically != null){
                db++;
                if(periodically) statement.setInt(db,1);
                    else statement.setInt(db,0);
            }
            if(!isNullorBlank(status)){
                db++;
                statement.setString(db, status);
            }
            db++;
            statement.setInt(db,userId);
            statement.setString(db+1, subject);
            statement.setString(db+2,nrClass);
            statement.setString(db+3,level);
            log.debug("[OfferDaoImpl] Executing update... Columns affected: {}",db+3);
            Integer nrRowsAffected = statement.executeUpdate();
            log.debug("[OfferDaoImpl] Update executed...");
            if(nrRowsAffected == 1) result = true;
                else result = false;
        }
        return result;
    }

    public void updateDate(Integer dateId, Date dateFrom, Date dateTo, Time hourFrom, Time hourTo, String status)throws SQLException{



        String sql = "UPDATE tutor_offers_dates SET ";
        log.debug(className+"Preparing sql command for updating date {}",dateId);
        if(dateFrom != null){
            sql += "datefrom = ?, ";
        }
        if(dateTo != null){
            sql += "dateto = ?, ";
        }
        if(hourFrom != null){
            sql += "hourfrom = ?, ";
        }
        if(hourTo != null){
            sql += "hourto = ?, ";
        }
        if(!isNullorBlank(status)){
            sql += "status = ?";
        }

        if (sql.endsWith(",")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        sql += " WHERE dateid = ?";
        log.debug(className+"Preparing connection.");
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            int db=0;
            log.debug(className+"Adding variable values.");
            if(dateFrom != null){
                db++;
                statement.setDate(db,dateFrom);
            }
            if(dateTo != null){
                db++;
                statement.setDate(db,dateTo);
            }
            if(hourFrom != null){
                db++;
                statement.setTime(db,hourFrom);
            }
            if(hourTo != null){
                db++;
                statement.setTime(db,hourTo);
            }
            if(!isNullorBlank(status)){
                db++;
                statement.setString(db,status);
            }
            db++;
            statement.setInt(db,dateId);
            log.debug(className+" Sql command prepaired!");
            statement.executeUpdate();
            log.debug(className+" Update executed!");
        }
    }

    public List<Map<String, Object>> listOffersFromUserOnGivenDate(Integer userId, String subject, String nrClass, String level,String status, Date dateFrom, Date dateTo,
                                                                   Time hourFrom, Time hourTo, int type)throws SQLException{
        List<Map<String, Object>> result = new ArrayList<>();
        int offerId;
        if(type == 1) {
            offerId = getOfferId(userId, subject, nrClass,level);
        }else {
            offerId = userId;
        }
        String sql = "SELECT * FROM tutor_offers_dates WHERE offerid = ? AND datefrom <= ? AND dateto >= ?  AND hourfrom <= ? AND hourto >= ?";
        if(!isNullorBlank(status)){
            sql+=" AND status LIKE ?";
        }
        log.debug("[OfferDaoImpl] Preparing to list offers from user {} with offer id {} on date {} -> {}",userId,offerId,dateFrom,dateTo);
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,offerId);
            statement.setDate(2,dateTo);
            statement.setDate(3,dateFrom);
            statement.setTime(4,hourTo);
            statement.setTime(5,hourFrom);
            if(!isNullorBlank(status)){
                statement.setString(6,status);
            }
            log.debug("[OfferDaoImpl] Executing query...");
            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    result.add(readOffersFromResultSet(rs,2));
                    log.debug(className+" Got offer dates!");
                }
            }
        }
        return result;
    }

    public List<Map<String, Object>> listAllOffersFromTutor(int userId) throws SQLException{
        String sql = "SELECT * FROM tutor_offers WHERE tutorid = ?";
        String sql2 = "SELECT * FROM tutor_offers_dates WHERE offerid = ?";
        List<Map<String, Object>> result = new ArrayList<>();
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            PreparedStatement statement2 = connection.prepareStatement(sql2)){
            log.debug(className+" Prepairing statements for DB!");
            statement.setInt(1,userId);
            log.debug(className+"Executing select1 query");
            try(ResultSet rs = statement.executeQuery()){
                Map<String,Object> resultMap = new HashMap<>();
                while(rs.next()){
                    resultMap = readOffersFromResultSet(rs,1);
                    log.debug(className+"Adding offer results for offer id: {}", resultMap.get("offerId"));
                    statement2.setInt(1, rs.getInt("offerid"));
                    log.debug(className+"Executing select2 query");
                    try(ResultSet rs2 = statement2.executeQuery()){
                        List<Map<String, Object>> resultDates = new ArrayList<>();
                        while(rs2.next()){
                            resultDates.add(readOffersFromResultSet(rs2,2));
                            log.debug(className+"Added date results for offer id: {}", resultMap.get("offerId"));
                        }
                        resultMap.put("date_list",resultDates);
                        log.debug(className+"Added date list to map");
                    }
                    result.add(resultMap);
                    log.debug(className+"Added result to list, size: {}",result.size());
                }
            }
            log.debug(className+"Got results!");
        }
        return result;
    }

    public List<Map<String,Object>> listOffers(String subject, String grade, Date dateFrom, Date dateTo)throws SQLException{
        String sqlOffer = "SELECT * FROM tutor_offers WHERE subject = ? AND grade = ? AND status = 'ACTIVE'";
        String sqlDate = "SELECT * FROM tutor_offers_dates WHERE offerid = ? AND status = 'ACTIVE'";

        if(dateFrom != null && dateTo != null){
            sqlDate += " AND (datefrom <= ? AND dateto >= ? )";
        }

        List<Map<String, Object>> result = new ArrayList<>();

        log.debug(className+"Getting connection...");
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statementOffer = connection.prepareStatement(sqlOffer);
            PreparedStatement statementDate = connection.prepareStatement(sqlDate)){

            log.debug(className+"Preparing statementOffer...");
            statementOffer.setString(1,subject);
            statementOffer.setString(2,grade);

            log.debug(className+"Getting result for offers...");
            try(ResultSet rs = statementOffer.executeQuery()){
                log.debug(className+"Getting results...");
                Map<String,Object> offer;
                while(rs.next()){
                    //offer = readOffersFromResultSet(rs,1);
                    offer = new HashMap<>();
                    offer.put("tutorId",rs.getInt("tutorid"));
                    offer.put("offerId",rs.getInt("offerid"));
                    offer.put("level",rs.getString("teaching_level"));

                    log.debug(className+"Read offers from resultset!");
                    statementDate.setInt(1, (int) offer.get("offerId"));
                    log.debug(className+"Set date statement offer id!");

                    if(dateFrom != null && dateTo != null){
                        log.debug(className+"Setting datefrom: {}, dateto: {}",dateFrom,dateTo);
                        statementDate.setDate(2,dateTo);
                        statementDate.setDate(3,dateFrom);
                    }

                    List<Map<String,Object>> dates = new ArrayList<>();
                    Map<String,Object> dateInfo = new HashMap<>();

                    log.debug(className+"Getting dates for offerId {}",offer.get("offerId"));
                    try(ResultSet rsd = statementDate.executeQuery()){
                        while(rsd.next()){
                            log.debug(className+"Reading date result...");
                            dateInfo.put("offerId",rsd.getInt("offerid"));
                            dateInfo.put("dateFrom",rsd.getDate("datefrom"));
                            dateInfo.put("dateTo",rsd.getDate("dateto"));
                            dates.add(dateInfo);
                            //dates.add(readOffersFromResultSet(rsd,2));
                        }
                    }

                    log.debug(className+"Saving result...");
                    offer.put("dates",dates);
                    offer.put("rating",DatabaseFactory.getReviewDao().getAverageRating((int)offer.get("tutorId")));
                    result.add(offer);
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> findOfferById(int id) throws SQLException {
        Map<String, Object> offer = new HashMap<>();
        String getOfferSql = "SELECT * FROM tutor_offers WHERE offerid = ?";
        String getDateSql  = "SELECT * FROM tutor_offers_dates WHERE offerid = ?";

        try(Connection connection = connectionManager.getConnection();
            PreparedStatement offerStatement = connection.prepareStatement(getOfferSql);
            PreparedStatement dateStatement = connection.prepareStatement(getDateSql)){

            offerStatement.setInt(1,id);

            try(ResultSet rs = offerStatement.executeQuery()){
                if(rs.next()){
                    offer = readOffersFromResultSet(rs,1);

                    dateStatement.setInt(1,id);
                    try(ResultSet rsd = dateStatement.executeQuery()){
                        while(rsd.next()){
                            offer.put("date",readOffersFromResultSet(rsd,2));
                        }
                    }
                }
            }
        }

        return offer;
    }

    @Override
    public List<Map<String, Object>> search(String subject, String grade, String level, String location, String meetingType, Integer nrStudents, Date dateFrom, Date dateTo)throws SQLException {
        List<Map<String,Object>> result = new ArrayList<>();
        log.debug(className+"Preparing string statement...");
        String sqlOffer = "SELECT * FROM tutor_offers WHERE (";
        if(subject != null && !subject.isEmpty()){
            sqlOffer += " subject ILIKE ? OR";
        }
        if(level != null && !level.isEmpty()){
            sqlOffer += " teaching_level ILIKE ? OR";
        }
        if(grade != null && !grade.isEmpty()){
            sqlOffer += " grade ILIKE ? OR";
        }
        if(meetingType != null && !meetingType.isEmpty()){
            sqlOffer += " meeting_type ILIKE ? OR";
        }
        if(location != null && !location.isEmpty()){
            sqlOffer += " location ILIKE ? OR";
        }
        if(nrStudents != null) sqlOffer +=" number_of_students = ?";

        if (sqlOffer.endsWith("R")) {
            sqlOffer = sqlOffer.substring(0, sqlOffer.length() - 2);
        }

        sqlOffer += ") AND status LIKE 'ACTIVE'";


        String sqlDate = "SELECT * FROM tutor_offers_dates WHERE offerid = ?";

        if(dateFrom != null && dateTo != null) sqlDate += " AND datefrom <= ? AND dateto >= ?";
        sqlDate += " AND status LIKE 'ACTIVE'";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statementOffer = connection.prepareStatement(sqlOffer);
            PreparedStatement statementDate = connection.prepareStatement(sqlDate)){
            log.debug(className+"Preparing offer statement");
            int db=0;
            if( subject != null && !subject.isEmpty()){
                db++;
                statementOffer.setString(db,subject);
            }
            if(level != null && !level.isEmpty()){
                db++;
                statementOffer.setString(db,level);
            }
            if(grade != null && !grade.isEmpty()){
                db++;
                statementOffer.setString(db,grade);
            }
            if(meetingType != null && !meetingType.isEmpty()){
                db++;
                statementOffer.setString(db,meetingType);
            }
            if(location != null && !location.isEmpty()){
                db++;
                statementOffer.setString(db,location);
            }

            if(nrStudents != null){
                db++;
                statementOffer.setInt(db,nrStudents);
            }

            log.debug(className + "Prepaired statements " + sqlOffer);

            log.debug(className+"Getting offer results... s {},g {},l {}, mt {}, l {}, nr {}", subject,grade,level,meetingType,location,nrStudents);
            try(ResultSet rs = statementOffer.executeQuery()){
            	log.debug("After executeQuery");
                Map<String,Object> offerResult;
                while(rs.next()){
                    log.debug("Adding offer with id: {}", rs.getInt("offerid"));
                    offerResult = readOffersFromResultSet(rs,1);
                    statementDate.setInt(1,rs.getInt("offerid"));
                    if(dateFrom != null && dateTo != null){
                        statementDate.setDate(2,dateTo);
                        statementDate.setDate(3,dateFrom);
                    }
                    List<Map<String,Object>> dateResult = new ArrayList<>();
                    log.debug(className+"Getting date results...");
                    try(ResultSet rsd = statementDate.executeQuery()){
                        while(rsd.next()){
                            log.debug(className+"Adding date with id {}", rsd.getInt("dateid"));
                            dateResult.add(readOffersFromResultSet(rsd,2));
                        }
                    }
                    log.debug(className+"Saving result...");
                    offerResult.put("dates",dateResult);
                    result.add(offerResult);
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listBasedOnDate(Date date) throws SQLException {
        String sql = "SELECT * FROM tutor_offers_dates WHERE dateto < ? AND status LIKE 'ACTIVE'";
        List<Map<String,Object>> result = new ArrayList<>();
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            log.debug(className+"Preparing statement... date: {}",date);
            statement.setDate(1,date);

            try(ResultSet rs = statement.executeQuery()){
                log.debug(className+"Getting results...");
                while(rs.next()){
                    result.add(readOffersFromResultSet(rs,2));
                }
                log.debug(className+"Got results... ");
            }
        }
        return result;
    }

    @Override
    public List<Map<String,Object>> listOffersWithoutDate() throws SQLException {
        String sqlOffer = "SELECT * FROM tutor_offers WHERE status LIKE 'ACTIVE'";
        String sqlDate = "SELECT dateid FROM tutor_offers_dates WHERE offerid = ? AND status LIKE 'ACTIVE'";
        List<Map<String,Object>> result = new ArrayList<>();
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statementOffer = connection.prepareStatement(sqlOffer);
            PreparedStatement statementDate = connection.prepareStatement(sqlDate)){
            log.debug(className+"Getting active offers..");
            try(ResultSet rsOffer = statementOffer.executeQuery()){
                log.debug(className+"Getting dates...");
                while(rsOffer.next()){
                    statementDate.setInt(1,rsOffer.getInt("offerid"));
                    log.debug(className+"Executing date statement...");
                    try(ResultSet rs = statementDate.executeQuery()){
                        if(!rs.next()){
                            log.debug(className+"No active date found...");
                            result.add(readOffersFromResultSet(rsOffer,1));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void updateOfferStatus(Integer offerId, String status) throws SQLException {
        String sql="UPDATE tutor_offers SET status = ? WHERE offerid = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1,status);
            statement.setInt(2,offerId);

            log.debug(className+"Executing update...");
            statement.executeUpdate();
        }
    }

    private Boolean isNullorBlank(String info){
        return info == null || info.trim().length() == 0;
    }

    private int getOfferId(Integer userId, String subject, String nrClass, String level) throws SQLException{
        String sql = "SELECT * FROM tutor_offers WHERE tutorid = ? AND grade LIKE ? AND subject LIKE ? AND teaching_level LIKE ?";
        int result = 0;
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            log.debug(className+"Preparing statement grade: {} and subject {}", nrClass, subject);
            statement.setInt(1,userId);
            statement.setString(2, nrClass);
            statement.setString(3, subject);
            statement.setString(4,level);

            log.debug(className+"Getting parent offer id!");
            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    result = rs.getInt("offerid");
                    log.debug(className+"Got parent offer id: {} ", result);
                }
            }
        }
        return result;
    }

    private Map<String, Object> readOffersFromResultSet(ResultSet rs, int type) throws SQLException{
        Map<String, Object> result = new HashMap<>();
        if(type == 1){
            result.put("tutorId", rs.getInt("tutorid"));
            result.put("subject", rs.getString("subject"));
            result.put("nrClass", rs.getString("grade"));
            result.put("level", rs.getString("teaching_level"));
            result.put("nrStudents", rs.getInt("number_of_students"));
            result.put("meetingType", rs.getString("meeting_type"));
            result.put("location",rs.getString("location"));
            result.put("price", rs.getInt("price"));
            result.put("periodically", rs.getInt("periodically"));
            result.put("offerId",rs.getInt("offerid"));
            result.put("status",rs.getString("status"));
            result.put("created_at", rs.getTimestamp("created_at"));
        }
        if(type == 2){
            result.put("dateFrom", rs.getDate("datefrom"));
            result.put("dateTo", rs.getDate("dateto"));
            result.put("hourFrom", rs.getTime("hourfrom"));
            result.put("hourTo", rs.getTime("hourto"));
            result.put("dateId",rs.getInt("dateid"));
            result.put("offerId",rs.getInt("offerid"));
            result.put("status",rs.getString("status"));
        }
        return result;
    }
}
