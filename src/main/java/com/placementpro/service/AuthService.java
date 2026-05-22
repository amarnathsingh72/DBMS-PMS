package com.placementpro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;

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

    // ==================== LOGIN ====================

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

    // ==================== SIGNUP ====================

    /**
     * Register a new student. Returns null on success, or error message on failure.
     */
    public String registerStudent(String usn, String name, String email, String phone,
                                  String dept, int semester, double cgpa, String password) {
        try {
            // Check if USN already exists
            Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM STUDENT WHERE Student_USN = ?", Integer.class, usn);
            if (exists != null && exists > 0) {
                return "A student with USN " + usn + " already exists.";
            }
            // Check if email already exists
            Integer emailExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM STUDENT WHERE Email = ?", Integer.class, email);
            if (emailExists != null && emailExists > 0) {
                return "This email is already registered.";
            }

            String hash = hashPassword(password);
            jdbcTemplate.update(
                "INSERT INTO STUDENT (Student_USN, Name, Email, Phone, Dept, Semester, CGPA, Password_Hash) VALUES (?,?,?,?,?,?,?,?)",
                usn, name, email, phone, dept, semester, cgpa, hash);
            return null; // success
        } catch (Exception e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    /**
     * Register a new faculty. Returns null on success, or error message on failure.
     */
    public String registerFaculty(String facultyId, String name, String email,
                                  String dept, boolean isPlacementOfficer, String password) {
        try {
            Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM FACULTY WHERE Faculty_ID = ?", Integer.class, facultyId);
            if (exists != null && exists > 0) {
                return "Faculty ID " + facultyId + " already exists.";
            }

            String hash = hashPassword(password);
            jdbcTemplate.update(
                "INSERT INTO FACULTY (Faculty_ID, Name, Email, Dept, Is_Placement_Officer, Password_Hash) VALUES (?,?,?,?,?,?)",
                facultyId, name, email, dept, isPlacementOfficer, hash);
            return null;
        } catch (Exception e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    /**
     * Register a new company. Returns the auto-generated Company_ID on success, or -1 on failure.
     * Sets errorMsg[0] with error details on failure.
     */
    public int registerCompany(String companyName, String location, String website,
                               String hrContact, String password, String[] errorMsg) {
        try {
            String hash = hashPassword(password);
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO COMPANY (Company_Name, Location, Website, HR_Contact, Password_Hash, Is_Verified) VALUES (?,?,?,?,?,FALSE)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, companyName);
                ps.setString(2, location);
                ps.setString(3, website);
                ps.setString(4, hrContact);
                ps.setString(5, hash);
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            return key != null ? key.intValue() : -1;
        } catch (Exception e) {
            if (errorMsg != null && errorMsg.length > 0) {
                errorMsg[0] = "Registration failed: " + e.getMessage();
            }
            return -1;
        }
    }
}
