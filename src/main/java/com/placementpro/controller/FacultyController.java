package com.placementpro.controller;

import com.placementpro.dto.NotificationDTO;
import com.placementpro.service.FacultyService;
import com.placementpro.service.NotificationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        String facultyId = (String) session.getAttribute("userId");
        if (role == null || !"FACULTY".equals(role)) return "redirect:/login";

        String name = facultyService.getFacultyName(facultyId);
        Map<String, Object> stats = facultyService.getFacultyStats();
        List<Map<String, Object>> students = facultyService.getAllStudents();
        List<Map<String, Object>> companies = facultyService.getAllCompanies();
        List<Map<String, Object>> pipeline = facultyService.getApplicationPipeline();
        List<Map<String, Object>> atRisk = facultyService.getAtRiskStudents();
        List<NotificationDTO> notifications = notificationService.getNotifications(facultyId, "FACULTY");
        int unreadCount = notificationService.getUnreadCount(facultyId, "FACULTY");

        model.addAttribute("facultyName", name);
        model.addAttribute("facultyId", facultyId);
        model.addAttribute("stats", stats);
        model.addAttribute("students", students);
        model.addAttribute("companies", companies);
        model.addAttribute("pipeline", pipeline);
        model.addAttribute("atRisk", atRisk);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "faculty-dashboard";
    }

    @PostMapping("/send-alerts")
    public String sendAlerts(HttpSession session, RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        String facultyId = (String) session.getAttribute("userId");
        if (role == null || !"FACULTY".equals(role)) return "redirect:/login";

        int alertCount = facultyService.sendRiskAlerts(facultyId);
        if (alertCount > 0) {
            redirectAttributes.addFlashAttribute("alertSuccess", "Urgent intervention warnings successfully sent to " + alertCount + " at-risk students and faculty members!");
        } else {
            redirectAttributes.addFlashAttribute("alertInfo", "No unplaced students with zero applications found to alert.");
        }
        return "redirect:/faculty/dashboard?tab=atrisk";
    }

    @GetMapping("/reports")
    public String showReports(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        String facultyId = (String) session.getAttribute("userId");
        if (role == null || !"FACULTY".equals(role)) return "redirect:/login";

        String name = facultyService.getFacultyName(facultyId);
        List<Map<String, Object>> deptReport = facultyService.getDeptPlacementReport();
        List<Map<String, Object>> topCompanies = facultyService.getTopCompanies();

        model.addAttribute("facultyName", name);
        model.addAttribute("deptReport", deptReport);
        model.addAttribute("topCompanies", topCompanies);

        return "faculty-reports";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllRead(HttpSession session) {
        String facultyId = (String) session.getAttribute("userId");
        if (facultyId == null) return "redirect:/login";
        notificationService.markAllAsRead(facultyId, "FACULTY");
        return "redirect:/faculty/dashboard?tab=notifications";
    }

    @GetMapping("/reports/export")
    public void exportReport(HttpSession session, HttpServletResponse response) throws IOException {
        String role = (String) session.getAttribute("role");
        if (role == null || !"FACULTY".equals(role)) return;

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=placement_report.csv");

        List<Map<String, Object>> deptReport = facultyService.getDeptPlacementReport();
        
        StringBuilder csv = new StringBuilder();
        csv.append("Department,Total Students,Placed Students,Placement %,Avg Package (LPA)\n");
        for (Map<String, Object> row : deptReport) {
            csv.append(row.get("Dept")).append(",")
               .append(row.get("Total_Students")).append(",")
               .append(row.get("Placed_Students")).append(",")
               .append(row.get("Placement_Percentage")).append(",")
               .append(row.get("Avg_Package_LPA") != null ? row.get("Avg_Package_LPA") : "0").append("\n");
        }

        response.getWriter().write(csv.toString());
    }
}
