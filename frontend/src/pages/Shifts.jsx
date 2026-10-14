import React, { useEffect, useState } from "react";
import api from "../api/backendApi.js";
import StudentLayout from "../components/StudentLayout";
import "./Shifts.css";

function Shifts() {
  const [myShifts, setMyShifts] = useState([]);
  const [openShifts, setOpenShifts] = useState([]);
  const [employments, setEmployments] = useState([]);
  const [message, setMessage] = useState("");

  const loadMyShifts = () => {
    api.get("/shifts/me")
      .then((response) => {
        setMyShifts(response.data);
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not load your shifts.");
      });
  };

  const loadOpenShifts = () => {
    const from = new Date();

    const to = new Date();
    to.setDate(to.getDate() + 30);

    api.get("/shifts/open", {
        params: {
        from: from.toISOString(),
        to: to.toISOString()
        }
    })
        .then((response) => {
        setOpenShifts(response.data);
        })
        .catch((error) => {
        console.error(error);
        setMessage("Could not load open shifts.");
        });
    };

  const loadEmployments = () => {
    api.get("/employments/me")
      .then((response) => {
        setEmployments(response.data);
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not load your employments.");
      });
  };

  useEffect(() => {
    loadMyShifts();
    loadOpenShifts();
    loadEmployments();
  }, []);

  const offerShift = (shiftId) => {
    const reason = window.prompt(
      "Why are you offering this shift? (Optional)"
    );

    if (reason === null) {
      return;
    }

    api.post(`/shifts/${shiftId}/offer`, {
      reason: reason
    })
      .then(() => {
        setMessage("Shift offered successfully.");
        loadMyShifts();
        loadOpenShifts();
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not offer shift.");
      });
  };

  const withdrawOffer = (shiftId) => {
    api.delete(`/shifts/${shiftId}/offer`)
      .then(() => {
        setMessage("Shift offer withdrawn.");
        loadMyShifts();
        loadOpenShifts();
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not withdraw shift offer.");
      });
  };

  const claimShift = (shift) => {
    const matchingEmployment = employments.find(
        (employment) => employment.jobId === shift.jobId
    );

    if (!matchingEmployment) {
        setMessage("You do not have an employment available for this shift.");
        return;
    }

    api.post(`/shifts/${shift.id}/claim`, {
        employmentId: matchingEmployment.employmentId
    })
        .then(() => {
        setMessage("Shift claimed successfully.");
        loadMyShifts();
        loadOpenShifts();
        })
        .catch((error) => {
        console.error(error);

        const errorCode = error.response?.data?.error;

        if (errorCode === "SHIFT_ALREADY_CLAIMED") {
            setMessage("This shift has already been claimed.");
        } else if (errorCode === "OVERLAPPING_SHIFT") {
            setMessage("You already have a shift during this time.");
        } else if (errorCode === "WRONG_JOB") {
            setMessage("You are not eligible to work this job.");
        } else if (errorCode === "CANNOT_CLAIM_OWN_SHIFT") {
            setMessage("You cannot claim your own offered shift.");
        } else {
            setMessage("Could not claim shift.");
        }

        loadOpenShifts();
        });
    };
    const formatDate = (date) => {
        return new Date(date).toLocaleDateString([], {
            weekday: "short",
            month: "short",
            day: "numeric"
        });
    };

    const formatTime = (date) => {
        return new Date(date).toLocaleTimeString([], {
            hour: "numeric",
            minute: "2-digit"
        });
    };

    return (
        <StudentLayout
            title="Shift Management"
            subtitle="Manage your scheduled shifts and pick up available shifts."
        >
            {message && (
            <div className="shift-message">
                {message}
            </div>
            )}

            <section className="shift-section">
            <div className="shift-section-heading">
                <div>
                <span className="shift-label">YOUR SCHEDULE</span>
                <h2>My Shifts</h2>
                </div>

                <span className="shift-count">
                {myShifts.length} {myShifts.length === 1 ? "shift" : "shifts"}
                </span>
            </div>

            {myShifts.length === 0 ? (
                <div className="shift-empty">
                You do not currently have any shifts.
                </div>
            ) : (
                <div className="shift-list">
                {myShifts.map((shift) => (
                    <div className="student-shift-card" key={shift.id}>
                    <div className="shift-date">
                        <strong>{formatDate(shift.startAt)}</strong>

                        <span>
                        {formatTime(shift.startAt)} – {formatTime(shift.endAt)}
                        </span>
                    </div>

                    <div className="shift-details">
                        <span
                        className={
                            shift.status === "OFFERED"
                            ? "shift-status offered"
                            : "shift-status scheduled"
                        }
                        >
                        {shift.status === "OFFERED"
                            ? "Offered"
                            : "Scheduled"}
                        </span>

                        {shift.offer?.reason && (
                        <p>
                            <strong>Reason:</strong> {shift.offer.reason}
                        </p>
                        )}
                    </div>

                    <div className="shift-actions">
                        {shift.status === "SCHEDULED" && (
                        <button
                            className="offer-button"
                            onClick={() => offerShift(shift.id)}
                        >
                            Offer Shift
                        </button>
                        )}

                        {shift.status === "OFFERED" && (
                        <button
                            className="withdraw-button"
                            onClick={() => withdrawOffer(shift.id)}
                        >
                            Withdraw Offer
                        </button>
                        )}
                    </div>
                    </div>
                ))}
                </div>
            )}
            </section>

            <section className="shift-section">
            <div className="shift-section-heading">
                <div>
                <span className="shift-label">AVAILABLE SHIFTS</span>
                <h2>Open Shifts</h2>
                </div>

                <span className="shift-count">
                    {openShifts.length} available
                </span>
            </div>

            {openShifts.length === 0 ? (
                <div className="shift-empty">
                No open shifts are currently available.
                </div>
            ) : (
                <div className="shift-list">
                {openShifts.map((shift) => (
                    <div className="student-shift-card" key={shift.id}>
                    <div className="shift-date">
                        <strong>{formatDate(shift.startAt)}</strong>

                        <span>
                        {formatTime(shift.startAt)} – {formatTime(shift.endAt)}
                        </span>
                    </div>

                    <div className="shift-details">
                        <strong className="shift-job">
                        {shift.jobTitle}
                        </strong>

                        {shift.reason && (
                        <p>
                            <strong>Reason:</strong> {shift.reason}
                        </p>
                        )}

                        <span
                        className={
                            shift.eligible
                            ? "eligibility eligible"
                            : "eligibility not-eligible"
                        }
                        >
                        {shift.eligible ? "Eligible" : "Not Eligible"}
                        </span>
                    </div>

                    <div className="shift-actions">
                        <button
                        className="pickup-button"
                        onClick={() => claimShift(shift)}
                        disabled={!shift.eligible}
                        >
                        {shift.eligible
                            ? "Pick Up Shift"
                            : "Not Eligible"}
                        </button>
                    </div>
                    </div>
                ))}
                </div>
            )}
            </section>
        </StudentLayout>
    );
}

export default Shifts;