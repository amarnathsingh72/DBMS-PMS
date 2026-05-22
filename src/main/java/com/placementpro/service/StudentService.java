package com.placementpro.service;

import com.placementpro.dto.ApplicationDTO;
import com.placementpro.dto.InterviewDTO;
import com.placementpro.dto.JobPostingDTO;
import com.placementpro.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public StudentDTO getStudentDashboardInfo(String usn) {
        String profileQuery = "SELECT Name, Dept, CGPA, Is_Placed, Email, Phone FROM STUDENT WHERE Student_USN = ?";

        StudentDTO dto = jdbcTemplate.queryForObject(profileQuery, (rs, rowNum) -> {
            StudentDTO s = new StudentDTO();
            s.setUsn(usn);
            s.setName(rs.getString("Name"));
            s.setDept(rs.getString("Dept"));
            s.setCgpa(rs.getDouble("CGPA"));
            s.setPlaced(rs.getBoolean("Is_Placed"));
            s.setEmail(rs.getString("Email"));
            s.setPhone(rs.getString("Phone"));
            return s;
        }, usn);

        if (dto != null) {
            Integer appCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM APPLICATIONS WHERE Student_USN = ?", Integer.class, usn);
            dto.setApplicationsSubmitted(appCount != null ? appCount : 0);

            Integer shortCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM APPLICATIONS WHERE Student_USN = ? AND Status IN ('Shortlisted','Interview Scheduled','Selected')", Integer.class, usn);
            dto.setShortlistedCount(shortCount != null ? shortCount : 0);

            Integer interviewCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INTERVIEW_SCHEDULE i JOIN APPLICATIONS a ON i.App_ID = a.App_ID WHERE a.Student_USN = ? AND i.Result = 'Pending'", Integer.class, usn);
            dto.setInterviewsScheduled(interviewCount != null ? interviewCount : 0);
        }

        return dto;
    }

    public List<JobPostingDTO> getAvailableJobsForStudent(String usn, StudentDTO student) {
        String jobQuery = "SELECT jp.Job_ID, c.Company_Name, jp.Role_Name, jp.Package_LPA, jp.Min_CGPA, jp.Allowed_Depts, jp.Deadline " +
                          "FROM JOB_POSTINGS jp JOIN COMPANY c ON jp.Company_ID = c.Company_ID " +
                          "WHERE jp.Is_Active = TRUE ORDER BY jp.Deadline ASC";

        List<Integer> appliedJobIds = jdbcTemplate.queryForList("SELECT Job_ID FROM APPLICATIONS WHERE Student_USN = ?", Integer.class, usn);

        return jdbcTemplate.query(jobQuery, (rs, rowNum) -> {
            JobPostingDTO job = new JobPostingDTO();
            job.setJobId(rs.getInt("Job_ID"));
            job.setCompanyName(rs.getString("Company_Name"));
            job.setRoleName(rs.getString("Role_Name"));
            job.setPackageLpa(rs.getDouble("Package_LPA"));
            job.setMinCgpa(rs.getDouble("Min_CGPA"));
            job.setAllowedDepts(rs.getString("Allowed_Depts"));
            job.setDeadline(rs.getDate("Deadline"));

            if (appliedJobIds.contains(job.getJobId())) {
                job.setHasApplied(true);
                job.setEligible(false);
                job.setEligibilityMessage("Already Applied");
            } else if (student.isPlaced()) {
                job.setEligible(false);
                job.setEligibilityMessage("Already Placed");
            } else if (student.getCgpa() < job.getMinCgpa()) {
                job.setEligible(false);
                job.setEligibilityMessage("Min CGPA: " + job.getMinCgpa());
            } else if (job.getAllowedDepts() != null && !job.getAllowedDepts().contains(student.getDept())) {
                job.setEligible(false);
                job.setEligibilityMessage("Dept not eligible");
            } else {
                job.setEligible(true);
                job.setEligibilityMessage("Eligible");
            }
            return job;
        });
    }

    public List<ApplicationDTO> getMyApplications(String usn) {
        String query = "SELECT a.App_ID, a.Job_ID, a.Apply_Date, a.Status, c.Company_Name, jp.Role_Name, jp.Package_LPA " +
                       "FROM APPLICATIONS a " +
                       "JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID " +
                       "JOIN COMPANY c ON jp.Company_ID = c.Company_ID " +
                       "WHERE a.Student_USN = ? ORDER BY a.Apply_Date DESC";

        List<ApplicationDTO> apps = jdbcTemplate.query(query, (rs, rowNum) -> {
            ApplicationDTO app = new ApplicationDTO();
            app.setAppId(rs.getInt("App_ID"));
            app.setStudentUsn(usn);
            app.setJobId(rs.getInt("Job_ID"));
            app.setApplyDate(rs.getDate("Apply_Date"));
            app.setStatus(rs.getString("Status"));
            app.setCompanyName(rs.getString("Company_Name"));
            app.setRoleName(rs.getString("Role_Name"));
            app.setPackageLpa(rs.getDouble("Package_LPA"));
            return app;
        }, usn);

        // Load interview rounds for each application
        for (ApplicationDTO app : apps) {
            List<InterviewDTO> interviews = jdbcTemplate.query(
                "SELECT * FROM INTERVIEW_SCHEDULE WHERE App_ID = ? ORDER BY Interview_Date",
                (rs, rowNum) -> {
                    InterviewDTO iv = new InterviewDTO();
                    iv.setInterviewId(rs.getInt("Interview_ID"));
                    iv.setAppId(rs.getInt("App_ID"));
                    iv.setRoundName(rs.getString("Round_Name"));
                    iv.setInterviewDate(rs.getDate("Interview_Date"));
                    iv.setInterviewTime(rs.getString("Interview_Time"));
                    iv.setVenue(rs.getString("Venue"));
                    iv.setResult(rs.getString("Result"));
                    return iv;
                }, app.getAppId()
            );
            app.setInterviews(interviews);
        }

        return apps;
    }

    public void applyToJob(String usn, int jobId) {
        jdbcTemplate.update("INSERT INTO APPLICATIONS (Student_USN, Job_ID) VALUES (?, ?)", usn, jobId);
    }
}
