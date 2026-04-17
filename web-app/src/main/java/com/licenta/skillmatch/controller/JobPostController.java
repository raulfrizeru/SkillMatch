package com.licenta.skillmatch.controller;

import com.licenta.skillmatch.dto.JobEditDto;
import com.licenta.skillmatch.dto.JobPostDto;
import com.licenta.skillmatch.dto.RegisterDto;
import com.licenta.skillmatch.dto.UserEditDto;
import com.licenta.skillmatch.entity.Employer;
import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.service.JobPostService;
import com.licenta.skillmatch.service.JobApplicationService;
import com.licenta.skillmatch.repository.UserRepository;
import com.licenta.skillmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class JobPostController {


    @Autowired
    private JobPostService jobPostService;

    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private UserService userService;

    @GetMapping("/jobs")
    public String showJobPostsPage(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sortBy,
                                    @RequestParam(required = false) String status,
                                    Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("");

        if (role.equals("ROLE_CANDIDATE")) {
            List<JobPostDto> jobs = jobPostService.findActiveJobPostsFiltered(search, sortBy);
            model.addAttribute("jobs", jobs);
            model.addAttribute("role", "CANDIDATE");
            model.addAttribute("search", search);
            model.addAttribute("sortBy", sortBy);
            return "jobs";
        } else if (role.equals("ROLE_EMPLOYER")) {
            User user = userService.findByUsername(username);
            List<JobPostDto> jobs = jobPostService.findJobPostsByEmployerIdFiltered(user.getId(), search, sortBy, status);
            model.addAttribute("jobs", jobs);
            model.addAttribute("role", "EMPLOYER");
            model.addAttribute("search", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("status", status);
            return "jobs";
        } else {
            return "redirect:/login";
        }

    }

    @GetMapping("/jobs/{id}")
    public String showJobPostDetails(@PathVariable Long id, Model model) {
        Optional<JobPost> jobPost = jobPostService.findJobPostByIdOptional(id);
        if (jobPost.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String role = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .findFirst()
                    .orElse("");
            String username = authentication.getName();
            JobPost post = jobPost.get();
            JobPostDto jobPostDto = JobPostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .description(post.getDescription())
                    .employerId(post.getEmployer().getId())
                    .isActive(post.isActive())
                    .companyName(post.getEmployer().getCompanyName())
                    .employerDescription(post.getEmployer().getDescription())
                    .createdDate(post.getCreatedDate())
                    .applicantCount(post.getApplications() != null ? post.getApplications().size() : 0)
                    .build();
            model.addAttribute("job", jobPostDto);

            if (role.equals("ROLE_CANDIDATE")) {
                model.addAttribute("role", "CANDIDATE");

                // Check if candidate has CV uploaded
                User user = userService.findByUsername(username);
                if (user instanceof Candidate) {
                    Candidate candidate = (Candidate) user;
                    boolean hasCv = candidate.getCvFilePath() != null && !candidate.getCvFilePath().isEmpty();
                    model.addAttribute("hasCv", hasCv);
                }
            } else if (role.equals("ROLE_EMPLOYER")) {
                model.addAttribute("role", "EMPLOYER");
            }
            return "job-details";
        }
        return "redirect:/jobs";
    }

    @PostMapping("/jobs/{id}/apply")
    public String applyToJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.findByUsername(username);
        if (user != null && user instanceof Candidate) {
            Candidate candidate = (Candidate) user;

            // Check if candidate has CV uploaded
            if (candidate.getCvFilePath() == null || candidate.getCvFilePath().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please upload your CV before applying to jobs!");
                return "redirect:/jobs/" + id;
            }

            // Check if candidate has already applied
            if (jobApplicationService.hasAlreadyApplied(candidate.getId(), id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "You have already applied to this job!");
                return "redirect:/jobs/" + id;
            }

            jobApplicationService.createJobApplication(candidate.getId(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Only candidates can apply to jobs.");
        }

        return "redirect:/jobs/" + id;
    }

    @GetMapping("/jobs/new")
    public String showAddJobForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("");

        if (!role.equals("ROLE_EMPLOYER")) {
            return "redirect:/jobs";
        }

        JobEditDto jobEditDto = new JobEditDto();
        model.addAttribute("job", jobEditDto);
        return "add-job";
    }

    @PostMapping("/jobs/save")
    public String saveJob(@ModelAttribute("job") JobEditDto jobEditDto, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            if (user != null && user instanceof com.licenta.skillmatch.entity.Employer) {
                com.licenta.skillmatch.entity.Employer employer = (com.licenta.skillmatch.entity.Employer) user;
                Long jobId = jobPostService.createNewJob(jobEditDto, employer);
                redirectAttributes.addFlashAttribute("successMessage", "Job post created successfully!");
                return "redirect:/jobs/" + jobId;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Only employers can create job posts.");
                return "redirect:/jobs/new";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create job post: " + e.getMessage());
            return "redirect:/jobs/new";
        }
    }

    @GetMapping("/jobs/edit/{id}")
    public String showEditJobForm(@PathVariable Long id, Model model) {
        JobEditDto jobDto = jobPostService.getJobForEdit(id);
        model.addAttribute("job", jobDto);
        return "edit-job";
    }

    @PostMapping("/jobs/edit/{id}")
    public String updateJob(@PathVariable Long id, @ModelAttribute JobEditDto jobDto, RedirectAttributes redirectAttributes) {
        try {
            jobPostService.updateJob(id, jobDto);
            redirectAttributes.addFlashAttribute("successMessage", "Job post updated successfully!");
            return "redirect:/jobs/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update job post!");
            return "redirect:/jobs/edit/" + id;
        }
    }

    @GetMapping("/jobs/delete/{id}")
    public String deleteJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            jobPostService.deleteJob(id);
            redirectAttributes.addFlashAttribute("successMessage", "Job post deleted successfully!");
            return "redirect:/jobs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete job post!");
            return "redirect:/jobs";
        }
    }

    @GetMapping("/jobs/{id}/applicants")
    public String showJobApplicants(@PathVariable Long id, Model model) {
        Optional<JobPost> jobPost = jobPostService.findJobPostByIdOptional(id);
        if (jobPost.isPresent()) {
            JobPost post = jobPost.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            if (user instanceof Employer) {
                Employer employer = (Employer) user;
                if (!(employer.getId()==post.getEmployer().getId())) {
                    return "redirect:/jobs";
                }
            } else {
                return "redirect:/jobs";
            }

            JobPostDto jobPostDto = JobPostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .applicantCount(post.getApplications() != null ? post.getApplications().size() : 0)
                    .build();

            var applicants = jobApplicationService.getApplicantsByJobId(id);

            model.addAttribute("job", jobPostDto);
            model.addAttribute("applicants", applicants);
            return "job-applicants";
        }
        return "redirect:/jobs";
    }

}

