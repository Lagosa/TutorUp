package itreact.tutorup.server.ws;


import itreact.tutorup.server.service.SearchManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/search")
public class SearchWS {

    private static Logger log = LoggerFactory.getLogger(SearchWS.class);
    private final String className = "[SearchWs] ";

    /**
     * Used to search for offers/requests
     * @param wordsToSearch the stringwhat to search
     * @param type offer/request
     * @return
     */

    @Path("/word")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String wordSearch(@FormParam("text")String wordsToSearch, @FormParam("type") String type){
        Map<String, Object> result = new HashMap<>();
        try{
            result.put("result", SearchManager.getInstance().simpleSearch(wordsToSearch, type));
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
        	log.error("Exception occured while searching for [{}]", wordsToSearch, e);
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    @Path("/filterOffer")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String filterOfferSearch(@FormParam("subject") String subject, @FormParam("grade") String grade, @FormParam("level") String level,
                               @FormParam("meeting_type") String meetingType, @FormParam("dates")String date,
                               @FormParam("location")String location,@FormParam("number_of_students") Integer nrStudents){
        Map<String,Object> result = new HashMap<>();
        try{
            log.debug(className+"Got request, getting results...");
            result.put("result", SearchManager.getInstance().offerFilterSearch(subject,grade,level,location,meetingType,date,nrStudents));
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
            result.put("exception","");
            return new JSONObject(result).toString();
        }
    }

    @Path("/filterRequest")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String filterRequestSearch(@FormParam("subject") String subject, @FormParam("grade") String grade, @FormParam("level") String level,
                               @FormParam("meeting_type") String meetingType, @FormParam("dates")String date){
        Map<String,Object> result = new HashMap<>();
        try{
            log.debug(className+"Got request, getting results...");
            result.put("result", SearchManager.getInstance().requestFilterSearch(subject,grade,level,date,meetingType));
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch (Exception e){
            result.put("exception","");
            return new JSONObject(result).toString();
        }
    }

    @Path("/getSubjects")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getSubjects(){
        Map<String,Object> result = new HashMap<>();
        try{
            result.put("subjects",SearchManager.getInstance().getSubjects());
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch(Exception e){
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }

    @Path("/getGrades")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getGrades(){
        Map<String,Object> result = new HashMap<>();
        try{
            result.put("grades",SearchManager.getInstance().getGrades());
            result.put("exception","");
            return new JSONObject(result).toString();
        }catch(Exception e){
            result.put("exception","databaseError");
            return new JSONObject(result).toString();
        }
    }
}
