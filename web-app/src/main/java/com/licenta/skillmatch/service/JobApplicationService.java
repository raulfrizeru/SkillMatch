package com.licenta.skillmatch.service;

import com.licenta.skillmatch.dto.CandidateJobScoreDto;
import com.licenta.skillmatch.dto.JobApplicationDto;
import com.licenta.skillmatch.dto.JobApplicantDto;
import com.licenta.skillmatch.dto.BookedInterviewDto;
import com.licenta.skillmatch.dto.DashboardInterviewDto;
import com.licenta.skillmatch.dto.CandidateScoreStatsDto;
import com.licenta.skillmatch.dto.EmployerJobScoreStatsDto;
import com.licenta.skillmatch.entity.JobApplication;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.CandidateJobScore;
import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.entity.BookedInterview;
import com.licenta.skillmatch.entity.PastInterview;
import com.licenta.skillmatch.repository.JobApplicationRepository;
import com.licenta.skillmatch.repository.CandidateRepository;
import com.licenta.skillmatch.repository.JobPostRepository;
import com.licenta.skillmatch.repository.CandidateJobScoreRepository;
import com.licenta.skillmatch.repository.BookedInterviewRepository;
import com.licenta.skillmatch.repository.PastInterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Autowired
    private BookedInterviewRepository bookedInterviewRepository;
    @Autowired
    private PastInterviewRepository pastInterviewRepository;

    public List<JobApplicationDto> getApplicationsByCandidate(Long candidateId) {
        return jobApplicationRepository.findByCandidateId(candidateId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public JobApplicationDto getJobApplicationDtoByCandidateAndJob(Long candidateId, Long jobId) {
        JobApplication jobApplication = jobApplicationRepository.findByCandidateIdAndJobPostId(candidateId, jobId);
        if (jobApplication != null) {
            return convertToDto(jobApplication);
        }
        return null;
    }

    public CandidateJobScoreDto getCandidateJobScoreDto(Long candidateId, Long jobId) {
        Optional<CandidateJobScore> scoreOpt = candidateJobScoreRepository.findByCandidateIdAndJobPostId(candidateId, jobId);
        if (scoreOpt.isPresent()) {
            CandidateJobScore score = scoreOpt.get();
            return com.licenta.skillmatch.dto.CandidateJobScoreDto.builder()
                    .id(score.getId())
                    .semanticScore(score.getSemanticScore())
                    .skillScore(score.getSkillScore())
                    .experienceScore(score.getExperienceScore())
                    .domainScore(score.getDomainScore())
                    .softSkillsScore(score.getSoftSkillsScore())
                    .interviewScore(score.getInterviewScore())
                    .finalScore(score.getFinalScore())
                    .candidateId(score.getCandidate() != null ? score.getCandidate().getId() : null)
                    .jobPostId(score.getJobPost() != null ? score.getJobPost().getId() : null)
                    .build();
        }
        return null;
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

            Optional<CandidateJobScore> scoreOpt = candidateJobScoreRepository
                    .findByCandidateIdAndJobPostId(candidateId, jobPostId);

            if (scoreOpt.isPresent()) {
                CandidateJobScore score = scoreOpt.get();

                jobApplication.setSemanticScore(score.getSemanticScore());
                jobApplication.setSkillsScore(score.getSkillScore());
                jobApplication.setDomainScore(score.getDomainScore());
                jobApplication.setExperienceScore(score.getExperienceScore());
                jobApplication.setSoftSkillsScore(score.getSoftSkillsScore());
                jobApplication.setInterviewScore(score.getInterviewScore());
                jobApplication.setFinalScore(score.getFinalScore());


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


        List<JobApplication> withScore = allApplications.stream()
                .filter(app -> app.getFinalScore() != null)
                .sorted((app1, app2) -> app2.getFinalScore().compareTo(app1.getFinalScore()))
                .collect(Collectors.toList());

        List<JobApplication> withoutScore = allApplications.stream()
                .filter(app -> app.getFinalScore() == null)
                .sorted((app1, app2) -> app1.getApplyDate().compareTo(app2.getApplyDate()))
                .collect(Collectors.toList());


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

    public Optional<JobApplicationDto> findDtoById(Long applicationId) {
        return jobApplicationRepository.findById(applicationId).map(this::convertToDto);
    }

    public void updateApplicationStatus(Long applicationId, String status) {
        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        jobApplication.setStatus(status);
        jobApplicationRepository.save(jobApplication);
    }

    public BookedInterviewDto bookInterview(Long applicationId, LocalDateTime interviewDateTime) {
        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        BookedInterview bookedInterview = new BookedInterview();
        bookedInterview.setJobApplication(jobApplication);
        bookedInterview.setInterviewDateTime(interviewDateTime);
        bookedInterview.setCreatedAt(LocalDateTime.now());

        BookedInterview savedInterview = bookedInterviewRepository.save(bookedInterview);
        jobApplication.setStatus("INTERVIEW_BOOKED");
        jobApplicationRepository.save(jobApplication);

        return convertBookedInterviewToDto(savedInterview);
    }

    public void hireCandidate(Long applicationId) {
        updateApplicationStatus(applicationId, "ACCEPTED");
    }

    public void rejectCandidate(Long applicationId) {
        updateApplicationStatus(applicationId, "REJECTED");
    }

    private BookedInterviewDto convertBookedInterviewToDto(BookedInterview bookedInterview) {
        return BookedInterviewDto.builder()
                .id(bookedInterview.getId())
                .jobApplicationId(bookedInterview.getJobApplication().getId())
                .interviewDateTime(bookedInterview.getInterviewDateTime())
                .interviewScore(bookedInterview.getInterviewScore())
                .createdAt(bookedInterview.getCreatedAt())
                .completedAt(bookedInterview.getCompletedAt())
                .build();
    }

    public List<DashboardInterviewDto> getUpcomingInterviewsByCandidate(Long candidateId) {
        List<BookedInterview> interviews = bookedInterviewRepository.findByJobApplicationCandidateId(candidateId);
        return interviews.stream()
                .filter(bi -> bi.getCompletedAt() == null)
                .filter(bi -> bi.getInterviewDateTime().isAfter(LocalDateTime.now()))
                .map(this::convertToDashboardInterviewDto)
                .collect(Collectors.toList());
    }

    public List<DashboardInterviewDto> getPastInterviewsByCandidate(Long candidateId) {
        List<BookedInterview> interviews = bookedInterviewRepository.findByJobApplicationCandidateId(candidateId);
        return interviews.stream()
                .filter(bi -> bi.getCompletedAt() != null)
                .map(this::convertToDashboardInterviewDto)
                .collect(Collectors.toList());
    }

    public List<DashboardInterviewDto> getUpcomingInterviewsByEmployer(Long employerId) {
        List<BookedInterview> interviews = bookedInterviewRepository.findByJobApplicationJobPostEmployerId(employerId);
        return interviews.stream()
                .filter(bi -> bi.getCompletedAt() == null)
                .filter(bi -> bi.getInterviewDateTime().isAfter(LocalDateTime.now()))
                .map(bi -> convertToDashboardInterviewDto(bi, true))
                .collect(Collectors.toList());
    }

    public List<DashboardInterviewDto> getPastInterviewsByEmployer(Long employerId) {
        List<BookedInterview> interviews = bookedInterviewRepository.findByJobApplicationJobPostEmployerId(employerId);
        return interviews.stream()
                .filter(bi -> bi.getCompletedAt() != null)
                .map(bi -> convertToDashboardInterviewDto(bi, false))
                .collect(Collectors.toList());
    }

    public List<DashboardInterviewDto> getOverdueInterviewsByEmployer(Long employerId) {
        List<BookedInterview> interviews = bookedInterviewRepository.findByJobApplicationJobPostEmployerId(employerId);
        return interviews.stream()
                .filter(bi -> bi.getCompletedAt() == null)
                .filter(bi -> bi.getInterviewDateTime().isBefore(LocalDateTime.now()))
                .map(bi -> convertToDashboardInterviewDto(bi, true))
                .collect(Collectors.toList());
    }

    private DashboardInterviewDto convertToDashboardInterviewDto(BookedInterview interview) {
        return convertToDashboardInterviewDto(interview, false);
    }

    private DashboardInterviewDto convertToDashboardInterviewDto(BookedInterview interview, Boolean isEmployerView) {
        JobApplication jobApp = interview.getJobApplication();
        // Multiply scores by 100 for dashboard display
        Double displayInterviewScore = interview.getInterviewScore() != null ? interview.getInterviewScore() * 100 : null;
        return DashboardInterviewDto.builder()
                .id(interview.getId())
                .jobApplicationId(jobApp.getId())
                .candidateName(jobApp.getCandidate().getFirstName() + " " + jobApp.getCandidate().getLastName())
                .jobTitle(jobApp.getJobPost().getTitle())
                .companyName(jobApp.getJobPost().getEmployer().getCompanyName())
                .interviewDateTime(interview.getInterviewDateTime())
                .interviewScore(displayInterviewScore)
                .completedAt(interview.getCompletedAt())
                .canRate(isEmployerView && interview.getCompletedAt() == null &&
                        interview.getInterviewDateTime().isBefore(LocalDateTime.now()))
                .build();
    }

    public List<EmployerJobScoreStatsDto> getJobScoreStats(Long employerId) {
        List<JobPost> jobs = jobPostRepository.findAll().stream()
                .filter(job -> job.getEmployer().getId()==employerId)
                .toList();

        return jobs.stream()
                .map(job -> {
                    List<JobApplication> applications = jobApplicationRepository.findByJobPostId(job.getId())
                            .stream()
                            .filter(app -> app.getFinalScore() != null)
                            .toList();

                    if (applications.isEmpty()) {
                        return EmployerJobScoreStatsDto.builder()
                                .jobId(job.getId())
                                .jobTitle(job.getTitle())
                                .totalApplicants(0)
                                .build();
                    }

                    List<Double> scores = applications.stream()
                            .map(JobApplication::getFinalScore)
                            .map(s -> Math.round(s * 10000.0) / 100.0)
                            .sorted()
                            .toList();

                    Double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    Double max = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                    Double min = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                    Double median = scores.size() % 2 == 0
                            ? (scores.get(scores.size() / 2 - 1) + scores.get(scores.size() / 2)) / 2
                            : scores.get(scores.size() / 2);

                    average = Math.round(average * 100.0) / 100.0;
                    max = Math.round(max * 100.0) / 100.0;
                    min = Math.round(min * 100.0) / 100.0;
                    median = Math.round(median * 100.0) / 100.0;

                    return EmployerJobScoreStatsDto.builder()
                            .jobId(job.getId())
                            .jobTitle(job.getTitle())
                            .averageScore(average)
                            .maxScore(max)
                            .minScore(min)
                            .medianScore(median)
                            .totalApplicants(applications.size())
                            .build();
                })
                .toList();
    }

    public List<CandidateScoreStatsDto> getCandidateScoreStats(Long candidateId) {
        List<JobApplication> applications = jobApplicationRepository.findByCandidateId(candidateId)
                .stream()
                .filter(app -> app.getFinalScore() != null)
                .toList();

        if (applications.isEmpty()) {
            return new ArrayList<>();
        }

        Double maxScore = applications.stream()
                .mapToDouble(app -> Math.round(app.getFinalScore() * 10000.0) / 100.0)
                .max()
                .orElse(0.0);

        Double minScore = applications.stream()
                .mapToDouble(app -> Math.round(app.getFinalScore() * 10000.0) / 100.0)
                .min()
                .orElse(0.0);

        return applications.stream()
                .map(app -> {
                    double score = Math.round(app.getFinalScore() * 10000.0) / 100.0;
                    return CandidateScoreStatsDto.builder()
                        .jobTitle(app.getJobPost().getTitle())
                        .companyName(app.getJobPost().getEmployer().getCompanyName())
                        .score(score)
                        .isMax(score == maxScore)
                        .isMin(score == minScore)
                        .build();
                })
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .toList();
    }

    public void rateInterview(Long bookedInterviewId, Double score) {
        BookedInterview interview = bookedInterviewRepository.findById(bookedInterviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        // Convert score from 0-100 to 0-1 for storage
        Double normalizedScore = score / 100.0;
        interview.setInterviewScore(normalizedScore);
        interview.setCompletedAt(LocalDateTime.now());
        bookedInterviewRepository.save(interview);

        // Create PastInterview record
        JobApplication jobApp = interview.getJobApplication();
        PastInterview pastInterview = new PastInterview();
        pastInterview.setCandidate(jobApp.getCandidate());
        pastInterview.setCompany(jobApp.getJobPost().getEmployer().getCompanyName());
        pastInterview.setJobTitle(jobApp.getJobPost().getTitle());
        pastInterview.setScore(normalizedScore);
        pastInterviewRepository.save(pastInterview);

        // Update JobApplication status
        jobApp.setStatus("INTERVIEW_DONE");
        jobApplicationRepository.save(jobApp);
    }
}
