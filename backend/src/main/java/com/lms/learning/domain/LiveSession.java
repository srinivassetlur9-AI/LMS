package com.lms.learning.domain;

import com.lms.identity.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "live_sessions")
@Getter @Setter
public class LiveSession extends BaseEntity {
    @Column(nullable = false) private String title;
    private String description;
    @Column(name = "instructor_name", nullable = false) private String instructorName;
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes = 60;
    @Column(name = "join_url") private String joinUrl;
    @Column(name = "course_ref") private String courseRef;
    @Column(nullable = false) private boolean active = true;
}
