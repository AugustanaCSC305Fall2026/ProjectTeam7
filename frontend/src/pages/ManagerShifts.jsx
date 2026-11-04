import { useState } from "react";
import api from "../api/backendApi";
import "./ManagerShifts.css";

function localDateValue(date) {
  const localDate = new Date(
    date.getTime() - date.getTimezoneOffset() * 60000
  );
  return localDate.toISOString().slice(0, 10);
}

function formatDateTime(value) {
  return new Date(value).toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function ManagerShifts() {
  const today = new Date();
  const weekEnd = new Date(today);
  weekEnd.setDate(weekEnd.getDate() + 6);

  const [from, setFrom] = useState(localDateValue(today));
  const [to, setTo] = useState(localDateValue(weekEnd));
  const [shifts, setShifts] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [error, setError] = useState("");

  async function loadShifts(event) {
    event.preventDefault();
    setError("");

    const startAt = new Date(`${from}T00:00:00`);
    const endAt = new Date(`${to}T00:00:00`);

    if (
      !from ||
      !to ||
      Number.isNaN(startAt.getTime()) ||
      Number.isNaN(endAt.getTime()) ||
      endAt < startAt
    ) {
      setError("Choose a valid date range.");
      return;
    }

    // The backend treats "to" as exclusive, so include the selected end date.
    endAt.setDate(endAt.getDate() + 1);

    setLoading(true);
    try {
      const [shiftResponse, employeeResponse] = await Promise.all([
        api.get("/manager/shifts", {
          params: {
            from: startAt.toISOString(),
            to: endAt.toISOString(),
          },
        }),
        api.get("/manager/employees"),
      ]);

      setShifts(shiftResponse.data);
      setEmployees(employeeResponse.data);
      setHasLoaded(true);
    } catch (err) {
      const status = err.response?.status;
      setError(
        status === 401
          ? "Your session has expired. Please log in again."
          : status === 403
            ? "Your account is not authorized to view shifts."
            : "Unable to load shifts for this date range."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="manager-shifts">
      <h2>Employee Shifts</h2>
      <p className="manager-shifts__intro">
        View scheduled shifts for employees on your job.
      </p>

      <form className="manager-shifts__filters" onSubmit={loadShifts}>
        <label>
          From
          <input
            type="date"
            value={from}
            onChange={(event) => setFrom(event.target.value)}
            required
          />
        </label>

        <label>
          Through
          <input
            type="date"
            value={to}
            onChange={(event) => setTo(event.target.value)}
            required
          />
        </label>

        <button type="submit" disabled={loading}>
          {loading ? "Loading..." : "Load Shifts"}
        </button>
      </form>

      {error && <p className="manager-shifts__error" role="alert">{error}</p>}

      {hasLoaded && !loading && !error && shifts.length === 0 && (
        <p className="manager-shifts__empty">No shifts in this date range.</p>
      )}

      {!loading && !error && shifts.map((shift) => {
        const employee = employees.find(
          (item) => item.employmentId === shift.employmentId
        );

        return (
          <article className="manager-shift-card" key={shift.id}>
            <div>
              <h3>{employee?.name || "Employee details unavailable"}</h3>
              <p>{shift.jobTitle}</p>
            </div>

            <p>
              <strong>Start:</strong> {formatDateTime(shift.startAt)}
            </p>
            <p>
              <strong>End:</strong> {formatDateTime(shift.endAt)}
            </p>
            <p>
              <strong>Status:</strong> {shift.status}
            </p>
            {shift.note && <p><strong>Note:</strong> {shift.note}</p>}
          </article>
        );
      })}
    </section>
  );
}

export default ManagerShifts;