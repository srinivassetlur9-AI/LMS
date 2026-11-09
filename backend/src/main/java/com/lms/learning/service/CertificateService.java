package com.lms.learning.service;

import com.lms.identity.domain.User;
import com.lms.identity.repo.UserRepository;
import com.lms.identity.web.ApiException;
import com.lms.learning.domain.Certificate;
import com.lms.learning.domain.Course;
import com.lms.learning.repo.CertificateRepository;
import com.lms.learning.web.LearningDtos.CertificateView;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Certificates of completion. One per learner and course, issued by LearningService when a course reaches 100%. */
@Service
public class CertificateService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final CertificateRepository certificates;
    private final UserRepository users;

    public CertificateService(CertificateRepository certificates, UserRepository users) {
        this.certificates = certificates;
        this.users = users;
    }

    /** Idempotent: a learner who completes a course twice still has one certificate. */
    @Transactional
    public void issueFor(Long userId, Course course) {
        if (certificates.findByUserIdAndCourseRef(userId, course.getCourseRef()).isPresent()) return;
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        Certificate c = new Certificate();
        c.setUserId(userId);
        c.setCourseRef(course.getCourseRef());
        c.setCourseTitle(course.getTitle());
        c.setLearnerName((u.getFirstName() + " " + u.getLastName()).trim());
        c.setInstructorName(course.getInstructorName());
        c.setIssuedAt(Instant.now());
        c.setCertificateNo(newNumber(c.getIssuedAt()));
        certificates.save(c);
    }

    @Transactional(readOnly = true)
    public List<CertificateView> mine(Long userId) {
        return certificates.findByUserIdOrderByIssuedAtDesc(userId).stream().map(CertificateService::view).toList();
    }

    @Transactional(readOnly = true)
    public CertificateView get(Long userId, Long id) {
        return certificates.findByIdAndUserId(id, userId).map(CertificateService::view)
                .orElseThrow(() -> ApiException.notFound("Certificate not found"));
    }

    private String newNumber(Instant at) {
        String no;
        do {
            no = "UNI-" + at.atZone(ZoneOffset.UTC).getYear() + "-" + String.format("%06d", RANDOM.nextInt(1_000_000));
        } while (certificates.findByCertificateNo(no).isPresent());
        return no;
    }

    private static CertificateView view(Certificate c) {
        return new CertificateView(c.getId(), c.getCertificateNo(), c.getCourseRef(), c.getCourseTitle(),
                c.getLearnerName(), c.getInstructorName(), c.getIssuedAt());
    }
}
