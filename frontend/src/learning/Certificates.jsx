import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { Alert, errorText } from '../components/AuthLayout.jsx';
import logo from '../assets/uniaisys-logo.png';

const longDate = (iso) => new Date(iso).toLocaleDateString(undefined, { day: 'numeric', month: 'long', year: 'numeric' });

function CertificateSheet({ c }) {
  return (
    <div id="certificate-print" className="cert-sheet">
      <img src={logo} alt="UNIAISYS" className="cert-logo" />
      <div className="cert-kicker">UNIAISYS · Learn · Grow · Achieve</div>
      <h2 className="cert-title">Certificate of Completion</h2>
      <p className="muted">This is to certify that</p>
      <div className="cert-name">{c.learnerName}</div>
      <p className="muted">has successfully completed the course</p>
      <div className="cert-course">{c.courseTitle}</div>
      <p className="muted">on {longDate(c.issuedAt)}</p>
      <div className="cert-foot">
        <div><div className="cert-sign">{c.instructorName}</div><div className="muted">Instructor</div></div>
        <div><div className="cert-sign">{c.certificateNo}</div><div className="muted">Certificate number</div></div>
      </div>
    </div>
  );
}

/** Certificates earned by completing courses. Issued automatically by the server at 100% progress. */
export default function Certificates() {
  const [list, setList] = useState(null);
  const [open, setOpen] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => { api('/learning/certificates').then(setList).catch(setError); }, []);

  return (
    <>
      <div className="page-head">
        <h1>Certificates</h1>
        <p className="muted" style={{ margin: 0 }}>Earned automatically when you complete a course.</p>
      </div>
      <Alert>{error && errorText(error)}</Alert>

      {list === null ? <p className="muted">Loading…</p> : list.length === 0 ? (
        <div className="card"><p className="muted" style={{ margin: 0 }}>No certificates yet. Complete a course from My Courses to earn one.</p></div>
      ) : (
        <div className="course-grid">
          {list.map((c) => (
            <div className="card course-card" key={c.id}>
              <div className="course-thumb tone-amber"><span aria-hidden="true">🏅</span></div>
              <h3>{c.courseTitle}</h3>
              <div className="muted course-meta">Issued {longDate(c.issuedAt)}</div>
              <div className="muted course-meta">No. {c.certificateNo}</div>
              <div className="course-foot"><span /><button type="button" className="btn small" onClick={() => setOpen(c)}>View certificate</button></div>
            </div>
          ))}
        </div>
      )}

      {open && (
        <div className="modal-bg" onClick={() => setOpen(null)}>
          <div className="modal cert-modal" onClick={(e) => e.stopPropagation()}>
            <CertificateSheet c={open} />
            <div className="toolbar no-print" style={{ justifyContent: 'flex-end', marginTop: '1rem', marginBottom: 0 }}>
              <button type="button" className="btn ghost" onClick={() => setOpen(null)}>Close</button>
              <button type="button" className="btn" onClick={() => window.print()}>Print / Save as PDF</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
