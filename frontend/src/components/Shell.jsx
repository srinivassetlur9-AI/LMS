import { useState } from 'react';
import logo from '../assets/uniaisys-logo.png';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

const ICONS = {
  dashboard: <><rect x="3" y="3" width="7" height="9" rx="1.5" /><rect x="14" y="3" width="7" height="5" rx="1.5" /><rect x="14" y="12" width="7" height="9" rx="1.5" /><rect x="3" y="16" width="7" height="5" rx="1.5" /></>,
  courses: <><path d="M3 8l9-4 9 4-9 4z" /><path d="M7 10.5V15c0 1.4 2.2 3 5 3s5-1.600 5-3v-4.500" /><path d="M21 8v6" /></>,
  certificates: <><circle cx="12" cy="9" r="5.5" /><path d="M9 14l-1.500 7 4.500-2.500 4.500 2.500L15 14" /></>,
  live: <><rect x="4" y="7" width="16" height="13" rx="2.500" /><path d="M9 3l3 4 3-4" /><path d="M10.500 11.500l3.500 2-3.500 2z" /></>,
  calendar: <><rect x="3.500" y="5" width="17" height="15.500" rx="2.500" /><path d="M8 3v4M16 3v4M3.500 10h17" /></>,
  mentor: <><path d="M20 12a8 8 0 01-11.600 7.100L4 20l1-4.200A8 8 0 1120 12z" /><path d="M9.500 9.500a2.500 2.500 0 114 2c-.9.600-1.500 1-1.500 2M12 16.500v.01" /></>,
  profile: <><circle cx="12" cy="8" r="4" /><path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" /></>,
  security: <><path d="M12 3l8 3v6c0 5-3.4 8-8 9-4.6-1-8-4-8-9V6z" /><path d="M9 12l2 2 4-4" /></>,
  users: <><circle cx="9" cy="8" r="3.5" /><path d="M2.5 20c0-3.5 2.9-6 6.5-6s6.5 2.5 6.5 6" /><path d="M16 4.5a3.5 3.5 0 010 7M18 14.3c2 .8 3.5 2.7 3.5 5.7" /></>,
  billing: <><rect x="3" y="5" width="18" height="14" rx="2.5" /><path d="M3 10h18M7 15h4" /></>,
  signout: <><path d="M9 4H6a2 2 0 00-2 2v12a2 2 0 002 2h3" /><path d="M16 8l4 4-4 4M20 12H9" /></>,
};
const Icon = ({ name }) => <svg className="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{ICONS[name]}</svg>;

export default function Shell({ children }) {
  const { user, logout, canAdminister } = useAuth();
  const nav = useNavigate();
  const [collapsed, setCollapsed] = useState(false);
  const initials = (user.firstName[0] + user.lastName[0]).toUpperCase();
  const showBilling = user.roles.some((r) => ['SUPER_ADMIN', 'FINANCE_ADMIN', 'SUPPORT_AGENT'].includes(r));
  const link = (to, icon, label) => <NavLink to={to} title={label}><Icon name={icon} /><span>{label}</span></NavLink>;

  return (
    <div className={`shell${collapsed ? ' collapsed' : ''}`}>
      <header className="topbar">
        <img className="topbar-logo" src={logo} alt="UNIAISYS" />
        <nav className="topbar-nav" aria-label="Primary">
          <NavLink to="/dashboard"><Icon name="dashboard" /><span>Dashboard</span></NavLink>
          <NavLink to="/profile"><Icon name="profile" /><span>My Profile</span></NavLink>
        </nav>
        <div className="topbar-user">
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontWeight: 600 }}>{user.firstName} {user.lastName}</div>
            <div className="muted" style={{ fontSize: '.8rem' }}>{user.roles.map(roleLabel).join(', ')}</div>
          </div>
          <div className="avatar">{initials}</div>
        </div>
      </header>
      <aside className="side">
        <div className="side-head">
          <h2 className="side-title">Quick Links</h2>
          <button type="button" className="side-toggle" aria-label="Toggle sidebar" aria-expanded={!collapsed} onClick={() => setCollapsed((c) => !c)}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true"><path d="M3 6h18M9 12h12M3 18h18" /></svg>
          </button>
        </div>
        <div className="side-section">My Learning</div>
        {link('/my-courses', 'courses', 'My Courses')}
        {link('/certificates', 'certificates', 'Certificates')}
        <hr className="side-divider" />
        <div className="side-section">Live Learning</div>
        {link('/live-learning', 'live', 'Live Sessions')}
        {link('/calendar', 'calendar', 'Calendar')}
        {link('/ask-mentor', 'mentor', 'Ask Mentor')}
        <hr className="side-divider" />
        <div className="side-section">Account</div>
        {link('/security', 'security', 'Security & Settings')}
        {(canAdminister || showBilling) && <><hr className="side-divider" /><div className="side-section">Administration</div></>}
        {canAdminister && link('/users', 'users', 'Users')}
        {showBilling && link('/billing', 'billing', 'Billing & Devices')}
        <div className="spacer" />
        <hr className="side-divider" />
        <div className="side-section">Support</div>
        <a href="#signout" title="Sign out" onClick={async (e) => { e.preventDefault(); await logout(); nav('/login'); }}><Icon name="signout" /><span>Sign out</span></a>
      </aside>
      <main className="main content">{children}</main>
      <footer className="footer">© {new Date().getFullYear()} UNIAISYS</footer>
    </div>
  );
}

export const roleLabel = (r) => r.split('_').map((w) => w[0] + w.slice(1).toLowerCase()).join(' ');
