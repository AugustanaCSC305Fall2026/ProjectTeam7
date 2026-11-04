import React, { useEffect, useState } from "react";
import backendApi from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./Calendar.css";

function Calendar() {
    const [shifts, setShifts] = useState([]);
    const [error, setError] = useState("");
    const [currentMonth, setCurrentMonth] = useState(new Date());

    useEffect(() => {
        loadShifts();
    }, [currentMonth]);

    const loadShifts = async () => {
        try {
            setError("");
            setShifts([]);

            const year = currentMonth.getFullYear();
            const month = currentMonth.getMonth();

            const from = new Date(year, month, 1);
            const to = new Date(year, month + 1, 1);

            const response = await backendApi.get("/shifts/upcoming", {
                params: {
                    from: from.toISOString(),
                    to: to.toISOString()
                }
            });

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

    
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const calendarDays = [];

    for (let i = 0; i < firstDay; i++) {
        calendarDays.push(null);
    }

    for (let day = 1; day <= daysInMonth; day++) {
        calendarDays.push(day);
    }

    const monthName = currentMonth.toLocaleDateString([], {
        month: "long",
        year: "numeric"
    });

    const getShiftsForDay = (day) => {
        return shifts.filter((shift) => {
            const shiftDate = new Date(shift.startAt);

            return (
                shiftDate.getDate() === day &&
                shiftDate.getMonth() === month &&
                shiftDate.getFullYear() === year
            );
        });
    };
    const previousMonth = () => {
        setCurrentMonth(new Date(year, month - 1, 1));
    };

    const nextMonth = () => {
        setCurrentMonth(new Date(year, month + 1, 1));
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
                            WORK SCHEDULE
                        </span>

                        <div className="calendar-navigation">
                            <button onClick={previousMonth}>
                                Previous
                            </button>

                            <h2>{monthName}</h2>

                            <button onClick={nextMonth}>
                                Next
                            </button>
                        </div>
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
                                <>
                                    <span className="calendar-day-number">
                                        {day}
                                    </span>

                                    {getShiftsForDay(day).map((shift) => (
                                        <div className="calendar-shift-event" key={shift.id}>
                                            <strong>
                                                {shift.jobTitle || "Scheduled Shift"}
                                            </strong>

                                            <span>
                                                {formatTime(shift.startAt)} – {formatTime(shift.endAt)}
                                            </span>
                                        </div>
                                    ))}
                                </>
                            )}
                        </div>
                    ))}
                </div>
            </section>
        </StudentLayout>
    );
}

export default Calendar;