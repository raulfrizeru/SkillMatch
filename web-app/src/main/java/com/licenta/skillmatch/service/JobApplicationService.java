package com.licenta.skillmatch.service;

import com.licenta.skillmatch.dto.JobApplicationDto;
import com.licenta.skillmatch.dto.JobApplicantDto;
import com.licenta.skillmatch.entity.JobApplication;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.CandidateJobScore;
import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.repository.JobApplicationRepository;
import com.licenta.skillmatch.repository.CandidateRepository;
import com.licenta.skillmatch.repository.JobPostRepository;
import com.licenta.skillmatch.repository.CandidateJobScoreRepository;
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

    @Autowired
    private CandidateJobScoreRepository candidateJobScoreRepository;

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
                .candidateName(jobApplication.getCandidate().getFirstName() + " " + jobApplication.getCandidate().getLastName())
                .candidateEmail(jobApplication.getCandidate().getEmail())
                .candidateCvPath(jobApplication.getCandidate().getCvFilePath())
                .build();
    }

    public void createJobApplication(Long candidateId, Long jobPostId) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        Optional<JobPost> jobPostOpt = jobPostRepository.findById(jobPostId);
        if (candidateOpt.isPresent() && jobPostOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            JobPost jobPost = jobPostOpt.get();
            JobApplication jobApplication = new JobApplication();
            jobApplication.setCandidate(candidate);
            jobApplication.setJobPost(jobPost);
            jobApplication.setApplyDate(LocalDateTime.now());

            // Caută CandidateJobScore dacă există
            Optional<CandidateJobScore> scoreOpt = candidateJobScoreRepository
                    .findByCandidateIdAndJobPostId(candidateId, jobPostId);

            if (scoreOpt.isPresent()) {
                CandidateJobScore score = scoreOpt.get();
                // Preluează scorurile
                jobApplication.setSemanticScore(score.getSemanticScore());
                jobApplication.setSkillsScore(score.getSkillScore());
                jobApplication.setDomainScore(score.getDomainScore());
                jobApplication.setExperienceScore(score.getExperienceScore());
                jobApplication.setSoftSkillsScore(score.getSoftSkillsScore());
                jobApplication.setInterviewScore(score.getInterviewScore());
                jobApplication.setFinalScore(score.getFinalScore());

                // Preluează datele extrase
                if (score.getExtractedCvJson() != null) {
                    candidate.setExtractedCvJson(score.getExtractedCvJson());
                }
                if (score.getCvOcrText() != null) {
                    candidate.setExtractedCvOcr(score.getCvOcrText());
                }
                if (score.getExtractedJobJson() != null) {
                    jobPost.setExtractedJobJson(score.getExtractedJobJson());
                }

                candidateRepository.save(candidate);
                jobPostRepository.save(jobPost);

                // Șterge CandidateJobScore după preluare
                candidateJobScoreRepository.delete(score);
            }

            jobApplicationRepository.save(jobApplication);
        } else {
            throw new IllegalArgumentException("Candidate or Job Post not found");
        }
    }

    public List<JobApplicantDto> getApplicantsByJobId(Long jobPostId) {
        List<JobApplication> allApplications = jobApplicationRepository.findByJobPostId(jobPostId);

        // Separă aplicanții cu scor de cei fără scor
        List<JobApplication> withScore = allApplications.stream()
                .filter(app -> app.getFinalScore() != null)
                .sorted((app1, app2) -> app2.getFinalScore().compareTo(app1.getFinalScore()))
                .collect(Collectors.toList());

        List<JobApplication> withoutScore = allApplications.stream()
                .filter(app -> app.getFinalScore() == null)
                .sorted((app1, app2) -> app1.getApplyDate().compareTo(app2.getApplyDate()))
                .collect(Collectors.toList());

        // Combină lista: mai întâi cei cu scor, apoi cei fără scor
        List<JobApplication> sorted = new java.util.ArrayList<>(withScore);
        sorted.addAll(withoutScore);

        return sorted.stream()
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
                .candidateCvPath(candidate.getCvFilePath())
                .build();
    }
}
