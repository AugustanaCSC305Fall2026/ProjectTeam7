import { Link } from "react-router-dom";
import "./SupervisorDashboard.css";

function SupervisorDashboard() {
  return (
    <section className="supervisor-dashboard">
      <h2>Supervisor Dashboard</h2>
      <p className="supervisor-dashboard__intro">
        Manage shifts and review employee correction requests.
      </p>

      <div className="supervisor-dashboard__actions">
        <Link
          className="supervisor-dashboard__card"
          to="/supervisor/schedule"
        >
          <span className="supervisor-dashboard__card-title">
            Create a shift
          </span>
          <span className="supervisor-dashboard__card-description">
            Schedule work for an existing job.
          </span>
        </Link>

        <Link
          className="supervisor-dashboard__card"
          to="/supervisor/corrections"
        >
          <span className="supervisor-dashboard__card-title">
            Review correction requests
          </span>
          <span className="supervisor-dashboard__card-description">
            View and respond to pending time corrections.
          </span>
        </Link>
      </div>
    </section>
  );
}

export default SupervisorDashboard;