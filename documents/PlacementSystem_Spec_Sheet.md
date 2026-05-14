# PlacementPro — College Placement Management System
**A role-based, database-driven placement pipeline that connects students, recruiters, and faculty in one structured workflow**

**Version:** 1.0  
**Date:** May 2026  
**Course:** DATABASE MANAGEMENT SYSTEMS – BAI402G  
**Semester:** IV Sem CSE (AI & ML) — Academic Year 2025–26 EVEN

---

## SECTION 0 — PRODUCT OVERVIEW

### What is PlacementPro?

PlacementPro is a modern, full-stack web application backed by a relational DBMS that manages the end-to-end college placement process — from student profile creation to final offer letter logging. It replaces manual spreadsheet workflows with a structured, role-gated system where every actor (student, company recruiter, faculty placement officer) sees only the data and controls relevant to their role.


The system is not just a record store. It actively routes information — notifying students when a company reschedules, alerting faculty when a candidate is shortlisted, and generating placement analytics on demand. Every significant event in the workflow is persisted, queryable, and reportable.

### The Three Roles

| Role | What they do in the system |
|---|---|
| **Student** | Build profile, browse eligible jobs, apply, track application status, view interview schedule |
| **Company / Recruiter** | Post jobs, set eligibility criteria, shortlist candidates, schedule/reschedule interviews, log results |
| **Faculty / Placement Officer** | Manage student + company records, oversee all applications, generate placement reports, manage notifications |

### Core Engineering Principle

Every feature maps to a discrete database operation — an INSERT, a JOIN query, a VIEW, a TRIGGER firing, or a STORED PROCEDURE executing. There are no "magic AI boxes." The intelligence of the system comes from well-designed relational logic, not external APIs.

---

## SECTION 1 — CORE FEATURES

### F1 — Role-Based Authentication & Session Management
Each user logs in with a USN / Company ID / Faculty ID and a password. On login, the system loads the correct dashboard panel for their role. Cross-role access is blocked at the application layer — a student session cannot call any method that touches the admin control panel. Passwords are stored as SHA-256 hashes.

### F2 — Student Profile Engine
Students create and maintain a structured profile:
- Personal details (name, USN, department, semester)
- Academic record (CGPA, backlogs, 10th/12th marks)
- Skills (added from a master skill list)
- Certifications (name, issuing body, date)
- Internship / project history (title, company, duration, description)
- Resume upload (file path stored in DB, PDF stored on disk)

The profile is the input to every downstream feature — eligibility checks, job recommendations, and report generation all read from this normalized data.

### F3 — Job Posting & Eligibility Engine
Companies post jobs with structured eligibility rules:
- Minimum CGPA threshold
- Allowed departments
- Required certifications (optional)
- Application deadline

When a student browses jobs, the system runs an eligibility check query against their profile. Ineligible jobs are shown as grayed out with the reason displayed (e.g., "CGPA 6.2 — minimum required 7.0"). Students can only apply to jobs they qualify for.

### F4 — Application Tracker
Students submit applications through the system. Each application record stores the student USN, Job ID, apply date, and current status. Status progresses through a defined lifecycle:

`Applied → Shortlisted → Interview Scheduled → Selected / Rejected`

Students see this pipeline as a live status card. Faculty see it as a filterable table across all students and all companies.

### F5 — Interview Scheduling & Reschedule Notification *(Trigger-backed)*
Companies schedule interview rounds linked to a specific application. Each round has a date, time, venue/link, and round type (Aptitude / Technical / HR). When a company **reschedules** an existing interview (UPDATE on `Interview_Schedule`), a **database TRIGGER fires automatically** and inserts a notification record for both the affected student and the faculty placement officer. The web frontend polls or refreshes to show unread notifications on login.

This is a real-world feature: nobody finds out about a rescheduled interview by refreshing a table manually. The trigger enforces that the notification is never forgotten.


### F6 — Placement Report Generator *(Stored Procedure-backed)*
Faculty trigger a `GENERATE_PLACEMENT_REPORT` stored procedure that:
1. Counts total students per department
2. Counts placed students per department
3. Calculates placement percentage per department
4. Returns average package per department
5. Lists top 5 companies by number of hires

The procedure result populates a summary table on the admin dashboard. No ad-hoc SQL is needed — one button press executes the procedure and refreshes the view.

