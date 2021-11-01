package itreact.tutorup.server.service;

import itreact.tutorup.server.db.DatabaseFactory;
import itreact.tutorup.server.db.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.awt.image.DataBuffer;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchManager {
    private static SearchManager ourInstance = new SearchManager();
    public static SearchManager getInstance(){
        return ourInstance;
    }

    private static Logger log = LoggerFactory.getLogger(SearchManager.class);
    private final String className = "[SearchManager] ";

    public List<Map<String,Object>> simpleSearch(String text, String type)throws SQLException{
        List<Map<String,Object>> results = new ArrayList<>();
        text = text.toLowerCase();
        String[] words = textToWords(text);
        log.debug("simpleSearch - splitted words: {}", Arrays.asList(words));
        for(int i=0;i < words.length;i++){
            words[i] = words[i].toLowerCase() + "%";
        }
        log.debug("simpleSearch - splitted words with lover and %: {}", Arrays.asList(words));
        if("offer".equals(type)){
            results = offerSearch(words);
        }else if("request".equals(type)){
            results = requestSearch(words);
        }

        quickSort(results, 0,results.size()-1);
        //results = this.sortedSearchResults;

        return results;
    }


    public List<Map<String, Object>> requestFilterSearch(String subject, String grade, String level , String dateString, String meetingType)throws SQLException {
        Map<String, Date> date = new HashMap<>();
        if(dateString != null && !dateString.isEmpty()) date = OffersManager.getInstance().stringToDate(dateString);
        if(subject != null && !subject.isEmpty())subject = subject.toLowerCase();
        if(grade != null && !grade.isEmpty())grade = grade.toLowerCase();
        if(level != null && !level.isEmpty())level = level.toLowerCase();
        if(meetingType != null && !meetingType.isEmpty())meetingType = meetingType.toLowerCase();
        List<Map<String,Object>> searchResults = DatabaseFactory.getRequestsDao().search(subject,grade,level,meetingType,date.get("from"), date.get("to"));
        log.debug(className+"Got data from DB, defining rank...");

        searchResults = defineRequestRank(searchResults,subject,grade,level,meetingType, date);
        log.debug(className+"Sorting by rank...");
        searchResults = AutomaticMatchingAlgorithm.getInstance().sortBasedOnRank(searchResults);

        searchResults = attachUsers(searchResults,"studentId");

        log.debug(className+"Sorted!");
        return searchResults;
    }

    public List<Map<String, Object>> offerFilterSearch(String subject, String grade, String level, String location, String meetingType, String dateString, Integer nrStudents)throws SQLException {
        Map<String, Date> date = new HashMap<>();
        log.debug(className+"Converting data...");
        if(dateString != null && !dateString.isEmpty()) date = OffersManager.getInstance().stringToDate(dateString);
        if(subject != null && !subject.isEmpty())subject = subject.toLowerCase();
        if(grade != null && !grade.isEmpty())grade = grade.toLowerCase();
        if(level != null && !level.isEmpty())level = level.toLowerCase();
        if(meetingType != null && !meetingType.isEmpty())meetingType = meetingType.toLowerCase();
        if(location != null && !location.isEmpty())location = location.toLowerCase();
        log.debug(className+"Sending data to search...");
        List<Map<String,Object>> searchResults = DatabaseFactory.getOfferDao().search(subject,grade,level,location,meetingType,nrStudents,date.get("from"),date.get("to"));
        log.debug(className+"Got data from DB, defining rank...");

        searchResults = defineOfferRank(searchResults,subject,grade,level,location,meetingType,date,nrStudents);
        log.debug(className+"Sorting by rank...");
        searchResults = AutomaticMatchingAlgorithm.getInstance().sortBasedOnRank(searchResults);

        searchResults = attachUsers(searchResults,"tutorId");

        return searchResults;
    }

    private List<Map<String,Object>> defineRequestRank(List<Map<String,Object>> list, String subject, String grade, String level,String meetingType, Map<String, Date> date){
        for (Map<String, Object> request : list) {
            request.put("rank", 0);
            log.debug(className+"Calculating rank for request {}...", request.get("requestId"));
            if(date != null && !date.isEmpty() && date.get("to").compareTo((Date)request.get("dateFrom")) >= 0 && date.get("from").compareTo((Date)request.get("dateTo")) <= 0){
                request.put("rank",6);
            }

            if (subject != null && !subject.isEmpty() && grade != null && !grade.isEmpty() && subject.equals(request.get("subject")) && grade.equals(request.get("grade"))) {
                request.put("rank", (int) request.get("rank") + 10);
            }
            if((subject != null && !subject.isEmpty() && subject.equals(request.get("subject")) || (grade != null && !grade.isEmpty() &&  grade.equals(request.get("grade"))))){
                request.put("rank", (int) request.get("rank")+7);
            }
            if(level != null && !level.isEmpty() && level.equals(request.get("level"))){
                request.put("rank",(int)request.get("rank")+4);
            }
            if(meetingType != null && !meetingType.isEmpty() && meetingType.equals(request.get("meetingType"))){
                request.put("rank",(int)request.get("rank")+1);
            }
        }
        return list;
    }

    private List<Map<String,Object>> defineOfferRank(List<Map<String,Object>> list, String subject, String grade, String level,
                                                String location, String meetingType, Map<String, Date> date, Integer nrStudents){
        List<Map<String,Object>> dates;
        for (Map<String, Object> offer : list) {
            offer.put("rank", 0);
            log.debug(className+"Calculating rank for offer {}...", offer.get("offerId"));
            if(date != null && !date.isEmpty()) {
                dates = (List<Map<String, Object>>) offer.get("dates");
                log.debug(className + " - date");

                for (Map<String, Object> offerDate : dates) {
                    if (date != null && date.get("to").compareTo((Date) offerDate.get("dateFrom")) >= 0 && date.get("from").compareTo((Date) offerDate.get("dateTo")) <= 0) {
                        offer.put("rank", 6);
                    }
                }
            }
            log.debug(className+" - subject + grade");
            if (subject != null && !subject.isEmpty() && grade != null && !grade.isEmpty()  && subject.equals(offer.get("subject")) && grade.equals(offer.get("grade"))) {
                offer.put("rank", (int) offer.get("rank") + 10);
            }else
            if((subject != null && !subject.isEmpty() && subject.equals(offer.get("subject")) || (grade != null && !grade.isEmpty() &&  grade.equals(offer.get("grade"))))){
                offer.put("rank", (int) offer.get("rank")+7);
            }
            log.debug(className+" - level");
            if(level != null && !level.isEmpty() && level.equals(offer.get("level"))){
                offer.put("rank",(int)offer.get("rank")+4);
            }
            log.debug(className+" - location");
            if(location != null && !location.isEmpty() && location.equals(offer.get("location"))){
                offer.put("rank",(int)offer.get("rank")+3);
            }
            log.debug(className+" - nr students");
            if(nrStudents != null && (nrStudents == offer.get("nrStudents") || (nrStudents <= (int) offer.get("nrStudents") + 1 && nrStudents >= (int) offer.get("nrStudents") - 1))){
                offer.put("rank",(int)offer.get("rank")+2);
            }
            log.debug(className+" - meetingType");
            if(meetingType != null && !meetingType.isEmpty() && meetingType.equals(offer.get("meetingType"))){
                offer.put("rank",(int)offer.get("rank")+1);
            }
        }
        return list;
    }

    private List<Map<String,Object>> offerSearch(String[] words)throws SQLException{
        List<Map<String,Object>> searchResults = new ArrayList<>();
        for (String word : words) {
            searchResults.addAll(DatabaseFactory.getOfferDao().search(word,word,word,word,word,null,null,null));
        }

        // Remove duplicates
        searchResults = removeDuplicates(searchResults,"offerId");

        searchResults = attachUsers(searchResults,"tutorId");

        return searchResults;
    }

    public List<Map<String,Object>> attachUsers(List<Map<String,Object>> list, String id)throws SQLException{
        Map<String, Object> user;
        for(Map<String,Object> offer : list){
            log.debug("getting user for offer: {}", offer);
            log.debug("userId from offer with key={} :: {}", id, offer.get(id));
            user = DatabaseFactory.getUserDao().findById((int)offer.get(id));
            offer.put("username",user.get("username"));
            offer.put("photo",user.get("picture_url"));
            offer.put("skill", user.get("skill"));
            offer.put("first_name", user.get("first_name"));
            offer.put("last_name", user.get("last_name"));
            offer.put("token", user.get("token"));
            Integer avRating = ReviewManager.getInstance().getAverageRating((String)user.get("username"));
            offer.put("rating", avRating);
            //needed to display the ratings in search result
            Map<String, Object> ratingsMap = new HashMap<>();
            ratingsMap.put("averageRating", avRating);
            offer.put("reviews", ratingsMap);
        }
        return list;
    }

    private List<Map<String,Object>> requestSearch(String[] words)throws SQLException{
        List<Map<String,Object>> searchResults = new ArrayList<>();
        for(String word : words){
            searchResults.addAll(DatabaseFactory.getRequestsDao().search(word,word,word,word,null,null));
        }

        // Remove duplicates
        searchResults = removeDuplicates(searchResults, "requestId");

        searchResults = attachUsers(searchResults,"studentId");

        return searchResults;
    }

    private String[] textToWords(String text){
        return text.split(" ");
    }

    private List<Map<String,Object>> removeDuplicates(List<Map<String,Object>> results, String id){
        int offerId;
        for(int i=0;i<results.size();i++){
            offerId = (int) results.get(i).get(id);
            for(int j=i+1;j<results.size();j++){
                if(offerId == (int) results.get(j).get(id)){
                    results.remove(j);
                }
            }
        }
        return results;
    }
    
    private void quickSort(List<Map<String,Object>> sortedSearchResults, int beginning, int end){
    	if (sortedSearchResults == null || sortedSearchResults.isEmpty()) {
    		return;
    	}
        int i= beginning, j = end;

        String p = (String) sortedSearchResults.get((i+j)/2).get("subject");
        log.debug(className+"While loop...");
        while(i<j){
            log.debug(className+"while i...");
            while(p.compareTo((String)sortedSearchResults.get(i).get("subject")) > 0)i++;
            log.debug(className+"while j...");
            while(p.compareTo((String)sortedSearchResults.get(j).get("subject")) < 0)j--;
            log.debug(className+"if");
            if(i<=j){
                Map<String,Object> copy = sortedSearchResults.get(i);
                sortedSearchResults.add(i,sortedSearchResults.get(j));
                sortedSearchResults.remove(i+1);
                sortedSearchResults.add(j,copy);
                sortedSearchResults.remove(j+1);
                i++;
                j--;
            }
        }
        log.debug(className+"recursive...");
        if(beginning < j) quickSort(sortedSearchResults, beginning,j);
        if(i < end) quickSort(sortedSearchResults, i, end);
    }

    public List<String> getSubjects()throws SQLException {
        return DatabaseFactory.getSubjectsDao().getSubjects();
    }

    public List<String> getGrades()throws SQLException {
        return DatabaseFactory.getGradesDao().getGrades();
    }
}
