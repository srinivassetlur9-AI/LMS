package com.lms.learning.domain;

import com.lms.identity.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "courses")
@Getter @Setter
public class Course extends BaseEntity {
    @Column(name = "course_ref", nullable = false) private String courseRef;
    @Column(nullable = false) private String title;
    private String description;
    @Column(nullable = false) private String category;
    @Column(name = "instructor_name", nullable = false) private String instructorName;
    @Column(name = "lesson_count", nullable = false) private int lessonCount;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(nullable = false) private boolean active = true;
}
