package itreact.tutorup.server.ws;

import itreact.tutorup.server.db.UserDto;
import itreact.tutorup.server.service.NotificationsManager;
import itreact.tutorup.server.service.RequestsManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Media;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/request")
public class RequestsWS {

    private final Logger log = LoggerFactory.getLogger(RequestsWS.class);
    private static String className = "[RequestWS] ";

    @Path("/set")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String setRequest(@Context HttpServletRequest request, @FormParam("subject") String subject, @FormParam("grade") String grade,
                           @FormParam("level") String level, @FormParam("meetingType") String meetingType, @FormParam("dates") String date){
        Map<String, Object> exception = new HashMap<>();
        try {
            UserDto user = findUserInRequest(request);
            Boolean result = RequestsManager.getInstance().setRequest(user.getId(), subject, grade, level, meetingType, date);
            if (!result) {

                log.debug(className + "Throwing requestAlreadyExisting exception!");
                exception.put("exception","requestAlreadyExisting");
                return new JSONObject(exception).toString();
            }
            exception.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            exception.put("exception","");
            return new JSONObject(exception).toString();
        }catch (Exception e){
            log.error("Error: ",e);

            exception.put("exception","databaseError");
            return new JSONObject(exception).toString();
        }
    }

    @Path("/delete")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteRequest(@Context HttpServletRequest request, @FormParam("subject") String subject, @FormParam("grade") String grade, @FormParam("level")String level){
        Map<String,Object> result = new HashMap<>();
        try{
            UserDto user = findUserInRequest(request);
            RequestsManager.getInstance().deleteRequest(user.getId(),subject,grade,level);
            result.put("notifications",NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
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
