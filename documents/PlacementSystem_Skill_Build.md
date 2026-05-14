# PlacementPro — Skill Build
**Phase-by-phase build guide for AI-assisted development (Antigravity / Claude / Gemini)**

**Version:** 1.0  
**Date:** May 2026  
**Course:** DATABASE MANAGEMENT SYSTEMS – BAI402G

---

## SECTION 0 — HOW TO USE THIS SKILL

This document is a **build sequence**, not documentation. Each phase has a gate — you cannot proceed to the next phase until the current gate passes. Each phase is designed to be built in 2–4 AI prompts, where each prompt generates one isolated component or DAO class.

### Operating Principles
1. **Database first.** Create all tables, trigger, and stored procedure in MySQL Workbench before writing any Java code. Test them manually. The Java app is just a view layer over a working DB.
2. **One panel, one prompt.** Never try to build the entire application in one prompt. Build `LoginPanel.java` → test it → build `StudentDashboard.java` → test it. Incremental and isolated.
3. **DAO isolation.** All SQL lives in DAO classes (`StudentDAO.java`, `JobDAO.java`, etc.). Never write a SQL query directly inside a JPanel class. This keeps prompts simple — "build a DAO method that fetches eligible jobs for student USN X."
4. **Test at every gate.** Each phase ends with a specific test. Run it. If it passes, proceed. If it fails, debug before moving forward — prompting the next phase while the previous phase is broken leads to compounding errors.

---

## SECTION 1 — BUILD PHASES

### Phase Overview

| Phase | What you build | Gate Condition | Est. Prompts |
|---|---|---|---|
| **P0** | MySQL schema + test data | All 9 tables created, trigger fires, procedure returns data | 3–4 |
| **P1** | JDBC connection + ThemeConstants | DBConnectionPool.java returns valid connection | 1 |
| **P2** | Login system + session | All 3 roles can log in and reach correct dashboard | 3–4 |
| **P3** | Student Dashboard + Browse Jobs (read-only) | Student sees job list with eligibility check | 4–5 |
| **P4** | Application flow + trigger test | Student applies → company sees applicant → reschedule fires notification | 5–6 |
| **P5** | Faculty admin + stored procedure | Report panel shows correct dept placement % | 3–4 |
| **P6** | Gemini AI integration + final polish | AI skill advice appears on job expand | 2–3 |

**Total estimated prompts: 21–30**  
**Total build time: 8–12 days at 2–3 hours/day**

---

## SECTION 2 — PHASE 0: DATABASE FOUNDATION

**Goal:** All tables, trigger, stored procedure, and views working in MySQL Workbench. 50 rows of realistic test data inserted.

### Sub-Steps

#### P0.1 — Create Schema + Tables
**Prompt to Antigravity:**
```
I'm building a college placement management system. Create the MySQL schema for 9 tables:
STUDENT, COMPANY, JOB_POSTINGS, APPLICATIONS, INTERVIEW_SCHEDULE, STUDENT_CERTIFICATIONS, NOTIFICATIONS, FACULTY, SKILLS.

Use these exact column definitions:
[paste the table definitions from Section 3 of the spec sheet]

Generate the complete CREATE TABLE statements with all PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK, and DEFAULT constraints. Schema name: placementpro_db.
```

**Expected output:** One `.sql` file with 9 CREATE TABLE statements. Run it in MySQL Workbench.

#### P0.2 — Create Trigger
**Prompt:**
```
Write the AFTER UPDATE trigger `trg_interview_reschedule` on the INTERVIEW_SCHEDULE table. When Interview_Date or Interview_Time changes, automatically insert notification rows for:
1. The student linked to the application
2. All faculty where Is_Placement_Officer = TRUE

Use this exact notification message format:
- Student: "Your interview for [Role_Name] at [Company_Name] has been rescheduled to [New_Date] [New_Time]"
- Faculty: "Interview rescheduled — Student: [USN] | Role: [Role_Name] | New Date: [New_Date]"

Include the complete trigger SQL.
```

**Test:** Manually run `UPDATE Interview_Schedule SET Interview_Date = '2026-06-15' WHERE Interview_ID = 1;` in Workbench → check NOTIFICATIONS table for 2 new rows.

