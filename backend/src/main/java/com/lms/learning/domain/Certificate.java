package com.lms.learning.domain;

import com.lms.identity.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Issued once, automatically, when a learner completes a course. Names are copied so the certificate never changes later. */
@Entity @Table(name = "certificates")
@Getter @Setter
public class Certificate extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "course_ref", nullable = false) private String courseRef;
    @Column(name = "course_title", nullable = false) private String courseTitle;
    @Column(name = "learner_name", nullable = false) private String learnerName;
    @Column(name = "instructor_name", nullable = false) private String instructorName;
    @Column(name = "certificate_no", nullable = false) private String certificateNo;
    @Column(name = "issued_at", nullable = false) private Instant issuedAt;
}