### F7 — Notification Centre
Every user role has a notification inbox. Notification types include:
- Interview scheduled (student)
- Interview rescheduled (student + faculty)
- Application shortlisted (student)
- Application rejected (student)
- New job posted in eligible department (student)
- Student applied to a posted job (company)

Notifications are read/unread flagged and timestamped. Faculty can mark all as read in bulk.

### F8 — Placement Analytics Dashboard (Faculty)
Read-only analytics panel showing:
- Department-wise placement percentage (bar chart or table)
- Company-wise hire count
- Month-wise application volume
- Number of students with 0 applications (at risk)
- Average CGPA of placed vs unplaced students

All powered by SQL VIEWs and GROUP BY queries — no external BI tool needed.

---

## SECTION 2 — F5 DEEP DIVE: RESCHEDULE TRIGGER + NOTIFICATION SYSTEM

*This is the most technically interesting feature in the project — a real-world workflow where a database event drives a user-facing notification without application code explicitly writing the notification row.*

### The Problem It Solves
In a manual system, if a company HR updates the interview date in a spreadsheet, students may or may not find out. In PlacementPro, the database itself guarantees the notification is created the moment the schedule changes.

### Trigger Definition

```sql
DELIMITER $$

CREATE TRIGGER trg_interview_reschedule
AFTER UPDATE ON Interview_Schedule
FOR EACH ROW
BEGIN
  -- Only fire if the interview date or time actually changed
  IF OLD.Interview_Date != NEW.Interview_Date OR OLD.Interview_Time != NEW.Interview_Time THEN

    -- Notify the student
    INSERT INTO Notifications (User_ID, User_Role, Message, Created_At, Is_Read)
    SELECT 
      a.Student_USN,
      'STUDENT',
      CONCAT('Your interview for ', j.Role_Name, ' at ', c.Company_Name,
             ' has been rescheduled to ', NEW.Interview_Date, ' ', NEW.Interview_Time),
      NOW(),
      FALSE
    FROM Applications a
    JOIN Job_Postings j ON a.Job_ID = j.Job_ID
    JOIN Company c ON j.Company_ID = c.Company_ID
    WHERE a.App_ID = NEW.App_ID;

    -- Notify all faculty placement officers
    INSERT INTO Notifications (User_ID, User_Role, Message, Created_At, Is_Read)
    SELECT 
      f.Faculty_ID,
      'FACULTY',
      CONCAT('Interview rescheduled — Student: ', a.Student_USN,
             ' | Role: ', j.Role_Name, ' | New Date: ', NEW.Interview_Date),
      NOW(),
      FALSE
    FROM Applications a
    JOIN Job_Postings j ON a.Job_ID = j.Job_ID
    JOIN Faculty f ON f.Is_Placement_Officer = TRUE
    WHERE a.App_ID = NEW.App_ID;

  END IF;
END$$

DELIMITER ;
```

### Why This Design
- **Zero application code needed** — the backend logic just calls `UPDATE Interview_Schedule SET ...` and the trigger fires invisibly
- **Atomic** — if the UPDATE succeeds, the notifications exist. No partial state
- **Scalable** — add more notification targets (email, SMS gateway) by extending the trigger without touching business logic


### Stored Procedure: Placement Report

```sql
DELIMITER $$

CREATE PROCEDURE sp_generate_placement_report()
BEGIN
  -- Department-wise placement summary
  SELECT 
    s.Dept,
    COUNT(DISTINCT s.Student_USN) AS Total_Students,
    COUNT(DISTINCT CASE WHEN s.Is_Placed = TRUE THEN s.Student_USN END) AS Placed_Students,
    ROUND(
      COUNT(DISTINCT CASE WHEN s.Is_Placed = TRUE THEN s.Student_USN END) * 100.0 /
      COUNT(DISTINCT s.Student_USN), 2
    ) AS Placement_Percentage,
    ROUND(AVG(CASE WHEN s.Is_Placed = TRUE THEN jp.Package_LPA END), 2) AS Avg_Package_LPA
  FROM Student s
  LEFT JOIN Applications a ON s.Student_USN = a.Student_USN
  LEFT JOIN Job_Postings jp ON a.Job_ID = jp.Job_ID
  WHERE a.Status = 'Selected' OR a.Status IS NULL
  GROUP BY s.Dept
  ORDER BY Placement_Percentage DESC;

  -- Top 5 companies by hire count
  SELECT 
    c.Company_Name,
    COUNT(DISTINCT a.Student_USN) AS Hires,
    ROUND(AVG(jp.Package_LPA), 2) AS Avg_Package
  FROM Applications a
  JOIN Job_Postings jp ON a.Job_ID = jp.Job_ID
  JOIN Company c ON jp.Company_ID = c.Company_ID
  WHERE a.Status = 'Selected'
  GROUP BY c.Company_Name
  ORDER BY Hires DESC
  LIMIT 5;
END$$

DELIMITER ;
```