#### P0.3 — Create Stored Procedure
**Prompt:**
```
Write stored procedure `sp_generate_placement_report()` that returns two result sets:
1. Department-wise summary: Dept, Total_Students, Placed_Students, Placement_Percentage, Avg_Package_LPA
2. Top 5 companies by hire count: Company_Name, Hires, Avg_Package

Use the STUDENT, APPLICATIONS, JOB_POSTINGS, COMPANY tables with appropriate JOINs.
```

**Test:** Run `CALL sp_generate_placement_report();` → verify both result sets appear.

#### P0.4 — Create Views
**Prompt:**
```
Create 3 SQL views for the placement system:
1. vw_eligible_students — shows students eligible for active jobs (CGPA check, not already placed)
2. vw_application_pipeline — joins APPLICATIONS + STUDENT + JOB_POSTINGS + COMPANY to show full application details
3. vw_dept_placement — department-wise placement percentage summary

Include the complete CREATE VIEW statements.
```

#### P0.5 — Insert Test Data
**Prompt:**
```
Generate SQL INSERT statements for realistic test data:
- 20 students across 4 departments (CSE, ECE, MECH, CIVIL), CGPA range 6.0–9.5
- 8 companies with 2 job postings each
- 30 applications spread across students and jobs
- 5 certifications for random students
- 10 interview schedules
- 2 faculty (one placement officer, one regular)

Include student passwords as SHA-256 hash of "password123".
Use realistic Indian college names, company names, and role names.
```

**Gate Condition:**  
✅ All 9 tables exist in `placementpro_db`  
✅ Trigger fires correctly (verified manually)  
✅ Procedure returns data (verified manually)  
✅ Views return data  
✅ 50+ test rows inserted  

---

## SECTION 3 — PHASE 1: JDBC CONNECTION LAYER

**Goal:** Java can connect to MySQL. ThemeConstants class exists with all colors and fonts.

### Sub-Steps

#### P1.1 — Database Connection Pool
**Prompt:**
```
Create a Java singleton class DBConnectionPool.java that manages MySQL connections. Use this JDBC URL:
jdbc:mysql://localhost:3306/placementpro_db?useSSL=false&serverTimezone=UTC

The pool should:
- Load credentials from config.properties (db.user, db.password)
- Maintain a max of 5 connections
- Have a static getConnection() method
- Handle SQLException gracefully with console log

Include the complete class code.
```

**Test:** Create a test main() that calls `DBConnectionPool.getConnection()` → print connection → close it. No exceptions thrown.

#### P1.2 — ThemeConstants
**Prompt:**
```
Create a ThemeConstants.java class with all color and font constants for the PlacementPro design system:

Colors:
- COLOR_ACCENT = #1A56DB
- COLOR_BG = #F4F6FA
- COLOR_SURFACE = #FFFFFF
- COLOR_BORDER = #E2E8F0
- COLOR_SIDEBAR = #1E2A3B
- COLOR_TEXT_PRIMARY = #1A202C
- COLOR_TEXT_SECONDARY = #64748B
- STATUS_APPLIED = #3B82F6
- STATUS_SHORTLISTED = #F59E0B
- STATUS_INTERVIEW = #8B5CF6
- STATUS_SELECTED = #10B981
- STATUS_REJECTED = #EF4444

Fonts:
- FONT_TITLE (Segoe UI, Bold, 20)
- FONT_HEADING (Segoe UI, Bold, 15)
- FONT_BODY (Segoe UI, Plain, 13)
- FONT_BUTTON (Segoe UI, Bold, 13)

Use java.awt.Color and java.awt.Font.
```

**Gate Condition:**  
✅ Connection pool returns valid connection  
✅ ThemeConstants compiles  
✅ config.properties.example checked into repo with placeholder values  

---

## SECTION 4 — PHASE 2: LOGIN + SESSION SYSTEM

**Goal:** All three roles can log in. Correct dashboard opens based on role. Session object holds user ID and role.

### Sub-Steps

#### P2.1 — Session Manager
**Prompt:**
```
Create a Session.java singleton that stores the current logged-in user's ID and role (STUDENT / FACULTY / COMPANY). Include methods:
- static void login(String userId, String role)
- static void logout()
- static String getUserId()
- static String getRole()
- static boolean isLoggedIn()
```

