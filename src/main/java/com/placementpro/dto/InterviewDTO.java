package com.placementpro.dto;

import java.util.Date;

public class InterviewDTO {
    private int interviewId;
    private int appId;
    private String roundName;
    private Date interviewDate;
    private String interviewTime;
    private String venue;
    private String result;
    private String studentUsn;
    private String studentName;
    private String roleName;
    private String companyName;

    public int getInterviewId() { return interviewId; }
    public void setInterviewId(int interviewId) { this.interviewId = interviewId; }
    public int getAppId() { return appId; }
    public void setAppId(int appId) { this.appId = appId; }
    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }
    public Date getInterviewDate() { return interviewDate; }
    public void setInterviewDate(Date interviewDate) { this.interviewDate = interviewDate; }
    public String getInterviewTime() { return interviewTime; }
    public void setInterviewTime(String interviewTime) { this.interviewTime = interviewTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getStudentUsn() { return studentUsn; }
    public void setStudentUsn(String studentUsn) { this.studentUsn = studentUsn; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}
