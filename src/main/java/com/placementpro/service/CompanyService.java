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
            j.setEligibilityMessage(String.valueOf(rs.getInt("app_count"))); // reuse field for applicant count
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
            app.setCompanyName(rs.getString("Name")); // reuse for student name
            app.setRoleName(rs.getString("Role_Name"));
            app.setPackageLpa(rs.getDouble("Package_LPA"));
            return app;
        }, jobId);
    }

    public void updateApplicationStatus(int appId, String status) {
        jdbcTemplate.update("UPDATE APPLICATIONS SET Status = ? WHERE App_ID = ?", status, appId);
        // If selected, mark student as placed
        if ("Selected".equals(status)) {
            String usn = jdbcTemplate.queryForObject("SELECT Student_USN FROM APPLICATIONS WHERE App_ID = ?", String.class, appId);
            jdbcTemplate.update("UPDATE STUDENT SET Is_Placed = TRUE WHERE Student_USN = ?", usn);
        }
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