#### P2.2 — DAO for Login Validation
**Prompt:**
```
Create AuthDAO.java with three methods:
1. validateStudent(String usn, String password) → returns true if hash matches
2. validateFaculty(String facultyId, String password) → returns true if hash matches
3. validateCompany(int companyId, String password) → returns true if hash matches

Use SHA-256 hashing with MessageDigest to compare input password hash with stored hash.
Include complete JDBC code with PreparedStatement.
```

#### P2.3 — Login Panel UI
**Prompt:**
```
Create LoginPanel.java as a JPanel with:
- 3 JTabbedPane tabs: "Student" / "Faculty" / "Company"
- Each tab has: ID text field, password field (JPasswordField), login button
- On login button click, call AuthDAO.validateX() → if valid, call Session.login() and open the correct dashboard frame
- Use ThemeConstants for all colors and fonts
- Center the panel on a 500x400 JFrame

Include complete Swing code.
```

**Test:** Run the frame → enter test student USN + "password123" → should log in and call the (not-yet-built) StudentDashboard.

#### P2.4 — Dashboard Shell Classes (empty)
**Prompt:**
```
Create three empty JFrame classes:
- StudentDashboard.java → extends JFrame, title "PlacementPro | Student", size 1024x680
- FacultyDashboard.java → extends JFrame, title "PlacementPro | Admin", size 1200x750
- CompanyDashboard.java → extends JFrame, title "PlacementPro | Recruiter", size 1100x700

Each just sets visible = true on construction for now. No panels yet.
```

**Gate Condition:**  
✅ LoginPanel opens  
✅ Valid credentials open the correct empty dashboard  
✅ Invalid credentials show inline error label (not a popup)  
✅ Session.getRole() returns correct role after login  

---

## SECTION 5 — PHASE 3: STUDENT DASHBOARD + BROWSE JOBS

**Goal:** Student sees their dashboard with 4 quick stat cards and a job browse table with eligibility checks.

### Sub-Steps

#### P3.1 — StudentDAO (profile fetch)
**Prompt:**
```
Create StudentDAO.java with:
- getStudentProfile(String usn) → returns a Student object (name, email, dept, cgpa, is_placed)
- getApplicationCount(String usn) → returns int count of applications
- getShortlistedCount(String usn) → returns count where status = 'Shortlisted'
- getInterviewCount(String usn) → returns count where status = 'Interview Scheduled'

Use JDBC with PreparedStatement. Include Student.java POJO class.
```

#### P3.2 — Quick Stat Cards Component
**Prompt:**
```
Create a helper method in UIComponents.java:
static JPanel quickStatCard(String label, String value)

Returns a white JPanel with:
- Top: label in FONT_SMALL, COLOR_TEXT_SECONDARY
- Center: value in 28px bold, COLOR_TEXT_PRIMARY
- Border: 1px solid COLOR_BORDER
- Padding: 16px all sides
- Size: 180x100

Use ThemeConstants.
```

#### P3.3 — Student Dashboard Layout
**Prompt:**
```
Build StudentDashboard.java with BorderLayout:
- NORTH: TopBarPanel (45px height, white bg, shows student name + notification bell + logout button)
- CENTER: Main content panel with BoxLayout vertical:
  - Row 1: 4 quick stat cards in FlowLayout (Applications / Shortlisted / Interviews / Placed status)
  - Row 2: JTabbedPane with tabs: Browse Jobs / My Applications / Profile / Notifications

On frame load:
- Fetch student data from StudentDAO
- Populate quick stat cards with actual counts
- Default tab: Browse Jobs

Use ThemeConstants for all styling.
```

#### P3.4 — JobDAO + Eligibility Checker
**Prompt:**
```
Create JobDAO.java with:
- getActiveJobs() → returns List<Job> of all jobs where Is_Active = TRUE
- checkEligibility(String usn, int jobId) → returns EligibilityResult object with:
  - boolean isEligible
  - String reason (if not eligible, e.g., "CGPA 6.5 — minimum required 7.0")

EligibilityResult checks:
1. Student CGPA >= Job Min_CGPA
2. Student Dept in Job Allowed_Depts (comma-separated check)
3. Student Is_Placed = FALSE

Include Job.java POJO and EligibilityResult.java class.
```

