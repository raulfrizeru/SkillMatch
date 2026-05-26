package com.licenta.skillmatch.service;

import com.licenta.skillmatch.dto.JobEditDto;
import com.licenta.skillmatch.dto.JobPostDto;
import com.licenta.skillmatch.entity.Employer;
import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.repository.JobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobPostService {
    @Autowired
    private JobPostRepository jobPostRepository;


    public JobPost saveJobPost(JobPost jobPost) {
        return jobPostRepository.save(jobPost);
    }

    public List<JobPost> findAllJobPosts() {
        return jobPostRepository.findAll();
    }

    public List<JobPost> findJobPostById(Long id) {
        return Collections.singletonList(jobPostRepository.findById(id).orElse(null));
    }

    public List<JobPostDto> findActiveJobPosts() {
        return jobPostRepository.findByIsActiveTrue().stream()
                .map(this::convertToDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public List<JobPostDto> findActiveJobPostsFiltered(String searchTerm, String sortBy) {
        List<JobPostDto> jobs = findActiveJobPosts();

        // Filter by title, description or company
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            jobs = jobs.stream()
                    .filter(job -> job.getTitle().toLowerCase().contains(lowerSearchTerm) ||
                                   job.getDescription().toLowerCase().contains(lowerSearchTerm) ||
                                   job.getCompanyName().toLowerCase().contains(lowerSearchTerm))
                    .collect(Collectors.toList());
        }


        if ("date_newest".equals(sortBy)) {
            jobs.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
        } else if ("date_oldest".equals(sortBy)) {
            jobs.sort((a, b) -> a.getCreatedDate().compareTo(b.getCreatedDate()));
        } else if ("name_asc".equals(sortBy)) {
            jobs.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
        } else if ("name_desc".equals(sortBy)) {
            jobs.sort((a, b) -> b.getTitle().compareTo(a.getTitle()));
        }

        return jobs;
    }

    public List<JobPostDto> findJobPostsByEmployerIdFiltered(Long employerId, String searchTerm, String sortBy, String statusFilter) {
        List<JobPostDto> jobs = findJobPostsByEmployerId(employerId);

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            jobs = jobs.stream()
                    .filter(job -> job.getTitle().toLowerCase().contains(lowerSearchTerm) ||
                                   job.getDescription().toLowerCase().contains(lowerSearchTerm) ||
                                   job.getCompanyName().toLowerCase().contains(lowerSearchTerm))
                    .collect(Collectors.toList());
        }

        if ("active".equals(statusFilter)) {
            jobs = jobs.stream().filter(JobPostDto::isActive).collect(Collectors.toList());
        } else if ("inactive".equals(statusFilter)) {
            jobs = jobs.stream().filter(job -> !job.isActive()).collect(Collectors.toList());
        }


        if ("date_newest".equals(sortBy)) {
            jobs.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
        } else if ("date_oldest".equals(sortBy)) {
            jobs.sort((a, b) -> a.getCreatedDate().compareTo(b.getCreatedDate()));
        } else if ("name_asc".equals(sortBy)) {
            jobs.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
        } else if ("name_desc".equals(sortBy)) {
            jobs.sort((a, b) -> b.getTitle().compareTo(a.getTitle()));
        }

        return jobs;
    }

    public Optional<JobPost> findJobPostByIdOptional(Long id) {
        return jobPostRepository.findById(id);
    }

    public Optional<JobPostDto> findJobPostDtoById(Long id) {
        return jobPostRepository.findById(id).map(this::convertToDto);
    }

    public List<JobPostDto> findJobPostsByEmployerId(Long employerId) {
        return jobPostRepository.findAll().stream()
                .filter(jobPost -> Objects.equals(jobPost.getEmployer().getId(), employerId))
                .map(this::convertToDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }


    public void updateJob(Long id, JobEditDto jobEditDto) {
        Optional<JobPost> jobPost = jobPostRepository.findById(id);
        if (jobPost.isPresent()) {
            JobPost post = jobPost.get();
            post.setTitle(jobEditDto.getTitle());
            post.setDescription(jobEditDto.getDescription());
            post.setActive(jobEditDto.getIsActive() != null ? jobEditDto.getIsActive() : false);
            jobPostRepository.save(post);
        }
    }

    public void deleteJob(Long id) {
        if (jobPostRepository.existsById(id)) {
            jobPostRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Job post not found with id: " + id);
        }
    }

    public JobPostDto convertToDto(JobPost jobPost) {
        if (jobPost == null || jobPost.getEmployer() == null) {
            return null;
        }
        return JobPostDto.builder()
                .id(jobPost.getId())
                .title(jobPost.getTitle())
                .description(jobPost.getDescription())
                .employerId(jobPost.getEmployer().getId())
                .isActive(jobPost.isActive())
                .companyName(jobPost.getEmployer().getCompanyName())
                .employerDescription(jobPost.getEmployer().getDescription())
                .createdDate(jobPost.getCreatedDate())
                .applicantCount(jobPost.getApplications() != null ? jobPost.getApplications().size() : 0)
                .build();
    }

    public JobEditDto getJobForEdit(Long id) {
        JobPost jobPost = jobPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job post not found with id: " + id));

        return JobEditDto.builder()
                .id(jobPost.getId())
                .title(jobPost.getTitle())
                .description(jobPost.getDescription())
                .isActive(jobPost.isActive())
                .build();
    }

    public Long createNewJob(JobEditDto jobEditDto, Long employerId) {
        JobPost jobPost = new JobPost();
        jobPost.setTitle(jobEditDto.getTitle());
        jobPost.setDescription(jobEditDto.getDescription());
        jobPost.setActive(jobEditDto.getIsActive() != null ? jobEditDto.getIsActive() : true);

        Employer employer = new Employer();
        employer.setId(employerId);
        jobPost.setEmployer(employer);

        JobPost savedJob = jobPostRepository.save(jobPost);
        return savedJob.getId();
    }

}
