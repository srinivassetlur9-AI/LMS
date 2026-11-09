package com.lms.learning.repo;

import com.lms.learning.domain.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByActiveTrueOrderByTitleAsc();
    Optional<Course> findByCourseRefAndActiveTrue(String courseRef);
}