#### P3.5 — Browse Jobs Panel
**Prompt:**
```
Create BrowseJobsPanel.java as a JPanel with:
- JTable showing active jobs: Company | Role | Package | Deadline | Eligibility | Apply
- Eligibility column: green "✓ Eligible" or red "✗ [reason]" using JobDAO.checkEligibility()
- Apply column: JButton "Apply" (enabled only if eligible), disabled button shows gray
- On Apply click → insert into APPLICATIONS via ApplicationDAO → show green toast "Application submitted"

Use ThemeConstants for table styling (alternating row colors, header font, 38px row height).
```

**Gate Condition:**  
✅ Student dashboard loads with correct stat counts  
✅ Browse Jobs table shows all active jobs  
✅ Eligibility column shows correct status with reason  
✅ Apply button only enabled for eligible jobs  
✅ Clicking Apply inserts a row in APPLICATIONS table (verify in Workbench)  

---

## SECTION 6 — PHASE 4: APPLICATION FLOW + TRIGGER TEST

**Goal:** Student applies → company sees the applicant → company reschedules interview → trigger fires → student + faculty get notification.

### Sub-Steps

#### P4.1 — ApplicationDAO
**Prompt:**
```
Create ApplicationDAO.java with:
- applyForJob(String usn, int jobId) → inserts into APPLICATIONS with status 'Applied'
- getStudentApplications(String usn) → returns List<Application> with job details joined
- getJobApplicants(int jobId) → returns List<Application> for a specific job (for company view)
- updateApplicationStatus(int appId, String newStatus) → UPDATE status

Include Application.java POJO with fields: appId, studentUSN, studentName, jobId, roleName, companyName, status, applyDate.
```

#### P4.2 — My Applications Panel (Student)
**Prompt:**
```
Create MyApplicationsPanel.java with a JTable showing the student's applications:
Columns: Company | Role | Apply Date | Status (with colored status chip)

Use ApplicationDAO.getStudentApplications() to populate.
Refresh the table on tab switch.

Each status gets a colored chip using UIComponents.statusChip(status) method:
- Applied: blue bg
- Shortlisted: amber bg
- Interview Scheduled: purple bg
- Selected: green bg
- Rejected: red bg

Include the statusChip() method in UIComponents.java.
```

#### P4.3 — Company Applicant Panel
**Prompt:**
```
Create ApplicantManagementPanel.java for the company dashboard.
Shows a table of applicants for a selected job:
Columns: Student Name | USN | CGPA | Dept | Status | Actions

Actions column has:
- JComboBox to change status (Shortlisted / Rejected) → calls ApplicationDAO.updateApplicationStatus()
- "Schedule Interview" button → opens InterviewScheduleDialog

Use JobDAO.getJobApplicants(jobId) to populate.
```

#### P4.4 — Interview Schedule Dialog
**Prompt:**
```
Create InterviewScheduleDialog.java as a JDialog with fields:
- Round name (JComboBox: Aptitude / Technical / HR)
- Date (JTextField or JSpinner for date)
- Time (JTextField HH:MM format)
- Venue (JTextField)
- Submit button

On submit → call InterviewDAO.scheduleInterview(appId, roundName, date, time, venue) → inserts into INTERVIEW_SCHEDULE.

Include InterviewDAO.java with:
- scheduleInterview(...) → INSERT
- rescheduleInterview(int interviewId, String newDate, String newTime) → UPDATE (this fires the trigger)
```

**Test the trigger:**
1. Schedule an interview via the dialog → verify row appears in INTERVIEW_SCHEDULE
2. In MySQL Workbench, run `UPDATE Interview_Schedule SET Interview_Date = '2026-06-20' WHERE Interview_ID = X;`
3. Check NOTIFICATIONS table → should have 2 new rows (student + faculty)
4. Verify the notification message format is correct

