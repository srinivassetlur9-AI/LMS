package com.lms.learning.domain;

import com.lms.identity.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "mentor_questions")
@Getter @Setter
public class MentorQuestion extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "course_ref") private String courseRef;
    @Column(nullable = false) private String subject;
    @Column(nullable = false) private String question;
    @Column(nullable = false) private String status = "OPEN";
    private String answer;
    @Column(name = "answered_by") private Long answeredBy;
    @Column(name = "answered_by_name") private String answeredByName;
    @Column(name = "answered_at") private Instant answeredAt;
}
