package itreact.tutorup.server.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ReviewDao  {
    void setReview(int reviewedId, int reviewerId, int review, String comment)throws SQLException;

    int getAverageRating(int userId)throws SQLException;

    List<Map<String, Object>> listReviewsOfUser(int userId)throws SQLException;

    void deleteReview(int  reviewerId, int reviewedId)throws SQLException;

    Map<String, Object> getOneReview(int reviewerId, int reviewedId)throws SQLException;
}
