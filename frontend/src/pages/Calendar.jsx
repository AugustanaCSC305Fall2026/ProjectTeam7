import React, { useEffect, useState } from "react";
import backendApi from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./Calendar.css";

function Calendar() {
    const [shifts, setShifts] = useState([]);
    const [error, setError] = useState("");
    const [currentMonth, setCurrentMonth] = useState(new Date());
    const [userEvents, setUserEvents] = useState([]);
    const [showEventModal, setShowEventModal] = useState(false);
    const [editingEventId, setEditingEventId] = useState(null);
    const [eventError, setEventError] = useState("");
    const [newEvent, setNewEvent] = useState({title: "",description: "",startTime: "",endTime: ""});

    useEffect(() => {
        loadShifts();
        loadUserEvents();
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

const loadUserEvents = async () => {
    try {
        const response = await backendApi.get("/user-events");
        setUserEvents(response.data);
    } catch (err) {
        console.error(err);
        setEventError("Unable to load your events.");
    }
};

const startEditingEvent = (event) => {
    const formatDateTime = (date) => {
        const d = new Date(date);
        const localDate = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
        return localDate.toISOString().slice(0, 16);
    };

    setEditingEventId(event.idString);

    setNewEvent({
        title: event.title,
        description: event.description || "",
        startTime: formatDateTime(event.startTime),
        endTime: formatDateTime(event.endTime)
    });

    setEventError("");
    setShowEventModal(true);
};

const addEvent = async (e) => {
    e.preventDefault();

    try {
        setEventError("");

        if (!newEvent.title || !newEvent.startTime || !newEvent.endTime) {
            setEventError("Please fill in all required fields.");
            return;
        }

        if (new Date(newEvent.endTime) <= new Date(newEvent.startTime)) {
            setEventError("End time must be after start time.");
            return;
        }

        
    const eventData = {
        title: newEvent.title,
        description: newEvent.description,
        startTime: new Date(newEvent.startTime).toISOString(),
        endTime: new Date(newEvent.endTime).toISOString()
    };

    if (editingEventId) {
        await backendApi.put(`/user-events/${editingEventId}`, eventData);
    } else {
        await backendApi.post("/user-events", eventData);
    }
    setNewEvent({title: "",description: "",startTime: "",endTime: ""});
    setEditingEventId(null);
    setShowEventModal(false);
    await loadUserEvents();

    } catch (err) {
        console.error(err);
        setEventError(editingEventId ? "Unable to update event." : "Unable to create event.");
    }
};
const deleteEvent = async (id) => {
    try {
        setEventError("");

        await backendApi.delete(`/user-events/${id}`);

        await loadUserEvents();

    } catch (err) {
        console.error(err);
        setEventError("Unable to delete event.");
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

    const getUserEventsForDay = (day) => {
        return userEvents.filter((event) => {
            const eventDate = new Date(event.startTime);

            return (
                eventDate.getDate() === day &&
                eventDate.getMonth() === month &&
                eventDate.getFullYear() === year
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

    <div>
        <span className="calendar-count">
            {shifts.length} shifts · {userEvents.length} events
        </span>

     <button
          className="calender-add-button"
           onClick={() => {
                setEditingEventId(null);
                setNewEvent({title: "", description: "", startTime: "", endTime: ""});
                setEventError("");
                setShowEventModal(true);
            }}
           >
           + Add Event
    </button>
</div>
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
                                    {getUserEventsForDay(day).map((event) => (
                                        <div className="calendar-user-event" key={event.idString}>
                                            <strong>{event.title}</strong>

                                            <span>
                                                {formatTime(event.startTime)} – {formatTime(event.endTime)}
                                            </span>
                                            <button
                                                className="calendar-edit-event"
                                                onClick={() => startEditingEvent(event)}
                                            >
                                                Edit
                                            </button>

                                            <button
                                                className="calendar-delete-event"
                                                onClick={() => deleteEvent(event.idString)}
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    ))}
                                </>
                            )}
                        </div>
                    ))}
                </div>
            </section>
            {showEventModal && (
                <div className="calendar-modal">
                    <form
                        className="calendar-modal-content"
                        onSubmit={addEvent}
                    >
                        <h2>{editingEventId ? "Edit Event" : "Add New Event"}</h2>

                        {eventError && (
                            <p className="calendar-error">{eventError}</p>
                        )}

                        <input
                            type="text"
                            placeholder="Event Title"
                            value={newEvent.title}
                            onChange={(e) =>
                                setNewEvent({
                                    ...newEvent,
                                    title: e.target.value
                                })
                            }
                            required
                        />

                        <textarea
                            placeholder="Event Description"
                            value={newEvent.description}
                            onChange={(e) =>
                                setNewEvent({
                                    ...newEvent,
                                    description: e.target.value
                                })
                            }
                        />

                        <label>Start Time</label>

                        <input
                            type="datetime-local"
                            value={newEvent.startTime}
                            onChange={(e) =>
                                setNewEvent({
                                    ...newEvent,
                                    startTime: e.target.value
                                })
                            }
                            required
                        />

                        <label>End Time</label>

                        <input
                            type="datetime-local"
                            value={newEvent.endTime}
                            onChange={(e) =>
                                setNewEvent({
                                    ...newEvent,
                                    endTime: e.target.value
                                })
                            }
                            required
                        />

                        <button type="submit">
                            {editingEventId ? "Save Changes" : "Save Event"}
                        </button>

                        <button
                            type="button"
                            onClick={() => {
                                setShowEventModal(false);
                                setEditingEventId(null);
                                setNewEvent({title: "", description: "", startTime: "", endTime: ""});
                                setEventError("");
                            }}
                        >
                            Cancel
                        </button>
                    </form>
                </div>
            )}
        </StudentLayout>
    );
}

export default Calendar;