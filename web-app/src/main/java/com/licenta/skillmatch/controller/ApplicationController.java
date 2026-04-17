package com.licenta.skillmatch.controller;

import com.licenta.skillmatch.dto.*;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.Employer;
import com.licenta.skillmatch.repository.CandidateRepository;
import com.licenta.skillmatch.service.JobApplicationService;
import com.licenta.skillmatch.service.JobPostService;
import com.licenta.skillmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class ApplicationController {
    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobPostService jobPostService;

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

        User user = (User) userRepository.findByUsername(username);
        if (user != null) {
            Candidate candidate = (Candidate) user;
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

        User user = userRepository.findByUsername(username);

        if (role.equals("ROLE_CANDIDATE")) {
            Candidate candidate = (Candidate) user;
            CandidateProfileDto profile = userService.getCandidateProfile(candidate.getId());
            model.addAttribute("profile", profile);
            model.addAttribute("role", "CANDIDATE");
        } else if (role.equals("ROLE_EMPLOYER")) {
            Employer employer = (Employer) user;
            EmployerProfileDto profile = userService.getEmployerProfile(employer.getId());
            model.addAttribute("profile", profile);
            model.addAttribute("role", "EMPLOYER");
        } else {
            return "redirect:/login";
        }

        return "profile";
    }



}