---

## SECTION 3 — DATA MODEL

### Entity Relationship Summary

The ER diagram has 6 core entities. PlacementPro extends it to 9 tables to support notifications, skills, and faculty management — each addition is justified by a feature requirement.

### Table Definitions

#### T1 — STUDENT
| Column | Type | Constraint |
|---|---|---|
| Student_USN | VARCHAR(15) | PRIMARY KEY |
| Name | VARCHAR(100) | NOT NULL |
| Email | VARCHAR(100) | UNIQUE, NOT NULL |
| Phone | VARCHAR(15) | |
| Dept | VARCHAR(50) | NOT NULL |
| Semester | INT | |
| CGPA | DECIMAL(3,1) | CHECK (CGPA BETWEEN 0 AND 10) |
| Backlogs | INT | DEFAULT 0 |
| Is_Placed | BOOLEAN | DEFAULT FALSE |
| Password_Hash | VARCHAR(64) | NOT NULL |
| Resume_Path | VARCHAR(255) | |
| Created_At | TIMESTAMP | DEFAULT NOW() |

#### T2 — COMPANY
| Column | Type | Constraint |
|---|---|---|
| Company_ID | INT | PRIMARY KEY, AUTO_INCREMENT |
| Company_Name | VARCHAR(100) | NOT NULL |
| Location | VARCHAR(100) | |
| Website | VARCHAR(255) | |
| HR_Contact | VARCHAR(100) | |
| Password_Hash | VARCHAR(64) | NOT NULL |
| Is_Verified | BOOLEAN | DEFAULT FALSE |

#### T3 — JOB_POSTINGS
| Column | Type | Constraint |
|---|---|---|
| Job_ID | INT | PRIMARY KEY, AUTO_INCREMENT |
| Company_ID | INT | FK → Company |
| Role_Name | VARCHAR(100) | NOT NULL |
| Package_LPA | DECIMAL(5,2) | |
| Min_CGPA | DECIMAL(3,1) | DEFAULT 0 |
| Allowed_Depts | VARCHAR(255) | Comma-separated or JSON |
| Preferred_Cert | VARCHAR(255) | |
| Deadline | DATE | |
| Is_Active | BOOLEAN | DEFAULT TRUE |
| Posted_At | TIMESTAMP | DEFAULT NOW() |

#### T4 — APPLICATIONS
| Column | Type | Constraint |
|---|---|---|
| App_ID | INT | PRIMARY KEY, AUTO_INCREMENT |
| Student_USN | VARCHAR(15) | FK → Student |
| Job_ID | INT | FK → Job_Postings |
| Apply_Date | DATE | DEFAULT CURDATE() |
| Status | ENUM('Applied','Shortlisted','Interview Scheduled','Selected','Rejected') | DEFAULT 'Applied' |
| UNIQUE | (Student_USN, Job_ID) | Prevents duplicate applications |

#### T5 — INTERVIEW_SCHEDULE
| Column | Type | Constraint |
|---|---|---|
| Interview_ID | INT | PRIMARY KEY, AUTO_INCREMENT |
| App_ID | INT | FK → Applications |
| Round_Name | VARCHAR(50) | e.g., 'Aptitude', 'Technical', 'HR' |
| Interview_Date | DATE | NOT NULL |
| Interview_Time | TIME | |
| Venue | VARCHAR(255) | |
| Result | ENUM('Pending','Pass','Fail') | DEFAULT 'Pending' |
| Scheduled_By | INT | FK → Company |

#### T6 — STUDENT_CERTIFICATIONS
| Column | Type | Constraint |
|---|---|---|
| Cert_ID | INT | PRIMARY KEY, AUTO_INCREMENT |
| Student_USN | VARCHAR(15) | FK → Student |
| Cert_Name | VARCHAR(100) | NOT NULL |
| Issued_By | VARCHAR(100) | |
| Issue_Date | DATE | |
| Cert_URL | VARCHAR(255) | |

