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

    const formatTime = (date) => {
        return new Date(date).toLocaleTimeString([], {
            hour: "numeric",
            minute: "2-digit"
        });
    };

    const today = new Date();

    const year = today.getFullYear();
    const month = today.getMonth();

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const calendarDays = [];

    for (let i = 0; i < firstDay; i++) {
        calendarDays.push(null);
    }

    for (let day = 1; day <= daysInMonth; day++) {
        calendarDays.push(day);
    }

    const monthName = today.toLocaleDateString([], {
        month: "long",
        year: "numeric"
    });

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
                            WORK SCHEDULE
                        </span>

                        <h2>{monthName}</h2>
                    </div>

                    <span className="calendar-count">
                        {shifts.length}{" "}
                        {shifts.length === 1 ? "shift" : "shifts"}
                    </span>
                </div>

                <div className="calendar-weekdays">
                    <div>Sun</div>
                    <div>Mon</div>
                    <div>Tue</div>
                    <div>Wed</div>
                    <div>Thu</div>
                    <div>Fri</div>
                    <div>Sat</div>
                </div>

                <div className="calendar-grid">
                    {calendarDays.map((day, index) => (
                        <div className="calendar-day" key={index}>
                            {day && (
                                <span className="calendar-day-number">
                                    {day}
                                </span>
                            )}
                        </div>
                    ))}
                </div>
            </section>
        </StudentLayout>
    );
}

export default Calendar;