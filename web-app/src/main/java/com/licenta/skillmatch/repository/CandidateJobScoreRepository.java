package com.licenta.skillmatch.repository;

import com.licenta.skillmatch.entity.CandidateJobScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateJobScoreRepository extends JpaRepository<CandidateJobScore, Long> {
    Optional<CandidateJobScore> findByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);
    void deleteByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);
}

