package itreact.tutorup.server.service;


import itreact.tutorup.server.config.ConfigurationFactory;
import itreact.tutorup.server.db.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

public class AutomaticMatchingAlgorithm {
    public static AutomaticMatchingAlgorithm ourInstance = new AutomaticMatchingAlgorithm();
    public static AutomaticMatchingAlgorithm getInstance(){return ourInstance;}
    private final Logger log = LoggerFactory.getLogger(AutomaticMatchingAlgorithm.class);
    private final String className = "[AMA] ";

    public void triggerMatching(Integer studentId, String subject, String grade, String level, Date dateFrom, Date dateTo)throws SQLException {
        log.debug(className+"Triggered matching algorithm!");
        // - search for tutors with the exact same subject, grade
        // - rank results by level (the exact match the top, than the levels in order of how close are to the requested level)
        // - rank results based on how much days correspond
        //
        // - if there are results with the same rank than decide based on ratings of the tutor
        log.debug(className+"Getting offers that are relevant...");
        List<Map<String, Object>> offersList = OffersManager.getInstance().listOffers(subject,grade, dateFrom,dateTo);

        if(offersList.size() == 0){
            return ;
        }


        log.debug(className+"Ranking offers... list size: {}",offersList.size());
        for(int i=0;i<offersList.size();i++){
            offersList.add(i,setRank(offersList.get(i),level,dateFrom,dateTo));
            offersList.remove(i+1);
        }

        log.debug(className+"Sorting offers based on rank values...");
        offersList = sortBasedOnRank(offersList);

        int requestId = (int) DatabaseFactory.getRequestsDao().listRequestsThatMatch(studentId,subject,grade,level).get(0).get("requestId");

        log.debug(className+"Ordering offers with same rank...");
        // if there are offers with the same rank, it will be ordered based on rating
        for(int i=0;i<offersList.size();i++){
            if(i < offersList.size()-1 &&(int)offersList.get(i).get("rank") == (int)offersList.get(i+1).get("rank") && (int)offersList.get(i).get("rating")>(int)offersList.get(i+1).get("rating")){
                Map<String, Object> copyOfOffer = offersList.get(i);
                log.debug(className+"Swapping offers...");
                offersList.add(i,offersList.get(i+1));
                offersList.remove(i+1);
                offersList.add(i+1,copyOfOffer);
                offersList.remove(i+2);
            }
            log.debug(className+"Saving notifications with the AMA offer results...");
            int offerId = (int)offersList.get(i).get("offerId");
            NotificationsManager.getInstance().save(offerId,studentId+"",requestId+"",i+"","AMA","AMAresults");

        }

        log.debug(className+"Writing result to notification system...");
        NotificationsManager.getInstance().save(studentId,"AMA","Good news!","We found you some offers that might be interesting for you!",  requestId+"",offersList.size()+"");
    }

    private Map<String,Object> setRank(Map<String, Object> offer, String levelRequest, Date dateFromRequest, Date dateToRequest){
        log.debug(className+"Setting rank based on the level...");
        offer.put("rank",determineLevelRank((String) offer.get("level"),levelRequest));

        log.debug(className+"Setting rank based on date...");
        List<Map<String, Object>> dateList = (List<Map<String,Object>>) offer.get("dates");
        for(int i=0; i<dateList.size(); i++) {
            if (dateFromRequest != null && dateToRequest != null) {
                offer.put("rank", (int) offer.get("rank") + determineDateRank((Date) dateList.get(i).get("dateFrom"), (Date) dateList.get(i).get("dateTo"), dateFromRequest, dateToRequest));
            } else {
                offer.put("rank", (int) offer.get("rank") + determineIntervalRank((Date) dateList.get(i).get("dateFrom"), (Date) dateList.get(i).get("dateTo")));
            }
        }

        return offer;
    }

    public List<Map<String,Object>> sortBasedOnRank(List<Map<String,Object>> list){
        log.debug(className+"Sorting values with quicksort...");
        if(list.size()!=0){
            quickSort(list,0,list.size()-1);
        }

        return list;
    }

