package com.placementpro.dto;

import java.sql.Timestamp;

public class NotificationDTO {
    private int notifId;
    private String userId;
    private String userRole;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;
    private String notifType;

    public int getNotifId() { return notifId; }
    public void setNotifId(int notifId) { this.notifId = notifId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getNotifType() { return notifType; }
    public void setNotifType(String notifType) { this.notifType = notifType; }
}
