import { NavLink, useNavigate } from "react-router-dom";
import "./StudentLayout.css";
import logo from "../assets/augustana-logo.png";

function StudentLayout({ title, subtitle, children }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
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

        <p className="sidebar-label">MAIN</p>

        <nav className="student-nav">
          <NavLink to="/">Dashboard</NavLink>
          <NavLink to="/hours">Hours</NavLink>
          <NavLink to="/calendar">Calendar</NavLink>
          <NavLink to="/shifts">Shift Management</NavLink>
        </nav>

        <p className="sidebar-label coming-soon-label">COMING SOON</p>

        <div className="student-nav future-nav">
          <span>
            Jobs <small>SOON</small>
          </span>
          <span>
            Messages <small>SOON</small>
          </span>
        </div>

        <div className="sidebar-bottom">
          <NavLink to="/profile">Profile & Settings</NavLink>

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
            <div className="user-avatar">U</div>
            <span>Student</span>
          </div>
        </header>

        <div className="student-content">{children}</div>
      </main>
    </div>
  );
}

export default StudentLayout;