#### T7 — NOTIFICATIONS *(supports Trigger F5)*
| Column | Type | Constraint |
|---|---|---|
| Notif_ID | INT | PRIMARY KEY, AUTO_INCREMENT |
| User_ID | VARCHAR(50) | Student USN or Faculty_ID |
| User_Role | ENUM('STUDENT','FACULTY','COMPANY') | NOT NULL |
| Message | TEXT | NOT NULL |
| Created_At | TIMESTAMP | DEFAULT NOW() |
| Is_Read | BOOLEAN | DEFAULT FALSE |
| Notif_Type | VARCHAR(50) | e.g., 'RESCHEDULE', 'SHORTLIST' |

#### T8 — FACULTY
| Column | Type | Constraint |
|---|---|---|
| Faculty_ID | VARCHAR(20) | PRIMARY KEY |
| Name | VARCHAR(100) | NOT NULL |
| Email | VARCHAR(100) | UNIQUE |
| Dept | VARCHAR(50) | |
| Is_Placement_Officer | BOOLEAN | DEFAULT FALSE |
| Password_Hash | VARCHAR(64) | NOT NULL |

#### T9 — SKILLS (optional extension)
| Column | Type | Constraint |
|---|---|---|
| Skill_ID | INT | PRIMARY KEY, AUTO_INCREMENT |
| Student_USN | VARCHAR(15) | FK → Student |
| Skill_Name | VARCHAR(100) | NOT NULL |
| Proficiency | ENUM('Beginner','Intermediate','Advanced') | |

### Relationships Summary
- Student **1:M** Applications (one student, many applications)
- Job_Postings **1:M** Applications (one job, many applicants)
- Company **1:M** Job_Postings (one company, many roles)
- Applications **1:M** Interview_Schedule (one application, multiple rounds)
- Student **1:M** Student_Certifications
- Interview_Schedule **→** Notifications (via TRIGGER on UPDATE)

### Views

```sql
-- V1: Eligible students for a given job
CREATE VIEW vw_eligible_students AS
SELECT s.Student_USN, s.Name, s.CGPA, s.Dept, jp.Job_ID, jp.Role_Name
FROM Student s
JOIN Job_Postings jp ON s.CGPA >= jp.Min_CGPA
WHERE s.Is_Placed = FALSE AND jp.Is_Active = TRUE;

-- V2: Application pipeline with company and student info
CREATE VIEW vw_application_pipeline AS
SELECT 
  a.App_ID, s.Name AS Student_Name, s.USN, s.CGPA,
  c.Company_Name, jp.Role_Name, a.Status, a.Apply_Date
FROM Applications a
JOIN Student s ON a.Student_USN = s.Student_USN
JOIN Job_Postings jp ON a.Job_ID = jp.Job_ID
JOIN Company c ON jp.Company_ID = c.Company_ID;

-- V3: Department placement summary
CREATE VIEW vw_dept_placement AS
SELECT Dept,
  COUNT(*) AS Total,
  SUM(Is_Placed) AS Placed,
  ROUND(SUM(Is_Placed)*100.0/COUNT(*), 1) AS Pct
FROM Student
GROUP BY Dept;
```

---

## SECTION 4 — TECH STACK

| Layer | Technology | Reason |
|---|---|---|
| **Frontend** | React + Vite | Industry standard for building fast, reactive dashboards |
| **Styling** | Tailwind CSS | Utility-first CSS for premium, responsive design |
| **Backend** | Node.js (Express) | High performance, non-blocking I/O for concurrent users |
| **Database** | MySQL 8.x | Supports triggers, stored procedures, ENUMs, and views |
| **Auth** | JWT + Bcrypt | Secure, stateless authentication with hashed passwords |
| **Reports** | PDFKit & JSON2CSV | Automated generation of offer letters and CSV analytics |
| **HTTP Client** | Axios | Reliable communication between React and Express |

### Why this stack?
This stack (MERN-style but with MySQL) is what industry leaders and senior developers use for scalable web applications. It allows for a much better user experience than a desktop app, with smooth transitions, real-time updates, and cross-device compatibility.

### Antigravity Compatibility
Because the project is built with Antigravity (AI-assisted vibe coding), each part is a modular component. The backend API is separated from the frontend React components, making it easy to test and iterate.


---

## SECTION 5 — SCREEN SPECIFICATIONS

