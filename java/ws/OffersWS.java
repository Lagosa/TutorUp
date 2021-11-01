package itreact.tutorup.server.ws;

import itreact.tutorup.server.db.UserDto;
import itreact.tutorup.server.service.NotificationsManager;
import itreact.tutorup.server.service.OffersManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import java.util.*;

@Path("/offer")
public class OffersWS {
	
	private final Logger log = LoggerFactory.getLogger(OffersWS.class);
	
    /**
     * Tutor publishing an offer
     * @param request request sent containing the token
     * @param subject what class offers to held (eg. Math, Romanian, etc.)
     * @param classnr what grade the tutor offers to teach (eg. 11, 12, student, etc.)
     * @param nrStudents how many students the tutor accepts to teach in the same time
     * @param level on what level he knows to teach (beginner, intermediate, professional)
     * @param location in which city the tutor offers to teach
     * @param price the price of a one-hour session
     * @param periodically the mentioned date repeats, or it is a one-time offer (true = repeats, false = one-time)
     * @param dates a string representing the dates when available (eg. 2019-09-25_2019-09-27,2019-10-01_2019-10-15)
     * @param hours a string representing the hours when available on specific dates (eg. 12:00_14:15,16:20_19:45)
     * @param meetingType offline/online
     */
    @POST
    @Path("/publish")
    @Produces(MediaType.APPLICATION_JSON)
    public String tutorPublishingOffers(@Context HttpServletRequest request, @FormParam("subject") String subject,
                                        @FormParam("classnr") String classnr, @FormParam("nrStudents") Integer
                                        nrStudents, @FormParam("level") String level, @FormParam("location") String
                                        location, @FormParam("price") Integer price, @FormParam("periodically") Boolean
                                        periodically, @FormParam("dates") String dates, @FormParam("hours") String hours,
                                        @FormParam("meetingType") String meetingType){
        Map<String,Object> exception = new HashMap<>();
        try {
            UserDto user = findUserInRequest(request);
            int status;
            log.debug("Start saving the offer...");
            log.debug("subject={} :: classnr={} :: nrStudents={} :: level={} :: location={} :: price={} :: periodically={} :: dates={} :: hours={} :: meetingType={}",
            		subject, classnr, nrStudents, level, location, price, periodically, dates, hours, meetingType);
            
            status = OffersManager.getInstance().publishOffer(user.getId(), subject, classnr, nrStudents, level, location, price, periodically,
                    dates, hours, meetingType);
            if (status == 0) {
                log.error("Already exists an offer with these information");
                exception.put("exception","offerExistingOnThisDate");
                return new JSONObject(exception).toString();
            } else if (status == 2) {
                log.error("Unable to publish the offer!");
                exception.put("exception","unableToPublishOffer");
                return new JSONObject(exception).toString();
            }
            log.debug("Offer successfully saved!");

            exception.put("notifications",NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e) {
            log.error("Database error!", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }

    }

    /**
     * Updates the information of an offer -- can be used to delete (mark unavailable/occupied an offer)
     * @param request request containing the token
     * @param subject has the role of identifying offer, not updatable
     * @param nrClass has the role of identifying offer, not updatable
     * @param price the price per hour of a session
     * @param meetingType online/offline
     * @param location location of the meeting if offline
     * @param nrStudents number of maximum students accepted for one session
     * @param periodically the date repeats periodically or not

     */
    @POST
    @Path("/updateOffer")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateOfferInformation(@Context HttpServletRequest request, @FormParam("subject") String subject,
                              @FormParam("nrclass") String nrClass,
                              @FormParam("price") Integer price,@FormParam("level")String level, @FormParam("meeting_type") String meetingType,
                              @FormParam("location") String location, @FormParam("numberOfStudents") Integer nrStudents,
                              @FormParam("periodically") Boolean periodically){
        Map<String, Object> exception = new HashMap<>();
        try {
            UserDto user = findUserInRequest(request);
            log.debug("[USERWS] Updating information...");
            Boolean result = OffersManager.getInstance().updateOfferInformation(user.getId(),subject,nrClass,level,price,meetingType,location,nrStudents,
                    periodically);
            if(!result){
                exception.put("exception","databaseError");
                return new JSONObject(exception).toString();
            }
            log.debug("[USERWS] Information updated!");

            exception.put("notifications",NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e){
        	log.error("Database error!", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }

    /**
     * Occupies dates and hours in an offer, ads a date and hour if there isn't any on that date and hour
     * @param request request containing the user's token
     * @param subject has the role of identifying the offer
     * @param nrClass has the role of identifying the offer
     * @param status status to update the date(s) in DB
     * @param date date(s) that should be occupied
     * @param hour hour(s) that should be occupied
     */
    @POST
    @Path("/updateOfferDateStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateOfferDateStatus(@Context HttpServletRequest request, @FormParam("subject") String subject,
                                        @FormParam("nrClass")String nrClass, @FormParam("status")String status, @FormParam("date")String date,
                                        @FormParam("hour")String hour,@FormParam("level")String level){
        Map<String, Object> exception = new HashMap<>();
        try{
            UserDto user = findUserInRequest(request);
            log.debug("[USERWS] Updating status of an offer...");
            Boolean result = OffersManager.getInstance().updateOfferStatus(user.getId(), subject, nrClass,level,status,date,hour);
            if(!result){
                exception.put("exception","databaseError");
                return new JSONObject(exception).toString();
            }
            log.debug("[USERWS] Date and hour updated");

            exception.put("notifications",NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            exception.put("exception","");
            return new JSONObject(exception).toString();

        }catch (Exception e){
        	log.error("Database error!", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }

    @POST
    @Path("/listOffersFromTutor")
    @Produces(MediaType.APPLICATION_JSON)
    public String listOffersFromTutor(@Context HttpServletRequest request){
        Map<String, Object> exception = new HashMap<>();
        try {
            log.debug("Got request from web");
            UserDto user = findUserInRequest(request);
            log.debug("Got user based on token");
            List<Map<String, Object>> result = OffersManager.getInstance().listOffersFromTutor(user.getId());
            log.debug("Got result!");

            exception.put("list",result);
            exception.put("notifications",NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e){
        	log.error("Database error!", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }
    

    @POST
    @Path("/listOffersFromUserOnDate")
    @Produces(MediaType.APPLICATION_JSON)
    public String listOffersFromDate(@Context HttpServletRequest request, @FormParam("subject") String subject, @FormParam("grade") String nrClass,
                                     @FormParam("date") String date, @FormParam("hour") String hour, @FormParam("status") String status,@FormParam("level")String level){
        Map<String, Object> exception = new HashMap<>();
        try{
            UserDto user = findUserInRequest(request);
            exception.put("list",OffersManager.getInstance().listOffersFromUserOnGivenDate(user.getId(),subject,nrClass,level,date,hour, status));
            exception.put("notifications",NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e){
        	log.error("Database error!", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }

    // Get AutomaticMatchingAlgorithm results from notification
    @POST
    @Path("/getAMAresults")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAMAresults(@FormParam("requestId") Integer requestId, @FormParam("numberOfResults") Integer nrResults){
        Map<String,Object> result = new HashMap<>();
        try{
            result.put("list",NotificationsManager.getInstance().listAMAResults(requestId,nrResults));

            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
        	log.error("Database error!", e);
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    private UserDto findUserInRequest(HttpServletRequest request) {
        UserDto user = (UserDto) request.getAttribute("user");
        log.info("user from request: {}", user);
        return user;
    }
}

