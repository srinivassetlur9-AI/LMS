-- Module 3: My Learning (My Courses). Catalogue and per-learner enrolments with progress.
CREATE TABLE courses (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_ref       VARCHAR(60)  NOT NULL,
  title            VARCHAR(200) NOT NULL,
  description      VARCHAR(500) NULL,
  category         VARCHAR(60)  NOT NULL,
  instructor_name  VARCHAR(120) NOT NULL,
  lesson_count     INT          NOT NULL DEFAULT 0,
  duration_minutes INT          NOT NULL DEFAULT 0,
  active           TINYINT(1)   NOT NULL DEFAULT 1,
  created_at       DATETIME(6)  NOT NULL,
  updated_at       DATETIME(6)  NOT NULL,
  created_by       BIGINT NULL,
  CONSTRAINT uq_courses_ref UNIQUE (course_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE enrollments (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT       NOT NULL,
  course_ref       VARCHAR(60)  NOT NULL,
  status           VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',   -- IN_PROGRESS | COMPLETED
  progress_percent INT          NOT NULL DEFAULT 0,
  enrolled_at      DATETIME(6)  NOT NULL,
  last_accessed_at DATETIME(6)  NULL,
  completed_at     DATETIME(6)  NULL,
  created_at       DATETIME(6)  NOT NULL,
  updated_at       DATETIME(6)  NOT NULL,
  created_by       BIGINT NULL,
  CONSTRAINT uq_enroll UNIQUE (user_id, course_ref),
  CONSTRAINT fk_enroll_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_enroll_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO courses (course_ref, title, description, category, instructor_name, lesson_count, duration_minutes, created_at, updated_at) VALUES
 ('gen-ai-everyone',  'Generative AI for Everyone',    'What generative AI is, how it works and how to use it at work.',   'Artificial Intelligence', 'Dr. Ananya Rao', 24, 360, NOW(6), NOW(6)),
 ('machine-learning', 'Machine Learning',              'Supervised and unsupervised learning with hands-on projects.',      'Data Science',            'Priya Sharma',   32, 540, NOW(6), NOW(6)),
 ('cloud-computing',  'Cloud Computing',               'Cloud fundamentals, services, deployment and cost control.',        'Cloud Computing',         'Arjun Verma',    20, 300, NOW(6), NOW(6)),
 ('python-basics',    'Python for Beginners',          'Learn Python from scratch with small, practical exercises.',        'Web Development',         'Rajesh Kumar',   28, 420, NOW(6), NOW(6)),
 ('fullstack-web',    'Full Stack Web Development',    'Build and deploy modern web apps with React and Spring Boot.',      'Web Development',         'Kavya Reddy',    40, 720, NOW(6), NOW(6)),
 ('product-mgmt',     'Product Management Essentials', 'Discovery, roadmaps and delivery for new product managers.',        'Business & Management',   'Rahul Mehta',    18, 270, NOW(6), NOW(6));
