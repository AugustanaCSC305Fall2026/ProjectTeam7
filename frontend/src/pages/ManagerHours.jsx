import { Fragment, useEffect, useState } from "react";
import api from "../api/backendApi";
import "./ManagerHours.css";

function formatDateTime(value) {
  if (!value) return "Still open";

  return new Date(value).toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function formatMinutes(minutes = 0) {
  return `${Math.floor(minutes / 60)}h ${minutes % 60}m`;
}

function toDateTimeLocal(value) {
  if (!value) return "";

  const date = new Date(value);
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 16);
}

function fromDateTimeLocal(value) {
  return new Date(value).toISOString();
}

function ManagerHours() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editingTimes, setEditingTimes] = useState({});
  const [savingId, setSavingId] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    async function loadHours() {
      try {
        const response = await api.get("/manager/hours");
        setEmployees(response.data);
      } catch (err) {
        const status = err.response?.status;
        setError(
          status === 401
            ? "Your session has expired. Please log in again."
            : status === 403
              ? "Your account is not authorized to view employee hours."
              : "Unable to load employee hours."
        );
      } finally {
        setLoading(false);
      }
    }

    loadHours();
  }, []);

    function startEditing(entry) {
    setError("");
    setMessage("");
    setEditingTimes((current) => ({
      ...current,
      [entry.id]: {
        clockInAt: toDateTimeLocal(entry.clockInAt),
        clockOutAt: toDateTimeLocal(entry.clockOutAt),
      },
    }));
  }

  function updateEditingTime(entryId, field, value) {
    setEditingTimes((current) => ({
      ...current,
      [entryId]: {
        ...current[entryId],
        [field]: value,
      },
    }));
  }

  function cancelEditing(entryId) {
    setEditingTimes((current) => {
      const updated = { ...current };
      delete updated[entryId];
      return updated;
    });
  }

  async function saveEditing(event, entry) {
    event.preventDefault();

    const times = editingTimes[entry.id];
    if (!times?.clockInAt || !times?.clockOutAt) {
      setError("Enter both a clock-in and clock-out time.");
      return;
    }

    const clockIn = new Date(times.clockInAt);
    const clockOut = new Date(times.clockOutAt);

    if (
      Number.isNaN(clockIn.getTime()) ||
      Number.isNaN(clockOut.getTime()) ||
      clockOut <= clockIn
    ) {
      setError("Clock-out must be later than clock-in.");
      return;
    }

    setError("");
    setMessage("");
    setSavingId(entry.id);

    try {
      await api.patch(`/manager/time-entries/${entry.id}`, {
        clockInAt: fromDateTimeLocal(times.clockInAt),
        clockOutAt: fromDateTimeLocal(times.clockOutAt),
      });

      cancelEditing(entry.id);
      setMessage("Time entry updated.");

      const response = await api.get("/manager/hours");
      setEmployees(response.data);
    } catch (err) {
      const status = err.response?.status;
      const code = err.response?.data?.code || err.response?.data?.error;
      const errors = {
        INVALID_TIME_RANGE: "Clock-out must be later than clock-in.",
        TIME_ENTRY_NOT_CLOSED: "Only closed time entries can be edited.",
        TIME_ENTRY_CHANGED:
          "This entry changed since it was loaded. Reload the hours and try again.",
        TIME_ENTRY_NOT_FOUND: "This time entry is no longer available.",
      };

      setError(
        status === 401
          ? "Your session has expired. Please log in again."
          : status === 403
            ? "Your account is not authorized to edit this entry."
            : errors[code] || "Unable to update this time entry."
      );
    } finally {
      setSavingId("");
    }
  }

  return (
    <section className="manager-hours">
      <h2>Employee Hours</h2>
      <p className="manager-hours__intro">
        Review recorded time entries for employees on your job.
      </p>

      {message && (
        <p className="manager-hours__message" role="status">{message}</p>
        )}

      {loading && <p>Loading employee hours...</p>}
      {!loading && error && (
        <p className="manager-hours__error" role="alert">{error}</p>
      )}
      {!loading && !error && employees.length === 0 && (
        <p>No active employees are assigned to your job.</p>
      )}

      {!loading && !error && employees.map((employee) => (
        <article
          className="manager-hours__employee"
          key={employee.employmentId}
        >
          <header className="manager-hours__employee-header">
            <div>
              <h3>{employee.employeeName}</h3>
              <p>{employee.email}</p>
            </div>
            <strong>Total: {formatMinutes(employee.totalMinutes)}</strong>
          </header>

          {employee.entries.length === 0 ? (
            <p className="manager-hours__empty">
              No time entries for this employee.
            </p>
          ) : (
            <div className="manager-hours__table-wrapper">
            <table className="manager-hours__table">
                <thead>
                    <tr>
                    <th>Clock in</th>
                    <th>Clock out</th>
                    <th>Break</th>
                    <th>Worked</th>
                    <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    {employee.entries.map((entry) => {
                    const isEditing = Boolean(editingTimes[entry.id]);
                    const canEdit = Boolean(entry.clockOutAt) && !entry.onBreak;

                    return (
                        <Fragment key={entry.id}>
                        <tr>
                            <td>{formatDateTime(entry.clockInAt)}</td>
                            <td>
                            {entry.onBreak ? "On break" : formatDateTime(entry.clockOutAt)}
                            </td>
                            <td>{entry.breakMinutes} min</td>
                            <td>{formatMinutes(entry.workedMinutes)}</td>
                            <td>
                            {canEdit && !isEditing && (
                                <button
                                type="button"
                                className="manager-hours__button"
                                onClick={() => startEditing(entry)}
                                disabled={savingId !== ""}
                                >
                                Edit
                                </button>
                            )}
                            {!canEdit && <span>Unavailable while open or on break</span>}
                            </td>
                        </tr>

                        {isEditing && (
                            <tr className="manager-hours__edit-row">
                            <td colSpan={5}>
                                <form onSubmit={(event) => saveEditing(event, entry)}>
                                <label>
                                    Clock in
                                    <input
                                    type="datetime-local"
                                    value={editingTimes[entry.id].clockInAt}
                                    onChange={(event) =>
                                        updateEditingTime(
                                        entry.id,
                                        "clockInAt",
                                        event.target.value
                                        )
                                    }
                                    required
                                    />
                                </label>

                                <label>
                                    Clock out
                                    <input
                                    type="datetime-local"
                                    value={editingTimes[entry.id].clockOutAt}
                                    onChange={(event) =>
                                        updateEditingTime(
                                        entry.id,
                                        "clockOutAt",
                                        event.target.value
                                        )
                                    }
                                    required
                                    />
                                </label>

                                <button
                                    type="submit"
                                    className="manager-hours__button"
                                    disabled={savingId !== ""}
                                >
                                    {savingId === entry.id ? "Saving..." : "Save changes"}
                                </button>

                                <button
                                    type="button"
                                    className="manager-hours__cancel-button"
                                    onClick={() => cancelEditing(entry.id)}
                                    disabled={savingId === entry.id}
                                >
                                    Cancel
                                </button>
                                </form>
                            </td>
                            </tr>
                        )}
                        </Fragment>
                    );
                    })}
                </tbody>
                </table>
            </div>
          )}
        </article>
      ))}
    </section>
  );
}

export default ManagerHours;