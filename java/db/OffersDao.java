package itreact.tutorup.server.db;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.Map;

public interface OffersDao {
    Map<String, Object> listOneOfferFromUser(Integer userId, String subject, String classnr, String level)throws SQLException;

    List<Map<String,Object>> listOffersFromUserOnGivenDate(Integer userId, String subject, String nrClass,String level, String status, Date dateFrom, Date dateTo,
                                                            Time hourFrom, Time hourTo, int type)throws SQLException;

    Boolean publishOffer(Integer userId, String subject, String nrclass, Integer nrStudents, String level,
                      String location, Integer price, Boolean periodically, String meetingType) throws SQLException;
    Boolean setNewOfferDate(Integer userId, String subject, String nrClass, String level,Date dateFrom, Date dateTo, Time hourFrom, Time hourTo, Integer type)throws SQLException;

    Boolean deleteOffer(Integer userId, String subject, String classnr, String level) throws SQLException;

    void deleteOfferDate(Integer offerId)throws SQLException;

    Boolean updateOffer(Integer userId, String subject, String nrClass, Integer price, String meetingType, String location,
                        Integer nrStudents, Boolean periodically, String status, String level)throws SQLException;

    void updateDate(Integer dateId, Date dateFrom, Date dateTo, Time hourFrom, Time hourTo, String status)throws SQLException;

    List<Map<String,Object>> listAllOffersFromTutor(int userId)throws SQLException;

    List<Map<String,Object>> listOffers(String subject, String grade, Date dateFrom, Date dateTo)throws SQLException;

    Map<String, Object> findOfferById(int id)throws SQLException;

    List<Map<String, Object>> search(String subject, String grade, String level, String location, String meetingType, Integer nrStudents, Date dateFrom, Date dateTo)throws SQLException;

    List<Map<String, Object>> listBasedOnDate(Date date)throws SQLException;

    List<Map<String,Object>> listOffersWithoutDate()throws SQLException;

    void updateOfferStatus(Integer offerId, String status)throws SQLException;
}
