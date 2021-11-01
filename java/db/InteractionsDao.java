package itreact.tutorup.server.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface InteractionsDao {
    void saveInteraction(Integer requesterId, Integer receiverId,String description)throws SQLException;
    void updateStatus(Integer requesterId, Integer receiverId, String status)throws SQLException;

    boolean checkInteractionStatus(Integer senderId, Integer receiverId, String status)throws SQLException;

    List<Map<String, Object>> getInteractions(int userId)throws SQLException;
}
