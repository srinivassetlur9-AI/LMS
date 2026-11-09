---
name: learning-management-system
description: Build and extend Project LMS, an integrated enterprise Learning Management System delivered module by module. Use for any LMS work - identity and user management (registration, login, JWT/OAuth, RBAC, profiles), My Learning - including screens, architecture, database schema, APIs and code.
---

# Learning Management System (LMS)

Version 1.3.2 · Author: Srinivas R Setlur

## Purpose
Project LMS is one integrated enterprise LMS. Every module integrates through common APIs,
shared data contracts, a single authentication service, entitlements and end-to-end workflows.
The LMS is built incrementally: one module at a time, each one building on the contracts of
the modules before it.

## Module index

| # | Module | Status | Build it now? |
|---|--------|--------|---------------|
| 1 | Identity & User Management | In development | Yes |
| 3 | My Learning | In development (items added one at a time) | Yes - only the items listed below |

Rules for planned modules:
- Do not implement a module whose status is Planned.
- Do leave clean integration points for it (see "Contracts Module 1 must expose").
- When a module is started, its section below is filled in and its status changes here.

## Shared conventions (all modules)
- Tech stack: React JS (frontend), Java Spring Boot (backend), MySQL (database), Docker (deployment).
- One auth service: every module validates tokens issued by Module 1; no module stores passwords.
- API style: REST, versioned under /api/v1/<module>/..., JSON, consistent error format.
- Every table carries id, created_at, updated_at, created_by; soft delete where records are audited.
- UI: follow the look and feel in "assets/LMS Look & Feel.png" (kept inside this skill folder).

## Workflow
1. Identify which module the request belongs to, using the module index.
2. If the module is Planned, say so and offer to add only the integration hooks.
3. Read the existing code and schema first; extend it rather than regenerating it.
4. Deliver the outputs listed under Output format for that module only.
5. Check the module's acceptance criteria before calling it done.

## Module 1 - Identity & User Management
### Scope
1. User registration for learners, instructors and organizations.
2. Email/mobile verification, account activation, forgot password and password reset.
3. Login, logout, JWT/OAuth authentication, refresh tokens and session management.
4. Role-Based Access Control for: Learner, Instructor, Organization Admin, Super Admin,
   Content Admin, Exam Admin, Event Admin, Finance Admin, Evaluator, Support Agent.
5. User profile, account settings, language preference and security settings.
6. Authentication and authorization APIs used by all other modules.
7. Screens follow the design in "assets/LMS Look & Feel.png".
   - Page background: a very pale blue-white wash (only a hint of blue; the blue must not be strong), not flat grey.
     A teacher-and-student illustration sits behind the content, bottom-right, on the app pages, the landing page and the auth pages (login, register, forgot/reset password); on the auth pages it is centred in
     the right-hand panel, directly under the form card, rather than pushed to the far right edge
     (frontend/src/assets/teacher-student.svg). It is soft in colour so text remains easy to read.
   - The illustration must be visible, not hidden behind solid white blocks: cards, the left menu and the top bar are
     semi-transparent (about 45-75% white) so the picture shows through, and there are no large empty plain areas that
     cover it. Modals and form inputs stay solid white. Text must stay easy to read.
   - Zoom: the whole app renders at 80% zoom so more fits on screen and the background picture is visible. Done by setting
     the root font size to 80% (--ui-scale in frontend/src/styles.css) with all sizes in rem, not px (breakpoints and
     1px borders excepted). To change the zoom, change only --ui-scale.
   - Font: Inter (self-hosted through the @fontsource/inter package, no external font request), weights 400-700.
   - Buttons: small and calm, not bright. Soft blue (#4a6cc4, hover #3c5aa8), compact padding (about .45rem x .9rem),
     font size .88rem, weight 500, 8px radius. The same soft blue is used for links and the active menu item.
   - Left menu: compact. Small padding and gaps between items, a narrow column (about 208px), and "Sign out" directly
     under the last menu item, with no large blank space between items or above the Sign out link.
8. Built with React JS, Java Spring Boot, MySQL and Docker.

### Contracts Module 1 must expose (used by later modules)
- Token validation endpoint / middleware that returns user id, organization id and roles.
- Permission check: can(user, action, resource).
- Entitlements interface: an empty, documented hook that later modules will populate
  (e.g. which courses a user has access to).
- Device/session list per user, so later modules can enforce device limits.

### Security requirements
- Passwords hashed with Argon2 or bcrypt; never logged or returned.
- Short-lived access tokens, rotating refresh tokens, revocation on logout and password change.
- Rate limiting and lockout on login, OTP and password-reset endpoints.
- Verification and reset links/OTPs are single use and expire.
- Follow OWASP ASVS for authentication and session management.

### Acceptance criteria
- Each of the 10 roles can log in and sees only what its permissions allow.
- Registration -> verification -> activation -> login -> reset password works end to end.
- Another module can protect an endpoint using only the exposed contracts.

## Module 3 - My Learning
Built one item at a time. Each item adds a left-sidebar entry (with an icon), its own page and route, API endpoints
under /api/v1/learning, and a migration. Only items marked Done exist; do not build the others until asked.

Sidebar groups: **My Learning** holds My Courses and Certificates; **Live Learning** holds Live Sessions, Calendar
and Ask Mentor (the things that happen with instructors and in real time).

| Item | Sidebar group | Sidebar entry | Status |
|------|---------------|---------------|--------|
| My Courses | My Learning | My Courses | Done |
| Certificates | My Learning | Certificates | Done |
| Live Learning (sessions) | Live Learning | Live Sessions | Done |
| Calendar | Live Learning | Calendar | Done |
| Ask Mentor | Live Learning | Ask Mentor | Done |

### My Courses (done)
- Page /my-courses: the learner's enrolled courses as cards (thumbnail, category, title, instructor, lessons, duration,
  progress bar) with All / In progress / Completed tabs, and an "Explore courses" list with an Enroll button.
