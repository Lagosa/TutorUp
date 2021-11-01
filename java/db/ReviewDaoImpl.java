package itreact.tutorup.server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewDaoImpl implements ReviewDao {
    private ConnectionManager connectionManager;
    public ReviewDaoImpl(ConnectionManager connectionManager){this.connectionManager = connectionManager;}
    private final Logger log = LoggerFactory.getLogger(OffersDaoImpl.class);
    private String className = "[ReviewDaoImpl] ";

    @Override
    public void setReview(int reviewedId, int reviewerId, int review, String comment)throws SQLException{
        String insert = "INSERT INTO tutor_review (revieweduserid, revieweruserid, rating, remark, datewhenreviewed) VALUES  (?,?,?,?,now())";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(insert)){
            statement.setInt(1,reviewedId);
            statement.setInt(2,reviewerId);
            statement.setInt(3,review);
            statement.setString(4,comment);

            log.debug(className+"Setting review...");
            statement.executeUpdate();
        }
    }

    @Override
    public int getAverageRating(int userId)throws SQLException{
        int result=0;
        String sql = "SELECT avg FROM tutor_review_view WHERE reviewedUserID = ? ";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,userId);

            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    result = rs.getInt("avg");
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listReviewsOfUser(int userId) throws SQLException {
        List<Map<String,Object>> result = new ArrayList<>();
        String sql = "SELECT * FROM tutor_review WHERE reviewedUserId = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,userId);

            try(ResultSet rs = statement.executeQuery()){
                while(rs.next()){
                    result.add(getReviewsFromResultSet(rs));

                }
            }
        }
        return result;
    }

    @Override
    public void deleteReview(int reviewerId, int reviewedId) throws SQLException {
        String sql = "DELETE FROM tutor_review WHERE revieweduserid = ? AND revieweruserid = ?";
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,reviewedId);
            statement.setInt(2,reviewerId);

            statement.executeUpdate();
        }
    }

    @Override
    public Map<String, Object> getOneReview(int reviewerId, int reviewedId) throws SQLException {
        log.debug(className+"Got reuquest...");
        String sql = "SELECT reviewid FROM tutor_review WHERE revieweruserid = ? AND revieweduserid = ?";
        Map<String, Object> result = new HashMap<>();
        try(Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            log.debug(className+"Prepairing statement... reviewer: {}, reviewed: {}",reviewerId,reviewedId);
            statement.setInt(1,reviewerId);
            statement.setInt(2,reviewedId);

            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    result.put("id",rs.getInt("reviewid"));
                    log.debug(className+"Got result!");
                }
            }
        }
        return result;
    }

    private Map<String,Object> getReviewsFromResultSet(ResultSet rs)throws SQLException{
        Map<String,Object> review = new HashMap<>();
        review.put("reviewId",rs.getInt("reviewid"));
        review.put("reviewedUserId",rs.getInt("revieweduserid"));
        review.put("reviewerUserId",rs.getInt("revieweruserid"));
        review.put("rating", rs.getInt("rating"));
        review.put("comment", rs.getString("remark"));
        review.put("date", rs.getDate("datewhenreviewed"));

        return review;
    }
}
