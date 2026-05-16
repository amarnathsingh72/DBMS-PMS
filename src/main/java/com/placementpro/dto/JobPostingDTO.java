package com.placementpro.dto;

import java.util.Date;

public class JobPostingDTO {
    private int jobId;
    private String companyName;
    private String roleName;
    private double packageLpa;
    private double minCgpa;
    private String allowedDepts;
    private Date deadline;
    
    // UI specific fields
    private boolean isEligible;
    private String eligibilityMessage;
    private boolean hasApplied;

    // Getters and Setters
    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    
    public double getPackageLpa() { return packageLpa; }
    public void setPackageLpa(double packageLpa) { this.packageLpa = packageLpa; }
    
    public double getMinCgpa() { return minCgpa; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }
    
    public String getAllowedDepts() { return allowedDepts; }
    public void setAllowedDepts(String allowedDepts) { this.allowedDepts = allowedDepts; }
    
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public boolean isEligible() { return isEligible; }
    public void setEligible(boolean eligible) { isEligible = eligible; }

    public String getEligibilityMessage() { return eligibilityMessage; }
    public void setEligibilityMessage(String eligibilityMessage) { this.eligibilityMessage = eligibilityMessage; }

    public boolean isHasApplied() { return hasApplied; }
    public void setHasApplied(boolean hasApplied) { this.hasApplied = hasApplied; }
}
