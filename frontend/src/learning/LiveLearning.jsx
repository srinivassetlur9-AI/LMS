import { useCallback, useEffect, useState } from 'react';
import { api, del, post } from '../api.js';
import { Alert, errorText } from '../components/AuthLayout.jsx';

export const when = (iso) => new Date(iso).toLocaleString(undefined, { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });

/** One live session card, shared by Live Learning and the Calendar agenda. */
export function SessionCard({ s, busy, onToggle }) {
  const d = new Date(s.startsAt);
  return (
    <div className="card live-card">
      <div className="date-badge">
        <b>{d.getDate()}</b>
        <span>{d.toLocaleString(undefined, { month: 'short' })}</span>
      </div>
      <div className="live-body">
        <h3>{s.title}</h3>
        {s.courseTitle && <span className={`chip ${s.inMyCourses ? 'green' : ''}`}>{s.inMyCourses ? 'Your course · ' : ''}{s.courseTitle}</span>}
        <div className="muted course-meta">{s.instructorName} · {when(s.startsAt)} · {s.durationMinutes} min</div>
        {s.description && <p className="muted course-desc">{s.description}</p>}
      </div>
      <div className="live-action">
        {s.ended ? <span className="chip">Ended</span> : (
          <>
            {s.registered && <span className="chip green">Registered</span>}
            {s.registered && s.joinUrl && <a className="btn small" href={s.joinUrl} target="_blank" rel="noreferrer">Join</a>}
            <button type="button" className={`btn small${s.registered ? ' ghost' : ''}`} disabled={busy === s.id} onClick={() => onToggle(s)}>
              {s.registered ? 'Cancel' : 'Register'}
            </button>
          </>
        )}
      </div>
    </div>
  );
}

/** Loads the sessions and handles register / cancel. Used by both pages. */
export function useSessions() {
  const [sessions, setSessions] = useState(null);
  const [busy, setBusy] = useState(null);
  const [error, setError] = useState(null);
  const load = useCallback(() => api('/learning/live-sessions').then(setSessions).catch(setError), []);
  useEffect(() => { load(); }, [load]);
  async function toggle(s) {
    setBusy(s.id); setError(null);
    try {
      const u = s.registered ? await del(`/learning/live-sessions/${s.id}/register`) : await post(`/learning/live-sessions/${s.id}/register`);
      setSessions((list) => list.map((x) => (x.id === u.id ? u : x)));
    } catch (e) { setError(e); } finally { setBusy(null); }
  }
  return { sessions, busy, error, toggle };
}

export default function LiveLearning() {
  const { sessions, busy, error, toggle } = useSessions();
  const [tab, setTab] = useState('UPCOMING');
  const shown = (sessions || []).filter((s) => (tab === 'PAST' ? s.ended : !s.ended && (tab === 'UPCOMING' || s.inMyCourses)));
  const forMine = (sessions || []).filter((s) => !s.ended && s.inMyCourses).length;
  const mine = (sessions || []).filter((s) => s.registered && !s.ended).length;

  return (
    <>
      <div className="page-head">
        <h1>Live Learning</h1>
        <p className="muted" style={{ margin: 0 }}>Join live classes, workshops and Q&amp;A sessions with instructors.</p>
      </div>
      <Alert>{error && errorText(error)}</Alert>
      <div className="tabs">
        <button type="button" className={tab === 'UPCOMING' ? 'active' : ''} onClick={() => setTab('UPCOMING')}>Upcoming{sessions ? ` (${(sessions || []).filter((s) => !s.ended).length})` : ''}</button>
        <button type="button" className={tab === 'MINE' ? 'active' : ''} onClick={() => setTab('MINE')}>For my courses{sessions ? ` (${forMine})` : ''}</button>
        <button type="button" className={tab === 'PAST' ? 'active' : ''} onClick={() => setTab('PAST')}>Past</button>
      </div>
      {tab === 'UPCOMING' && sessions && <p className="muted">You are registered for {mine} upcoming session{mine === 1 ? '' : 's'}.</p>}
      {sessions === null ? <p className="muted">Loading…</p> : shown.length === 0 ? (
        <div className="card"><p className="muted" style={{ margin: 0 }}>{tab === 'MINE' ? 'No upcoming sessions for your courses. Enroll in a course from My Courses to see its live sessions here.' : 'Nothing here yet.'}</p></div>
      ) : shown.map((s) => <SessionCard key={s.id} s={s} busy={busy} onToggle={toggle} />)}
    </>
  );
}
