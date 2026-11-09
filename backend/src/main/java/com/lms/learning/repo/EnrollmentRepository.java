package com.lms.learning.repo;

import com.lms.learning.domain.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(Long userId);
    Optional<Enrollment> findByUserIdAndCourseRef(Long userId, String courseRef);
}