#### P4.5 — Notification Panel
**Prompt:**
```
Create NotificationPanel.java for student + faculty dashboards.
Shows a scrollable list of notification cards:
- Unread: left border 3px blue, light blue background
- Read: no border, white background
- Each card: icon 🔔, message text, timestamp
- Click a card → mark as read via NotificationDAO.markAsRead(notifId)

Include NotificationDAO.java with:
- getNotifications(String userId, String role) → returns List<Notification>
- markAsRead(int notifId) → UPDATE Is_Read = TRUE
- getUnreadCount(String userId, String role) → returns int

Add a notification bell with badge in StudentDashboard top bar. Badge shows unread count.
```

**Gate Condition:**  
✅ Student can apply to jobs  
✅ Company sees applicants in their table  
✅ Company can schedule interview → row appears in INTERVIEW_SCHEDULE  
✅ Manually running UPDATE on Interview_Schedule fires trigger → notifications created  
✅ Student sees notification in their inbox  
✅ Faculty sees notification in their inbox  
✅ Clicking notification marks it as read  

---

## SECTION 7 — PHASE 5: FACULTY ADMIN + STORED PROCEDURE

**Goal:** Faculty can see all students, all companies, all applications. Report panel calls the stored procedure and displays results.

### Sub-Steps

#### P5.1 — Faculty Dashboard Layout
**Prompt:**
```
Create FacultyDashboard.java with:
- WEST: Sidebar (180px, dark bg #1E2A3B) with vertical menu:
  - Students
  - Companies
  - Applications
  - Reports
  - Notifications
- CENTER: Main content panel that swaps based on sidebar selection

Use ThemeConstants.COLOR_SIDEBAR for sidebar background.
Active menu item: white text, 3px left accent bar.
Inactive: gray text #A8B9CC.
```

#### P5.2 — Faculty Student Management Panel
**Prompt:**
```
Create StudentManagementPanel.java with:
- JTable showing all students: USN | Name | Dept | CGPA | Email | Is_Placed
- Search bar at top (filters by name or USN)
- Add Student button → opens AddStudentDialog
- Edit button → opens EditStudentDialog with pre-filled fields

Use StudentDAO.getAllStudents() and StudentDAO.addStudent(...) methods.
```

#### P5.3 — Faculty Application Overview
**Prompt:**
```
Create ApplicationOverviewPanel.java that queries the vw_application_pipeline view.
Shows a table: App_ID | Student_Name | USN | CGPA | Company | Role | Status | Apply_Date

Filters at top:
- Status dropdown (All / Applied / Shortlisted / Interview Scheduled / Selected / Rejected)
- Department dropdown (All / CSE / ECE / MECH / CIVIL)

Use ApplicationDAO.getFilteredApplications(status, dept) which queries the view with WHERE clauses.
```

#### P5.4 — Report Panel + Stored Procedure Call
**Prompt:**
```
Create PlacementReportPanel.java with:
- "Generate Report" button at top
- On click → call ReportDAO.generatePlacementReport() which calls the stored procedure
- Display two result sets:
  1. Department table: Dept | Total | Placed | Placement % | Avg Package
  2. Top Companies table: Company | Hires | Avg Package
- Optional: Add a simple bar chart (JavaFX BarChart or JFreeChart) for dept placement %

Include ReportDAO.java with:
- generatePlacementReport() → uses CallableStatement to call sp_generate_placement_report()
- Returns two List<PlacementSummary> and List<CompanyHire>

Handle multiple result sets with:
ResultSet rs1 = stmt.getResultSet();
stmt.getMoreResults();
ResultSet rs2 = stmt.getResultSet();
```

**Gate Condition:**  
✅ Faculty can log in and see sidebar  
✅ Student management panel shows all students  
✅ Application overview shows vw_application_pipeline data  
✅ Report panel calls stored procedure  
✅ Both result sets display correctly  
✅ Placement percentages match manual calculation  

---

## SECTION 8 — PHASE 6: GEMINI AI INTEGRATION + POLISH

**Goal:** AI skill advice feature works. All panels styled consistently. Final acceptance criteria pass.

### Sub-Steps

