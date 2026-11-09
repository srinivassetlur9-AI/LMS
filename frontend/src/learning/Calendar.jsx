import { useMemo, useState } from 'react';
import { Alert, errorText } from '../components/AuthLayout.jsx';
import { SessionCard, useSessions } from './LiveLearning.jsx';

const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const key = (d) => `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;

/** Month view of the live sessions. Sessions you registered for are filled; the rest are outlined. */
export default function Calendar() {
  const { sessions, busy, error, toggle } = useSessions();
  const today = new Date();
  const [month, setMonth] = useState(new Date(today.getFullYear(), today.getMonth(), 1));
  const [picked, setPicked] = useState(today);

  const byDay = useMemo(() => {
    const m = {};
    (sessions || []).forEach((s) => { (m[key(new Date(s.startsAt))] ||= []).push(s); });
    return m;
  }, [sessions]);

  const first = month.getDay();
  const days = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
  const cells = [...Array(first).fill(null), ...Array.from({ length: days }, (_, i) => new Date(month.getFullYear(), month.getMonth(), i + 1))];
  const shift = (n) => setMonth(new Date(month.getFullYear(), month.getMonth() + n, 1));
  const agenda = byDay[key(picked)] || [];

  return (
    <>
      <div className="page-head">
        <h1>Calendar</h1>
        <p className="muted" style={{ margin: 0 }}>Your live sessions at a glance. Filled dates are sessions you registered for.</p>
      </div>
      <Alert>{error && errorText(error)}</Alert>

      <div className="card">
        <div className="cal-head">
          <button type="button" className="btn small ghost" onClick={() => shift(-1)} aria-label="Previous month">‹</button>
          <h2 style={{ margin: 0 }}>{month.toLocaleString(undefined, { month: 'long', year: 'numeric' })}</h2>
          <button type="button" className="btn small ghost" onClick={() => shift(1)} aria-label="Next month">›</button>
        </div>
        <div className="cal-grid">
          {DAYS.map((d) => <div key={d} className="cal-dow">{d}</div>)}
          {cells.map((d, i) => {
            if (!d) return <div key={`e${i}`} />;
            const ev = byDay[key(d)] || [];
            const mine = ev.some((s) => s.registered);
            const cls = ['cal-day', key(d) === key(today) ? 'today' : '', key(d) === key(picked) ? 'picked' : '', ev.length ? (mine ? 'mine' : 'has') : ''].join(' ');
            return (
              <button type="button" key={key(d)} className={cls} onClick={() => setPicked(d)}>
                {d.getDate()}{ev.length > 0 && <span className="cal-dot" />}
              </button>
            );
          })}
        </div>
      </div>

      <h2>{picked.toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long' })}</h2>
      {sessions === null ? <p className="muted">Loading…</p> : agenda.length === 0 ? (
        <div className="card"><p className="muted" style={{ margin: 0 }}>No live sessions on this day.</p></div>
      ) : agenda.map((s) => <SessionCard key={s.id} s={s} busy={busy} onToggle={toggle} />)}
    </>
  );
}
