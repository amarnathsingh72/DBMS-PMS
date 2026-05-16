# PlacementPro — Project Overview & Progress Tracker

## 1. Project Purpose
PlacementPro is a role-based, database-driven management system designed to automate the college placement pipeline. It connects Students, Recruiters, and Faculty in a structured workflow, replacing manual spreadsheet tracking with real-time data and automated notifications.

## 2. Tech Stack (Hybrid Java-Web)
To meet the requirement of **40-50% Java for the frontend**, we are using a hybrid architecture:

| Layer | Technology | Role |
|---|---|---|
| **Backend** | Java (Spring Boot 3.2.5) | Business logic, Security, and API services. |
| **Frontend Rendering** | **Thymeleaf (Java)** | Handles UI data binding and logic using Java-style attributes. |
| **Frontend Interactivity** | JavaScript / Tailwind CSS | Premium aesthetics, animations, and client-side behavior. |
| **Database** | **MySQL 8.x** | Persistent storage, triggers for notifications, and stored procedures for reports. |
| **Build Tool** | Maven | Dependency management and build automation. |

## 3. What We Have Built (Progress)

### ✅ Phase 0: Foundation
- [x] Initialized Spring Boot project structure.
- [x] Configured `pom.xml` with required dependencies (spring-boot-starter-jdbc, thymeleaf, web, mysql).
- [x] Set up `application.properties` for MySQL connection.
- [x] Created the main Application Entry point.

### ✅ Phase 1: Authentication Shell
- [x] Created `LoginController` to handle web requests.
- [x] Designed a premium `login.html` template using Thymeleaf and Tailwind CSS.
- [x] Implemented role-switching logic (Student/Faculty/Company) on the UI.

### ✅ Phase 2: Database Integration
- [x] Created `AuthService` using Spring `JdbcTemplate` for real DB authentication.
- [x] Implemented Session management (`HttpSession`) for web.
- [x] Role-based redirect after login (Student → /student/dashboard, etc.).
- [x] Logout endpoint with session invalidation.

### ✅ Phase 3: Student Portal (S2, S3, S4, S5)
- [x] `StudentController` — dashboard with full data binding.
- [x] `StudentService` — profile, stats, job eligibility, applications, interviews.
- [x] **Browse Jobs** tab — Job Eligibility Engine checks CGPA, Dept, already applied.
- [x] **Apply to Job** — inserts into APPLICATIONS via POST.
- [x] **My Applications** tab — shows status chips + expandable interview rounds.
- [x] **Notifications** tab — read/unread with mark-as-read.

### ✅ Phase 4: Company Portal (S6, S7)
- [x] `CompanyController` — dashboard, post job, applicant management.
- [x] `CompanyService` — jobs listing, applicant queries, status updates, interview scheduling.
- [x] **My Jobs** tab — all posted jobs with applicant count.
- [x] **Post New Job** tab — form to insert into JOB_POSTINGS.
- [x] **Applicant Management** — change status dropdown (Applied → Shortlisted → Selected/Rejected).
- [x] **Schedule Interview** — modal with date/time picker, inserts into INTERVIEW_SCHEDULE.
- [x] **Reschedule Interview** — UPDATE fires `trg_interview_reschedule` TRIGGER automatically.
- [x] Auto-set `Is_Placed = TRUE` when student selected.

### ✅ Phase 5: Faculty Admin Panel (S8, S9)
- [x] `FacultyController` — full admin dashboard + reports page.
- [x] `FacultyService` — all students, companies, pipeline, at-risk, placement report.
- [x] **Students** tab — all students with USN, CGPA, placement status.
- [x] **Companies** tab — all companies with verification status.
- [x] **Applications** tab — uses `vw_application_pipeline` VIEW.
- [x] **At-Risk** tab — Q1 query: students with 0 applications.
- [x] **Notifications** tab — with "Mark All Read" bulk action.
- [x] **Reports Page** — replicates `sp_generate_placement_report()` output with progress bars.
- [x] Top 5 companies by hire count with ranking.

### ✅ Phase 6: Notification System (F5, F7)
- [x] `NotificationService` — CRUD for notifications.
- [x] `NotificationDTO` — data transfer for notification inbox.
- [x] Unread badge count in navbar for Student + Faculty.
- [x] Mark individual / mark all as read.
- [x] Trigger-backed reschedule notifications (no application code writes to NOTIFICATIONS for reschedules).

### 🏗️ Phase 7: Polish & Testing (Next)
- [ ] Run `mvn spring-boot:run` and verify all flows.
- [ ] Test all 20 acceptance criteria from spec.
- [ ] Add error handling and edge case guards.

---

## 4. Database Connection Details
- **Database Name**: `placementpro_db`
- **User**: `root`
- **Host**: `localhost`
- **Port**: `3306`
- **Setup Script**: `placementpro_setup.sql` (located in the project root)

## 5. Files Created

### Java (Backend — ~60% of project logic)
| File | Purpose |
|---|---|
| `PlacementProApplication.java` | Spring Boot entry point |
| `controller/LoginController.java` | Auth routing, session management |
| `controller/StudentController.java` | Student dashboard, apply, notifications |
| `controller/CompanyController.java` | Company dashboard, post job, applicants, interviews |
| `controller/FacultyController.java` | Admin panel, reports |
| `service/AuthService.java` | SHA-256 login validation via JdbcTemplate |
| `service/StudentService.java` | Student profile, eligibility engine, applications |
| `service/CompanyService.java` | Job CRUD, applicant mgmt, interview scheduling |
| `service/FacultyService.java` | Reports, analytics, at-risk queries |
| `service/NotificationService.java` | Notification CRUD, mark-read |
| `dto/StudentDTO.java` | Student profile + stats |
| `dto/JobPostingDTO.java` | Job + eligibility status |
| `dto/ApplicationDTO.java` | Application + interview rounds |
| `dto/InterviewDTO.java` | Interview schedule details |
| `dto/NotificationDTO.java` | Notification data |

### Thymeleaf Templates (Frontend — Java-rendered HTML)
| File | Screen |
|---|---|
| `login.html` | S1 — Login with role tabs |
| `student-dashboard.html` | S2/S3/S4/S5 — Full student portal |
| `company-dashboard.html` | S6 — Company dashboard + post job |
| `company-applicants.html` | S7 — Applicant management + interview modal |
| `faculty-dashboard.html` | S8 — Admin panel (5 tabs) |
| `faculty-reports.html` | S9 — Placement report with progress bars |

## 6. Test Credentials
All test accounts use password: `password123`

| Role | Login ID |
|---|---|
| Student | `4SF24CI001` through `4SF24CI020` |
| Company | `1` through `8` |
| Faculty | `FAC001` (Placement Officer) or `FAC002` |
