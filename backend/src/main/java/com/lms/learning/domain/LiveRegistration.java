package com.lms.learning.domain;

import com.lms.identity.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "live_registrations")
@Getter @Setter
public class LiveRegistration extends BaseEntity {
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "session_id", nullable = false) private Long sessionId;
}
