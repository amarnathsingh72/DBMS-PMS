package com.placementpro.service;

import com.placementpro.dto.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<NotificationDTO> getNotifications(String userId, String role) {
        String query = "SELECT * FROM NOTIFICATIONS WHERE User_ID = ? AND User_Role = ? ORDER BY Created_At DESC";
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            NotificationDTO n = new NotificationDTO();
            n.setNotifId(rs.getInt("Notif_ID"));
            n.setUserId(rs.getString("User_ID"));
            n.setUserRole(rs.getString("User_Role"));
            n.setMessage(rs.getString("Message"));
            n.setCreatedAt(rs.getTimestamp("Created_At"));
            n.setRead(rs.getBoolean("Is_Read"));
            n.setNotifType(rs.getString("Notif_Type"));
            return n;
        }, userId, role);
    }

    public int getUnreadCount(String userId, String role) {
        String query = "SELECT COUNT(*) FROM NOTIFICATIONS WHERE User_ID = ? AND User_Role = ? AND Is_Read = FALSE";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, userId, role);
        return count != null ? count : 0;
    }

    public void markAsRead(int notifId) {
        jdbcTemplate.update("UPDATE NOTIFICATIONS SET Is_Read = TRUE WHERE Notif_ID = ?", notifId);
    }

    public void markAllAsRead(String userId, String role) {
        jdbcTemplate.update("UPDATE NOTIFICATIONS SET Is_Read = TRUE WHERE User_ID = ? AND User_Role = ?", userId, role);
    }

    /**
     * Creates an in-app notification in the database.
     */
    public void createNotification(String userId, String userRole, String message, String notifType) {
        jdbcTemplate.update(
            "INSERT INTO NOTIFICATIONS (User_ID, User_Role, Message, Notif_Type) VALUES (?, ?, ?, ?)",
            userId, userRole, message, notifType
        );
    }
}
