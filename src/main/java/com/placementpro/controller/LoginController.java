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
        if (session.getAttribute("userId") != null) {
            return "redirect:/dashboard";
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
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
