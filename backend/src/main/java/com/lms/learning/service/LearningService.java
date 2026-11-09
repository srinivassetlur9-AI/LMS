package com.lms.learning.service;

import com.lms.identity.web.ApiException;
import com.lms.learning.domain.Course;
import com.lms.learning.domain.Enrollment;
import com.lms.learning.repo.CourseRepository;
import com.lms.learning.repo.EnrollmentRepository;
import com.lms.learning.web.LearningDtos.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** My Courses: the learner's enrolled courses and progress. Enrolment is open for now; a later step can gate it on entitlements. */
@Service
public class LearningService {
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final CertificateService certificates;

    public LearningService(CourseRepository courses, EnrollmentRepository enrollments, CertificateService certificates) {
        this.courses = courses;
        this.enrollments = enrollments;
        this.certificates = certificates;
    }

    @Transactional(readOnly = true)
    public List<CourseView> catalogue(Long userId) {
        Set<String> mine = enrollments.findByUserIdOrderByEnrolledAtDesc(userId).stream()
                .map(Enrollment::getCourseRef).collect(Collectors.toSet());
        return courses.findByActiveTrueOrderByTitleAsc().stream()
                .map(c -> new CourseView(c.getCourseRef(), c.getTitle(), c.getDescription(), c.getCategory(), c.getInstructorName(),
                        c.getLessonCount(), c.getDurationMinutes(), mine.contains(c.getCourseRef()))).toList();
    }

    @Transactional(readOnly = true)
    public List<MyCourseView> mine(Long userId) {
        Map<String, Course> byRef = courses.findAll().stream().collect(Collectors.toMap(Course::getCourseRef, Function.identity()));
        return enrollments.findByUserIdOrderByEnrolledAtDesc(userId).stream()
                .filter(e -> byRef.containsKey(e.getCourseRef()))
                .map(e -> view(e, byRef.get(e.getCourseRef()))).toList();
    }

    @Transactional
    public MyCourseView enroll(Long userId, String courseRef) {
        Course c = courses.findByCourseRefAndActiveTrue(courseRef).orElseThrow(() -> ApiException.notFound("Course not found"));
        Enrollment e = enrollments.findByUserIdAndCourseRef(userId, courseRef).orElseGet(() -> {
            Enrollment n = new Enrollment();
            n.setUserId(userId);
            n.setCourseRef(courseRef);
            n.setEnrolledAt(Instant.now());
            return enrollments.save(n);
        });
        return view(e, c);
    }

    /** Called by the course player (a later step) as the learner moves through lessons. */
    @Transactional
    public MyCourseView updateProgress(Long userId, String courseRef, int percent) {
        Enrollment e = enrollments.findByUserIdAndCourseRef(userId, courseRef)
                .orElseThrow(() -> ApiException.notFound("You are not enrolled in this course"));
        Course c = courses.findByCourseRefAndActiveTrue(courseRef).orElseThrow(() -> ApiException.notFound("Course not found"));
        e.setProgressPercent(Math.max(e.getProgressPercent(), percent)); // progress never goes backwards
        e.setLastAccessedAt(Instant.now());
        if (e.getProgressPercent() >= 100 && !"COMPLETED".equals(e.getStatus())) {
            e.setStatus("COMPLETED");
            e.setCompletedAt(Instant.now());
        }
        if ("COMPLETED".equals(e.getStatus())) certificates.issueFor(userId, c);
        return view(e, c);
    }

    private static MyCourseView view(Enrollment e, Course c) {
        return new MyCourseView(c.getCourseRef(), c.getTitle(), c.getDescription(), c.getCategory(), c.getInstructorName(),
                c.getLessonCount(), c.getDurationMinutes(), e.getStatus(), e.getProgressPercent(),
                e.getEnrolledAt(), e.getLastAccessedAt(), e.getCompletedAt());
    }
}
