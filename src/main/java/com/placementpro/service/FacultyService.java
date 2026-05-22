package com.placementpro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FacultyService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    public String getFacultyName(String facultyId) {
        return jdbcTemplate.queryForObject("SELECT Name FROM FACULTY WHERE Faculty_ID = ?", String.class, facultyId);
    }

    public boolean isPlacementOfficer(String facultyId) {
        Boolean result = jdbcTemplate.queryForObject("SELECT Is_Placement_Officer FROM FACULTY WHERE Faculty_ID = ?", Boolean.class, facultyId);
        return result != null && result;
    }

    // --- Placement Report (calls stored procedure sp_generate_placement_report) ---
    public List<Map<String, Object>> getDeptPlacementReport() {
        return jdbcTemplate.queryForList(
            "SELECT s.Dept, COUNT(DISTINCT s.Student_USN) AS Total_Students, " +
            "COUNT(DISTINCT CASE WHEN s.Is_Placed = TRUE THEN s.Student_USN END) AS Placed_Students, " +
            "ROUND(COUNT(DISTINCT CASE WHEN s.Is_Placed = TRUE THEN s.Student_USN END) * 100.0 / COUNT(DISTINCT s.Student_USN), 2) AS Placement_Percentage, " +
            "ROUND(AVG(CASE WHEN s.Is_Placed = TRUE THEN jp.Package_LPA END), 2) AS Avg_Package_LPA " +
            "FROM STUDENT s LEFT JOIN APPLICATIONS a ON s.Student_USN = a.Student_USN AND a.Status = 'Selected' " +
            "LEFT JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID GROUP BY s.Dept ORDER BY Placement_Percentage DESC"
        );
    }

    public List<Map<String, Object>> getTopCompanies() {
        return jdbcTemplate.queryForList(
            "SELECT c.Company_Name, COUNT(DISTINCT a.Student_USN) AS Hires, ROUND(AVG(jp.Package_LPA), 2) AS Avg_Package " +
            "FROM APPLICATIONS a JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID JOIN COMPANY c ON jp.Company_ID = c.Company_ID " +
            "WHERE a.Status = 'Selected' GROUP BY c.Company_Name ORDER BY Hires DESC LIMIT 5"
        );
    }

    // --- Application Pipeline (uses vw_application_pipeline view) ---
    public List<Map<String, Object>> getApplicationPipeline() {
        return jdbcTemplate.queryForList("SELECT * FROM vw_application_pipeline ORDER BY Apply_Date DESC");
    }

    // --- At-Risk Students (Q1 from spec) ---
    public List<Map<String, Object>> getAtRiskStudents() {
        return jdbcTemplate.queryForList(
            "SELECT s.Student_USN, s.Name, s.CGPA, s.Dept, s.Email FROM STUDENT s " +
            "LEFT JOIN APPLICATIONS a ON s.Student_USN = a.Student_USN " +
            "WHERE a.App_ID IS NULL AND s.Is_Placed = FALSE"
        );
    }

    /**
     * Sends urgent placement alerts to both at-risk students and faculty members.
     */
    public int sendRiskAlerts(String triggeredByFacultyId) {
        List<Map<String, Object>> atRisk = getAtRiskStudents();
        if (atRisk.isEmpty()) return 0;

        String facultyName = getFacultyName(triggeredByFacultyId);

        // 1) Send in-app & email notification to each student
        for (Map<String, Object> s : atRisk) {
            String usn = (String) s.get("Student_USN");
            String name = (String) s.get("Name");
            String email = (String) s.get("Email");

            String studentMsg = "URGENT ALERT: You have zero active job applications. " +
                                "Please browse eligible roles and submit applications immediately to ensure placement readiness.";
            notificationService.createNotification(usn, "STUDENT", studentMsg, "RISK_ALERT");

            if (email != null && !email.isEmpty()) {
                emailService.sendNotificationEmail(email, name, "Urgent Placement Action Required", studentMsg);
            }
        }

        // 2) Notify all Placement Officers/Faculty members
        List<Map<String, Object>> officers = jdbcTemplate.queryForList(
            "SELECT Faculty_ID, Name, Email FROM FACULTY");
        
        String facultyMsg = "INTERVENTION ALERT: Placement Officer " + facultyName + 
                            " has dispatched automated intervention warnings to " + atRisk.size() + 
                            " unplaced students with zero active applications.";

        for (Map<String, Object> officer : officers) {
            String facId = (String) officer.get("Faculty_ID");
            String facEmail = (String) officer.get("Email");
            String facName = (String) officer.get("Name");

            notificationService.createNotification(facId, "FACULTY", facultyMsg, "RISK_ALERT");

            if (facEmail != null && !facEmail.isEmpty()) {
                emailService.sendNotificationEmail(facEmail, facName, "Student Risk Intervention Dispatched", facultyMsg);
            }
        }

        return atRisk.size();
    }

    // --- All Students ---
    public List<Map<String, Object>> getAllStudents() {
        return jdbcTemplate.queryForList("SELECT Student_USN, Name, Dept, CGPA, Is_Placed, Email FROM STUDENT ORDER BY Student_USN");
    }

    // --- All Companies ---
    public List<Map<String, Object>> getAllCompanies() {
        return jdbcTemplate.queryForList("SELECT Company_ID, Company_Name, Location, HR_Contact, Is_Verified FROM COMPANY ORDER BY Company_ID");
    }

    // --- Dashboard stats ---
    public Map<String, Object> getFacultyStats() {
        int totalStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM STUDENT", Integer.class);
        int placedStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM STUDENT WHERE Is_Placed = TRUE", Integer.class);
        int totalCompanies = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM COMPANY WHERE Is_Verified = TRUE", Integer.class);
        int activeJobs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JOB_POSTINGS WHERE Is_Active = TRUE", Integer.class);
        int totalApplications = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM APPLICATIONS", Integer.class);
        int atRisk = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM STUDENT s LEFT JOIN APPLICATIONS a ON s.Student_USN = a.Student_USN WHERE a.App_ID IS NULL AND s.Is_Placed = FALSE", Integer.class);

        return Map.of(
            "totalStudents", totalStudents,
            "placedStudents", placedStudents,
            "totalCompanies", totalCompanies,
            "activeJobs", activeJobs,
            "totalApplications", totalApplications,
            "atRisk", atRisk
        );
    }
}
