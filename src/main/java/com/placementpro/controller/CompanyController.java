package com.placementpro.controller;

import com.placementpro.dto.ApplicationDTO;
import com.placementpro.dto.JobPostingDTO;
import com.placementpro.dto.NotificationDTO;
import com.placementpro.service.CompanyService;
import com.placementpro.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        String userIdStr = (String) session.getAttribute("userId");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";

        int companyId = Integer.parseInt(userIdStr);
        String companyName = companyService.getCompanyName(companyId);
        List<JobPostingDTO> jobs = companyService.getJobsByCompany(companyId);
        Map<String, Object> stats = companyService.getCompanyStats(companyId);

        model.addAttribute("companyName", companyName);
        model.addAttribute("companyId", companyId);
        model.addAttribute("jobs", jobs);
        model.addAttribute("stats", stats);

        return "company-dashboard";
    }

    @GetMapping("/applicants")
    public String viewApplicants(@RequestParam int jobId, HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";

        int companyId = Integer.parseInt((String) session.getAttribute("userId"));
        List<ApplicationDTO> applicants = companyService.getApplicantsForJob(jobId);
        String companyName = companyService.getCompanyName(companyId);

        model.addAttribute("applicants", applicants);
        model.addAttribute("jobId", jobId);
        model.addAttribute("companyName", companyName);
        model.addAttribute("companyId", companyId);

        return "company-applicants";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam int appId, @RequestParam String status, @RequestParam int jobId, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";
        companyService.updateApplicationStatus(appId, status);
        return "redirect:/company/applicants?jobId=" + jobId;
    }

    @PostMapping("/post-job")
    public String postJob(@RequestParam String roleName, @RequestParam double packageLpa,
                          @RequestParam double minCgpa, @RequestParam String allowedDepts,
                          @RequestParam String deadline, HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";
        int companyId = Integer.parseInt((String) session.getAttribute("userId"));
        companyService.postJob(companyId, roleName, packageLpa, minCgpa, allowedDepts, deadline);
        return "redirect:/company/dashboard";
    }

    @PostMapping("/schedule-interview")
    public String scheduleInterview(@RequestParam int appId, @RequestParam String roundName,
                                    @RequestParam String interviewDate, @RequestParam String interviewTime,
                                    @RequestParam(required = false) String venue, @RequestParam int jobId,
                                    HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";
        int companyId = Integer.parseInt((String) session.getAttribute("userId"));
        companyService.scheduleInterview(appId, roundName, interviewDate, interviewTime, venue, companyId);
        return "redirect:/company/applicants?jobId=" + jobId;
    }

    @PostMapping("/reschedule-interview")
    public String rescheduleInterview(@RequestParam int interviewId, @RequestParam String newDate,
                                      @RequestParam String newTime, @RequestParam int jobId,
                                      HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";
        // This fires trg_interview_reschedule TRIGGER automatically
        companyService.rescheduleInterview(interviewId, newDate, newTime);
        return "redirect:/company/applicants?jobId=" + jobId;
    }
}
