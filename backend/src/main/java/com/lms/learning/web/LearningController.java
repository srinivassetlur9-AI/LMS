package com.lms.learning.web;

import com.lms.identity.security.AuthPrincipal;
import com.lms.learning.service.LearningService;
import com.lms.learning.web.LearningDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Module 3 - My Learning. Every call acts on the signed-in learner only. */
@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {
    private final LearningService learning;

    public LearningController(LearningService learning) { this.learning = learning; }

    @GetMapping("/courses")
    public List<CourseView> catalogue(@AuthenticationPrincipal AuthPrincipal me) { return learning.catalogue(me.userId()); }

    @GetMapping("/my-courses")
    public List<MyCourseView> mine(@AuthenticationPrincipal AuthPrincipal me) { return learning.mine(me.userId()); }

    @PostMapping("/courses/{ref}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public MyCourseView enroll(@AuthenticationPrincipal AuthPrincipal me, @PathVariable String ref) {
        return learning.enroll(me.userId(), ref);
    }

    @PutMapping("/my-courses/{ref}/progress")
    public MyCourseView progress(@AuthenticationPrincipal AuthPrincipal me, @PathVariable String ref,
                                 @Valid @RequestBody ProgressRequest req) {
        return learning.updateProgress(me.userId(), ref, req.progressPercent());
    }
}