#### P6.1 — Gemini API Client
**Prompt:**
```
Create GeminiClient.java with method:
static String getSkillAdvice(String studentSkills, String jobRequirements)

Uses Java 11+ HttpClient to call:
https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=API_KEY

Prompt format:
"A student is applying for a software job. Student's current skills and certifications: [studentSkills]. Job requirements: [jobRequirements]. Give exactly 3 short, specific suggestions to improve their chances. Format as a numbered list. No preamble."

Parse the response JSON to extract the "text" field from candidates[0].content.parts[0].text.
Use org.json library or manual substring parsing.

Handle API errors gracefully: return "Suggestions unavailable — check API key" if request fails.
```

**Config:**
```
Add to config.properties:
gemini.api.key=YOUR_API_KEY_HERE

Load with:
String apiKey = ConfigLoader.getProperty("gemini.api.key");
```

#### P6.2 — AI Advice UI Integration
**Prompt:**
```
In BrowseJobsPanel.java, add a row detail panel that expands when a job row is clicked.
Show:
- Full job description
- Eligibility criteria details
- "💡 Get AI Advice" ghost button (white bg, blue border)

On button click:
1. Fetch student skills from StudentDAO.getStudentSkills(usn)
2. Call GeminiClient.getSkillAdvice(skills, job.requirements) in a SwingWorker background thread
3. Display result in a yellow suggestion card (#FFFBEB bg, #92400E text)

Use SwingWorker to avoid freezing the UI:
new SwingWorker<String, Void>() {
    protected String doInBackground() {
        return GeminiClient.getSkillAdvice(...);
    }
    protected void done() {
        String advice = get();
        // update suggestion label
    }
}.execute();
```

#### P6.3 — Final Polish Checklist
**Prompt:**
```
Review all panels and apply consistent styling:
1. All tables use 38px row height, alternating row colors
2. All buttons use ThemeConstants colors
3. All status chips use the statusChip() helper
4. All panels have 24px outer padding
5. No JOptionPane popups — use toasts for success messages
6. All text fields have 32px height
7. All forms use GridBagLayout with consistent insets

Create a UIComponents.java class with helper methods:
- primaryButton(String label)
- ghostButton(String label)
- dangerButton(String label)
- statusChip(String status)
- quickStatCard(String label, String value)
- showToast(JPanel parent, String message, boolean isSuccess)
```

#### P6.4 — CSV Export (Bonus)
**Prompt:**
```
Add "Export to CSV" button to PlacementReportPanel.
On click → write the department summary table to placement_report.csv using FileWriter.
Format:
Department,Total Students,Placed Students,Placement %,Avg Package
CSE,50,42,84.0,7.2
...

Include error handling for FileNotFoundException.
```

**Gate Condition:**  
✅ AI advice button works and shows suggestions  
✅ All panels use ThemeConstants consistently  
✅ No UI freezes when calling Gemini (SwingWorker used)  
✅ CSV export works  
✅ All 20 acceptance criteria from spec sheet pass  

---

## SECTION 9 — PROMPTING DISCIPLINE

### Good Prompt Structure
```
Role: [e.g., "Create a Java DAO class"]
Context: [e.g., "for the PlacementPro system"]
Task: [e.g., "with a method to fetch all active jobs"]
Constraints: [e.g., "Use JDBC PreparedStatement, return List<Job>"]
Format: [e.g., "Include the complete class code"]
```

### Examples of Good Prompts

**✅ Good:**
```
Create StudentDAO.java with a method getStudentProfile(String usn) that queries the STUDENT table and returns a Student object. Use JDBC PreparedStatement. Include the Student.java POJO class with fields: usn, name, email, dept, cgpa, isPlaced. Handle SQLException with try-catch and console log.
```

**✅ Good:**
```
Build a Swing JPanel called BrowseJobsPanel.java with a JTable showing job postings. Columns: Company | Role | Package | Deadline. Use JobDAO.getActiveJobs() to populate. Style the table with 38px row height, alternating white and #F8FAFC row colors, header font FONT_HEADING from ThemeConstants.
```

### Examples of Bad Prompts

**❌ Bad:**
```
Build the placement system.
```
*Too vague — no specific component named, no constraints.*

**❌ Bad:**
```
Create the database and the Java code and make it look good.
```
*Three tasks in one prompt — database, code, and styling should be separate prompts.*

**❌ Bad:**
```
Make a job browse screen with AI and notifications and everything.
```
*"Everything" is not a specification. Each feature is a separate prompt.*

