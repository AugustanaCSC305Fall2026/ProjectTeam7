import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./SupervisorCorrections.css";

function formatDateTime(value) {
  if (!value) return "—";

  return new Date(value).toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function toDateTimeLocal(value) {
  if (!value) return "";

  const date = new Date(value);
  const localDate = new Date(
    date.getTime() - date.getTimezoneOffset() * 60000
  );
  return localDate.toISOString().slice(0, 16);
}

function fromDateTimeLocal(value) {
  return value ? new Date(value).toISOString() : null;
}

function SupervisorCorrections() {
  const [requests, setRequests] = useState([]);
  const [reviewNotes, setReviewNotes] = useState({});
  const [reviewTimes, setReviewTimes] = useState({});
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadRequests = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/admin/corrections/pending");
      setRequests(response.data);
    } catch (err) {
      const status = err.response?.status;

      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to review corrections.");
      } else {
        setError(
          err.response?.data?.message || "Unable to load correction requests."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRequests();
  }, []);

    const reviewRequest = async (request, decision) => {
    const requestId = request.correctionRequestId;
    setBusyId(requestId);
    setError("");
    setMessage("");

    try {
      const reviewNote = reviewNotes[requestId]?.trim() || "";
      const payload = { reviewNote };

      if (decision === "approve") {
        const times = reviewTimes[requestId] || {};
        const clockIn =
          times.clockIn ?? toDateTimeLocal(request.proposedClockInAt);
        const clockOut =
          times.clockOut ?? toDateTimeLocal(request.proposedClockOutAt);

        if (!clockIn || !clockOut) {
          setError("Enter both final clock-in and clock-out times.");
          return;
        }

        payload.approvedClockInAt = fromDateTimeLocal(clockIn);
        payload.approvedClockOutAt = fromDateTimeLocal(clockOut);
      }

      await api.patch(
        `/admin/corrections/${requestId}/${decision}`,
        payload
      );

      setMessage(
        decision === "approve"
          ? "Correction approved."
          : "Correction rejected."
      );

      await loadRequests();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.code ||
          "Unable to review this correction request."
      );
    } finally {
      setBusyId("");
    }
  };

  return (
    <section className="supervisor-corrections">
      <h2>Time Correction Requests</h2>

      {message && <p role="status">{message}</p>}
      {error && <p role="alert">{error}</p>}
      {loading && <p>Loading correction requests...</p>}

      {!loading && !error && requests.length === 0 && (
        <p className="correction-empty">No pending correction requests.</p>
      )}

      {!loading &&
        requests.map((request) => (
          <article
            className="correction-card"
            key={request.correctionRequestId}
          >
            <h3>{request.employeeName}</h3>
            <p>{request.employeeEmail}</p>
            <p>{request.jobTitle}</p>

            <p>
              <strong>Original:</strong>{" "}
              {formatDateTime(request.originalClockInAt)} –{" "}
              {formatDateTime(request.originalClockOutAt)}
            </p>

            <p>
              <strong>Requested:</strong>{" "}
              {formatDateTime(request.proposedClockInAt)} –{" "}
              {formatDateTime(request.proposedClockOutAt)}
            </p>
                      <div className="correction-time-fields">
              <label htmlFor={`approved-in-${request.correctionRequestId}`}>
                Final clock-in
              </label>
              <input
                id={`approved-in-${request.correctionRequestId}`}
                type="datetime-local"
                value={
                  reviewTimes[request.correctionRequestId]?.clockIn ??
                  toDateTimeLocal(request.proposedClockInAt)
                }
                onChange={(event) =>
                  setReviewTimes((current) => ({
                    ...current,
                    [request.correctionRequestId]: {
                      ...current[request.correctionRequestId],
                      clockIn: event.target.value,
                    },
                  }))
                }
              />

              <label htmlFor={`approved-out-${request.correctionRequestId}`}>
                Final clock-out
              </label>
              <input
                id={`approved-out-${request.correctionRequestId}`}
                type="datetime-local"
                value={
                  reviewTimes[request.correctionRequestId]?.clockOut ??
                  toDateTimeLocal(request.proposedClockOutAt)
                }
                onChange={(event) =>
                  setReviewTimes((current) => ({
                    ...current,
                    [request.correctionRequestId]: {
                      ...current[request.correctionRequestId],
                      clockOut: event.target.value,
                    },
                  }))
                }
              />
            </div>

            <p>
              <strong>Reason:</strong> {request.reason}
            </p>

            <label htmlFor={`review-note-${request.correctionRequestId}`}>
              Review note (optional)
            </label>
            <textarea
              id={`review-note-${request.correctionRequestId}`}
              value={reviewNotes[request.correctionRequestId] || ""}
              onChange={(event) =>
                setReviewNotes((current) => ({
                  ...current,
                  [request.correctionRequestId]: event.target.value,
                }))
              }
            />

            <button
              disabled={busyId !== ""}
              onClick={() =>
                reviewRequest(request, "approve")
              }
            >
              {busyId === request.correctionRequestId
                ? "Saving..."
                : "Approve"}
            </button>

            <button
              disabled={busyId !== ""}
              onClick={() =>
                reviewRequest(request, "reject")
              }
            >
              Reject
            </button>
          </article>
        ))}
    </section>
  );
}

export default SupervisorCorrections;