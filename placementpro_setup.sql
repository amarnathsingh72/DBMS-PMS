-- ---------------------------------------------------------
-- PlacementPro Database Setup Script
-- Run this entire script in MySQL Workbench to initialize
-- the schema, tables, views, triggers, and sample data.
-- ---------------------------------------------------------

CREATE DATABASE IF NOT EXISTS placementpro_db;
USE placementpro_db;

-- ---------------------------------------------------------
-- 1. DROP EXISTING TABLES/VIEWS TO ALLOW RE-RUNNING SCRIPT
-- ---------------------------------------------------------
DROP VIEW IF EXISTS vw_eligible_students;
DROP VIEW IF EXISTS vw_application_pipeline;
DROP VIEW IF EXISTS vw_dept_placement;
DROP TRIGGER IF EXISTS trg_interview_reschedule;
DROP PROCEDURE IF EXISTS sp_generate_placement_report;

DROP TABLE IF EXISTS SKILLS;
DROP TABLE IF EXISTS FACULTY;
DROP TABLE IF EXISTS NOTIFICATIONS;
DROP TABLE IF EXISTS STUDENT_CERTIFICATIONS;
DROP TABLE IF EXISTS INTERVIEW_SCHEDULE;
DROP TABLE IF EXISTS APPLICATIONS;
DROP TABLE IF EXISTS JOB_POSTINGS;
DROP TABLE IF EXISTS COMPANY;
DROP TABLE IF EXISTS STUDENT;

-- ---------------------------------------------------------
-- 2. CREATE TABLES
-- ---------------------------------------------------------

