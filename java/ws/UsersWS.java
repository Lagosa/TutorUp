package itreact.tutorup.server.ws;

import itreact.tutorup.server.service.NotificationsManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import itreact.tutorup.server.db.UserDto;
import itreact.tutorup.server.service.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
public class UsersWS {
	
	private Logger log = LoggerFactory.getLogger(UsersWS.class);
	
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public String authenticate(@FormParam("email") String email, @FormParam("password") String password,
                               @Context HttpServletRequest request) {
        Map<String,Object> exception = new HashMap<>();
        try {
            Map<String,Object> userData = UserManager.getInstance().authenticate(email, password);

            if (userData == null) {
                log.info("Could not find user with email: {} and password", email);
                exception.put("exception","authenticationException");
                return new JSONObject(exception).toString();
            } else if (userData.isEmpty()) {
            	log.info("User with email is not active: {}", email);
            	exception.put("exception","notActiveUser");
                return new JSONObject(exception).toString();
            }

            userData.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser((int)userData.get("id")));
            userData.put("exception","");
            return new JSONObject(userData).toString();
        }catch (Exception e) {
        	log.error("", e);
        	exception.put("exception","databaseError");
        	return new JSONObject(exception).toString();
        }
    }

    @POST
    @Path("/password_change")
    @Produces(MediaType.APPLICATION_JSON)
    public String password_change(@Context HttpServletRequest request, @FormParam("new_password") String new_password,
                                  @FormParam("old_password") String old_password){
        Map<String, Object> exception = new HashMap<>();
        try {
            UserDto user = findUserInRequest(request);
            Boolean rs = UserManager.getInstance().change_password(new_password, old_password, user.getId());
            if (!rs) {
                exception.put("exception","invalidOldPassword");
                return new JSONObject(exception).toString();
            }

            exception.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e){
        	log.error("", e);
        	exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public String register(@FormParam("first_name") String first_name, @FormParam("last_name") String last_name,
                           @FormParam("birth_year") Integer birth_year, @FormParam("birth_month") Integer birth_month,
                           @FormParam("birth_day") Integer birth_day, @FormParam("username") String username,
                           @FormParam("password") String password, @FormParam("email") String email,
                           @FormParam("lang") String language, @FormParam("phone_number") String phone_number,
                           @FormParam("skill") String skill, @FormParam("city") String city) {
        Map<String, String> exception = new HashMap<>();
        try {
        	log.debug("UserManager: ", UserManager.getInstance());
            Map<String, Object> registrationData = UserManager.getInstance().register(first_name, last_name, birth_year, birth_month,
                    birth_day, username, password, email, language, phone_number, skill, city);

            if (registrationData == null || registrationData.isEmpty()) {
                exception.put("exception","databaseError");
                return new JSONObject(exception).toString();
            }

            // Check if there were validation problems
            if(registrationData.get("token") == "133454")
            {
                exception.put("exception","passwordWeak");
                return new JSONObject(exception).toString();
            }

            if(registrationData.get("token") == "468464")
            {
                exception.put("exception","notEmail");
                return new JSONObject(exception).toString();
            }

            if(registrationData.get("username") == "3z5s9d5sa3dw1a35ds1a3sdaw5wqe564we984u"){
                exception.put("exception","registrationUsernameOccupied");
                return new JSONObject(exception).toString();
            }else if(registrationData.get("email") == "3z5s9d5sa3dw1a35ds1a3sdaw5wqe564we984e"){
                exception.put("exception","registrationEmailOccupied");
                return new JSONObject(exception).toString();
            }

            registrationData.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser((int)registrationData.get("id")));
            registrationData.put("exception","");
            return new JSONObject(registrationData).toString();
        }  catch (Exception e) {
        	log.error("", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }


    /**
     * it is used when the user changes its information from the user profile page
     */
    @POST
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public String update(@Context HttpServletRequest request,
                         @FormParam("first_name") String first_name, @FormParam("last_name") String last_name,
                         @FormParam("birth_year") Integer birth_year, @FormParam("birth_month") Integer birth_month,
                         @FormParam("birth_day") Integer birth_day, @FormParam("username") String username,
                         @FormParam("email") String email, @FormParam("lang") String language,
                         @FormParam("phone_number") String phone_number,
                         @FormParam("skill") String skill, @FormParam("city") String city,
                         @FormParam("picture_url") String picture_url,
                         @FormParam("biography") String bio, @FormParam("educational_background") String edu_backg)  {
        Map<String,String> exception = new HashMap<>();
        try {
            UserDto userDto = findUserInRequest(request);
            Map<String, Object> updatedData = UserManager.getInstance().update(first_name, last_name, birth_year, birth_month,
                    birth_day, username, email, "", userDto.getId(),"","", language, phone_number, skill, city,
                    picture_url, bio, edu_backg);
            if (updatedData == null || updatedData.isEmpty()) {
                exception.put("exception","databaseError");
                return new JSONObject(exception).toString();
            }
            if(updatedData.get("email") == "3z5s9d5sa3dw1a35ds1a3sdaw5wqe564we984e"){
                exception.put("exception","updateEmailOccupied");
                return new JSONObject(exception).toString();
            }
            if(updatedData.get("username") == "3z5s9d5sa3dw1a35ds1a3sdaw5wqe564we984u"){
                exception.put("exception","updateUsernameOccupied");
                return new JSONObject(exception).toString();
            }

            updatedData.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser((int)updatedData.get("id")));
            updatedData.put("exception","");
            return new JSONObject(updatedData).toString();
        } catch (Exception e) {
        	log.error("", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }

    @POST
    @Path("/reset_password")
    @Produces(MediaType.APPLICATION_JSON)
    public String reset_password(@FormParam("email") String email) {
        Map<String, String> exception = new HashMap<>();
        try{
            if(!UserManager.getInstance().resetPassword(email)){
                exception.put("exception","databaseError");
                return new JSONObject(exception).toString();
            }
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e){
        	log.error("", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }
    @GET
    @Path("/activateAccount")
    @Produces(MediaType.APPLICATION_JSON)
    public String activateAccount(@Context HttpServletRequest request){
        Map<String, String> exception = new HashMap<>();
        try{
            UserDto userDto = findUserInRequest(request);
            if(!UserManager.getInstance().activateAccount(userDto.getId())) {
                exception.put("exception","databaseError");
                return new JSONObject(exception).toString();
            }
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e){
        	log.error("", e);
            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }

    @POST
    @Path("/listUserInformation")
    @Produces(MediaType.APPLICATION_JSON)
    public String listUserInformation(@FormParam("username") String username){
    	log.debug("listUserInformation for username={}", username);
        Map<String, Object> result = new HashMap<>();
        try{
            result = UserManager.getInstance().listUserInformation(username);
            //there is no ID in the returned result object
            List<Map<String, Object>> notifications = NotificationsManager.getInstance().listNotificationsFromUser(username); 
            
            result.put("notifications", notifications);
            
            result.put("exception","");
            log.debug("listUserInformation - returns: {}", result);
            return new JSONObject(result).toString();
        }catch (Exception e){
        	log.error("", e);
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    @POST
    @Path("/getToken")
    public String getTokenByUsername(@FormParam("username") String username){
        Map<String,Object> result = new HashMap<>();
        try{
            result.put("token",UserManager.getInstance().getToken(username));
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
            log.error("Exception: ",e);
            result.put("exception", "databaseError");
            return new JSONObject(result).toString();
        }
    }

    @POST
    @Path("/getUsername")
    public String getUsernameByToken(@Context HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();
        try{
            result.put("username",findUserInRequest(request).getUsername());
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
            log.error("Exception: ",e);
            result.put("exception", "databaseError");
            return new JSONObject(result).toString();
        }
    }

    @POST
    @Path("/getNotifications")
    public String getNotifications(@Context HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();
        try{
            result.put("notifications",NotificationsManager.getInstance().getNotificationsFromUser(findUserInRequest(request).getId()));
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
            log.error("Exception: ",e);
            result.put("exception", "databaseError");
            return new JSONObject(result).toString();
        }
    }

    private UserDto findUserInRequest(HttpServletRequest request) {
        return (UserDto) request.getAttribute("user");
    }
}
