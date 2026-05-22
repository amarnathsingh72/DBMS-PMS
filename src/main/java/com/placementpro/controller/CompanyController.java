package com.placementpro.controller;

import com.placementpro.dto.ApplicationDTO;
import com.placementpro.dto.JobPostingDTO;
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

        // Fetch lists for the new sections representing actions taken
        List<ApplicationDTO> shortlisted = companyService.getApplicantsByStatus(companyId, "Shortlisted");
        List<ApplicationDTO> selected = companyService.getApplicantsByStatus(companyId, "Selected");
        List<ApplicationDTO> interviewScheduled = companyService.getApplicantsByStatus(companyId, "Interview Scheduled");
        List<ApplicationDTO> rejected = companyService.getApplicantsByStatus(companyId, "Rejected");

        // Fetch status counts for the alignment-fixed polar chart
        Map<String, Integer> statusCounts = companyService.getStatusCounts(companyId);

        model.addAttribute("companyName", companyName);
        model.addAttribute("companyId", companyId);
        model.addAttribute("jobs", jobs);
        model.addAttribute("stats", stats);
        model.addAttribute("statusCounts", statusCounts);

        // Add lists to model
        model.addAttribute("shortlisted", shortlisted);
        model.addAttribute("selected", selected);
        model.addAttribute("interviewScheduled", interviewScheduled);
        model.addAttribute("rejected", rejected);

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
    public String updateStatus(@RequestParam int appId, 
                               @RequestParam String status, 
                               @RequestParam(required = false, defaultValue = "-1") int jobId, 
                               HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";
        
        companyService.updateApplicationStatus(appId, status);
        
        if (jobId != -1) {
            return "redirect:/company/applicants?jobId=" + jobId;
        } else {
            // Redirect to dashboard with active tab depending on the updated state
            String tab = "jobs";
            if ("Shortlisted".equals(status)) tab = "shortlisted";
            else if ("Selected".equals(status)) tab = "selected";
            else if ("Interview Scheduled".equals(status)) tab = "interviews";
            else if ("Rejected".equals(status)) tab = "rejected";
            return "redirect:/company/dashboard?tab=" + tab;
        }
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
                                    @RequestParam(required = false) String venue, @RequestParam(required = false, defaultValue = "-1") int jobId,
                                    HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";
        int companyId = Integer.parseInt((String) session.getAttribute("userId"));
        companyService.scheduleInterview(appId, roundName, interviewDate, interviewTime, venue, companyId);
        
        if (jobId != -1) {
            return "redirect:/company/applicants?jobId=" + jobId;
        } else {
            return "redirect:/company/dashboard?tab=interviews";
        }
    }

    @PostMapping("/reschedule-interview")
    public String rescheduleInterview(@RequestParam int interviewId, @RequestParam String newDate,
                                      @RequestParam String newTime, @RequestParam(required = false, defaultValue = "-1") int jobId,
                                      HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"COMPANY".equals(role)) return "redirect:/login";
        companyService.rescheduleInterview(interviewId, newDate, newTime);
        
        if (jobId != -1) {
            return "redirect:/company/applicants?jobId=" + jobId;
        } else {
            return "redirect:/company/dashboard?tab=interviews";
        }
    }
}
