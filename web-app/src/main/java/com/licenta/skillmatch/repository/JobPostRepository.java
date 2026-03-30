package com.licenta.skillmatch.repository;

import com.licenta.skillmatch.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    public List<JobPost> findByIsActiveTrue(long id);
}
