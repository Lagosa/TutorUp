package itreact.tutorup.server.ws;

import itreact.tutorup.server.db.UserDto;
import itreact.tutorup.server.service.NotificationsManager;
import itreact.tutorup.server.service.ReviewManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/review")
public class ReviewWS {
    private Logger log = LoggerFactory.getLogger(ReviewWS.class);
    private String className = "[ReviewWS] ";

    @Path("/set")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String setReview(@Context HttpServletRequest request, @FormParam("reviewedUsername") String username,
                            @FormParam("review") Integer review, @FormParam("comment") String comment){
        Map<String, Object> result = new HashMap<>();
        try{
            UserDto user = findUserInRequest(request);
            log.debug(className+"Got user, sending to manager... username: {}, comment: {}, review: {}",
                    username,comment,review);
            result = ReviewManager.getInstance().setReview(user.getId(),username,review, comment);

            result.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            return new JSONObject(result).toString();
        }catch (Exception e){
            result.put("exception", "databaseError");
            return new JSONObject(result).toString();
        }
    }

    @Path("/delete")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteReview(@Context HttpServletRequest request, @FormParam("reviewedUsername") String username){
        Map<String,Object> result = new HashMap<>();
        try{
            UserDto user = findUserInRequest(request);
            log.debug(className+"Got user, sending to manager...");
            result = ReviewManager.getInstance().deleteReview(user.getId(),username);

            result.put("notifications",NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    private UserDto findUserInRequest(HttpServletRequest request){
        UserDto user = (UserDto) request.getAttribute("user");
        log.info("user from request: {}", user);
        return user;
    }
}
