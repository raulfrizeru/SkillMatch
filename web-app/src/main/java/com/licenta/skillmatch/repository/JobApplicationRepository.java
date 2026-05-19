package com.licenta.skillmatch.repository;

import com.licenta.skillmatch.entity.JobApplication;
import com.licenta.skillmatch.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCandidateId(Long candidateId);
    List<JobApplication> findByJobPostId(Long jobPostId);
    JobApplication findByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);
}