### S1 — Login Screen (All Roles)
**Component:** `LoginPage.jsx`  
Three tab options: Student | Faculty | Company. Each tab has a text field (USN / Faculty ID / Company ID) and password field. On submit, Axios calls `/api/auth/login`. On success, JWT is stored and the user is redirected to their dashboard.

### S2 — Student Dashboard
**Component:** `StudentDashboard.jsx`  
Top bar: student name, CGPA badge, notification bell with unread count. Four quick-stat cards: Applications Submitted / Shortlisted / Interviews Scheduled / Placed (yes/no). Below: Tabbed navigation with Browse Jobs, My Applications, My Profile, Notifications.

### S3 — Browse Jobs (Student)
**Component:** `JobBrowse.jsx`  
Table of active job postings. Each row: Company Name, Role, Package, Deadline, Eligibility Status (Eligible / Not Eligible + reason). "Apply" button is enabled only for eligible jobs.

### S4 — My Applications (Student)
**Component:** `ApplicationHistory.jsx`  
Table showing all applications for the logged-in student. Columns: Company, Role, Apply Date, Status (color-coded chips). Clicking a row expands to show interview rounds.

### S5 — Notifications Inbox (Student + Faculty)
**Component:** `Notifications.jsx`  
Scrollable list of notification cards. Each card: icon, message, timestamp, read/unread status. Faculty have a "Mark All Read" button.

### S6 — Company Dashboard
**Component:** `CompanyDashboard.jsx`  
Left sidebar: My Jobs / Post New Job / Applicants / Schedule Interview. Main area: summary charts and active job postings.

### S7 — Applicant Management (Company)
**Component:** `ApplicantList.jsx`  
Table of all applicants for a selected job. Dropdown per row to change status. "Schedule Interview" button opens a modern date/time picker modal.

### S8 — Faculty Admin Panel
**Component:** `AdminDashboard.jsx`  
Tabbed: Students | Companies | Applications | Reports. Reports tab: "Generate Report" button calls the backend which executes `sp_generate_placement_report()` and returns JSON to be rendered as charts.

### S9 — Placement Report Screen (Faculty)
**Component:** `ReportsPage.jsx`  
Sections for Dept Summary and Top Companies. "Export to CSV" and "Download PDF" buttons trigger backend generation using `json2csv` and `pdfkit`.


---

## SECTION 6 — CONSTRAINTS & ENGINEERING CHALLENGES

**C1 — JWT Expiration & Auth Refresh.** Managing session tokens in the browser safely. We will use HTTP-only cookies or secure local storage.

**C2 — Database Connection Pooling.** Using `mysql2/promise` with a connection pool to handle multiple concurrent requests efficiently.

**C3 — Trigger-application contract.** The backend code must never manually insert into `Notifications` for reschedule events — only the database trigger does.

**C4 — Eligibility rule storage.** `Allowed_Depts` is stored as a comma-separated string. We will handle the parsing in our SQL queries or backend logic.

**C5 — Handling PDF/CSV streams.** Ensuring the backend streams files correctly to the React frontend without loading massive datasets into memory.


---

## SECTION 7 — REAL-WORLD FEATURE LOG

These are small but meaningful features that reflect how placement systems actually work in practice — not just "store and retrieve."

| Feature | What happens | Where it is |
|---|---|---|
| Interview Reschedule Notification | DB trigger fires on UPDATE, creates notification records | Trigger `trg_interview_reschedule` |
| Eligibility check before apply | Students see why they can't apply, not just a disabled button | `JobBrowsePanel` + `EligibilityChecker.java` |
| Duplicate application prevention | UNIQUE constraint on (Student_USN, Job_ID) at DB level | `APPLICATIONS` table |
| Application deadline enforcement | Jobs past deadline appear grayed out | `JobBrowsePanel` WHERE clause |
| Resume path storage | Resume not stored in DB blob — path stored, file on disk | `STUDENT.Resume_Path` column |
| One student, one offer (optional) | Once `Is_Placed = TRUE`, student is hidden from company shortlist | Filter in `vw_eligible_students` |
| Notification badge count | Bell icon shows unread count, refreshes on panel load | `NotificationDAO.getUnreadCount()` |
| Company verification gate | Unverified companies can post but jobs are hidden until admin verifies | `Is_Verified` flag in COMPANY |

---

## SECTION 8 — SQL IMPLEMENTATION REFERENCE

### Complex Queries

