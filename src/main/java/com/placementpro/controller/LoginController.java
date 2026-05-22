package com.placementpro.controller;

import com.placementpro.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (session.getAttribute("userId") != null && role != null) {
            if ("STUDENT".equals(role)) return "redirect:/student/dashboard";
            if ("FACULTY".equals(role)) return "redirect:/faculty/dashboard";
            if ("COMPANY".equals(role)) return "redirect:/company/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, 
                              @RequestParam String password, 
                              @RequestParam String role, 
                              HttpSession session,
                              Model model) {
        
        boolean isValid = false;

        if ("STUDENT".equals(role)) {
            isValid = authService.validateStudent(username, password);
        } else if ("FACULTY".equals(role)) {
            isValid = authService.validateFaculty(username, password);
        } else if ("COMPANY".equals(role)) {
            isValid = authService.validateCompany(username, password);
        }

        if (isValid) {
            session.setAttribute("userId", username);
            session.setAttribute("role", role);
            if ("STUDENT".equals(role)) return "redirect:/student/dashboard";
            if ("FACULTY".equals(role)) return "redirect:/faculty/dashboard";
            if ("COMPANY".equals(role)) return "redirect:/company/dashboard";
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Invalid credentials. Please try again.");
            return "login";
        }
    }

    @PostMapping("/signup")
    public String handleSignup(@RequestParam String role,
                               @RequestParam(required = false) String username, // Student USN or Faculty ID
                               @RequestParam(required = false) String password,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) String email,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String dept,
                               @RequestParam(required = false) Integer semester,
                               @RequestParam(required = false) Double cgpa,
                               @RequestParam(required = false) Boolean isPlacementOfficer,
                               @RequestParam(required = false) String companyName,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String website,
                               @RequestParam(required = false) String hrContact,
                               Model model) {
        
        String error = null;
        if ("STUDENT".equals(role)) {
            if (semester == null) semester = 1;
            if (cgpa == null) cgpa = 0.0;
            error = authService.registerStudent(username, name, email, phone, dept, semester, cgpa, password);
            if (error == null) {
                model.addAttribute("success", "Student registered successfully! Please log in.");
            }
        } else if ("FACULTY".equals(role)) {
            boolean isPO = isPlacementOfficer != null && isPlacementOfficer;
            error = authService.registerFaculty(username, name, email, dept, isPO, password);
            if (error == null) {
                model.addAttribute("success", "Faculty registered successfully! Please log in.");
            }
        } else if ("COMPANY".equals(role)) {
            String[] errorMsg = new String[1];
            int companyId = authService.registerCompany(companyName, location, website, hrContact, password, errorMsg);
            if (companyId > 0) {
                model.addAttribute("success", "Company registered successfully! Your Company ID is: " + companyId + ". Please use this ID to sign in.");
            } else {
                error = errorMsg[0] != null ? errorMsg[0] : "Company registration failed.";
            }
        } else {
            error = "Invalid role selected.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("signupActive", true); // To keep the signup tab active on reload in case of error
        }
        return "login";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
