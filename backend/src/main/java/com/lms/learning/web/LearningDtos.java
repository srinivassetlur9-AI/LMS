package com.lms.learning.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class LearningDtos {
    private LearningDtos() {}

    // ---- My Courses ----
    /** One catalogue entry; enrolled tells the UI whether to offer Enroll. */
    public record CourseView(String courseRef, String title, String description, String category, String instructorName,
                             int lessonCount, int durationMinutes, boolean enrolled) {}
    /** One of the caller's courses with their progress. */
    public record MyCourseView(String courseRef, String title, String description, String category, String instructorName,
                               int lessonCount, int durationMinutes, String status, int progressPercent,
                               Instant enrolledAt, Instant lastAccessedAt, Instant completedAt) {}
    public record ProgressRequest(@Min(0) @Max(100) int progressPercent) {}

    // ---- Certificates ----
    public record CertificateView(Long id, String certificateNo, String courseRef, String courseTitle, String learnerName,
                                  String instructorName, Instant issuedAt) {}

    // ---- Live Learning / Calendar ----
    /** joinUrl is only filled in for sessions the caller registered for. */
    public record LiveSessionView(Long id, String title, String description, String instructorName, Instant startsAt,
                                  int durationMinutes, boolean registered, boolean ended, String joinUrl,
                                  String courseRef, String courseTitle, boolean inMyCourses) {}

    // ---- Ask Mentor ----
    public record AskRequest(@Size(max = 60) String courseRef, @NotBlank @Size(max = 200) String subject,
                             @NotBlank @Size(max = 2000) String question) {}
    public record AnswerRequest(@NotBlank @Size(max = 4000) String answer) {}
    public record MentorQuestionView(Long id, String courseRef, String subject, String question, String status,
                                     String answer, String answeredByName, Instant answeredAt, Instant createdAt,
                                     String learnerName) {}
}
