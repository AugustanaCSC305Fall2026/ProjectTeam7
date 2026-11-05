import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./ManagerDashboard.css";

function ManagerDashboard() {
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [noJob, setNoJob] = useState(false);
  const [updatingApplications, setUpdatingApplications] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    async function loadJob() {
      try {
        const response = await api.get("/manager/job");
        setJob(response.data);
      } catch (err) {
        const status = err.response?.status;

        if (status === 404) {
          setNoJob(true);
        } else if (status === 401) {
          setError("Your session has expired. Please log in again.");
        } else if (status === 403) {
          setError("Your account is not authorized to access the manager area.");
        } else {
          setError("Unable to load your assigned job.");
        }
      } finally {
        setLoading(false);
      }
    }

    loadJob();
  }, []);

  const toggleApplications = async () => {
    const nextStatus = !job.acceptingApplications;
    setUpdatingApplications(true);
    setMessage("");

    try {
      await api.patch("/manager/job/applications", {
        acceptingApplications: nextStatus,
      });

      setJob((currentJob) => ({
        ...currentJob,
        acceptingApplications: nextStatus,
      }));

      setMessage(
        nextStatus
          ? "Applications are now open."
          : "Applications are now closed."
      );
    } catch (error) {
      console.error(error);
      setMessage("Could not update the application status.");
    } finally {
      setUpdatingApplications(false);
    }
  };

  return (
    <section className="manager-dashboard">
      <h2>Manager Dashboard</h2>
      <p className="manager-dashboard__intro">
        Manage employees and review work for your assigned job.
      </p>

      {loading && <p>Loading assigned job...</p>}
      {!loading && noJob && <p>No job is currently assigned to your account.</p>}
      {!loading && error && <p className="manager-dashboard__error" role="alert">{error}</p>}

      {!loading && !error && !noJob && job && (
        <article className="manager-job-card">
          <p className="manager-job-card__label">YOUR ASSIGNED JOB</p>
          <h3>{job.title}</h3>
          <p>{job.department}</p>
          {job.description && <p>{job.description}</p>}

          <p>
            Applications:{" "}
            <strong>
              {job.acceptingApplications ? "Open" : "Closed"}
            </strong>
          </p>

          <button
            type="button"
            onClick={toggleApplications}
            disabled={updatingApplications}
          >
            {updatingApplications
              ? "Updating..."
              : job.acceptingApplications
                ? "Close applications"
                : "Open applications"}
          </button>

          {message && <p role="status">{message}</p>}
        </article>
      )}

      <div className="manager-dashboard__actions">
        <Link to="/manager/employees">View Employees</Link>
        <Link to="/manager/hours">Review Hours</Link>
        <Link to="/manager/shifts">View Shifts</Link>
      </div>
    </section>
  );
}

export default ManagerDashboard;