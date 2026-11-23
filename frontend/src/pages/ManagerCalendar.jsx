
import { useEffect, useState } from "react";
import backendApi from "../api/backendApi";
import "./Calendar.css";

function ManagerCalendar() {
    const [availability, setAvailability] = useState([]);
    const [shifts, setShifts] = useState([]);
    const [selectedEmployee, setSelectedEmployee] = useState("all");
    const [currentMonth, setCurrentMonth] = useState(new Date());
    const [error, setError] = useState("");
    const [employees, setEmployees] = useState([]);

    useEffect(() => {
        const loadAvailability = async () => {
            try {
                const response = await backendApi.get("/manager/availability");
                setAvailability(response.data);
            } catch (err) {
                console.error(err);
                setError("Unable to load employee availability.");
            }
        };

        loadAvailability();
    }, []);

    useEffect(() => {
        const loadShifts = async () => {
            try {
                const year = currentMonth.getFullYear();
                const month = currentMonth.getMonth();

                const from = new Date(year, month, 1);
                const to = new Date(year, month + 1, 1);

                const response = await backendApi.get("/manager/shifts", {
                    params: {
                        from: from.toISOString(),
                        to: to.toISOString()
                    }
                });

                setShifts(response.data);
            } catch (err) {
                console.error(err);
                setError("Unable to load employee shifts.");
            }
        };

        loadShifts();
    }, [currentMonth]);

    useEffect(() => {
        const loadEmployees = async () => {
            try {
                const response = await backendApi.get("/manager/employees");
                setEmployees(response.data);
            } catch (err) {
                console.error(err);
                setError("Unable to load employees.");
            }
        };

        loadEmployees();
    }, []);

    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const calendarDays = [
        ...Array(firstDay).fill(null),
        ...Array.from({ length: daysInMonth }, (_, i) => i + 1)
    ];

    const monthName = currentMonth.toLocaleDateString("en-US", {
        month: "long",
        year: "numeric"
    });

    const getAvailabilityForDay = (day) => {
        const date = new Date(year, month, day);

        const dayOfWeek = date
            .toLocaleDateString("en-US", { weekday: "long" })
            .toUpperCase();

        return availability
            .filter((employee) =>
                selectedEmployee === "all" ||
                employee.userId === selectedEmployee
            )
            .flatMap((employee) =>
                employee.windows
                    .filter((window) => window.dayOfWeek === dayOfWeek)
                    .map((window) => ({
                        employeeId: employee.userId,
                        employeeName: employee.employeeName,
                        startTime: window.startTime,
                        endTime: window.endTime
                    }))
            );
    };

    const getShiftsForDay = (day) => {
        return shifts
            .filter((shift) => {
                const shiftDate = new Date(shift.startAt);

                return (
                    shiftDate.getDate() === day &&
                    shiftDate.getMonth() === month &&
                    shiftDate.getFullYear() === year
                );
            })
            .map((shift) => {
                const employee = employees.find(
                    (employee) =>
                        employee.employmentId === shift.employmentId
                );

                return {
                    ...shift,
                    employeeId: employee?.id,
                    employeeName: employee?.name || "Unknown Employee"
                };
            })
            .filter(
                (shift) =>
                    selectedEmployee === "all" ||
                    shift.employeeId === selectedEmployee
            );
    };
    const hasVisibleShifts = shifts.some((shift) => {
        if (selectedEmployee === "all") {
            return true;
        }

        const employee = employees.find(
            (employee) => employee.employmentId === shift.employmentId
        );

        return employee?.id === selectedEmployee;
    });

    const formatTime = (date) => {
        return new Date(date).toLocaleTimeString("en-US", {
            hour: "numeric",
            minute: "2-digit"
        });
    };

    return (
        <section className="calendar-card">
            <div className="calendar-heading">
                <div>
                    <span className="calendar-label">EMPLOYEE SCHEDULE</span>
                    <h2>Manager Calendar</h2>
                </div>
            </div>

            {error && <p className="calendar-error">{error}</p>}

            <div className="calendar-employee-filter">
                <label htmlFor="employee-filter">Select Employee: </label>

                <select
                    id="employee-filter"
                    value={selectedEmployee}
                    onChange={(e) => setSelectedEmployee(e.target.value)}
                >
                    <option value="all">All Employees</option>

                    {employees.map((employee) => (
                        <option key={employee.id} value={employee.id}>
                            {employee.name}
                        </option>
                    ))}
                </select>
            </div>

            <div className="calendar-navigation">
                <button
                    onClick={() =>
                        setCurrentMonth(new Date(year, month - 1, 1))
                    }
                >
                    Previous
                </button>

                <h2>{monthName}</h2>

                <button
                    onClick={() =>
                        setCurrentMonth(new Date(year, month + 1, 1))
                    }
                >
                    Next
                </button>
            </div>
            {!hasVisibleShifts && (
                <p className="calendar-empty-message">
                    No scheduled shifts for the selected employee this month.
                </p>
            )}
            <div className="calendar-weekdays">
                {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((day) => (
                    <div key={day}>{day}</div>
                ))}
            </div>

            <div className="calendar-grid">
                {calendarDays.map((day, index) => (
                    <div className="calendar-day" key={index}>
                        {day !== null && (
                            <>
                                <span className="calendar-day-number">{day}</span>
                                {getShiftsForDay(day).map((shift) => (
                                    <div
                                        className="calendar-shift-event"
                                        key={shift.id}
                                    >
                                        <strong>{shift.employeeName}</strong>
                                        <span>
                                            Shift: {formatTime(shift.startAt)} – {formatTime(shift.endAt)}
                                        </span>
                                    </div>
                                ))}

                                {getAvailabilityForDay(day).map((entry, i) => (
                                    <div
                                        className="calendar-user-event"
                                        key={`${entry.employeeId}-${day}-${i}`}
                                    >
                                        <strong>{entry.employeeName}</strong>
                                        <span>
                                            Available: {entry.startTime} – {entry.endTime}
                                        </span>
                                    </div>
                                ))}
                            </>
                        )}
                    </div>
                ))}
            </div>

        </section>
    );
}

export default ManagerCalendar;
