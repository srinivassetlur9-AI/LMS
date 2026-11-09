package com.lms.learning.repo;

import com.lms.learning.domain.Certificate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId);
    Optional<Certificate> findByIdAndUserId(Long id, Long userId);
    Optional<Certificate> findByUserIdAndCourseRef(Long userId, String courseRef);
    Optional<Certificate> findByCertificateNo(String certificateNo);
}
