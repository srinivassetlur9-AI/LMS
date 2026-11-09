package com.lms.learning.service;

import com.lms.identity.domain.Role;
import com.lms.identity.domain.User;
import com.lms.identity.repo.UserRepository;
import com.lms.identity.security.AuthPrincipal;
import com.lms.identity.web.ApiException;
import com.lms.learning.domain.MentorQuestion;
import com.lms.learning.repo.CourseRepository;
import com.lms.learning.repo.MentorQuestionRepository;
import com.lms.learning.web.LearningDtos.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ask Mentor: learners post questions; instructors (and super admins) answer them from the mentor inbox. */
@Service
public class MentorService {
    private final MentorQuestionRepository questions;
    private final CourseRepository courses;
    private final UserRepository users;

    public MentorService(MentorQuestionRepository questions, CourseRepository courses, UserRepository users) {
        this.questions = questions;
        this.courses = courses;
        this.users = users;
    }

    @Transactional
    public MentorQuestionView ask(Long userId, AskRequest req) {
        String ref = req.courseRef() == null || req.courseRef().isBlank() ? null : req.courseRef();
        if (ref != null && courses.findByCourseRefAndActiveTrue(ref).isEmpty()) throw ApiException.notFound("Course not found");
        MentorQuestion q = new MentorQuestion();
        q.setUserId(userId);
        q.setCourseRef(ref);
        q.setSubject(req.subject().trim());
        q.setQuestion(req.question().trim());
        return view(questions.save(q), null);
    }

    @Transactional(readOnly = true)
    public List<MentorQuestionView> mine(Long userId) {
        return questions.findByUserIdOrderByCreatedAtDesc(userId).stream().map(q -> view(q, null)).toList();
    }

    @Transactional(readOnly = true)
    public List<MentorQuestionView> inbox(AuthPrincipal me) {
        requireMentor(me);
        List<MentorQuestion> open = questions.findByStatusOrderByCreatedAtAsc("OPEN");
        Map<Long, User> byId = users.findAllById(open.stream().map(MentorQuestion::getUserId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return open.stream().map(q -> {
            User u = byId.get(q.getUserId());
            return view(q, u == null ? "Learner" : (u.getFirstName() + " " + u.getLastName()).trim());
        }).toList();
    }

    @Transactional
    public MentorQuestionView answer(AuthPrincipal me, Long id, String answer) {
        requireMentor(me);
        MentorQuestion q = questions.findById(id).orElseThrow(() -> ApiException.notFound("Question not found"));
        if ("ANSWERED".equals(q.getStatus())) throw ApiException.badRequest("ALREADY_ANSWERED", "This question has already been answered");
        User mentor = users.findById(me.userId()).orElseThrow(() -> ApiException.notFound("User not found"));
        q.setAnswer(answer.trim());
        q.setStatus("ANSWERED");
        q.setAnsweredBy(me.userId());
        q.setAnsweredByName((mentor.getFirstName() + " " + mentor.getLastName()).trim());
        q.setAnsweredAt(Instant.now());
        return view(q, null);
    }

    private static void requireMentor(AuthPrincipal me) {
        if (!me.roles().contains(Role.INSTRUCTOR) && !me.roles().contains(Role.SUPER_ADMIN)) {
            throw ApiException.forbidden("FORBIDDEN", "Only mentors can answer questions");
        }
    }

    private static MentorQuestionView view(MentorQuestion q, String learnerName) {
        return new MentorQuestionView(q.getId(), q.getCourseRef(), q.getSubject(), q.getQuestion(), q.getStatus(), q.getAnswer(),
                q.getAnsweredByName(), q.getAnsweredAt(), q.getCreatedAt(), learnerName);
    }
}
