import React, { useEffect, useState } from "react";
import backendApi from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./Calendar.css";

function Calendar() {
    const [shifts, setShifts] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {
        loadShifts();
    }, []);

    const loadShifts = async () => {
        try {
            setError("");

            const response = await backendApi.get("/shifts/upcoming");

            setShifts(response.data);
        } catch (err) {
            console.error(err);
            setError("Unable to load your schedule.");
        }
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
            title="Calendar"
            subtitle="View your upcoming work schedule."
        >
            {error && (
                <p className="calendar-error">
                    {error}
                </p>
            )}

            <section className="calendar-card">
                <div className="calendar-heading">
                    <div>
                        <span className="calendar-label">
                            UPCOMING SCHEDULE
                        </span>

                        <h2>Your Shifts</h2>
                    </div>

                    <span className="calendar-count">
                        {shifts.length}{" "}
                        {shifts.length === 1 ? "shift" : "shifts"}
                    </span>
                </div>

                {shifts.length === 0 && !error ? (
                    <div className="calendar-empty">
                        No upcoming shifts scheduled.
                    </div>
                ) : (
                    <div className="calendar-shift-list">
                        {shifts.map((shift) => (
                            <div
                                className="calendar-shift"
                                key={shift.id}
                            >
                                <div className="calendar-date">
                                    <strong>
                                        {formatDate(shift.startAt)}
                                    </strong>
                                </div>

                                <div className="calendar-shift-info">
                                    <strong>
                                        {shift.jobTitle || "Scheduled Shift"}
                                    </strong>

                                    <span>
                                        {formatTime(shift.startAt)}
                                        {" – "}
                                        {formatTime(shift.endAt)}
                                    </span>
                                </div>

                                <span className="calendar-status">
                                    Scheduled
                                </span>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </StudentLayout>
    );
}

export default Calendar;