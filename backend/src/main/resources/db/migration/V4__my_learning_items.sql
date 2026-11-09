-- Module 3 (My Learning): Certificates, Live Learning (and the Calendar built on it), Ask Mentor.
CREATE TABLE certificates (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT       NOT NULL,
  course_ref     VARCHAR(60)  NOT NULL,
  course_title   VARCHAR(200) NOT NULL,
  learner_name   VARCHAR(200) NOT NULL,
  instructor_name VARCHAR(120) NOT NULL,
  certificate_no VARCHAR(40)  NOT NULL,
  issued_at      DATETIME(6)  NOT NULL,
  created_at     DATETIME(6)  NOT NULL,
  updated_at     DATETIME(6)  NOT NULL,
  created_by     BIGINT NULL,
  CONSTRAINT uq_cert_user_course UNIQUE (user_id, course_ref),
  CONSTRAINT uq_cert_no UNIQUE (certificate_no),
  CONSTRAINT fk_cert_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE live_sessions (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  title            VARCHAR(200) NOT NULL,
  description      VARCHAR(500) NULL,
  instructor_name  VARCHAR(120) NOT NULL,
  starts_at        DATETIME(6)  NOT NULL,
  duration_minutes INT          NOT NULL DEFAULT 60,
  join_url         VARCHAR(500) NULL,
  active           TINYINT(1)   NOT NULL DEFAULT 1,
  created_at       DATETIME(6)  NOT NULL,
  updated_at       DATETIME(6)  NOT NULL,
  created_by       BIGINT NULL,
  INDEX idx_live_start (starts_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE live_registrations (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  session_id  BIGINT      NOT NULL,
  created_at  DATETIME(6) NOT NULL,
  updated_at  DATETIME(6) NOT NULL,
  created_by  BIGINT NULL,
  CONSTRAINT uq_live_reg UNIQUE (user_id, session_id),
  CONSTRAINT fk_live_reg_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_live_reg_session FOREIGN KEY (session_id) REFERENCES live_sessions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mentor_questions (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT        NOT NULL,
  course_ref        VARCHAR(60)   NULL,
  subject           VARCHAR(200)  NOT NULL,
  question          VARCHAR(2000) NOT NULL,
  status            VARCHAR(20)   NOT NULL DEFAULT 'OPEN',      -- OPEN | ANSWERED
  answer            VARCHAR(4000) NULL,
  answered_by       BIGINT        NULL,
  answered_by_name  VARCHAR(200)  NULL,
  answered_at       DATETIME(6)   NULL,
  created_at        DATETIME(6)   NOT NULL,
  updated_at        DATETIME(6)   NOT NULL,
  created_by        BIGINT NULL,
  CONSTRAINT fk_mq_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_mq_user (user_id),
  INDEX idx_mq_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample live sessions, dated relative to when this migration runs.
INSERT INTO live_sessions (title, description, instructor_name, starts_at, duration_minutes, created_at, updated_at) VALUES
 ('The Future of AI in Education',   'Where AI is taking teaching and learning, with a live Q&A.',   'Dr. Ananya Rao', TIMESTAMPADD(DAY, 2, NOW(6)),  90, NOW(6), NOW(6)),
 ('Machine Learning Office Hours',   'Bring your questions on models, data and projects.',           'Priya Sharma',   TIMESTAMPADD(DAY, 4, NOW(6)),  60, NOW(6), NOW(6)),
 ('Cloud Cost Control Workshop',     'A hands-on look at keeping cloud bills under control.',        'Arjun Verma',    TIMESTAMPADD(DAY, 7, NOW(6)), 120, NOW(6), NOW(6)),
 ('Build and Ship a Web App',        'Live build of a small React and Spring Boot application.',     'Kavya Reddy',    TIMESTAMPADD(DAY, 10, NOW(6)), 90, NOW(6), NOW(6)),
 ('Python Q&A for Beginners',        'Ask anything about getting started with Python.',              'Rajesh Kumar',   TIMESTAMPADD(DAY, -3, NOW(6)), 60, NOW(6), NOW(6));
