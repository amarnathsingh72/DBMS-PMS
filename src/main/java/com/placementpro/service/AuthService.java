package com.placementpro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class AuthService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String hashPassword(String password) {
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

    public boolean validateStudent(String usn, String password) {
        String query = "SELECT COUNT(*) FROM STUDENT WHERE Student_USN = ? AND Password_Hash = ?";
        return validateUser(query, usn, password);
    }

    public boolean validateFaculty(String facultyId, String password) {
        String query = "SELECT COUNT(*) FROM FACULTY WHERE Faculty_ID = ? AND Password_Hash = ?";
        return validateUser(query, facultyId, password);
    }

    public boolean validateCompany(String companyIdStr, String password) {
        try {
            int companyId = Integer.parseInt(companyIdStr);
            String query = "SELECT COUNT(*) FROM COMPANY WHERE Company_ID = ? AND Password_Hash = ?";
            String hashedPwd = hashPassword(password);
            Integer count = jdbcTemplate.queryForObject(query, Integer.class, companyId, hashedPwd);
            return count != null && count > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateUser(String query, String id, String password) {
        String hashedPwd = hashPassword(password);
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id, hashedPwd);
        return count != null && count > 0;
    }
}
