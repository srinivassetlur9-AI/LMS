-- Tie live sessions to courses so a learner sees the sessions for the courses they take.
ALTER TABLE live_sessions ADD COLUMN course_ref VARCHAR(60) NULL;

UPDATE live_sessions SET course_ref = 'gen-ai-everyone'  WHERE title = 'The Future of AI in Education';
UPDATE live_sessions SET course_ref = 'machine-learning' WHERE title = 'Machine Learning Office Hours';
UPDATE live_sessions SET course_ref = 'cloud-computing'  WHERE title = 'Cloud Cost Control Workshop';
UPDATE live_sessions SET course_ref = 'fullstack-web'    WHERE title = 'Build and Ship a Web App';
UPDATE live_sessions SET course_ref = 'python-basics'    WHERE title = 'Python Q&A for Beginners';

INSERT INTO live_sessions (title, description, instructor_name, starts_at, duration_minutes, course_ref, created_at, updated_at) VALUES
 ('Cloud Platforms Walkthrough',   'A guided tour of the major cloud platforms and when to pick each.',  'Arjun Verma',    TIMESTAMPADD(DAY, 1, NOW(6)),   60, 'cloud-computing',  NOW(6), NOW(6)),
 ('Prompt Engineering Live Lab',   'Practise writing prompts that get reliable results, with feedback.', 'Dr. Ananya Rao', TIMESTAMPADD(DAY, 3, NOW(6)),   75, 'gen-ai-everyone',  NOW(6), NOW(6)),
 ('Build a Classifier Together',   'Train and evaluate a simple classifier step by step.',               'Priya Sharma',   TIMESTAMPADD(DAY, 6, NOW(6)),   90, 'machine-learning', NOW(6), NOW(6)),
 ('Roadmapping Live',              'Turn goals into a roadmap, with templates you can reuse.',           'Rahul Mehta',    TIMESTAMPADD(DAY, 8, NOW(6)),   60, 'product-mgmt',     NOW(6), NOW(6));
