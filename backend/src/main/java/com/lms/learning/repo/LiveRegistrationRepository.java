package com.lms.learning.repo;

import com.lms.learning.domain.LiveRegistration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveRegistrationRepository extends JpaRepository<LiveRegistration, Long> {
    List<LiveRegistration> findByUserId(Long userId);
    Optional<LiveRegistration> findByUserIdAndSessionId(Long userId, Long sessionId);
}