---

## SECTION 10 — ANTI-PATTERNS

| ❌ Never | ✅ Always |
|---|---|
| Write SQL queries inside JPanel classes | All SQL in DAO classes only |
| Call Gemini API on the Swing Event Dispatch Thread | Use SwingWorker for API calls |
| Use SELECT * in queries | Name columns explicitly |
| Hardcode API keys in source code | Load from config.properties via System.getenv() or ConfigLoader |
| Use JOptionPane for success messages | Use toast notifications (JLabel with Timer) |
| Build entire dashboard in one prompt | One panel per prompt |
| Prompt "build the login system" without specifying tabs | Specify: 3 tabs, ID field, password field, button, AuthDAO validation |
| Test only after Phase 6 | Test at every gate — fail fast |
| Use plain text passwords in DB | SHA-256 hash only |
| Prompt for "good UI" without naming colors | Reference ThemeConstants tokens explicitly |
| Skip manual trigger testing in Workbench | Always test trigger manually before wiring to Java |
| Mix business logic into UI event handlers | UI calls DAO, DAO handles logic |

---

## SECTION 11 — DEBUGGING PLAYBOOK

| Symptom | Likely Cause | Fix |
|---|---|---|
| "Connection refused" on JDBC | MySQL not running OR wrong port | Check MySQL status in Workbench, verify port 3306 |
| Trigger does not fire | Trigger syntax error OR UPDATE didn't change the value | Run `SHOW TRIGGERS;` in Workbench, verify DELIMITER |
| Stored procedure returns empty | Wrong WHERE clause OR no data matches | Test procedure directly in Workbench with sample data |
| Login always fails even with correct password | Password hash mismatch | Print hash in Java, compare with DB hash manually |
| UI freezes when clicking AI Advice | API call on EDT, no SwingWorker | Wrap GeminiClient.getSkillAdvice() in SwingWorker.doInBackground() |
| Notification badge shows 0 but there are unread | NotificationDAO query filters by wrong role | Print SQL and userId in console, verify role string matches ENUM |
| Status chip shows wrong color | Status string mismatch in switch | Print status string in console, check for extra spaces |
| Apply button always disabled | Eligibility check query wrong | Test JobDAO.checkEligibility() in a main() method with known USN + Job ID |
| CSV export fails silently | FileNotFoundException not caught | Add try-catch around FileWriter, print stack trace |
| Application crashes on reschedule | InterviewDAO.rescheduleInterview() has SQL error | Print the UPDATE statement in console, run it manually in Workbench |

---

## SECTION 12 — WHAT SUCCESS LOOKS LIKE

### Demo Day Checklist

**5 minutes before demo:**
1. MySQL Workbench open with placementpro_db loaded
2. Run `SELECT COUNT(*) FROM Applications;` → verify test data exists
3. Launch PlacementPro.jar → login as test student "4SF24CI020" / password123
4. Dashboard loads in <1 second

**During demo:**
1. **Student flow:** Browse jobs → see eligibility check → apply to 1 job → check My Applications tab → status shows "Applied"
2. **Company flow:** Login as company ID 1 → see applicants → shortlist one student → schedule interview with date picker
3. **Trigger demo:** In Workbench, manually UPDATE interview date → switch to student dashboard → notification bell badge updates → click bell → notification appears with rescheduled message
4. **Faculty flow:** Login as faculty → click Reports → click Generate → tables populate in <2 seconds → placement % matches department counts
5. **AI feature:** Expand a job row in Browse Jobs → click Get AI Advice → suggestion card loads in ~3 seconds → shows 3 numbered suggestions

**Post-demo verification (for evaluators):**
1. Open MySQL Workbench → show ER diagram → point to trigger and procedure
2. Run `DESCRIBE APPLICATIONS;` → show foreign key constraints
3. Run `SELECT * FROM vw_application_pipeline LIMIT 5;` → show view working
4. Show one DAO class in code → point out PreparedStatement usage → no SQL in UI classes

### Acceptance — All 20 Criteria Pass
✅ Refer to Section 9 of the spec sheet — each criterion is testable in <1 minute.

---

*PlacementPro Skill Build v1.0 — BAI402G Mini Project*
