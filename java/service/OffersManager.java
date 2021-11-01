package itreact.tutorup.server.service;

import itreact.tutorup.server.db.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OffersManager {
    private static OffersManager ourInstance = new OffersManager();

    public static OffersManager getInstance() {
        return ourInstance;
    }
    private final Logger log = LoggerFactory.getLogger(OffersManager.class);
    private String className = "[OffersManager] ";
    public int publishOffer(Integer userId, String subject, String classnr, Integer nrStudents, String level,
                             String location, Integer price, Boolean periodically,
                                String listOfDates, String listOfHours, String meetingType)throws SQLException, ParseException{

        // Delete expired offers
        deleteExpiratedOffers();

        // Convert string dates into separate dates
        // Sent date format: 2019-09-25_2019-09-27,2019-10-01_2019-10-15
        List<Map<String, Date>> dates = stringDatesToDates(listOfDates);
        log.debug(className+"Converted offer string list representing a date interval: {}", dates);

        // Convert string hours into separate strings
        // Sent date format: 12:00_14:15,16:20_19:45
        List<Map<String, Time>> hours = stringHoursToHours(listOfHours);
        log.debug(className+"Converted offer string list representing a hour interval: {}", hours);

        // Check if there is already an offer from the user with the same subject for the same class
        Map<String, Object> usersOffersOnDate = DatabaseFactory.getOfferDao().listOneOfferFromUser(userId, subject, classnr,level);

        if (!usersOffersOnDate.isEmpty()) {
            log.debug(className+"There is an existing offer with these information!");
            return 0;
        }
        log.debug(className+"Checked if already exists an offer like this!");

        // write into DB table the offer

        subject = subject.toLowerCase();
        classnr = classnr.toLowerCase();
        level = level.toLowerCase();
        location = location.toLowerCase();

        Boolean succes;
        succes = DatabaseFactory.getOfferDao().publishOffer(userId, subject, classnr, nrStudents, level, location, price,
                periodically, meetingType);
        log.debug(className+"Successfully written parent offer into DB!");

        for (int i = 0; i < dates.size() && succes; i++) {
            log.debug(className+"Writing offer number {} into DB...", i);
            succes = DatabaseFactory.getOfferDao().setNewOfferDate(userId,subject,classnr,level,dates.get(i).get("from"), dates.get(i).get("to"), hours.get(i).get("from"), hours.get(i).get("to"),1);
        }

        if (succes) {
            NotificationsManager.getInstance().save(userId,"server","Offer saved!","Your offer has been saved, you will get a notification when someone is interested in it! Hope to find the perfect candidate!","", "myoffers");
            log.debug(className+"Successfully saved the offer!");
            return 1;
        } else {
            log.debug(className+"There was a problem with saving the offer, now deleting its parts...");
            deleteOfferFromDB(userId, subject, classnr,level);
            log.debug(className+"Deleted the offer chunk!");
            return 2;
        }
    }

    // Updating information of an offer
    public Boolean updateOfferInformation(int userId, String subject, String nrClass, String level, Integer price, String meetingType, String location,
                                          Integer nrStudents, Boolean periodically)throws SQLException{
        // Delete expired offers
        deleteExpiratedOffers();

        log.debug(className+"Sending data to DB manager...");
        subject = subject.toLowerCase();
        nrClass = nrClass.toLowerCase();
        level = level.toLowerCase();
        if(!location.isEmpty())location = location.toLowerCase();

        return DatabaseFactory.getOfferDao().updateOffer(userId, subject, nrClass, price, meetingType, location, nrStudents, periodically, null,level);
    }

    // Updating date and/or hour status of an offer
    public Boolean updateOfferStatus(int userId, String subject, String nrClass,String level, String status,String date, String hour)throws SQLException, ParseException{
        // Delete expired offers
        deleteExpiratedOffers();

        log.debug(className+"Sending data to DB manager...");

        subject = subject.toLowerCase();
        nrClass = nrClass.toLowerCase();
        level = level.toLowerCase();

        // Convert string date, hour interval into date and time map
        Map<String, Date> dates = stringToDate(date);
        Map<String, Time> hours = stringToHour(hour);
        log.debug(className+"Getting offers hour {}-{}",hours.get("from"),hours.get("to"));
        // Get the date, dateid and hour of the offer that contains the sent date and hour
        List<Map<String, Object>> resultDates = DatabaseFactory.getOfferDao().listOffersFromUserOnGivenDate(userId, subject,
                nrClass,level,"ACTIVE",dates.get("from"),dates.get("to"), hours.get("from"), hours.get("to"),1);
        log.debug(className+"Got offer dates from DB, list size: {}",resultDates.size());
        if(resultDates.isEmpty()){
            DatabaseFactory.getOfferDao().setNewOfferDate(userId,subject,nrClass,level,dates.get("from"), dates.get("to"),hours.get("from"), hours.get("to"),1);
            return true;
        }
        // Go through the result dates and mark 'OCCUPIED' those who match entirely the date interval
        // if there is which who match only partially the given interval, get out the date or hour occupied and save the new date(s) and hour(s)
        Date resultDateFrom, resultDateTo, dateFrom = dates.get("from"), dateTo = dates.get("to");
        Time resultHourFrom, resultHourTo, hourFrom = hours.get("from"), hourTo = hours.get("to");
        Integer resultDateId, resultOfferId;
        log.debug(className+"Deciding which offer what case is and proceding: ");
        for(int i=0;i<resultDates.size();i++){
            resultDateFrom = (Date) resultDates.get(i).get("dateFrom");
            resultDateTo = (Date) resultDates.get(i).get("dateTo");
            resultHourFrom = (Time) resultDates.get(i).get("hourFrom");
            resultHourTo = (Time) resultDates.get(i).get("hourTo");
            resultDateId = (Integer) resultDates.get(i).get("dateId");
            resultOfferId = (Integer) resultDates.get(i).get("offerId");
            int r = dateFrom.compareTo(resultDateFrom);
            int r2 = dateTo.compareTo(resultDateTo);
            log.debug(className+"Offer {} result {} and {} date id {} :",i,r,r2,resultDateFrom);
            if(dateFrom.compareTo(resultDateFrom) <= 0 && dateTo.compareTo(resultDateTo) >= 0){
                // date sent contains the whole result date CASE 2
                // update the whole date interval
                log.debug(className+" DATE case 2");
                if(isCaseResultIsInsideSent2(hours,resultDates.get(i))){
                    // CASE 2 \hour
                    log.debug(className+" HOUR case 2");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,null,null,null,null,status);
                }else
                if(isCaseResultContainsSent4(hours,resultDates.get(i))){
                    // CASE 4 \hour
                    log.debug(className+" HOUR case 4 resultHourTo: {}",resultHourTo);
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,resultDateFrom,resultDateTo,hourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,resultDateTo, resultHourFrom, hourFrom,2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,resultDateTo, hourTo, resultHourTo,2);
                }else
                if(isCaseResultEndsWithSent1(hours,resultDates.get(i))){
                    // CASE 1 \hour
                    log.debug(className+" HOUR case 1");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId, resultDateFrom,resultDateTo,hourFrom,resultHourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,resultDateTo, resultHourFrom,hourFrom,2);
                }else
                if(isCaseResultBegginsWithSent3(hours,resultDates.get(i))){
                    // CASE 3 \hour
                    log.debug(className+" HOUR case 3");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId, resultDateFrom,resultDateTo, resultHourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,resultDateTo, hourTo,resultHourTo,2);
                }

            }else
            if(dateFrom.compareTo(resultDateFrom) > 0 && dateTo.compareTo(resultDateTo) < 0){
                // date sent is in the result date CASE 4
                // split the result date in two parts (DBfrom -> from, to -> DBto)
                log.debug(className+" DATE case 4");
                if(isCaseResultIsInsideSent2(hours,resultDates.get(i))) {
                    // CASE 2 \hour
                    log.debug(className+" HOUR case 2");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,dateFrom,dateTo,hourFrom,hourTo,status);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null, level,resultDateFrom, substractOneDay(dateFrom), resultHourFrom, resultHourTo, 2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null,level, addOneDay(dateTo), resultDateTo, resultHourFrom, resultHourTo, 2);
                }else
                if(isCaseResultContainsSent4(hours,resultDates.get(i))){
                    // CASE 4 \hour
                    log.debug(className+" HOUR case 4");
                    int offerId = resultOfferId;
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,dateFrom,dateTo,hourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(offerId,null,null,level, dateFrom,dateTo,resultHourFrom,hourFrom,2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(offerId,null,null,level, dateFrom,dateTo,hourTo,resultHourTo,2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(offerId,null,null,level,resultDateFrom,substractOneDay(dateFrom),resultHourFrom,resultHourTo,2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(offerId,null,null,level,addOneDay(dateTo),resultDateTo,resultHourFrom,resultHourTo,2);
                }else
                if(isCaseResultEndsWithSent1(hours,resultDates.get(i))){
                    // CASE 1 \hour
                    log.debug(className+" HOUR case 1");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,dateFrom, dateTo,hourFrom,resultHourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null,level, dateFrom, dateTo, resultHourFrom, hourFrom, 2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null,level, resultDateFrom, substractOneDay(dateFrom), resultHourFrom, resultHourTo, 2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null,level, addOneDay(dateTo), resultDateTo, resultHourFrom, resultHourTo, 2);
                }else
                if(isCaseResultBegginsWithSent3(hours,resultDates.get(i))){
                    // CASE 3 \hour
                    log.debug(className+" HOUR case 3");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,dateFrom, dateTo,resultHourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null, level,dateFrom, dateTo, hourTo,resultHourTo, 2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null,level, resultDateFrom, substractOneDay(dateFrom), resultHourFrom, resultHourTo, 2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId, null, null,level, addOneDay(dateTo), resultDateTo, resultHourFrom, resultHourTo, 2);
                }

            }else
            if(dateFrom.compareTo(resultDateTo) <= 0 && dateFrom.compareTo(resultDateFrom) > 0 &&
                dateTo.compareTo(resultDateTo) >= 0){
                // date sent begins in the result date and ends after it CASE 1
                // chop the end of the result interval
                log.debug(className+" DATE case 1");
                if(isCaseResultIsInsideSent2(hours,resultDates.get(i))){
                    // CASE 2 \hour
                    log.debug(className+" HOUR case 2");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId, dateFrom,resultDateTo, hourFrom, hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom, substractOneDay(dateFrom), resultHourFrom, (Time)resultDates.get(i).get("hourTo"),2);

                }else
                if(isCaseResultContainsSent4(hours,resultDates.get(i))){
                    // CASE 4 \hour
                    log.debug(className+" HOUR case 4");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,dateFrom,resultDateTo,hourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,dateFrom,resultDateTo,resultHourFrom,hourFrom,2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,dateFrom,resultDateTo,hourTo,resultHourTo,2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom, substractOneDay(dateFrom),resultHourFrom,resultHourTo,2);
                }else
                if(isCaseResultEndsWithSent1(hours,resultDates.get(i))){
                    // CASE 1 \hour
                    log.debug(className+" HOUR case 1");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId, dateFrom,resultDateTo,hourFrom,resultHourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,dateFrom, resultDateTo, resultHourFrom, hourFrom,2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom, substractOneDay(dateFrom), resultHourFrom, resultHourTo,2);
                }else
                if(isCaseResultBegginsWithSent3(hours,resultDates.get(i))){
                    // CASE 3 \hour
                    log.debug(className+" HOUR case 3");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId, dateFrom,resultDateTo,resultHourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,dateFrom, resultDateTo, hourTo,resultHourTo,2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom, substractOneDay(dateFrom), resultHourFrom, resultHourTo,2);
                }

            }else
            if(dateTo.compareTo(resultDateFrom) >= 0 && dateFrom.compareTo(resultDateFrom) <= 0 &&
                dateTo.compareTo(resultDateTo) < 0){
                // date sent ends with the result date beginning CASE 3
                // chop the beginning of the result interval
                log.debug(className+" DATE case 3");
                if(isCaseResultIsInsideSent2(hours,resultDates.get(i))){
                    // CASE 2 \hour
                    log.debug(className+" HOUR case 2");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,resultDateFrom,dateTo, hourFrom, hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,addOneDay(dateTo), resultDateTo, resultHourFrom, (Time)resultDates.get(i).get("hourTo"),2);
                }else
                if(isCaseResultContainsSent4(hours,resultDates.get(i))){
                    // CASE 4 \hour
                    log.debug(className+" HOUR case 4");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId,resultDateFrom,dateTo,hourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,dateTo,resultHourFrom,hourFrom,2);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,dateTo,hourTo,resultHourTo,2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,addOneDay(dateTo),resultDateTo,resultHourFrom,resultHourTo,2);
                }else
                if(isCaseResultEndsWithSent1(hours,resultDates.get(i))){
                    // CASE 1 \hour
                    log.debug(className+" HOUR case 1");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId, resultDateFrom,dateTo,hourFrom,resultHourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,dateTo, resultHourFrom, hourFrom,2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level, addOneDay(dateTo),resultDateTo, resultHourFrom, resultHourTo,2);
                }else
                if(isCaseResultBegginsWithSent3(hours,resultDates.get(i))){
                    // CASE 3 \hour
                    log.debug(className+" HOUR case 3");
                    DatabaseFactory.getOfferDao().updateDate(resultDateId, resultDateFrom,dateTo,resultHourFrom,hourTo,status);
                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null,level,resultDateFrom,dateTo, hourTo,resultHourTo,2);

                    DatabaseFactory.getOfferDao().setNewOfferDate(resultOfferId,null,null, level,addOneDay(dateTo),resultDateTo, resultHourFrom, resultHourTo,2);
                }
            }
        }
        return true;
    }

    public List<Map<String, Object>> listOffersFromUserOnGivenDate(Integer userId, String subject, String nrClass,String level, String date, String hour, String status)throws SQLException,ParseException{
        // Delete expired offers
        deleteExpiratedOffers();

        Map<String, Date> dDate = stringToDate(date);
        Map<String, Time> hTime = stringToHour(hour);
        return DatabaseFactory.getOfferDao().listOffersFromUserOnGivenDate(userId,subject,nrClass,level,status,dDate.get("from"),dDate.get("to"), hTime.get("from"), hTime.get("to"),1);
    }

    public boolean deleteOfferFromDB(int userId, String subject, String classnr, String level) throws SQLException{
        return DatabaseFactory.getOfferDao().deleteOffer(userId,subject,classnr,level);
    }

    public List<Map<String, Object>> listOffersFromTutor(int userId)throws SQLException{
        // Delete expired offers
        deleteExpiratedOffers();

        return DatabaseFactory.getOfferDao().listAllOffersFromTutor(userId);
    }

    public List<Map<String,Object>> listOffers(String subject, String grade, Date dateFrom, Date dateTo)throws SQLException
    {
        // Delete expired offers
        deleteExpiratedOffers();

        return DatabaseFactory.getOfferDao().listOffers(subject,grade,dateFrom,dateTo);
    }

    public Map<String,Object> findOfferById(int id)throws SQLException{
        // Delete expired offers
        deleteExpiratedOffers();

        return DatabaseFactory.getOfferDao().findOfferById(id);
    }

    // Substract one day from date
    private Date substractOneDay(Date date){
        Map<String, Integer> stringDate = convertDateToString(date);

        return new Date( stringDate.get("year") - 1900, stringDate.get("month") - 1, stringDate.get("day")-1);
    }

    // Add one day to date
    private Date addOneDay(Date date){
        Map<String, Integer> stringDate = convertDateToString(date);

        return new Date(stringDate.get("year")-1900, stringDate.get("month")-1, stringDate.get("day")+1);
    }

    private boolean isCaseResultEndsWithSent1(Map<String,Time> hours, Map<String,Object> resultDates){
        return hours.get("from").compareTo((Time) resultDates.get("hourFrom")) > 0 && hours.get("from").compareTo((Time) resultDates.get("hourTo")) < 0 &&
                hours.get("to").compareTo((Time) resultDates.get("hourTo")) >= 0;
    }

    private boolean isCaseResultIsInsideSent2(Map<String,Time> hours, Map<String,Object> resultDates){
        return hours.get("from").compareTo((Time) resultDates.get("hourFrom")) <= 0 && hours.get("to").compareTo((Time) resultDates.get("hourTo")) >= 0;
    }

    private boolean isCaseResultBegginsWithSent3(Map<String,Time> hours, Map<String,Object> resultDates){
        return hours.get("from").compareTo((Time) resultDates.get("hourFrom")) <= 0 && hours.get("to").compareTo((Time) resultDates.get("hourFrom")) >= 0 &&
                hours.get("to").compareTo((Time) resultDates.get("hourTo")) < 0 ;
    }

    private boolean isCaseResultContainsSent4(Map<String,Time> hours, Map<String,Object> resultDates){
        return hours.get("from").compareTo((Time) resultDates.get("hourFrom")) > 0 && hours.get("to").compareTo((Time) resultDates.get("hourTo")) < 0;
    }

    // Convert date to String
    private Map<String, Integer> convertDateToString(Date date){
        Map<String, Integer> result = new HashMap<>();

        String[] splitedDate = date.toString().split("-");

        result.put("year",Integer.parseInt(splitedDate[0]));
        result.put("month",Integer.parseInt(splitedDate[1]));
        result.put("day",Integer.parseInt(splitedDate[2]));

        return result;
    }

    // Convert string to date
    private List<Map<String,Date>> stringDatesToDates(String stringDate){
    	if (stringDate == null) {
    		return Collections.emptyList();
    	}
        String[] stringDates = stringDate.split(",");
        List<Map<String,Date>> dates = new ArrayList<>();
        for (String date : stringDates) {
            dates.add(stringToDate(date));
        }
        return dates;
    }

    // convert string to float representing hour
    private List<Map<String,Time>> stringHoursToHours(String stringHour)throws ParseException{
    	if (stringHour == null) {
    		return Collections.emptyList();
    	}
        String[] stringHours = stringHour.split(",");
        List<Map<String,Time>> hours = new ArrayList<>();
        for (String hour : stringHours) {
            hours.add(stringToHour(hour));
        }
        return hours;
    }

    // Converts a formated text list of dates into a map of Date types
    public Map<String, Date> stringToDate(String stringDate){
    	if (stringDate == null) {
    		return Collections.emptyMap();
    	}
        //2019-09-25_2019-09-27
        // separate string dates into  year,month,day format
        String[] stringDateFromAndTo = stringDate.split("_");
        String[] stringDateFromYMD = stringDateFromAndTo[0].split("-");
        String[] stringDateToYMD = stringDateFromAndTo[1].split("-");

        // convert string to date
        Map<String, Date> date = new HashMap<>();
        date.put("from",new Date( Integer.parseInt(stringDateFromYMD[0]) - 1900, Integer.parseInt(stringDateFromYMD[1]) - 1, Integer.parseInt(stringDateFromYMD[2])));
        date.put("to",new Date( Integer.parseInt(stringDateToYMD[0]) - 1900, Integer.parseInt(stringDateToYMD[1]) - 1, Integer.parseInt(stringDateToYMD[2])));

        return date;
    }

     // Converts a formated text list of hours into a map of float types representing hours
    public Map<String, Time> stringToHour(String stringHour)throws ParseException {
    	if (stringHour == null) {
    		return Collections.emptyMap();
    	}
        // 12:00_14:15
        Map<String, Time> hour = new HashMap<>();
        String[] stringHourFromAndTo = stringHour.split("_");
        DateFormat formatter =  new SimpleDateFormat("HH:mm");

        hour.put("from",new Time(formatter.parse( stringHourFromAndTo[0]).getTime()));

        hour.put("to",new Time(formatter.parse( stringHourFromAndTo[1]).getTime()));

        return hour;
    }

    private void deleteExpiratedOffers()throws SQLException{
        log.debug(className+"Deleting dates...");
        java.util.Date dateUtil = new java.util.Date();
        Date date = new Date(dateUtil.getTime());
        List<Map<String, Object>> offersExpirated = DatabaseFactory.getOfferDao().listBasedOnDate(date);
        if(offersExpirated.isEmpty()){
            log.debug(className+"No offer expired");
            return ;
        }
        log.debug(className+"Deleting dates...");
        for(Map<String,Object> offerDate : offersExpirated){
            DatabaseFactory.getOfferDao().updateDate((int)offerDate.get("dateId"),null,null,null,null,"INACTIVE");
        }

        deleteOfferWithoutDate();
    }
    private void deleteOfferWithoutDate()throws SQLException{
        log.debug(className+"Deleting offer without date...");
        log.debug(className+"Getting offer list...");
        List<Map<String,Object>> resultOffer = DatabaseFactory.getOfferDao().listOffersWithoutDate();
        log.debug(className+"Deleting....");
        for(Map<String,Object> offer:resultOffer){
            DatabaseFactory.getOfferDao().updateOfferStatus((int)offer.get("offerId"),"INACTIVE");
            log.debug(className+"Sending notification");
            NotificationsManager.getInstance().save((int)offer.get("tutorId"),"server","Offer without dates","Your offer for "+offer.get("subject")+", grade "+offer.get("nrClass")+", level " + offer.get("level") + " doesn't have any dates. " +
                    "If you wish to renew it please save another one.","./ws/offers/publish","publishOffer");
        }
    }
}