```sql
-- Q1: Students with no applications (at-risk report)
SELECT s.Student_USN, s.Name, s.CGPA, s.Dept
FROM Student s
LEFT JOIN Applications a ON s.Student_USN = a.Student_USN
WHERE a.App_ID IS NULL AND s.Is_Placed = FALSE;

-- Q2: Rejection rate per company
SELECT c.Company_Name,
  COUNT(a.App_ID) AS Total_Apps,
  SUM(CASE WHEN a.Status = 'Rejected' THEN 1 ELSE 0 END) AS Rejections,
  ROUND(SUM(CASE WHEN a.Status = 'Rejected' THEN 1 ELSE 0 END)*100.0/COUNT(a.App_ID), 1) AS Rejection_Rate
FROM Applications a
JOIN Job_Postings jp ON a.Job_ID = jp.Job_ID
JOIN Company c ON jp.Company_ID = c.Company_ID
GROUP BY c.Company_Name
ORDER BY Rejection_Rate DESC;

-- Q3: Students shortlisted but never selected (repeated rejections)
SELECT s.Student_USN, s.Name, COUNT(a.App_ID) AS Attempts
FROM Student s
JOIN Applications a ON s.Student_USN = a.Student_USN
WHERE a.Status = 'Rejected'
GROUP BY s.Student_USN, s.Name
HAVING COUNT(a.App_ID) >= 3;

-- Q4: Certifications held by placed students (to recommend to others)
SELECT sc.Cert_Name, COUNT(DISTINCT sc.Student_USN) AS Holders
FROM Student_Certifications sc
JOIN Student s ON sc.Student_USN = s.Student_USN
WHERE s.Is_Placed = TRUE
GROUP BY sc.Cert_Name
ORDER BY Holders DESC;
```

---

## SECTION 9 — ACCEPTANCE CRITERIA

A build is considered complete when all of the following pass:

1. Student can register with USN, name, email, CGPA, and department — record inserted in STUDENT table
2. Faculty can log in and access the admin panel; student login cannot access admin panel
3. Company can log in and access only company-facing panels
4. Company can post a job with minimum CGPA and allowed department — record inserted in JOB_POSTINGS
5. Student browsing jobs sees "Eligible" / "Not Eligible" status with a reason for ineligible jobs
6. Student cannot apply to an ineligible job (Apply button disabled)
7. Student can apply to an eligible job — record inserted in APPLICATIONS with status "Applied"
8. Duplicate application attempt (same student, same job) is rejected by the DB UNIQUE constraint
9. Company can shortlist a student — APPLICATION.Status updates to "Shortlisted"
10. Company can schedule an interview — record inserted in INTERVIEW_SCHEDULE
11. Company rescheduling an interview fires the trigger — NOTIFICATIONS table receives at least two new rows (one student, one faculty)
12. Student sees a notification badge update on their dashboard after a reschedule
13. Faculty can see all unread notifications in the notification inbox
14. Faculty clicks "Generate Report" — stored procedure `sp_generate_placement_report()` executes and populates the report panel
15. Placement report shows correct department-wise placement percentages
16. View `vw_application_pipeline` returns correct joined data for the admin applications table
17. View `vw_eligible_students` excludes already-placed students
18. Students with 0 applications appear in the at-risk query result
19. Rejection rate query returns valid percentages for all active companies
20. All three role login flows open the correct, isolated dashboard — no cross-role data leakage

---

## APPENDIX — DBMS CONCEPTS DEMONSTRATED

| Concept | Where used |
|---|---|
| Primary + Foreign Keys | All 9 tables |
| UNIQUE Constraint | Applications (Student_USN, Job_ID), Student.Email |
| CHECK Constraint | Student.CGPA BETWEEN 0 AND 10 |
| DEFAULT values | Applications.Status, Notifications.Is_Read |
| ENUM type | Applications.Status, Interview_Schedule.Result |
| TRIGGER (AFTER UPDATE) | `trg_interview_reschedule` |
| STORED PROCEDURE | `sp_generate_placement_report()` |
| VIEW | `vw_eligible_students`, `vw_application_pipeline`, `vw_dept_placement` |
| Complex JOIN | SQL queries in backend controllers |
| GROUP BY + HAVING | Repeated rejection logic |
| Subquery / LEFT JOIN | At-risk students dashboard |


---

*PlacementPro Spec Sheet v1.0 — BAI402G Mini Project*
