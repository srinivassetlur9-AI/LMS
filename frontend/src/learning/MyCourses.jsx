import { useCallback, useEffect, useState } from 'react';
import { api, post } from '../api.js';
import { Alert, errorText } from '../components/AuthLayout.jsx';
import { when } from './LiveLearning.jsx';

const TONES = { 'Artificial Intelligence': 'tone-pink', 'Data Science': 'tone-blue', 'Cloud Computing': 'tone-violet', 'Web Development': 'tone-sky', 'Business & Management': 'tone-amber' };
const FILTERS = [['ALL', 'All'], ['IN_PROGRESS', 'In progress'], ['COMPLETED', 'Completed']];
const hours = (m) => (m >= 60 ? `${Math.round((m / 60) * 10) / 10} h` : `${m} min`);

function Thumb({ course }) {
  return <div className={`course-thumb ${TONES[course.category] || 'tone-blue'}`}><span>{course.title.slice(0, 2).toUpperCase()}</span></div>;
}

/** Module 3 - My Learning: the signed-in learner's courses with progress, plus a catalogue to enrol from. */
export default function MyCourses() {
  const [mine, setMine] = useState(null);
  const [catalogue, setCatalogue] = useState([]);
  const [live, setLive] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [busy, setBusy] = useState('');
  const [error, setError] = useState(null);

  const load = useCallback(() => {
    api('/learning/my-courses').then(setMine).catch(setError);
    api('/learning/courses').then(setCatalogue).catch(setError);
    api('/learning/live-sessions').then(setLive).catch(() => {});
  }, []);
  useEffect(load, [load]);

  async function enroll(ref) {
    setBusy(ref); setError(null);
    try { await post(`/learning/courses/${ref}/enroll`); load(); } catch (e) { setError(e); } finally { setBusy(''); }
  }

  const shown = (mine || []).filter((c) => filter === 'ALL' || c.status === filter);
  const available = catalogue.filter((c) => !c.enrolled);

  return (
    <>
      <div className="page-head">
        <h1>My Courses</h1>
        <p className="muted" style={{ margin: 0 }}>Pick up where you left off, or start something new.</p>
      </div>
      <Alert>{error && errorText(error)}</Alert>

      <div className="tabs">
        {FILTERS.map(([k, label]) => (
          <button key={k} type="button" className={filter === k ? 'active' : ''} onClick={() => setFilter(k)}>
            {label}{mine && k !== 'ALL' ? ` (${mine.filter((c) => c.status === k).length})` : ''}
          </button>
        ))}
      </div>

      {mine === null ? <p className="muted">Loading…</p> : shown.length === 0 ? (
        <div className="card"><p className="muted" style={{ margin: 0 }}>
          {mine.length === 0 ? 'You have not enrolled in any course yet. Pick one below to get started.' : 'No courses in this view.'}
        </p></div>
      ) : (
        <div className="course-grid">
          {shown.map((c) => (
            <div className="card course-card" key={c.courseRef}>
              <Thumb course={c} />
              <span className="chip">{c.category}</span>
              <h3>{c.title}</h3>
              <div className="muted course-meta">{c.instructorName} · {c.lessonCount} lessons · {hours(c.durationMinutes)}</div>
              <div className="progress" role="progressbar" aria-valuenow={c.progressPercent} aria-valuemin={0} aria-valuemax={100}>
                <div style={{ width: `${c.progressPercent}%` }} />
              </div>
              {(() => {
                const next = live.find((s) => s.courseRef === c.courseRef && !s.ended);
                return next ? <div className="next-live"><span aria-hidden="true">🔴</span> Next live: <b>{next.title}</b> · {when(next.startsAt)}</div> : null;
              })()}
              <div className="course-foot">
                <span className="muted">{c.status === 'COMPLETED' ? 'Completed' : `${c.progressPercent}% complete`}</span>
                <button type="button" className="btn small ghost" disabled title="The course player arrives in the next step">
                  {c.status === 'COMPLETED' ? 'Review' : c.progressPercent > 0 ? 'Resume' : 'Start'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {available.length > 0 && (
        <>
          <h2 style={{ marginTop: '1.5rem' }}>Explore courses</h2>
          <div className="course-grid">
            {available.map((c) => (
              <div className="card course-card" key={c.courseRef}>
                <Thumb course={c} />
                <span className="chip">{c.category}</span>
                <h3>{c.title}</h3>
                <p className="muted course-desc">{c.description}</p>
                <div className="muted course-meta">{c.instructorName} · {c.lessonCount} lessons · {hours(c.durationMinutes)}</div>
                <div className="course-foot">
                  <span />
                  <button type="button" className="btn small" disabled={busy === c.courseRef} onClick={() => enroll(c.courseRef)}>
                    {busy === c.courseRef ? 'Enrolling…' : 'Enroll'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </>
  );
}
