package com.lms.learning.repo;

import com.lms.learning.domain.LiveSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    List<LiveSession> findByActiveTrueOrderByStartsAtAsc();
}
