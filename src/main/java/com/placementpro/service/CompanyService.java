package com.placementpro.service;

import com.placementpro.dto.ApplicationDTO;
import com.placementpro.dto.InterviewDTO;
import com.placementpro.dto.JobPostingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CompanyService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    public String getCompanyName(int companyId) {
        String query = "SELECT Company_Name FROM COMPANY WHERE Company_ID = ?";
        return jdbcTemplate.queryForObject(query, String.class, companyId);
    }

    public List<JobPostingDTO> getJobsByCompany(int companyId) {
        String query = "SELECT jp.Job_ID, jp.Role_Name, jp.Package_LPA, jp.Min_CGPA, jp.Allowed_Depts, jp.Deadline, jp.Is_Active, " +
                       "(SELECT COUNT(*) FROM APPLICATIONS a WHERE a.Job_ID = jp.Job_ID) AS app_count " +
                       "FROM JOB_POSTINGS jp WHERE jp.Company_ID = ? ORDER BY jp.Posted_At DESC";
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            JobPostingDTO j = new JobPostingDTO();
            j.setJobId(rs.getInt("Job_ID"));
            j.setRoleName(rs.getString("Role_Name"));
            j.setPackageLpa(rs.getDouble("Package_LPA"));
            j.setMinCgpa(rs.getDouble("Min_CGPA"));
            j.setAllowedDepts(rs.getString("Allowed_Depts"));
            j.setDeadline(rs.getDate("Deadline"));
            j.setEligibilityMessage(String.valueOf(rs.getInt("app_count")));
            return j;
        }, companyId);
    }

    public List<ApplicationDTO> getApplicantsForJob(int jobId) {
        String query = "SELECT a.App_ID, a.Student_USN, a.Apply_Date, a.Status, s.Name, s.CGPA, s.Dept, jp.Role_Name, jp.Package_LPA " +
                       "FROM APPLICATIONS a " +
                       "JOIN STUDENT s ON a.Student_USN = s.Student_USN " +
                       "JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID " +
                       "WHERE a.Job_ID = ? ORDER BY a.Apply_Date DESC";
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            ApplicationDTO app = new ApplicationDTO();
            app.setAppId(rs.getInt("App_ID"));
            app.setStudentUsn(rs.getString("Student_USN"));
            app.setApplyDate(rs.getDate("Apply_Date"));
            app.setStatus(rs.getString("Status"));
            app.setCompanyName(rs.getString("Name"));
            app.setRoleName(rs.getString("Role_Name"));
            app.setPackageLpa(rs.getDouble("Package_LPA"));
            return app;
        }, jobId);
    }

    /**
     * Returns all applicants for a given company filtered by application status.
     */
    public List<ApplicationDTO> getApplicantsByStatus(int companyId, String status) {
        String query = "SELECT a.App_ID, a.Student_USN, a.Apply_Date, a.Status, " +
                       "s.Name, s.CGPA, s.Dept, jp.Role_Name, jp.Package_LPA, jp.Job_ID " +
                       "FROM APPLICATIONS a " +
                       "JOIN STUDENT s ON a.Student_USN = s.Student_USN " +
                       "JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID " +
                       "WHERE jp.Company_ID = ? AND a.Status = ? " +
                       "ORDER BY a.Apply_Date DESC";
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            ApplicationDTO app = new ApplicationDTO();
            app.setAppId(rs.getInt("App_ID"));
            app.setStudentUsn(rs.getString("Student_USN"));
            app.setJobId(rs.getInt("Job_ID"));
            app.setApplyDate(rs.getDate("Apply_Date"));
            app.setStatus(rs.getString("Status"));
            app.setCompanyName(rs.getString("Name"));
            app.setRoleName(rs.getString("Role_Name"));
            app.setPackageLpa(rs.getDouble("Package_LPA"));
            return app;
        }, companyId, status);
    }

    /**
     * Returns status counts for a company's applicant pipeline.
     */
    public Map<String, Integer> getStatusCounts(int companyId) {
        String base = "SELECT COUNT(*) FROM APPLICATIONS a JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID WHERE jp.Company_ID = ?";
        Integer applied = jdbcTemplate.queryForObject(base + " AND a.Status = 'Applied'", Integer.class, companyId);
        Integer shortlisted = jdbcTemplate.queryForObject(base + " AND a.Status = 'Shortlisted'", Integer.class, companyId);
        Integer interview = jdbcTemplate.queryForObject(base + " AND a.Status = 'Interview Scheduled'", Integer.class, companyId);
        Integer selected = jdbcTemplate.queryForObject(base + " AND a.Status = 'Selected'", Integer.class, companyId);
        Integer rejected = jdbcTemplate.queryForObject(base + " AND a.Status = 'Rejected'", Integer.class, companyId);

        return Map.of(
            "applied", applied != null ? applied : 0,
            "shortlisted", shortlisted != null ? shortlisted : 0,
            "interview", interview != null ? interview : 0,
            "selected", selected != null ? selected : 0,
            "rejected", rejected != null ? rejected : 0
        );
    }

    /**
     * Updates application status AND sends notifications to student + faculty + email.
     */
    public void updateApplicationStatus(int appId, String status) {
        jdbcTemplate.update("UPDATE APPLICATIONS SET Status = ? WHERE App_ID = ?", status, appId);

        // Fetch related info for notifications
        Map<String, Object> info = jdbcTemplate.queryForMap(
            "SELECT a.Student_USN, jp.Role_Name, c.Company_Name, s.Email, s.Name, s.Dept " +
            "FROM APPLICATIONS a " +
            "JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID " +
            "JOIN COMPANY c ON jp.Company_ID = c.Company_ID " +
            "JOIN STUDENT s ON a.Student_USN = s.Student_USN " +
            "WHERE a.App_ID = ?", appId);

        String usn = (String) info.get("Student_USN");
        String roleName = (String) info.get("Role_Name");
        String companyName = (String) info.get("Company_Name");
        String studentEmail = (String) info.get("Email");
        String studentName = (String) info.get("Name");
        String dept = (String) info.get("Dept");

        // If selected, mark student as placed
        if ("Selected".equals(status)) {
            jdbcTemplate.update("UPDATE STUDENT SET Is_Placed = TRUE WHERE Student_USN = ?", usn);
        }

        // 1) Notify the student (in-app)
        String studentMsg = "Your application for " + roleName + " at " + companyName
                          + " has been updated to: " + status + ".";
        notificationService.createNotification(usn, "STUDENT", studentMsg, "STATUS_UPDATE");

        // 2) Notify all faculty placement officers (in-app)
        List<Map<String, Object>> officers = jdbcTemplate.queryForList(
            "SELECT Faculty_ID, Email FROM FACULTY WHERE Is_Placement_Officer = TRUE");
        for (Map<String, Object> officer : officers) {
            String facId = (String) officer.get("Faculty_ID");
            String facMsg = "Student " + usn + " (" + studentName + ", " + dept + ") — "
                          + roleName + " at " + companyName + " → " + status;
            notificationService.createNotification(facId, "FACULTY", facMsg, "STATUS_UPDATE");

            // Email faculty officer
            String facEmail = (String) officer.get("Email");
            if (facEmail != null) {
                emailService.sendNotificationEmail(facEmail, "Placement Officer",
                    "Applicant Status Update", facMsg);
            }
        }

        // 3) Notify faculty of same department (in-app)
        List<Map<String, Object>> deptFaculty = jdbcTemplate.queryForList(
            "SELECT Faculty_ID, Email FROM FACULTY WHERE Dept = ? AND Is_Placement_Officer = FALSE",
            dept);
        for (Map<String, Object> fac : deptFaculty) {
            String facId = (String) fac.get("Faculty_ID");
            String deptMsg = "[" + dept + "] Student " + studentName + " — "
                           + roleName + " at " + companyName + " → " + status;
            notificationService.createNotification(facId, "FACULTY", deptMsg, "DEPT_UPDATE");
        }

        // 4) Send email to student
        emailService.sendStatusEmail(studentEmail, studentName, roleName, companyName, status);
    }

    public void postJob(int companyId, String roleName, double packageLpa, double minCgpa, String allowedDepts, String deadline) {
        jdbcTemplate.update("INSERT INTO JOB_POSTINGS (Company_ID, Role_Name, Package_LPA, Min_CGPA, Allowed_Depts, Deadline) VALUES (?,?,?,?,?,?)",
                companyId, roleName, packageLpa, minCgpa, allowedDepts, deadline);
    }

    public void scheduleInterview(int appId, String roundName, String date, String time, String venue, int companyId) {
        jdbcTemplate.update("INSERT INTO INTERVIEW_SCHEDULE (App_ID, Round_Name, Interview_Date, Interview_Time, Venue, Scheduled_By) VALUES (?,?,?,?,?,?)",
                appId, roundName, date, time, venue, companyId);
        // Also update application status
        jdbcTemplate.update("UPDATE APPLICATIONS SET Status = 'Interview Scheduled' WHERE App_ID = ?", appId);
    }

    public void rescheduleInterview(int interviewId, String newDate, String newTime) {
        // This UPDATE fires the trg_interview_reschedule TRIGGER automatically
        jdbcTemplate.update("UPDATE INTERVIEW_SCHEDULE SET Interview_Date = ?, Interview_Time = ? WHERE Interview_ID = ?",
                newDate, newTime, interviewId);
    }

    public Map<String, Object> getCompanyStats(int companyId) {
        int totalJobs = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM JOB_POSTINGS WHERE Company_ID = ?", Integer.class, companyId);
        int totalApplicants = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM APPLICATIONS a JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID WHERE jp.Company_ID = ?", Integer.class, companyId);
        int selected = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM APPLICATIONS a JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID WHERE jp.Company_ID = ? AND a.Status = 'Selected'", Integer.class, companyId);
        int pendingInterviews = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INTERVIEW_SCHEDULE i JOIN APPLICATIONS a ON i.App_ID = a.App_ID JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID WHERE jp.Company_ID = ? AND i.Result = 'Pending'", Integer.class, companyId);

        return Map.of("totalJobs", totalJobs, "totalApplicants", totalApplicants, "selected", selected, "pendingInterviews", pendingInterviews);
    }
}