-- 1. STUDENT
CREATE TABLE STUDENT (
    Student_USN VARCHAR(15) PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Phone VARCHAR(15),
    Dept VARCHAR(50) NOT NULL,
    Semester INT,
    CGPA DECIMAL(3,1) CHECK (CGPA BETWEEN 0 AND 10),
    Backlogs INT DEFAULT 0,
    Is_Placed BOOLEAN DEFAULT FALSE,
    Password_Hash VARCHAR(64) NOT NULL,
    Resume_Path VARCHAR(255),
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. COMPANY
CREATE TABLE COMPANY (
    Company_ID INT PRIMARY KEY AUTO_INCREMENT,
    Company_Name VARCHAR(100) NOT NULL,
    Location VARCHAR(100),
    Website VARCHAR(255),
    HR_Contact VARCHAR(100),
    Password_Hash VARCHAR(64) NOT NULL,
    Is_Verified BOOLEAN DEFAULT FALSE
);

-- 3. JOB_POSTINGS
CREATE TABLE JOB_POSTINGS (
    Job_ID INT PRIMARY KEY AUTO_INCREMENT,
    Company_ID INT,
    Role_Name VARCHAR(100) NOT NULL,
    Package_LPA DECIMAL(5,2),
    Min_CGPA DECIMAL(3,1) DEFAULT 0,
    Allowed_Depts VARCHAR(255),
    Preferred_Cert VARCHAR(255),
    Deadline DATE,
    Is_Active BOOLEAN DEFAULT TRUE,
    Posted_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (Company_ID) REFERENCES COMPANY(Company_ID)
);

-- 4. APPLICATIONS
CREATE TABLE APPLICATIONS (
    App_ID INT PRIMARY KEY AUTO_INCREMENT,
    Student_USN VARCHAR(15),
    Job_ID INT,
    Apply_Date DATE DEFAULT (CURRENT_DATE),
    Status ENUM('Applied','Shortlisted','Interview Scheduled','Selected','Rejected') DEFAULT 'Applied',
    UNIQUE (Student_USN, Job_ID),
    FOREIGN KEY (Student_USN) REFERENCES STUDENT(Student_USN),
    FOREIGN KEY (Job_ID) REFERENCES JOB_POSTINGS(Job_ID)
);

-- 5. INTERVIEW_SCHEDULE
CREATE TABLE INTERVIEW_SCHEDULE (
    Interview_ID INT PRIMARY KEY AUTO_INCREMENT,
    App_ID INT,
    Round_Name VARCHAR(50),
    Interview_Date DATE NOT NULL,
    Interview_Time TIME,
    Venue VARCHAR(255),
    Result ENUM('Pending','Pass','Fail') DEFAULT 'Pending',
    Scheduled_By INT,
    FOREIGN KEY (App_ID) REFERENCES APPLICATIONS(App_ID),
    FOREIGN KEY (Scheduled_By) REFERENCES COMPANY(Company_ID)
);

-- 6. STUDENT_CERTIFICATIONS
CREATE TABLE STUDENT_CERTIFICATIONS (
    Cert_ID INT PRIMARY KEY AUTO_INCREMENT,
    Student_USN VARCHAR(15),
    Cert_Name VARCHAR(100) NOT NULL,
    Issued_By VARCHAR(100),
    Issue_Date DATE,
    Cert_URL VARCHAR(255),
    FOREIGN KEY (Student_USN) REFERENCES STUDENT(Student_USN)
);

-- 7. NOTIFICATIONS
CREATE TABLE NOTIFICATIONS (
    Notif_ID INT PRIMARY KEY AUTO_INCREMENT,
    User_ID VARCHAR(50),
    User_Role ENUM('STUDENT','FACULTY','COMPANY') NOT NULL,
    Message TEXT NOT NULL,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Is_Read BOOLEAN DEFAULT FALSE,
    Notif_Type VARCHAR(50)
);

-- 8. FACULTY
CREATE TABLE FACULTY (
    Faculty_ID VARCHAR(20) PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE,
    Dept VARCHAR(50),
    Is_Placement_Officer BOOLEAN DEFAULT FALSE,
    Password_Hash VARCHAR(64) NOT NULL
);

-- 9. SKILLS
CREATE TABLE SKILLS (
    Skill_ID INT PRIMARY KEY AUTO_INCREMENT,
    Student_USN VARCHAR(15),
    Skill_Name VARCHAR(100) NOT NULL,
    Proficiency ENUM('Beginner','Intermediate','Advanced'),
    FOREIGN KEY (Student_USN) REFERENCES STUDENT(Student_USN)
);

-- ---------------------------------------------------------
-- 3. CREATE VIEWS
-- ---------------------------------------------------------

CREATE VIEW vw_eligible_students AS
SELECT s.Student_USN, s.Name, s.CGPA, s.Dept, jp.Job_ID, jp.Role_Name
FROM STUDENT s
JOIN JOB_POSTINGS jp ON s.CGPA >= jp.Min_CGPA
WHERE s.Is_Placed = FALSE AND jp.Is_Active = TRUE;

CREATE VIEW vw_application_pipeline AS
SELECT 
  a.App_ID, s.Name AS Student_Name, s.Student_USN AS USN, s.CGPA,
  c.Company_Name, jp.Role_Name, a.Status, a.Apply_Date
FROM APPLICATIONS a
JOIN STUDENT s ON a.Student_USN = s.Student_USN
JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID
JOIN COMPANY c ON jp.Company_ID = c.Company_ID;

CREATE VIEW vw_dept_placement AS
SELECT Dept,
  COUNT(*) AS Total,
  SUM(CASE WHEN Is_Placed = TRUE THEN 1 ELSE 0 END) AS Placed,
  ROUND(SUM(CASE WHEN Is_Placed = TRUE THEN 1 ELSE 0 END)*100.0/COUNT(*), 1) AS Pct
FROM STUDENT
GROUP BY Dept;

-- ---------------------------------------------------------
-- 4. CREATE TRIGGERS & PROCEDURES
-- ---------------------------------------------------------

DELIMITER $$

CREATE TRIGGER trg_interview_reschedule
AFTER UPDATE ON INTERVIEW_SCHEDULE
FOR EACH ROW
BEGIN
  -- Only fire if the interview date or time actually changed
  IF OLD.Interview_Date != NEW.Interview_Date OR OLD.Interview_Time != NEW.Interview_Time THEN

    -- Notify the student
    INSERT INTO NOTIFICATIONS (User_ID, User_Role, Message, Created_At, Is_Read)
    SELECT 
      a.Student_USN,
      'STUDENT',
      CONCAT('Your interview for ', j.Role_Name, ' at ', c.Company_Name,
             ' has been rescheduled to ', NEW.Interview_Date, ' ', NEW.Interview_Time),
      NOW(),
      FALSE
    FROM APPLICATIONS a
    JOIN JOB_POSTINGS j ON a.Job_ID = j.Job_ID
    JOIN COMPANY c ON j.Company_ID = c.Company_ID
    WHERE a.App_ID = NEW.App_ID;

    -- Notify all faculty placement officers
    INSERT INTO NOTIFICATIONS (User_ID, User_Role, Message, Created_At, Is_Read)
    SELECT 
      f.Faculty_ID,
      'FACULTY',
      CONCAT('Interview rescheduled — Student: ', a.Student_USN,
             ' | Role: ', j.Role_Name, ' | New Date: ', NEW.Interview_Date),
      NOW(),
      FALSE
    FROM APPLICATIONS a
    JOIN JOB_POSTINGS j ON a.Job_ID = j.Job_ID
    JOIN FACULTY f ON f.Is_Placement_Officer = TRUE
    WHERE a.App_ID = NEW.App_ID;

  END IF;
END$$

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
  FROM STUDENT s
  LEFT JOIN APPLICATIONS a ON s.Student_USN = a.Student_USN AND a.Status = 'Selected'
  LEFT JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID
  GROUP BY s.Dept
  ORDER BY Placement_Percentage DESC;

  -- Top 5 companies by hire count
  SELECT 
    c.Company_Name,
    COUNT(DISTINCT a.Student_USN) AS Hires,
    ROUND(AVG(jp.Package_LPA), 2) AS Avg_Package
  FROM APPLICATIONS a
  JOIN JOB_POSTINGS jp ON a.Job_ID = jp.Job_ID
  JOIN COMPANY c ON jp.Company_ID = c.Company_ID
  WHERE a.Status = 'Selected'
  GROUP BY c.Company_Name
  ORDER BY Hires DESC
  LIMIT 5;
END$$

DELIMITER ;

-- ---------------------------------------------------------
-- 5. INSERT TEST DATA (Passwords are SHA-256 for 'password123')
-- ---------------------------------------------------------

-- Insert 20 Students
INSERT INTO STUDENT (Student_USN, Name, Email, Dept, Semester, CGPA, Password_Hash) VALUES 
('4SF24CI001', 'Rahul Sharma', 'rahul@example.com', 'CSE', 4, 8.5, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI002', 'Priya Singh', 'priya@example.com', 'ECE', 4, 7.8, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI003', 'Amit Kumar', 'amit@example.com', 'MECH', 4, 6.5, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI004', 'Neha Gupta', 'neha@example.com', 'CIVIL', 4, 9.1, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI005', 'Vikram Verma', 'vikram@example.com', 'CSE', 4, 8.0, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI006', 'Sanya M', 'sanya@example.com', 'ECE', 4, 8.2, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI007', 'Rohan Das', 'rohan@example.com', 'CSE', 4, 7.1, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI008', 'Kavya S', 'kavya@example.com', 'CIVIL', 4, 8.9, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI009', 'Arjun P', 'arjun@example.com', 'MECH', 4, 7.5, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI010', 'Aditi Rao', 'aditi@example.com', 'CSE', 4, 9.5, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI011', 'Varun T', 'varun@example.com', 'ECE', 4, 6.8, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI012', 'Pooja N', 'pooja@example.com', 'CSE', 4, 7.9, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI013', 'Karan L', 'karan@example.com', 'MECH', 4, 7.0, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI014', 'Shruti B', 'shruti@example.com', 'CIVIL', 4, 8.4, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI015', 'Nishant G', 'nishant@example.com', 'CSE', 4, 8.6, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI016', 'Ritu K', 'ritu@example.com', 'ECE', 4, 7.6, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI017', 'Deepak H', 'deepak@example.com', 'MECH', 4, 6.2, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI018', 'Ananya V', 'ananya@example.com', 'CSE', 4, 9.2, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI019', 'Rakesh M', 'rakesh@example.com', 'CIVIL', 4, 7.4, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('4SF24CI020', 'Shubham S', 'shubham@example.com', 'CSE', 4, 8.8, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');


-- Insert 8 Companies
INSERT INTO COMPANY (Company_Name, Location, Website, HR_Contact, Password_Hash, Is_Verified) VALUES 
('Infosys', 'Bangalore', 'infosys.com', 'hr@infosys.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE),
('TCS', 'Mumbai', 'tcs.com', 'careers@tcs.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE),
('Wipro', 'Pune', 'wipro.com', 'jobs@wipro.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE),
('Amazon', 'Hyderabad', 'amazon.in', 'recruiting@amazon.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE),
('L&T Construction', 'Chennai', 'lntecc.com', 'hr@lntecc.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE),
('Bosch', 'Bangalore', 'bosch.in', 'hr@bosch.in', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE),
('Tech Mahindra', 'Pune', 'techmahindra.com', 'hr@techmahindra.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE),
('Google', 'Bangalore', 'google.com', 'india-hr@google.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', TRUE);

-- Insert 16 Job Postings
INSERT INTO JOB_POSTINGS (Company_ID, Role_Name, Package_LPA, Min_CGPA, Allowed_Depts, Deadline) VALUES 
(1, 'System Engineer', 4.5, 6.5, 'CSE,ECE', '2026-06-01'),
(1, 'Power Programmer', 8.0, 8.0, 'CSE', '2026-06-01'),
(2, 'Ninja Developer', 3.36, 6.0, 'CSE,ECE,MECH,CIVIL', '2026-06-10'),
(2, 'Digital Developer', 7.0, 7.5, 'CSE,ECE', '2026-06-10'),
(3, 'Project Engineer', 3.5, 6.0, 'CSE,ECE,MECH,CIVIL', '2026-06-15'),
(3, 'Turbo Engineer', 6.5, 7.0, 'CSE,ECE', '2026-06-15'),
(4, 'SDE-1', 15.0, 8.5, 'CSE', '2026-05-30'),
(4, 'Cloud Support Associate', 8.5, 7.0, 'CSE,ECE', '2026-05-30'),
(5, 'Graduate Engineer Trainee', 6.0, 6.5, 'CIVIL,MECH', '2026-06-20'),
(5, 'Design Engineer', 7.5, 7.5, 'CIVIL', '2026-06-20'),
(6, 'Embedded Systems Engineer', 8.0, 7.5, 'ECE,MECH', '2026-06-25'),
(6, 'Software Developer', 9.0, 8.0, 'CSE', '2026-06-25'),
(7, 'Associate Software Engineer', 4.0, 6.0, 'CSE,ECE,MECH', '2026-07-01'),
(7, 'Data Analyst', 5.5, 6.5, 'CSE,ECE', '2026-07-01'),
(8, 'Software Engineer', 24.0, 9.0, 'CSE', '2026-05-25'),
(8, 'Site Reliability Engineer', 18.0, 8.5, 'CSE,ECE', '2026-05-25');

-- Insert Applications (Some applied, some shortlisted, some selected)
INSERT INTO APPLICATIONS (Student_USN, Job_ID, Status) VALUES 
('4SF24CI001', 7, 'Applied'),
('4SF24CI001', 12, 'Shortlisted'),
('4SF24CI002', 1, 'Selected'),
('4SF24CI002', 4, 'Interview Scheduled'),
('4SF24CI003', 9, 'Shortlisted'),
('4SF24CI004', 10, 'Selected'),
('4SF24CI005', 2, 'Applied'),
('4SF24CI006', 11, 'Shortlisted'),
('4SF24CI007', 3, 'Applied'),
('4SF24CI008', 9, 'Applied'),
('4SF24CI010', 15, 'Interview Scheduled'),
('4SF24CI020', 7, 'Shortlisted'),
('4SF24CI020', 15, 'Applied');

-- Mark selected students as Is_Placed = TRUE
UPDATE STUDENT SET Is_Placed = TRUE WHERE Student_USN IN ('4SF24CI002', '4SF24CI004');

-- Insert Certifications
INSERT INTO STUDENT_CERTIFICATIONS (Student_USN, Cert_Name, Issued_By) VALUES 
('4SF24CI001', 'AWS Certified Solutions Architect', 'Amazon'),
('4SF24CI010', 'Google Cloud Professional', 'Google'),
('4SF24CI020', 'Oracle Java SE 11 Developer', 'Oracle'),
('4SF24CI002', 'CCNA', 'Cisco'),
('4SF24CI004', 'AutoCAD Certified Professional', 'Autodesk');

-- Insert Interview Schedules
INSERT INTO INTERVIEW_SCHEDULE (App_ID, Round_Name, Interview_Date, Interview_Time, Scheduled_By) VALUES 
(2, 'Technical', '2026-06-25', '10:00:00', 6),
(4, 'HR', '2026-06-12', '14:30:00', 2),
(5, 'Technical', '2026-06-22', '11:00:00', 5),
(8, 'Technical', '2026-06-28', '15:00:00', 6),
(11, 'DSA Round', '2026-05-28', '09:00:00', 8),
(12, 'System Design', '2026-06-05', '16:00:00', 4);

-- Insert Faculty
INSERT INTO FACULTY (Faculty_ID, Name, Email, Dept, Is_Placement_Officer, Password_Hash) VALUES 
('FAC001', 'Dr. Suresh R', 'suresh@example.com', 'CSE', TRUE, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('FAC002', 'Prof. Anita K', 'anita@example.com', 'ECE', FALSE, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

-- Insert Skills
INSERT INTO SKILLS (Student_USN, Skill_Name, Proficiency) VALUES 
('4SF24CI020', 'Java', 'Advanced'),
('4SF24CI020', 'MySQL', 'Intermediate'),
('4SF24CI001', 'Python', 'Advanced'),
('4SF24CI010', 'C++', 'Advanced'),
('4SF24CI004', 'AutoCAD', 'Advanced');

-- ---------------------------------------------------------
-- SETUP COMPLETE
-- ---------------------------------------------------------
