package com.placementpro.utils;

public class Session {
    private static String userId;
    private static String role; // "STUDENT", "FACULTY", "COMPANY"

    public static void login(String currentUserId, String currentRole) {
        userId = currentUserId;
        role = currentRole;
    }

    public static void logout() {
        userId = null;
        role = null;
    }

    public static String getUserId() {
        return userId;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return userId != null && role != null;
    }
}
