import React, { useEffect, useState } from "react";
import backendApi from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./Hours.css";

function Hours() {
    const [entries, setEntries] = useState([]);
    const [totalMinutes, setTotalMinutes] = useState(0);
    const [totalGrossPayCents, setTotalGrossPayCents] = useState(0);
    const [selectedJobId, setSelectedJobId] = useState("");
    const [jobs, setJobs] = useState([]);
    const [error, setError] = useState("");
    const [correctionEntry, setCorrectionEntry] = useState(null);
    const [proposedClockIn, setProposedClockIn] = useState("");
    const [proposedClockOut, setProposedClockOut] = useState("");
    const [correctionReason, setCorrectionReason] = useState("");
    const [correctionMessage, setCorrectionMessage] = useState("");

    const loadHours = async (jobId = "") => {
        try {
            setError("");

            const response = await backendApi.get("/hours", {
                params: jobId ? { jobId } : {}
            });

            setEntries(response.data.entries);
            setTotalMinutes(response.data.totalMinutes);
            setTotalGrossPayCents(response.data.totalGrossPayCents ?? 0);

            if (!jobId) {
                const uniqueJobs = [];

                response.data.entries.forEach((entry) => {
                    if (!uniqueJobs.some((job) => job.id === entry.jobId)) {
                        uniqueJobs.push({
                            id: entry.jobId,
                            title: entry.jobTitle
                        });
                    }
                });

                setJobs(uniqueJobs);
            }
        } catch (err) {
            console.error(err);
            setError("Unable to load hours.");
        }
    };

    useEffect(() => {
        loadHours();
    }, []);

    const handleJobChange = (event) => {
        const jobId = event.target.value;
        setSelectedJobId(jobId);
        loadHours(jobId);
    };
    const toDateTimeLocal = (date) => {
        if (!date) return "";

        const value = new Date(date);
        const offset = value.getTimezoneOffset();

        return new Date(value.getTime() - offset * 60000)
            .toISOString()
            .slice(0, 16);
    };

    const openCorrectionForm = (entry) => {
        setCorrectionEntry(entry);
        setProposedClockIn(toDateTimeLocal(entry.clockInAt));
        setProposedClockOut(toDateTimeLocal(entry.clockOutAt));
        setCorrectionReason("");
        setCorrectionMessage("");
    };

    const closeCorrectionForm = () => {
        setCorrectionEntry(null);
        setCorrectionMessage("");
    };

    const handleCorrectionSubmit = async (event) => {
        event.preventDefault();

        if (!proposedClockIn || !proposedClockOut || !correctionReason.trim()) {
            setCorrectionMessage("Please complete all fields.");
            return;
        }

        try {
            setCorrectionMessage("");

            await backendApi.post("/me/corrections", {
                timeEntryId: correctionEntry.id,
                proposedClockInAt: new Date(proposedClockIn).toISOString(),
                proposedClockOutAt: new Date(proposedClockOut).toISOString(),
                reason: correctionReason.trim()
            });

            setCorrectionMessage("Correction request submitted successfully.");

            setTimeout(() => {
                setCorrectionEntry(null);
                setCorrectionMessage("");
            }, 1200);
        } catch (err) {
            console.error(err);

            const code = err.response?.data?.code;

            if (code === "DUPLICATE_PENDING_REQUEST") {
                setCorrectionMessage(
                    "A correction request is already pending for this entry."
                );
            } else if (code === "ENTRY_STILL_OPEN") {
                setCorrectionMessage(
                    "You cannot correct an entry while it is still open."
                );
            } else {
                setCorrectionMessage("Unable to submit correction request.");
            }
        }
    };

    const formatHours = (minutes = 0) => {
        const hours = Math.floor(minutes / 60);
        const remainingMinutes = minutes % 60;

        return `${hours}h ${remainingMinutes}m`;
    };

    const formatDate = (date) => {
        if (!date) {
            return "Currently working";
        }

        return new Date(date).toLocaleDateString([], {
            month: "short",
            day: "numeric",
            year: "numeric"
        });
    };

    const formatTime = (date) => {
        if (!date) {
            return "—";
        }

        return new Date(date).toLocaleTimeString([], {
            hour: "numeric",
            minute: "2-digit"
        });
    };

    return (
        <StudentLayout
            title="Hours"
            subtitle="Review your work hours and time entries."
        >
            <div className="hours-top-row">
                <section className="hours-summary-card">
                    <span className="hours-label">TOTAL HOURS</span>
                    <strong>{formatHours(totalMinutes)}</strong>
                    <p>
                        {selectedJobId
                            ? "Hours for selected job"
                            : "Hours across all jobs"}
                    </p>
                </section>

                <section className="hours-summary-card">
                    <span className="hours-label">GROSS PAY</span>
                    <strong>
                        {(totalGrossPayCents / 100).toLocaleString("en-US", {
                            style: "currency",
                            currency: "USD"
                        })}
                    </strong>
                    <p>
                        {selectedJobId
                            ? "Gross earnings for selected job"
                            : "Gross earnings across all jobs"}
                    </p>
                </section>

                <section className="hours-filter-card">
                    <label htmlFor="job-filter">Filter by job</label>

                    <select
                        id="job-filter"
                        value={selectedJobId}
                        onChange={handleJobChange}
                    >
                        <option value="">All Jobs</option>

                        {jobs.map((job) => (
                            <option key={job.id} value={job.id}>
                                {job.title}
                            </option>
                        ))}
                    </select>
                </section>
            </div>

            {error && <p className="hours-error">{error}</p>}

            <section className="hours-entries-card">
                <div className="hours-section-heading">
                    <div>
                        <span className="hours-label">TIME ENTRIES</span>
                        <h2>Work History</h2>
                    </div>

                    <span className="entry-count">
                        {entries.length} {entries.length === 1 ? "entry" : "entries"}
                    </span>
                </div>

                {entries.length === 0 && !error ? (
                    <div className="hours-empty">
                        No time entries found.
                    </div>
                ) : (
                    <div className="hours-table-wrapper">
                        <table className="hours-table">
                            <thead>
                                <tr>
                                    <th>Job</th>
                                    <th>Date</th>
                                    <th>Clock In</th>
                                    <th>Clock Out</th>
                                    <th>Break</th>
                                    <th>Worked</th>
                                    <th>Action</th>
                                </tr>
                            </thead>

                            <tbody>
                                {entries.map((entry) => (
                                    <tr key={entry.id}>
                                        <td>
                                            <strong>{entry.jobTitle}</strong>
                                        </td>

                                        <td>
                                            {formatDate(entry.clockInAt)}
                                        </td>

                                        <td>
                                            {formatTime(entry.clockInAt)}
                                        </td>

                                        <td>
                                            {entry.clockOutAt
                                                ? formatTime(entry.clockOutAt)
                                                : (
                                                    <span className="active-entry">
                                                        Active
                                                    </span>
                                                )}
                                        </td>

                                        <td>
                                            {entry.breakMinutes} min
                                        </td>

                                        <td>
                                            <strong>
                                                {entry.clockOutAt
                                                    ? formatHours(entry.workedMinutes)
                                                    : "In progress"}
                                            </strong>
                                        </td>
                                      <td>
                                            {entry.clockOutAt ? (
                                                <button
                                                    className="correction-button"
                                                    onClick={() => openCorrectionForm(entry)}
                                                >
                                                    Request Correction
                                                </button>
                                            ) : (
                                                <span className="correction-unavailable">—</span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
            {correctionEntry && (
                <div className="correction-overlay">
                    <div className="correction-modal">
                        <div className="correction-modal-header">
                            <div>
                                <span className="hours-label">TIME CORRECTION</span>
                                <h2>Request Correction</h2>
                            </div>

                            <button
                                className="correction-close"
                                onClick={closeCorrectionForm}
                                type="button"
                            >
                                ×
                            </button>
                        </div>

                        <p className="correction-job">
                            {correctionEntry.jobTitle}
                        </p>

                        <form onSubmit={handleCorrectionSubmit}>
                            <div className="correction-field">
                                <label htmlFor="proposed-clock-in">
                                    Correct Clock In
                                </label>

                                <input
                                    id="proposed-clock-in"
                                    type="datetime-local"
                                    value={proposedClockIn}
                                    onChange={(event) =>
                                        setProposedClockIn(event.target.value)
                                    }
                                />
                            </div>

                            <div className="correction-field">
                                <label htmlFor="proposed-clock-out">
                                    Correct Clock Out
                                </label>

                                <input
                                    id="proposed-clock-out"
                                    type="datetime-local"
                                    value={proposedClockOut}
                                    onChange={(event) =>
                                        setProposedClockOut(event.target.value)
                                    }
                                />
                            </div>

                            <div className="correction-field">
                                <label htmlFor="correction-reason">
                                    Reason
                                </label>

                                <textarea
                                    id="correction-reason"
                                    rows="4"
                                    placeholder="Explain why this time entry needs to be corrected."
                                    value={correctionReason}
                                    onChange={(event) =>
                                        setCorrectionReason(event.target.value)
                                    }
                                />
                            </div>

                            {correctionMessage && (
                                <p className="correction-message">
                                    {correctionMessage}
                                </p>
                            )}

                            <div className="correction-actions">
                                <button
                                    type="button"
                                    className="correction-cancel"
                                    onClick={closeCorrectionForm}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="correction-submit"
                                >
                                    Submit Request
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </StudentLayout>
    );
}

export default Hours;