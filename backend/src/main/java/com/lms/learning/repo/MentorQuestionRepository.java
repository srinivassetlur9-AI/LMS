package com.lms.learning.repo;

import com.lms.learning.domain.MentorQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorQuestionRepository extends JpaRepository<MentorQuestion, Long> {
    List<MentorQuestion> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<MentorQuestion> findByStatusOrderByCreatedAtAsc(String status);
}
