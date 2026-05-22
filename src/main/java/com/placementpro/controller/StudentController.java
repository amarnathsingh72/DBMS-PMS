package com.placementpro.controller;

import com.placementpro.dto.ApplicationDTO;
import com.placementpro.dto.JobPostingDTO;
import com.placementpro.dto.NotificationDTO;
import com.placementpro.dto.StudentDTO;
import com.placementpro.service.GeminiService;
import com.placementpro.service.NotificationService;
import com.placementpro.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GeminiService geminiService;

    @GetMapping("/ai-advice")
    public String getAiAdvice(HttpSession session, Model model) {
        String usn = (String) session.getAttribute("userId");
        StudentDTO student = studentService.getStudentDashboardInfo(usn);
        
        String prompt = "Give a 3-sentence career advice for a student named " + student.getName() + 
                       " from " + student.getDept() + " department with a CGPA of " + student.getCgpa() + 
                       ". Focus on placement readiness.";
        
        String advice = geminiService.getAIAdvice(prompt);
        model.addAttribute("aiAdvice", advice);
        return "student-dashboard :: ai-section";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String usn = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (role == null || !"STUDENT".equals(role)) return "redirect:/login";

        StudentDTO student = studentService.getStudentDashboardInfo(usn);
        List<JobPostingDTO> jobs = studentService.getAvailableJobsForStudent(usn, student);
        List<ApplicationDTO> applications = studentService.getMyApplications(usn);
        List<NotificationDTO> notifications = notificationService.getNotifications(usn, "STUDENT");
        int unreadCount = notificationService.getUnreadCount(usn, "STUDENT");

        model.addAttribute("student", student);
        model.addAttribute("jobs", jobs);
        model.addAttribute("applications", applications);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("activeTab", "dashboard");

        return "student-dashboard";
    }

    @PostMapping("/apply")
    public String applyToJob(@RequestParam int jobId, HttpSession session) {
        String usn = (String) session.getAttribute("userId");
        if (usn == null) return "redirect:/login";
        studentService.applyToJob(usn, jobId);
        return "redirect:/student/dashboard?tab=jobs";
    }

    @PostMapping("/notifications/read")
    public String markNotifRead(@RequestParam int notifId, HttpSession session) {
        String usn = (String) session.getAttribute("userId");
        if (usn == null) return "redirect:/login";
        notificationService.markAsRead(notifId);
        return "redirect:/student/dashboard?tab=notifications";
    }
}
