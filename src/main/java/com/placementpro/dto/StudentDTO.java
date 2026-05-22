package com.placementpro.dto;

public class StudentDTO {
    private String usn;
    private String name;
    private String dept;
    private double cgpa;
    private int applicationsSubmitted;
    private int shortlistedCount;
    private int interviewsScheduled;
    private boolean isPlaced;
    private String email;
    private String phone;

    // Getters and Setters
    public String getUsn() { return usn; }
    public void setUsn(String usn) { this.usn = usn; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    public int getApplicationsSubmitted() { return applicationsSubmitted; }
    public void setApplicationsSubmitted(int applicationsSubmitted) { this.applicationsSubmitted = applicationsSubmitted; }
    public int getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(int shortlistedCount) { this.shortlistedCount = shortlistedCount; }
    public int getInterviewsScheduled() { return interviewsScheduled; }
    public void setInterviewsScheduled(int interviewsScheduled) { this.interviewsScheduled = interviewsScheduled; }
    public boolean isPlaced() { return isPlaced; }
    public void setPlaced(boolean placed) { isPlaced = placed; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