- Resume/Start/Review buttons are disabled until the course player exists.
- Tables (migration V3__my_learning.sql): courses (catalogue, seeded with 6), enrollments (user, course, status
  IN_PROGRESS|COMPLETED, progress_percent, last_accessed_at, completed_at; one row per user and course).
- API: GET /learning/courses, GET /learning/my-courses, POST /learning/courses/{ref}/enroll (idempotent),
  PUT /learning/my-courses/{ref}/progress (0-100, never goes backwards, 100 completes the course).
- Enrolment is open to any signed-in user for now. Hook for later: gate it on the entitlement check.
- Backend code in com.lms.learning, frontend in frontend/src/learning, test LearningFlowTest.

### Certificates (done)
- Page /certificates: a card per certificate; "View certificate" opens a printable certificate (UNIAISYS logo, learner,
  course, date, instructor, certificate number) with Print / Save as PDF.
- Issued automatically, once per learner and course, when course progress reaches 100%. Names are copied into the
  certificate so it never changes later. Number format UNI-<year>-<6 digits>, unique.
- Table certificates (migration V4). API: GET /learning/certificates, GET /learning/certificates/{id} (own only).

### Live Learning (done)
- Page /live-learning: Upcoming and Past tabs; each session shows date badge, title, instructor, time, duration, and
  Register / Cancel. The join link is returned only to registered learners (joinUrl is empty for the sample data).
- Sessions belong to a course (live_sessions.course_ref, migration V5). Each session shows its course; sessions for
  courses the learner is enrolled in are marked "Your course" and listed under a "For my courses" tab, and My Courses
  cards show the next live session for that course. Sample data: 9 sessions across the 6 courses.
- Tables live_sessions (dated relative to the migration run) and live_registrations (migrations V4, V5).
- API: GET /learning/live-sessions, POST and DELETE /learning/live-sessions/{id}/register (idempotent; ended sessions
  cannot be registered).

### Calendar (done)
- Page /calendar: month view built on the live sessions. Dates with sessions get a dot, dates with a session the
  learner registered for are filled blue; picking a date lists that day's sessions with Register / Cancel.
- No tables or endpoints of its own; it reads GET /learning/live-sessions.

### Ask Mentor (done)
- Page /ask-mentor: a form (optional course, subject, question) and the learner's questions with status Open or
  Answered and the mentor's answer.
- Mentors (Instructor or Super Admin) get a "Mentor inbox" tab listing open questions from all learners, with an
  answer box. A question can be answered once.
- Table mentor_questions (migration V4). API: POST and GET /learning/mentor/questions, GET /learning/mentor/inbox and
  POST /learning/mentor/questions/{id}/answer (mentor roles only, 403 otherwise).

## Output format (per module)
- Frontend screens
- Module architecture and how it fits the end-to-end system architecture
- Database schema (new and changed tables)
- API endpoints
- UI architecture
- Production-ready code
- Summary of what was delivered, plus optional follow-up improvements

## Change log
- 1.0.0 - Module 1 defined; Modules 2 and 3 planned.
- 1.1.0 - Student Subscription and Payment module removed from this skill.
- 1.1.1 - Module 3 renamed from "Course Management, Video & Offline Learning" to "My Learning".
- 1.2.0 - My Courses added (first My Learning item).
- 1.3.0 - Certificates, Live Learning, Calendar and Ask Mentor added; sidebar gets a My Learning group.
- 1.3.1 - Sidebar split into My Learning and Live Learning groups.
- 1.3.2 - Live sessions linked to courses.
