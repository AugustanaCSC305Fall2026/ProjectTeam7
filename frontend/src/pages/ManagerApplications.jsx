import { useCallback, useEffect, useState } from "react";
import api from "../api/backendApi";
import "./ManagerApplications.css";

const decisionLabels = {
  APPROVED: "approved",
  REJECTED: "rejected",
  WAITLISTED: "added to the waitlist",
};

function ManagerApplications() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [updatingId, setUpdatingId] = useState("");

  const loadApplications = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/manager/applications");
      setApplications(response.data);
    } catch (err) {
      const status = err.response?.status;
      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to review applications.");
      } else if (status === 404) {
        setError("No job is currently assigned to your manager account.");
      } else {
        setError("Unable to load applications. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadApplications();
  }, [loadApplications]);

  const reviewApplication = async (application, status) => {
    setUpdatingId(application.applicationId);
    setError("");
    setMessage("");

    try {
      await api.patch(
        `/manager/applications/${application.applicationId}`,
        { status }
      );
      setMessage(
        `${application.applicantName}'s application was ${decisionLabels[status]}.`
      );
      await loadApplications();
    } catch (err) {
      const responseStatus = err.response?.status;
      if (responseStatus === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (responseStatus === 403) {
        setError("Your account is not authorized to review applications.");
      } else if (responseStatus === 404) {
        setError("That application was not found for your assigned job.");
      } else if (responseStatus === 409) {
        setError("That application has already been reviewed. Refresh the list.");
      } else {
        setError("Unable to update the application. Please try again.");
      }
    } finally {
      setUpdatingId("");
    }
  };

  return (
    <section className="manager-applications">
      <header className="manager-applications__heading">
        <h2>Applications</h2>
        <p>Review applicants for your assigned job.</p>
      </header>

      {message && (
        <p className="manager-applications__message" role="status">
          {message}
        </p>
      )}
      {error && (
        <p className="manager-applications__error" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p className="manager-applications__state" role="status">
          Loading applications...
        </p>
      ) : error ? (
        <button
          className="manager-applications__retry"
          type="button"
          onClick={loadApplications}
        >
          Retry
        </button>
      ) : applications.length === 0 ? (
        <p className="manager-applications__state">
          No applications have been submitted for your job.
        </p>
      ) : (
        <div className="manager-applications__list">
          {applications.map((application) => {
            const isUpdating = updatingId === application.applicationId;
            const isBusy = updatingId !== "";

            return (
              <article
                className="manager-application-card"
                key={application.applicationId}
              >
                <div className="manager-application-card__header">
                  <div>
                    <h3>{application.applicantName}</h3>
                    <a href={`mailto:${application.applicantEmail}`}>
                      {application.applicantEmail}
                    </a>
                  </div>
                  <span
                    className={`manager-application-card__status manager-application-card__status--${application.status.toLowerCase()}`}
                  >
                    {application.status}
                  </span>
                </div>

                <div className="manager-application-card__statement">
                  <h4>Application statement</h4>
                  <p>
                    {application.applicationStatement || "No statement provided."}
                  </p>
                </div>

                {application.status === "PENDING" && (
                  <div className="manager-application-card__actions">
                    <button
                      type="button"
                      onClick={() => reviewApplication(application, "APPROVED")}
                      disabled={isBusy}
                    >
                      {isUpdating ? "Updating..." : "Approve"}
                    </button>
                    <button
                      className="manager-application-card__waitlist"
                      type="button"
                      onClick={() => reviewApplication(application, "WAITLISTED")}
                      disabled={isBusy}
                    >
                      Waitlist
                    </button>
                    <button
                      className="manager-application-card__reject"
                      type="button"
                      onClick={() => reviewApplication(application, "REJECTED")}
                      disabled={isBusy}
                    >
                      Reject
                    </button>
                  </div>
                )}

                {application.status === "WAITLISTED" && (
                  <div className="manager-application-card__actions">
                    <button
                      type="button"
                      onClick={() => reviewApplication(application, "APPROVED")}
                      disabled={isBusy}
                    >
                      {isUpdating ? "Updating..." : "Approve"}
                    </button>
                    <button
                      className="manager-application-card__reject"
                      type="button"
                      onClick={() => reviewApplication(application, "REJECTED")}
                      disabled={isBusy}
                    >
                      Reject
                    </button>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

export default ManagerApplications;
