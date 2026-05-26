package com.licenta.skillmatch.repository;

import com.licenta.skillmatch.entity.BookedInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookedInterviewRepository extends JpaRepository<BookedInterview, Long> {
    Optional<BookedInterview> findByJobApplicationId(Long jobApplicationId);
    List<BookedInterview> findByJobApplicationCandidateId(Long candidateId);
    List<BookedInterview> findByJobApplicationJobPostEmployerId(Long employerId);
    List<BookedInterview> findByInterviewDateTimeBeforeAndCompletedAtIsNull(LocalDateTime dateTime);
}
