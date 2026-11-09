import { useAuth } from '../auth.jsx';
import { roleLabel } from '../components/Shell.jsx';

export default function Dashboard() {
  const { user } = useAuth();
  return (
    <>
      <div className="page-head">
        <h1>Welcome back, {user.firstName}!</h1>
        <p className="muted" style={{ margin: 0 }}>A little progress each day adds up to big results.</p>
      </div>

      <div className="grid3">
        <div className="card stat-card">
          <span className="stat-ico tone-green" aria-hidden="true">✅</span>
          <div><span className="muted">Account status</span><b>{user.status}</b></div>
        </div>
        <div className="card stat-card">
          <span className="stat-ico tone-blue" aria-hidden="true">🛡️</span>
          <div>
            <span className="muted">Verification</span>
            <div>
              <span className={`chip ${user.emailVerified ? 'green' : 'amber'}`}>Email {user.emailVerified ? 'verified' : 'pending'}</span>
              {user.mobile && <span className={`chip ${user.mobileVerified ? 'green' : 'amber'}`}>Mobile {user.mobileVerified ? 'verified' : 'pending'}</span>}
            </div>
          </div>
        </div>
        <div className="card stat-card">
          <span className="stat-ico tone-amber" aria-hidden="true">🕒</span>
          <div><span className="muted">Last sign-in</span><b style={{ fontSize: '.95rem' }}>{user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : 'First time here'}</b></div>
        </div>
      </div>

      <div className="card">
        <h2>Your access</h2>
        <p className="muted" style={{ marginTop: 0 }}>What you can do across the platform depends on your roles.</p>
        {user.roles.map((r) => <span key={r} className="chip">{roleLabel(r)}</span>)}
      </div>
    </>
  );
}
