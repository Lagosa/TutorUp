package itreact.tutorup.server.ws;

import itreact.tutorup.server.db.UserDto;
import itreact.tutorup.server.service.InteractionsManager;
import itreact.tutorup.server.service.NotificationsManager;
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

@Path("/interaction")
public class InteractionsWS {
    private static Logger log = LoggerFactory.getLogger(InteractionsWS.class);
    private final String className = "[InteractionsWS] ";

    /**
     * @param request          contains the sender's token
     * @param receiverUsername username of the user who's the offer/request is
     * @return exception/notification
     */
    @Path("/requestInteraction")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String requestInteraction(@Context HttpServletRequest request, @FormParam("receiverUsername") String receiverUsername, @FormParam("description") String description) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDto user = findUserInRequest(request);
            log.debug(className+"Got user: {}, saving interactions request",user.getId());
            InteractionsManager.getInstance().requestInteraction(user.getId(),receiverUsername,description);

            result.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser(user.getId()));
            result.put("exception", "");
            return new JSONObject(result).toString();
        }catch (Exception e){
            log.error("Error: ",e);
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    /**
     * Update an interactions status
     * @param senderToken token of the user who sent the interaction
     * @param request token with the id of user who updates the status and who received the interaction request
     * @param status DENIED/ACCEPTED
     * @return error/notification
     */
    @Path("/updateStatus")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String updateStatus(@Context HttpServletRequest request, @FormParam("senderToken")String senderToken,
                               @FormParam("status")String status){
        Map<String, Object> result = new HashMap<>();
        try {
            int receiverId = findUserInRequest(request).getId();

            InteractionsManager.getInstance().updateStatus(senderToken,receiverId,status);

            result.put("notifications", NotificationsManager.getInstance().listNotificationsFromUser(receiverId));
            result.put("exception", "");
            return new JSONObject(result).toString();
        }catch (Exception e){
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    @Path("/getInteractions")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getInteractions(@Context HttpServletRequest request){
        Map<String, Object > result = new HashMap<>();
        try{
            result.put("interactions",InteractionsManager.getInstance().getInteractions(findUserInRequest(request).getId()));
            result.put("exception","");

            return new JSONObject(result).toString();
        }catch (Exception e){
            log.error("Error: ",e);
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    private UserDto findUserInRequest(HttpServletRequest request){
        return (UserDto) request.getAttribute("user");
    }
}
