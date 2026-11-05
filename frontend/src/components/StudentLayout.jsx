import { NavLink, useNavigate } from "react-router-dom";
import "./StudentLayout.css";
import logo from "../assets/augustana-logo.png";

const studentNavItems = [
  { to: "/", label: "Dashboard" },
  { to: "/messages", label: "Messages" },
  { to: "/hours", label: "Hours" },
  { to: "/calendar", label: "Calendar" },
  { to: "/availability", label: "Availability" },
  { to: "/shifts", label: "Shift Management" },
];

function StudentLayout({
  children,
  title,
  subtitle,
  sectionLabel = "MAIN",
  userLabel,
  navItems = studentNavItems,
  showComingSoon = true,
  showProfileLink = true,
}) {
  const navigate = useNavigate();
  let storedUser = null;

  try {
    storedUser = JSON.parse(localStorage.getItem("user") || "null");
  } catch {
    storedUser = null;
  }

  const displayName = userLabel || [
    storedUser?.firstName,
    storedUser?.lastName,
  ].filter(Boolean).join(" ") || storedUser?.name || "Student";

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login", { replace: true });
  };

  return (
    <div className="student-layout">
      <aside className="student-sidebar">
        <div className="student-brand">
          <img src={logo} alt="Augustana College" />
          <div>
            <h2>cLockedIn</h2>
            <p>Campus work</p>
          </div>
        </div>

        <p className="sidebar-label">{sectionLabel}</p>

        <nav className="student-nav">
          {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end}>
          {item.label}
          </NavLink>))}
        </nav>

                {showComingSoon && (
          <>
            <p className="sidebar-label coming-soon-label">COMING SOON</p>

            <div className="student-nav future-nav">
              <span>
                Jobs <small>SOON</small>
              </span>
            </div>
          </>
        )}

        <div className="sidebar-bottom">
          {showProfileLink && (
  <NavLink to="/profile">Profile & Settings</NavLink>
)}

          <button className="logout-button" onClick={handleLogout}>
            Logout
          </button>

          <div className="semester-info">
            <strong>Fall 2026</strong>
          </div>
        </div>
      </aside>

      <main className="student-main">
        <header className="student-header">
          <div>
            <h1>{title}</h1>
            {subtitle && <p>{subtitle}</p>}
          </div>

          <div className="student-user">
            <div className="user-avatar">{displayName.charAt(0)}</div>
            <span>{displayName}</span>
          </div>
        </header>

        <div className="student-content">{children}</div>
      </main>
    </div>
  );
}

export default StudentLayout;
