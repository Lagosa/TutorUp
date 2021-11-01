package itreact.tutorup.server.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import itreact.tutorup.server.config.ConfigurationFactory;
import itreact.tutorup.server.db.DatabaseFactory;
import itreact.tutorup.server.db.UserDto;
import itreact.tutorup.server.email.EmailSender;
import itreact.tutorup.server.ws.UsersWS;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserManager {
	
	private Logger log = LoggerFactory.getLogger(UserManager.class);
	
    private static UserManager ourInstance = new UserManager();

    public static UserManager getInstance() {
        return ourInstance;
    }

    private UserManager() {
    }

    private static String classname = "[UserManager] ";

    /**
     * Authenticate the user with given token.
     *
     * @param token the token of the user
     * @return the authenticated user
     */
    public UserDto authenticate(String token) throws SQLException {
        Map<String, Object> data = DatabaseFactory.getUserDao().findByToken(token);
        return (data == null) ? null : new UserDto(data);
    }

    /**
     * Authenticate the user with given e-mail and giver password.
     *
     * @param email    the e-mail address of the user
     * @param password the user password
     * @return the authenticated user
     */
    public Map<String, Object> authenticate(String email, String password) throws SQLException {
        String encryptedPassword = encryptPassword(password);
        log.debug("authenticate user with email=[{}] and encrypted password=[{}]", email, encryptedPassword);
        Map<String, Object> user = DatabaseFactory.getUserDao().findByEmailAndPassword(email, encryptedPassword);
        if (user != null) {
            UserDto userDto = new UserDto(user);
            if (!userDto.isActive()) {
                user.clear();
            }
        }
        return user;
    }

    public Map<String, Object> register(String first_name, String last_name, Integer birth_year, Integer birth_month,
                                        Integer birth_day, String username, String password, String email, String language,
                                        String phone_number, String skill, String city) throws SQLException{
        String encryptedPassword = encryptPassword(password);
        String token = generateToken();

        // Decide what to be the displayed name, saved from now on as username
        if(username == null || username.isEmpty())
        {
            username = first_name + " " + last_name;
        }

        Map<String, Object> errorData = new HashMap<>();
        // Validate on server-side
        if(password.length() < 8 )
        {
            errorData.put("token","133454");
            return errorData;
        }

        if(!email.contains("@") || !email.contains("."))
        {
            errorData.put("token","468464");
            return errorData;
        }


        // Register in database
        Map<String , Object> userData = DatabaseFactory.getUserDao().registration(first_name, last_name, birth_year, birth_month, birth_day, username, encryptedPassword,
                email,language,token,phone_number,skill,city);

        // Check if unique email and username
        if(userData != null && !userData.isEmpty()&& userData.get("username") != "3z5s9d5sa3dw1a35ds1a3sdaw5wqe564we984u" && userData.get("email") != "3z5s9d5sa3dw1a35ds1a3sdaw5wqe564we984e") {

            // Send confirmation email to user
            String link = generateActivationLink(token);
            String emailSubject = "Confirm your registration on TutorUp!";
            String emailContent = "Dear " + last_name + " " + first_name + ", <br><br>" +
                    "You just registered on TutorUp. " +
                    "<br>For the registration to be complete, and to make sure that you are our new friend you need to click on the link below." +
                    "<i><br> If you didn't registered to TutorUp, than disregard this message!</i><br>" +
                    "<br>" +
                    "<b>For the confirmation access the folowing link:</b><br>" +
                    link +
                    "<br><br><br>" +
                    "<i>Sincerely,<br>" +
                    "TutorUp team</i>";
            EmailSender.getInstance().sendEmail(email, emailSubject, emailContent);

            // Send welcome notification
            NotificationsManager.getInstance().save((int)userData.get("id"),"server","Welcome on board!", "Successfully registered on TutorUp." +
                    " Begin your journey by exploring the available offers, or jump in and place your offer right now!","","searchoffers");
        }

        return userData;

    }

    public Boolean change_password(String new_password, String old_password, int id) throws SQLException {
        String new_encryptedPassword = encryptPassword(new_password);
        String old_encryptedPassword = encryptPassword(old_password);

        Boolean result = DatabaseFactory.getUserDao().change_password(new_encryptedPassword, old_encryptedPassword, id);

        return result;
    }

    public Map<String, Object> update(String first_name, String last_name, Integer birth_year, Integer birth_month,
                                      Integer birth_day, String  username, String  email, String token, int id,
                                      String password, String status, String language, String phone_number, String skill, String city,
                                      String picture_url, String bio, String edu_backg)throws SQLException{

        return DatabaseFactory.getUserDao().update(first_name,last_name,birth_year,birth_month,birth_day,username,email, token, id, password, status,
                language, phone_number, skill, city, picture_url, bio, edu_backg);
    }

    public Boolean resetPassword(String email) throws SQLException{
        boolean result = false;
        String generatedPassword = Long.toString(System.currentTimeMillis(),36).toLowerCase();
        String encryptedGeneratedPassword = encryptPassword(generatedPassword);
        Map<String, Object> userData  = DatabaseFactory.getUserDao().findByEmail(email);
        int id = (int) userData.get("id");
        update("","",null,null,null,"","","", id, encryptedGeneratedPassword,"","","",
                "","","","","");

        String emailSubject = "Recover your Meet account!";
        String emailContent = "Dear "+userData.get("last_name")+" "+userData.get("first_name")+", <br><br>" +
                "Here is your brand new password: " +
                "<b>"+generatedPassword+"</b><br>" +
                "Don't forget, you can change it anytime! Just access the change password option in your profile.<br><br><br>" +
                "<i>Sincerely,<br>" +
                "Meet team</i>";
        EmailSender.getInstance().sendEmail(email,emailSubject,emailContent);

        result = true;

        return result;
    }
    public Boolean activateAccount(int id) throws SQLException{
        Map<String, Object> userData = DatabaseFactory.getUserDao().findById(id);
        String status = userData.get("status").toString();
        log.debug(classname+"Updating status for user {}",id);
        if(status.equals("NEW")){
            status = "ACTIVE";
            update("","",null,null,null,"","","",id,
                    "",status,"","","","","","","");
            return true;
        }else {
            return false;
        }

    }

    public Map<String, Object> listUserInformation(String username)throws SQLException{
        log.debug(classname+"Getting user information...");
        Map<String, Object> userInformation = DatabaseFactory.getUserDao().findByUsername(username);
        Map<String, Object> reviews = new HashMap<>();
        int userId = (int) userInformation.get("id");

        log.debug(classname+"Getting offers from user: {}",userId);
        userInformation.put("offers",DatabaseFactory.getOfferDao().listAllOffersFromTutor(userId));
        for (Map<String, Object> offer : ((List<Map<String, Object>>) userInformation.get("offers"))) {
            offer.put("photo", userInformation.get("picture_url"));
            offer.put("first_name", userInformation.get("first_name"));
            offer.put("last_name", userInformation.get("last_name"));
            offer.put("username", userInformation.get("username"));
        }
        log.debug("Getting requests for user {}",userId);
        userInformation.put("requests",DatabaseFactory.getRequestsDao().listRequestsFromUser(userId));
        for (Map<String, Object> request : ((List<Map<String, Object>>) userInformation.get("requests"))) {
            request.put("photo", userInformation.get("picture_url"));
            request.put("first_name", userInformation.get("first_name"));
            request.put("last_name", userInformation.get("last_name"));
            request.put("username", userInformation.get("username"));
        }
        log.debug(classname+"Getting list of reviews from user: {}",userId);
        reviews.put("listOfReviews",ReviewManager.getInstance().listReviewsOfUser(userId));
        log.debug(classname+"Getting average rating for user...");
        reviews.put("averageRating",DatabaseFactory.getReviewDao().getAverageRating(userId));
        userInformation.put("reviews",reviews);

        log.debug(classname+"Remove non-public information");
        userInformation.remove("id");
        userInformation.remove("token");
        return userInformation;
    }

    public String getToken(String username)throws SQLException {
        return (String) DatabaseFactory.getUserDao().findByUsername(username).get("token");
    }

    /**
     * Encrypt the password with SHA256
     *
     * @param password the clear text password
     * @return the encrypted password
     */
    private static String encryptPassword(String password) {
        return DigestUtils.sha256Hex("blank[" + password + "]password");
    }

    private static String generateToken(){return UUID.randomUUID().toString();}

    private static String generateActivationLink(String token){
        return ConfigurationFactory.getInstance().getSiteUrl() + "ws/users/activateAccount?token="+token;
    }
}
