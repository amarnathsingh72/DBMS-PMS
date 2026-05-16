package com.placementpro.dao;

import com.placementpro.utils.DBConnectionPool;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDAO {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static boolean validateStudent(String usn, String password) {
        String query = "SELECT Student_USN FROM STUDENT WHERE Student_USN = ? AND Password_Hash = ?";
        return validateUser(query, usn, password);
    }

    public static boolean validateFaculty(String facultyId, String password) {
        String query = "SELECT Faculty_ID FROM FACULTY WHERE Faculty_ID = ? AND Password_Hash = ?";
        return validateUser(query, facultyId, password);
    }

    public static boolean validateCompany(int companyId, String password) {
        String query = "SELECT Company_ID FROM COMPANY WHERE Company_ID = ? AND Password_Hash = ?";
        String hashedPwd = hashPassword(password);
        System.out.println("[DEBUG] Attempting login for Company ID: " + companyId);
        Connection conn = null;
        try {
            conn = DBConnectionPool.getConnection();
            if (conn != null) {
                System.out.println("[DEBUG] DB Connection successful.");
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, companyId);
                    stmt.setString(2, hashedPwd);
                    try (ResultSet rs = stmt.executeQuery()) {
                        boolean exists = rs.next();
                        System.out.println("[DEBUG] Company exists in DB: " + exists);
                        return exists;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Company validation error: " + e.getMessage());
        } finally {
            DBConnectionPool.releaseConnection(conn);
        }
        return false;
    }
    
    private static boolean validateUser(String query, String id, String password) {
        String hashedPwd = hashPassword(password);
        System.out.println("[DEBUG] Attempting login for ID: " + id);
        Connection conn = null;
        try {
            conn = DBConnectionPool.getConnection();
            if (conn != null) {
                System.out.println("[DEBUG] DB Connection successful.");
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, id);
                    stmt.setString(2, hashedPwd);
                    try (ResultSet rs = stmt.executeQuery()) {
                        boolean exists = rs.next();
                        System.out.println("[DEBUG] User exists in DB: " + exists);
                        return exists;
                    }
                }
            } else {
                System.err.println("[ERROR] Could not get connection from pool (conn is null)");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Validation SQL error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnectionPool.releaseConnection(conn);
        }
        return false;
    }
}