    private void quickSort(List<Map<String, Object>> listOfOffers , int beginning, int end){
        int i = beginning, j = end;
        int p = (int) listOfOffers.get((i+j)/2).get("rank");
        log.debug(className+"Quick sort (beginning: {}, ending {})",beginning,end);

        while(i<j){
            log.debug(className+"while i");
             while((int) listOfOffers.get(i).get("rank") > p){
                 i++;
             }
             log.debug(className+"while j");
             while((int) listOfOffers.get(j).get("rank") < p){
                 j--;
             }
            log.debug(className+"Swap offers (i1: {}, i2: {}), ranks(i1: {}, i2: {})... ",i,j,listOfOffers.get(i).get("rank"),listOfOffers.get(j).get("rank") );
             if(i<=j){

                Map<String, Object> copy = listOfOffers.get(j);
                listOfOffers.add(j,listOfOffers.get(i));
                listOfOffers.remove(j+1);
                listOfOffers.add(i, copy);
                listOfOffers.remove(i+1);



                i++;
                j--;
             }
        }


        if(beginning < j) quickSort(listOfOffers, i,j);
        if(i < end) quickSort(listOfOffers, i, end);
    }

    private int determineLevelRank(String levelOffer, String levelRequest){
        if(levelOffer.equals(levelRequest)) return 7;

        // determine how close is the levelOffer to levelRequest
        int levelRequestNumber = determineValueOfLevel(levelRequest);
        int levelOfferNUmber = determineValueOfLevel(levelOffer);

        return  abs(levelOfferNUmber-levelRequestNumber);
    }

    private int determineDateRank(Date offerDateFrom, Date offerDateTo, Date requestDateFrom, Date requestDateTo){
        if(offerDateFrom.compareTo(requestDateFrom) <= 0 && offerDateTo.compareTo(requestDateTo) >= 0) return 7;

        // the lenght of the requested date interval in days
        long requestDateLenght = convertDateIntervalIntoDayNumber(requestDateTo, requestDateFrom);

        // the request date contains the offer date
        if(offerDateFrom.compareTo(requestDateFrom) >= 0 && offerDateTo.compareTo(requestDateTo) <=0){
            return determineValueOfDate(convertDateIntervalIntoDayNumber(offerDateTo, offerDateFrom), requestDateLenght)+1;
        }

        // the request date's beginning is the offer date
        if(offerDateFrom.compareTo(requestDateFrom) < 0 && offerDateTo.compareTo(requestDateTo) < 0 && offerDateTo.compareTo(requestDateFrom) >= 0){
            return determineValueOfDate(convertDateIntervalIntoDayNumber(offerDateTo,requestDateFrom), requestDateLenght)+1;
        }

        // request date ends with the offer date
        if(offerDateFrom.compareTo(requestDateFrom) > 0 && offerDateTo.compareTo(requestDateTo) > 0 && offerDateFrom.compareTo(requestDateTo) <=0){
            return determineValueOfDate(convertDateIntervalIntoDayNumber(requestDateTo,offerDateFrom), requestDateLenght)+1;
        }

        return 0;
    }

    // determine rank based on the offered interval length
    private int determineIntervalRank(Date dateFrom, Date dateTo){
        long intervalLenght = convertDateIntervalIntoDayNumber(dateTo,dateFrom);

        if(intervalLenght >= 14) return 3;
        if(intervalLenght >= 5) return 2;
        if(intervalLenght >= 3) return 1;

        return 0;
    }

    private long convertDateIntervalIntoDayNumber(Date dateFrom, Date dateTo){
        return TimeUnit.DAYS.convert(dateFrom.getTime() - dateTo.getTime(),TimeUnit.MILLISECONDS);
    }

    private int determineValueOfLevel(String level){
        if(level.equals("expert")) return 4;
        if(level.equals("advanced")) return 3;
        if(level.equals("intermediate")) return 2;
        if(level.equals("basic")) return 1;

        return 0;
    }

    private int determineValueOfDate(long daysMatching, long requestDateLenght){
        long daysNotMatching = requestDateLenght - daysMatching;
        if(daysNotMatching == 1 || daysNotMatching == 2) return 5;
        if(daysNotMatching == 3 || daysNotMatching == 4) return 4;
        if(daysNotMatching == 5 || daysNotMatching == 6) return 3;

        return 1;
    }
}
