package com.licenta.skillmatch.service;

import com.licenta.skillmatch.dto.JobApplicationDto;
import com.licenta.skillmatch.dto.JobApplicantDto;
import com.licenta.skillmatch.entity.JobApplication;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.repository.JobApplicationRepository;
import com.licenta.skillmatch.repository.CandidateRepository;
import com.licenta.skillmatch.repository.JobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {
    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    public List<JobApplicationDto> getApplicationsByCandidate(Long candidateId) {
        return jobApplicationRepository.findByCandidateId(candidateId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean hasAlreadyApplied(Long candidateId, Long jobPostId) {
        return jobApplicationRepository.findByCandidateId(candidateId).stream()
                .anyMatch(app -> Objects.equals(app.getJobPost().getId(), jobPostId));
    }

    private JobApplicationDto convertToDto(JobApplication jobApplication) {
        return JobApplicationDto.builder()
                .id(jobApplication.getId())
                .jobTitle(jobApplication.getJobPost().getTitle())
                .companyName(jobApplication.getJobPost().getEmployer().getCompanyName())
                .jobDescription(jobApplication.getJobPost().getDescription())
                .applyDate(jobApplication.getApplyDate())
                .status(jobApplication.getStatus())
                .finalScore(jobApplication.getFinalScore())
                .semanticScore(jobApplication.getSemanticScore())
                .skillsScore(jobApplication.getSkillsScore())
                .experienceScore(jobApplication.getExperienceScore())
                .domainScore(jobApplication.getDomainScore())
                .softSkillsScore(jobApplication.getSoftSkillsScore())
                .interviewScore(jobApplication.getInterviewScore())
                .jobPostId(jobApplication.getJobPost().getId())
                .build();
    }

    public void createJobApplication(Long candidateId, Long jobPostId) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        Optional<JobPost> jobPostOpt = jobPostRepository.findById(jobPostId);
        if (candidateOpt.isPresent() && jobPostOpt.isPresent()) {
            JobApplication jobApplication = new JobApplication();
            jobApplication.setCandidate(candidateOpt.get());
            jobApplication.setJobPost(jobPostOpt.get());
            jobApplication.setApplyDate(LocalDateTime.now());
            jobApplicationRepository.save(jobApplication);
        } else {
            throw new IllegalArgumentException("Candidate or Job Post not found");
        }
    }

    public List<JobApplicantDto> getApplicantsByJobId(Long jobPostId) {
        return jobApplicationRepository.findByJobPostId(jobPostId).stream()
                .map(this::convertToJobApplicantDto)
                .collect(Collectors.toList());
    }

    private JobApplicantDto convertToJobApplicantDto(JobApplication jobApplication) {
        Candidate candidate = jobApplication.getCandidate();
        return JobApplicantDto.builder()
                .id(jobApplication.getId())
                .candidateName(candidate.getFirstName() + " " + candidate.getLastName())
                .candidateEmail(candidate.getEmail())
                .status(jobApplication.getStatus())
                .finalScore(jobApplication.getFinalScore())
                .applyDate(jobApplication.getApplyDate())
                .build();
    }
}
