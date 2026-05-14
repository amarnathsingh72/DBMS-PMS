# PlacementPro - Implementation Plan (Web Version)

## System Overview
PlacementPro is being transformed from a desktop application into a modern, full-stack web application. This version follows the stack used by your seniors to ensure industry-standard practices and premium aesthetics.

*   **Frontend**: React (Vite), Tailwind CSS, Axios, React Router
*   **Backend**: Node.js, Express, MySQL2, JWT (Auth), Bcrypt (Hashing)
*   **Database**: MySQL 8.x
*   **Utilities**: pdfkit (PDF generation), json2csv (CSV reports)

## Role Capabilities
1.  **Student**: Profile management, eligible job discovery (dynamic SQL logic), applications, and real-time notifications.
2.  **Company**: Job posting, applicant shortlisting, interview scheduling with automated rescheduling triggers.
3.  **Faculty**: Global oversight, application pipeline visualization, and automated placement analytics.

## Core Database Engineering
*   **9 Normalized Tables**: STUDENT, COMPANY, JOB_POSTINGS, APPLICATIONS, INTERVIEW_SCHEDULE, STUDENT_CERTIFICATIONS, NOTIFICATIONS, FACULTY, SKILLS.
*   **Database Trigger (`trg_interview_reschedule`)**: Automatically dispatches notifications to students and faculty whenever a company reschedules an interview.
*   **Stored Procedure (`sp_generate_placement_report`)**: Aggregates placement statistics (department percentages, top hiring companies) directly on the DBMS.

## Development Strategy
We will build the application in two main blocks: the Backend API and the Frontend Dashboard.

### Phase 0: Database & Environment Setup
*   [ ] Initialize MySQL schema using `placementpro_setup.sql`.
*   [ ] Set up Node.js project and install dependencies (`express`, `mysql2`, `dotenv`, `cors`, etc.).
*   [ ] Configure environment variables for database connection.

### Phase 1: Backend Core & Authentication
*   [ ] Implement JWT-based authentication system.
*   [ ] Create middleware for role-based access control (Student/Company/Faculty).
*   [ ] Implement Bcrypt password hashing.

### Phase 2: Frontend Foundation (React + Tailwind)
*   [ ] Initialize React app with Vite.
*   [ ] Configure Tailwind CSS for premium aesthetics (Glassmorphism, Dark Mode).
*   [ ] Set up React Router and Axios instance.

### Phase 3: Student & Company Portals
*   [ ] Build Student profile and job browsing (Eligibility engine).
*   [ ] Build Company job posting and applicant management.

### Phase 4: Interview System & Triggers
*   [ ] Implement interview scheduling.
*   [ ] Verify DB Trigger notifications on interview reschedules.

### Phase 5: Reporting & Analytics
*   [ ] Implement PDF generation (pdfkit) for offer letters/reports.
*   [ ] Implement CSV export (json2csv) for placement data.
*   [ ] Execute Stored Procedures for admin dashboard charts.

