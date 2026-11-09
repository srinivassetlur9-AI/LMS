package com.lms.learning.domain;

import com.lms.identity.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "enrollments")
@Getter @Setter
public class Enrollment extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "course_ref", nullable = false) private String courseRef;
    @Column(nullable = false) private String status = "IN_PROGRESS";
    @Column(name = "progress_percent", nullable = false) private int progressPercent;
    @Column(name = "enrolled_at", nullable = false) private Instant enrolledAt;
    @Column(name = "last_accessed_at") private Instant lastAccessedAt;
    @Column(name = "completed_at") private Instant completedAt;
}
