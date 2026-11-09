import { useCallback, useEffect, useState } from 'react';
import { api, post } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Alert, errorText } from '../components/AuthLayout.jsx';

const stamp = (iso) => new Date(iso).toLocaleString(undefined, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });

function Question({ q, children }) {
  return (
    <div className="card">
      <div className="q-head">
        <h3 style={{ margin: 0 }}>{q.subject}</h3>
        <span className={`chip ${q.status === 'ANSWERED' ? 'green' : 'amber'}`}>{q.status === 'ANSWERED' ? 'Answered' : 'Waiting for a mentor'}</span>
      </div>
      <div className="muted course-meta">{q.learnerName ? `${q.learnerName} · ` : ''}{stamp(q.createdAt)}{q.courseRef ? ` · ${q.courseRef}` : ''}</div>
      <p>{q.question}</p>
      {q.answer && (
        <div className="q-answer">
          <b>{q.answeredByName}</b> <span className="muted">· {stamp(q.answeredAt)}</span>
          <p style={{ margin: '.35rem 0 0' }}>{q.answer}</p>
        </div>
      )}
      {children}
    </div>
  );
}

function Inbox() {
  const [items, setItems] = useState(null);
  const [drafts, setDrafts] = useState({});
  const [error, setError] = useState(null);
  const load = useCallback(() => api('/learning/mentor/inbox').then(setItems).catch(setError), []);
  useEffect(() => { load(); }, [load]);

  async function answer(q) {
    setError(null);
    try { await post(`/learning/mentor/questions/${q.id}/answer`, { answer: drafts[q.id] || '' }); load(); } catch (e) { setError(e); }
  }

  return (
    <>
      <Alert>{error && errorText(error)}</Alert>
      {items === null ? <p className="muted">Loading…</p> : items.length === 0 ? (
        <div className="card"><p className="muted" style={{ margin: 0 }}>No open questions. Nice work.</p></div>
      ) : items.map((q) => (
        <Question key={q.id} q={q}>
          <textarea placeholder="Write your answer" value={drafts[q.id] || ''} onChange={(e) => setDrafts({ ...drafts, [q.id]: e.target.value })} />
          <button type="button" className="btn small" style={{ marginTop: '.6rem' }} disabled={!(drafts[q.id] || '').trim()} onClick={() => answer(q)}>Send answer</button>
        </Question>
      ))}
    </>
  );
}

/** Ask Mentor: learners post questions; instructors and super admins answer them from the Mentor inbox tab. */
export default function AskMentor() {
  const { user } = useAuth();
  const isMentor = user.roles.some((r) => r === 'INSTRUCTOR' || r === 'SUPER_ADMIN');
  const [tab, setTab] = useState('MINE');
  const [mine, setMine] = useState(null);
  const [courses, setCourses] = useState([]);
  const [form, setForm] = useState({ courseRef: '', subject: '', question: '' });
  const [sending, setSending] = useState(false);
  const [sent, setSent] = useState('');
  const [error, setError] = useState(null);

  const load = useCallback(() => {
    api('/learning/mentor/questions').then(setMine).catch(setError);
    api('/learning/courses').then(setCourses).catch(() => {});
  }, []);
  useEffect(load, [load]);

  async function submit(e) {
    e.preventDefault();
    setSending(true); setError(null); setSent('');
    try {
      await post('/learning/mentor/questions', { ...form, courseRef: form.courseRef || null });
      setForm({ courseRef: '', subject: '', question: '' });
      setSent('Your question was sent. A mentor will reply here.');
      load();
    } catch (err) { setError(err); } finally { setSending(false); }
  }

  return (
    <>
      <div className="page-head">
        <h1>Ask Mentor</h1>
        <p className="muted" style={{ margin: 0 }}>Stuck on something? Ask a mentor and get the answer here.</p>
      </div>
      {isMentor && (
        <div className="tabs">
          <button type="button" className={tab === 'MINE' ? 'active' : ''} onClick={() => setTab('MINE')}>My questions</button>
          <button type="button" className={tab === 'INBOX' ? 'active' : ''} onClick={() => setTab('INBOX')}>Mentor inbox</button>
        </div>
      )}

      {tab === 'INBOX' ? <Inbox /> : (
        <>
          <form className="card" onSubmit={submit}>
            <h2>New question</h2>
            <label htmlFor="mq-course">Course (optional)</label>
            <select id="mq-course" value={form.courseRef} onChange={(e) => setForm({ ...form, courseRef: e.target.value })}>
              <option value="">General question</option>
              {courses.map((c) => <option key={c.courseRef} value={c.courseRef}>{c.title}</option>)}
            </select>
            <label htmlFor="mq-subject">Subject</label>
            <input id="mq-subject" required maxLength={200} value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} />
            <label htmlFor="mq-question">Your question</label>
            <textarea id="mq-question" required maxLength={2000} value={form.question} onChange={(e) => setForm({ ...form, question: e.target.value })} />
            <Alert>{error && errorText(error)}</Alert>
            <Alert kind="ok">{sent}</Alert>
            <button className="btn" style={{ marginTop: '1rem' }} disabled={sending}>{sending ? 'Sending…' : 'Send question'}</button>
          </form>

          <h2>My questions</h2>
          {mine === null ? <p className="muted">Loading…</p> : mine.length === 0 ? (
            <div className="card"><p className="muted" style={{ margin: 0 }}>You have not asked anything yet.</p></div>
          ) : mine.map((q) => <Question key={q.id} q={q} />)}
        </>
      )}
    </>
  );
}
