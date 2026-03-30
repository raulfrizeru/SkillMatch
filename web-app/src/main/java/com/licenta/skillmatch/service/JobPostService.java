package com.licenta.skillmatch.service;

import com.licenta.skillmatch.entity.JobPost;
import com.licenta.skillmatch.repository.JobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class JobPostService {
    @Autowired
    private JobPostRepository jobPostRepository;

    public JobPost createJobPost(JobPost jobPost) {
        return jobPostRepository.save(jobPost);
    }

    public List<JobPost> findAllJobPosts() {
        return jobPostRepository.findAll();
    }

    public List<JobPost> findJobPostById(Long id) {
        return Collections.singletonList(jobPostRepository.findById(id).orElse(null));
    }
}
