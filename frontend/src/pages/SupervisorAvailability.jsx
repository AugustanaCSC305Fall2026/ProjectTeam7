import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./SupervisorAvailability.css";

function SupervisorAvailability() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadAvailability() {
      try {
        const response = await api.get("/availability");
        setEmployees(response.data);
      } catch (err) {
        const status = err.response?.status;
        const defaultMessage = status === 401
          ? "Your session has expired. Please sign in again."
          : status === 403
            ? "You do not have permission to view employee availability."
            : "Unable to load employee availability.";
        setError(err.response?.data?.message || defaultMessage);
      } finally {
        setLoading(false);
      }
    }

    loadAvailability();
  }, []);

  return (
    <section className="availability-page">
      <header className="availability-header">
        <h2>Employee Availability</h2>
        <p>View availability submitted by employees.</p>
      </header>

      {loading && <div className="availability-empty">Loading availability…</div>}

      {!loading && error && (
        <div className="availability-message availability-error">{error}</div>
      )}

      {!loading && !error && employees.length === 0 && (
        <div className="availability-empty">
          No employee availability has been submitted yet.
        </div>
      )}

      {!loading && !error && employees.length > 0 && (
        <div className="availability-list">
          {employees.map((employee) => (
            <article className="availability-card" key={employee.userId}>
              <h3>{employee.employeeName}</h3>

              {employee.windows?.length ? (
                <ul>
                  {employee.windows.map((window, index) => (
                    <li key={`${window.dayOfWeek}-${window.startTime}-${index}`}>
                      <strong>{window.dayOfWeek}</strong>
                      {" · "}
                      {window.startTime}–{window.endTime}
                    </li>
                  ))}
                </ul>
              ) : (
                <p>No availability windows submitted.</p>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default SupervisorAvailability;
