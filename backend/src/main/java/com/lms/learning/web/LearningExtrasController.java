package com.lms.learning.web;

import com.lms.identity.security.AuthPrincipal;
import com.lms.learning.service.CertificateService;
import com.lms.learning.service.LiveService;
import com.lms.learning.service.MentorService;
import com.lms.learning.web.LearningDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Module 3 - My Learning: Certificates, Live Learning (and Calendar) and Ask Mentor. Every call acts on the signed-in user. */
@RestController
@RequestMapping("/api/v1/learning")
public class LearningExtrasController {
    private final CertificateService certificates;
    private final LiveService live;
    private final MentorService mentor;

    public LearningExtrasController(CertificateService certificates, LiveService live, MentorService mentor) {
        this.certificates = certificates;
        this.live = live;
        this.mentor = mentor;
    }

    // ---- Certificates ----
    @GetMapping("/certificates")
    public List<CertificateView> certificates(@AuthenticationPrincipal AuthPrincipal me) { return certificates.mine(me.userId()); }

    @GetMapping("/certificates/{id}")
    public CertificateView certificate(@AuthenticationPrincipal AuthPrincipal me, @PathVariable Long id) {
        return certificates.get(me.userId(), id);
    }

    // ---- Live Learning / Calendar ----
    @GetMapping("/live-sessions")
    public List<LiveSessionView> liveSessions(@AuthenticationPrincipal AuthPrincipal me) { return live.list(me.userId()); }

    @PostMapping("/live-sessions/{id}/register")
    public LiveSessionView register(@AuthenticationPrincipal AuthPrincipal me, @PathVariable Long id) {
        return live.register(me.userId(), id);
    }

    @DeleteMapping("/live-sessions/{id}/register")
    public LiveSessionView unregister(@AuthenticationPrincipal AuthPrincipal me, @PathVariable Long id) {
        return live.unregister(me.userId(), id);
    }

    // ---- Ask Mentor ----
    @PostMapping("/mentor/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public MentorQuestionView ask(@AuthenticationPrincipal AuthPrincipal me, @Valid @RequestBody AskRequest req) {
        return mentor.ask(me.userId(), req);
    }

    @GetMapping("/mentor/questions")
    public List<MentorQuestionView> myQuestions(@AuthenticationPrincipal AuthPrincipal me) { return mentor.mine(me.userId()); }

    /** Mentors only (Instructor or Super Admin): unanswered questions from all learners. */
    @GetMapping("/mentor/inbox")
    public List<MentorQuestionView> inbox(@AuthenticationPrincipal AuthPrincipal me) { return mentor.inbox(me); }

    @PostMapping("/mentor/questions/{id}/answer")
    public MentorQuestionView answer(@AuthenticationPrincipal AuthPrincipal me, @PathVariable Long id,
                                     @Valid @RequestBody AnswerRequest req) {
        return mentor.answer(me, id, req.answer());
    }
}
