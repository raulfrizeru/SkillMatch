package com.licenta.skillmatch.controller;

import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.CandidateJobScore;
import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.entity.JobApplication;
import com.licenta.skillmatch.repository.CandidateJobScoreRepository;
import com.licenta.skillmatch.repository.JobPostRepository;
import com.licenta.skillmatch.repository.UserRepository;
import com.licenta.skillmatch.repository.JobApplicationRepository;
import com.licenta.skillmatch.service.AiIntegrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AIController {
    @Autowired
    private AiIntegrationService aiIntegrationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JobPostRepository jobPostRepository;
    @Autowired
    private CandidateJobScoreRepository candidateJobScoreRepository;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/applications/score-application/{jobId}")
    public String scoreApplication(@PathVariable Long jobId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (!(user instanceof Candidate)) {
            return "redirect:/login";
        }
        Candidate candidate = (Candidate) user;

        Optional<JobPost> jobOpt = jobPostRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return "redirect:/applications?error=Job%20not%20found";
        }

        String model = "llama3.2:1b";
        aiIntegrationService.runAiScoring(candidate, jobOpt.get(), model);
        return "redirect:/applications";
    }

    @GetMapping("/jobs/{jobId}/calculate-match-score")
    public String calculateMatchScore(@PathVariable Long jobId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (!(user instanceof Candidate)) {
            return "redirect:/login";
        }
        Candidate candidate = (Candidate) user;

        Optional<JobPost> jobOpt = jobPostRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return "redirect:/jobs/" + jobId + "?error=Job%20not%20found";
        }
        JobPost job = jobOpt.get();

        String model = "llama3.2:1b";

        try {
            String jsonResponse = aiIntegrationService.runAiScoring(candidate, job, model);

            // Parse response from Python - response contains: scores, extracted_cv_json, extracted_job_json, cv_ocr_text
            @SuppressWarnings("unchecked")
            Map<String, Object> fullResponse = objectMapper.readValue(jsonResponse, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> scores = (Map<String, Object>) fullResponse.get("scores");

            if (scores == null) {
                return "redirect:/jobs/" + jobId + "?error=Invalid%20response%20from%20scoring";
            }


            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) scores.get("details");

            // Create or update CandidateJobScore
            CandidateJobScore candidateJobScore = candidateJobScoreRepository
                    .findByCandidateIdAndJobPostId(candidate.getId(), job.getId())
                    .orElse(new CandidateJobScore());

            candidateJobScore.setCandidate(candidate);
            candidateJobScore.setJobPost(job);


            if (details != null) {
                candidateJobScore.setSemanticScore((Double)details.get("semantic_score"));
                candidateJobScore.setSkillScore((Double)details.get("skills_score"));
                candidateJobScore.setDomainScore((Double)details.get("domain_score"));
                candidateJobScore.setExperienceScore((Double)details.get("experience_score"));
                candidateJobScore.setSoftSkillsScore((Double)details.get("soft_skills_score"));
                candidateJobScore.setInterviewScore((Double)details.get("interview_score"));
            }

            // Set final score from root level
            candidateJobScore.setFinalScore((Double)scores.get("final_score"));

            // Save extracted data if present
            if (fullResponse.containsKey("extracted_cv_json")) {
                candidateJobScore.setExtractedCvJson(objectMapper.writeValueAsString(fullResponse.get("extracted_cv_json")));
            }
            if (fullResponse.containsKey("extracted_job_json")) {
                candidateJobScore.setExtractedJobJson(objectMapper.writeValueAsString(fullResponse.get("extracted_job_json")));
            }
            if (fullResponse.containsKey("cv_ocr_text")) {
                candidateJobScore.setCvOcrText((String) fullResponse.get("cv_ocr_text"));
            }

            candidateJobScoreRepository.save(candidateJobScore);
            return "redirect:/jobs/" + jobId + "?success=Match%20score%20calculated%20successfully";
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown%20error";
            return "redirect:/jobs/" + jobId + "?error=" + errorMsg.replace(" ", "%20");
        }
    }

    @GetMapping("/jobs/{jobId}/applicants/{applicationId}/calculate-score")
    public String calculateScoreForApplicationFromJobApplicants(@PathVariable Long jobId, @PathVariable Long applicationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        Optional<JobPost> jobOpt = jobPostRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return "redirect:/jobs/" + jobId + "/applicants?error=Job%20not%20found";
        }
        JobPost job = jobOpt.get();

        Optional<JobApplication> appOpt = jobApplicationRepository.findById(applicationId);
        if (appOpt.isEmpty()) {
            return "redirect:/jobs/" + jobId + "/applicants?error=Application%20not%20found";
        }
        JobApplication jobApplication = appOpt.get();
        Candidate candidate = jobApplication.getCandidate();

        String model = "llama3.2:1b";

        try {
            String jsonResponse = aiIntegrationService.runAiScoring(candidate, job, model);

            // Parse response from Python
            @SuppressWarnings("unchecked")
            Map<String, Object> fullResponse = objectMapper.readValue(jsonResponse, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> scores = (Map<String, Object>) fullResponse.get("scores");

            if (scores == null) {
                return "redirect:/jobs/" + jobId + "/applicants?error=Invalid%20response%20from%20scoring";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) scores.get("details");

            // Update JobApplication with scores
            if (details != null) {
                jobApplication.setSemanticScore((Double)details.get("semantic_score"));
                jobApplication.setSkillsScore((Double)details.get("skills_score"));
                jobApplication.setDomainScore((Double)details.get("domain_score"));
                jobApplication.setExperienceScore((Double)details.get("experience_score"));
                jobApplication.setSoftSkillsScore((Double)details.get("soft_skills_score"));
                jobApplication.setInterviewScore((Double)details.get("interview_score"));
            }

            jobApplication.setFinalScore((Double)scores.get("final_score"));
            jobApplicationRepository.save(jobApplication);

            return "redirect:/jobs/" + jobId + "/applicants?success=Match%20score%20calculated%20successfully";
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown%20error";
            return "redirect:/jobs/" + jobId + "/applicants?error=" + errorMsg.replace(" ", "%20");
        }
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        return ((Number) value).doubleValue();
    }

    @GetMapping("/jobs/{jobId}/applicants/calculate-all-scores")
    public String calculateScoresForAllApplicants(@PathVariable Long jobId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        Optional<JobPost> jobOpt = jobPostRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return "redirect:/jobs/" + jobId + "/applicants?error=Job%20not%20found";
        }
        JobPost job = jobOpt.get();

        // Get all applications for this job that don't have a final score yet
        List<JobApplication> applicationsWithoutScore = jobApplicationRepository.findByJobPostId(jobId).stream()
                .filter(app -> app.getFinalScore() == null)
                .toList();

        if (applicationsWithoutScore.isEmpty()) {
            return "redirect:/jobs/" + jobId + "/applicants?success=All%20applicants%20already%20have%20scores";
        }

        String model = "llama3.2:1b";
        int successCount = 0;
        int failureCount = 0;

        for (JobApplication jobApplication : applicationsWithoutScore) {
            Candidate candidate = jobApplication.getCandidate();
            try {
                String jsonResponse = aiIntegrationService.runAiScoring(candidate, job, model);

                // Parse response from Python
                @SuppressWarnings("unchecked")
                Map<String, Object> fullResponse = objectMapper.readValue(jsonResponse, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> scores = (Map<String, Object>) fullResponse.get("scores");

                if (scores != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> details = (Map<String, Object>) scores.get("details");

                    // Update JobApplication with scores
                    if (details != null) {
                        jobApplication.setSemanticScore((Double)details.get("semantic_score"));
                        jobApplication.setSkillsScore((Double)details.get("skills_score"));
                        jobApplication.setDomainScore((Double)details.get("domain_score"));
                        jobApplication.setExperienceScore((Double)details.get("experience_score"));
                        jobApplication.setSoftSkillsScore((Double)details.get("soft_skills_score"));
                        jobApplication.setInterviewScore((Double)details.get("interview_score"));
                    }

                    jobApplication.setFinalScore((Double)scores.get("final_score"));
                    jobApplicationRepository.save(jobApplication);
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }

        // Build success message without using String.format with %20
        String message = "Calculated%20scores%20for%20" + successCount + "%20applicants";
        if (failureCount > 0) {
            message += ".%20" + failureCount + "%20failed";
        }

        return "redirect:/jobs/" + jobId + "/applicants?success=" + message;
    }
}

