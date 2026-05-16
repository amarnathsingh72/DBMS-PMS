package com.placementpro.dto;

import java.util.Date;
import java.util.List;

public class ApplicationDTO {
    private int appId;
    private String studentUsn;
    private int jobId;
    private String companyName;
    private String roleName;
    private double packageLpa;
    private Date applyDate;
    private String status;
    private List<InterviewDTO> interviews;

    public int getAppId() { return appId; }
    public void setAppId(int appId) { this.appId = appId; }
    public String getStudentUsn() { return studentUsn; }
    public void setStudentUsn(String studentUsn) { this.studentUsn = studentUsn; }
    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public double getPackageLpa() { return packageLpa; }
    public void setPackageLpa(double packageLpa) { this.packageLpa = packageLpa; }
    public Date getApplyDate() { return applyDate; }
    public void setApplyDate(Date applyDate) { this.applyDate = applyDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<InterviewDTO> getInterviews() { return interviews; }
    public void setInterviews(List<InterviewDTO> interviews) { this.interviews = interviews; }
}
