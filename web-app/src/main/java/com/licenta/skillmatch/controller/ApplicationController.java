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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("");

        if (role.equals("ROLE_CANDIDATE")) {
            CandidateProfileDto candidate = userService.getCandidateProfileByUsername(username);
            if (candidate != null) {
                List<DashboardInterviewDto> upcomingInterviews = jobApplicationService.getUpcomingInterviewsByCandidate(candidate.getId());
                List<DashboardInterviewDto> pastInterviews = jobApplicationService.getPastInterviewsByCandidate(candidate.getId());
                List<CandidateScoreStatsDto> scoreStats = jobApplicationService.getCandidateScoreStats(candidate.getId());

                model.addAttribute("role", "CANDIDATE");
                model.addAttribute("upcomingInterviews", upcomingInterviews);
                model.addAttribute("pastInterviews", pastInterviews);
                model.addAttribute("scoreStats", scoreStats);
                model.addAttribute("candidateName", candidate.getFirstName() + " " + candidate.getLastName());
            }
        } else if (role.equals("ROLE_EMPLOYER")) {
            EmployerProfileDto employer = userService.getEmployerProfileByUsername(username);
            if (employer != null) {
                List<DashboardInterviewDto> upcomingInterviews = jobApplicationService.getUpcomingInterviewsByEmployer(employer.getId());
                List<DashboardInterviewDto> overdueInterviews = jobApplicationService.getOverdueInterviewsByEmployer(employer.getId());
                List<DashboardInterviewDto> pastInterviews = jobApplicationService.getPastInterviewsByEmployer(employer.getId());
                List<EmployerJobScoreStatsDto> jobStats = jobApplicationService.getJobScoreStats(employer.getId());

                model.addAttribute("role", "EMPLOYER");
                model.addAttribute("upcomingInterviews", upcomingInterviews);
                model.addAttribute("overdueInterviews", overdueInterviews);
                model.addAttribute("pastInterviews", pastInterviews);
                model.addAttribute("jobStats", jobStats);
                model.addAttribute("companyName", employer.getCompanyName());
            }
        } else {
            return "redirect:/login";
        }

        return "dashboard";
    }

    @PostMapping("/interviews/{interviewId}/rate")
    public String rateInterview(@PathVariable Long interviewId,
                                @RequestParam Double score,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String role = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .findFirst()
                    .orElse("");

            if (!role.equals("ROLE_EMPLOYER")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only employers can rate interviews.");
                return "redirect:/dashboard";
            }

            jobApplicationService.rateInterview(interviewId, score);
            redirectAttributes.addFlashAttribute("successMessage", "Interview rated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to rate interview: " + e.getMessage());
        }

        return "redirect:/dashboard";
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
