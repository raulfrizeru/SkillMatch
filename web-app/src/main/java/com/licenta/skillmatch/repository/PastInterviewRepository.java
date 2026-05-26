package com.licenta.skillmatch.repository;

import com.licenta.skillmatch.entity.PastInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastInterviewRepository extends JpaRepository <PastInterview, Long> {
    List<PastInterview> findByCandidateId(Long candidateId);
    List<PastInterview> findByCompanyAndJobTitle(String company, String jobTitle);
}
