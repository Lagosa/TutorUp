package itreact.tutorup.server.service;

import itreact.tutorup.server.db.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewManager {

    private static ReviewManager ourInstance = new ReviewManager();
    public static ReviewManager getInstance(){return ourInstance;}
    private static Logger log = LoggerFactory.getLogger(ReviewManager.class);
    private final String className = "[ReviewManager] ";


    public Map<String, Object> setReview(int reviewerId, String reviewedUsername, Integer review, String comment)throws SQLException{
        log.debug(className+"Getting id by username: {}",reviewedUsername);
        Map<String,Object> reviewedUser = DatabaseFactory.getUserDao().findByUsername(reviewedUsername);
        Map<String,Object> result = new HashMap<>();

        if(!InteractionsManager.getInstance().hadInteraction(reviewerId,(int)reviewedUser.get("id")) &&
            !InteractionsManager.getInstance().hadInteraction((int)reviewedUser.get("id"),reviewerId)){
            result.put("exception","notInteracted");
            return result;
        }

        log.debug(className+"Checking for duplicate review...");
        Map<String, Object> duplicates = DatabaseFactory.getReviewDao().getOneReview(reviewerId,(int)reviewedUser.get("id"));
        if(!duplicates.isEmpty()){
            duplicates.put("exception","alreadyReviewedUser");
            return duplicates;
        }

        DatabaseFactory.getReviewDao().setReview((int)reviewedUser.get("id"),reviewerId,review, comment);

        String username =(String)DatabaseFactory.getUserDao().findById(reviewerId).get("username");
        NotificationsManager.getInstance().save((int)reviewedUser.get("id"),username,"You have a new review!",username+" reviewed you.","","userprofile");

        result.put("exception","");
        return result;
    }

    public Integer getAverageRating(String username)throws SQLException{
        Map<String,Object> user = DatabaseFactory.getUserDao().findByUsername(username);
        return DatabaseFactory.getReviewDao().getAverageRating((int)user.get("id"));
    }

    public List<Map<String, Object>> listReviewsOfUser(int userId)throws SQLException{
        List<Map<String,Object>> reviews = DatabaseFactory.getReviewDao().listReviewsOfUser(userId);

        for(int i=0; i<reviews.size();i++){
            reviews.add(i,expandReview(reviews.get(i)));
            reviews.remove(i+1);
        }

        return reviews;
    }

    public Map<String, Object> deleteReview(int userId, String username)throws SQLException{
        log.debug(className+"Getting reviewed user by username...");
        int reviewedId = (int) DatabaseFactory.getUserDao().findByUsername(username).get("id");
        log.debug(className+"Deleting review...");
        DatabaseFactory.getReviewDao().deleteReview(userId,reviewedId);
        String usernameReviewer = (String) DatabaseFactory.getUserDao().findById(userId).get("username");
        NotificationsManager.getInstance().save(reviewedId,username,usernameReviewer+" deleted his/her review.",usernameReviewer+" deleted the review left on your profile.","", "userprofile");
        return new HashMap<>();
    }

    private Map<String,Object> expandReview(Map<String,Object> review)throws SQLException{
        review.put("reviewedUsername",DatabaseFactory.getUserDao().findById((int)review.get("reviewedUserId")).get("username"));
        review.put("reviewerUsername",DatabaseFactory.getUserDao().findById((int)review.get("reviewerUserId")).get("username"));

        return review;
    }
}
