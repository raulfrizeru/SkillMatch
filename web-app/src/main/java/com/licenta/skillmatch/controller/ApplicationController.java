package com.licenta.skillmatch.controller;

import com.licenta.skillmatch.dto.*;
import com.licenta.skillmatch.service.JobApplicationService;
import com.licenta.skillmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ApplicationController {
    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard";
    }

    @GetMapping("/about")
    public String showAboutUsPage() {
        return "about";
    }

    @GetMapping("/login")
    public String ShowLoginForm(){
        return "login";
    }

    @GetMapping("/applications")
    public String showApplicationsPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        CandidateProfileDto candidate = userService.getCandidateProfileByUsername(username);
        if (candidate != null) {
            List<JobApplicationDto> applications = jobApplicationService.getApplicationsByCandidate(candidate.getId());
            model.addAttribute("applications", applications);
        }

        return "applications";
    }


    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("");

        if (role.equals("ROLE_CANDIDATE")) {
            CandidateProfileDto profile = userService.getCandidateProfileByUsername(username);
            model.addAttribute("profile", profile);
            model.addAttribute("role", "CANDIDATE");
        } else if (role.equals("ROLE_EMPLOYER")) {
            EmployerProfileDto profile = userService.getEmployerProfileByUsername(username);
            model.addAttribute("profile", profile);
            model.addAttribute("role", "EMPLOYER");
        } else {
            return "redirect:/login";
        }

        return "profile";
    }

}
