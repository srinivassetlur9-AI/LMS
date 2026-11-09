package com.lms.learning.service;

import com.lms.identity.web.ApiException;
import com.lms.learning.domain.LiveRegistration;
import com.lms.learning.domain.LiveSession;
import com.lms.learning.domain.Course;
import com.lms.learning.domain.Enrollment;
import com.lms.learning.repo.CourseRepository;
import com.lms.learning.repo.EnrollmentRepository;
import com.lms.learning.repo.LiveRegistrationRepository;
import com.lms.learning.repo.LiveSessionRepository;
import com.lms.learning.web.LearningDtos.LiveSessionView;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Live Learning. The Calendar page is a month view over the same sessions. */
@Service
public class LiveService {
    private final LiveSessionRepository sessions;
    private final LiveRegistrationRepository registrations;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public LiveService(LiveSessionRepository sessions, LiveRegistrationRepository registrations,
                       CourseRepository courses, EnrollmentRepository enrollments) {
        this.sessions = sessions;
        this.registrations = registrations;
        this.courses = courses;
        this.enrollments = enrollments;
    }

    @Transactional(readOnly = true)
    public List<LiveSessionView> list(Long userId) {
        Set<Long> mine = mine(userId);
        Ctx ctx = ctx(userId);
        return sessions.findByActiveTrueOrderByStartsAtAsc().stream().map(s -> view(s, mine.contains(s.getId()), ctx)).toList();
    }

    @Transactional
    public LiveSessionView register(Long userId, Long sessionId) {
        LiveSession s = find(sessionId);
        if (ended(s)) throw ApiException.badRequest("SESSION_ENDED", "This session has already ended");
        if (registrations.findByUserIdAndSessionId(userId, sessionId).isEmpty()) {
            LiveRegistration r = new LiveRegistration();
            r.setUserId(userId);
            r.setSessionId(sessionId);
            registrations.save(r);
        }
        return view(s, true, ctx(userId));
    }

    @Transactional
    public LiveSessionView unregister(Long userId, Long sessionId) {
        LiveSession s = find(sessionId);
        registrations.findByUserIdAndSessionId(userId, sessionId).ifPresent(registrations::delete);
        return view(s, false, ctx(userId));
    }

    private LiveSession find(Long id) {
        return sessions.findById(id).filter(LiveSession::isActive).orElseThrow(() -> ApiException.notFound("Session not found"));
    }

    private Set<Long> mine(Long userId) {
        return registrations.findByUserId(userId).stream().map(LiveRegistration::getSessionId).collect(Collectors.toSet());
    }

    private static boolean ended(LiveSession s) {
        return s.getStartsAt().plus(s.getDurationMinutes(), ChronoUnit.MINUTES).isBefore(Instant.now());
    }

    /** Course titles and the caller's enrolments, looked up once per request. */
    private record Ctx(Map<String, String> titles, Set<String> enrolled) {}

    private Ctx ctx(Long userId) {
        Map<String, String> titles = courses.findAll().stream().collect(Collectors.toMap(Course::getCourseRef, Course::getTitle));
        Set<String> enrolled = enrollments.findByUserIdOrderByEnrolledAtDesc(userId).stream()
                .map(Enrollment::getCourseRef).collect(Collectors.toSet());
        return new Ctx(titles, enrolled);
    }

    private static LiveSessionView view(LiveSession s, boolean registered, Ctx ctx) {
        String ref = s.getCourseRef();
        return new LiveSessionView(s.getId(), s.getTitle(), s.getDescription(), s.getInstructorName(), s.getStartsAt(),
                s.getDurationMinutes(), registered, ended(s), registered ? s.getJoinUrl() : null,
                ref, ref == null ? null : ctx.titles().get(ref), ref != null && ctx.enrolled().contains(ref));
    }
}
