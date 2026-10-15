import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./Home.css";

function Home() {
  const navigate = useNavigate();

  const [employments, setEmployments] = useState([]);
  const [employmentId, setEmploymentId] = useState("");
  const [upcomingShifts, setUpcomingShifts] = useState([]);
  const [totalMinutes, setTotalMinutes] = useState(0);
  const [message, setMessage] = useState("");
  const [isClockedIn, setIsClockedIn] = useState(false);
  const [isOnBreak, setIsOnBreak] = useState(false);
  const [clockedInAt, setClockedInAt] = useState(null);

  useEffect(() => {
    loadEmployments();
    loadUpcomingShifts();
    loadHours();
  }, []);

  const loadEmployments = async () => {
    try {
        const response = await api.get("/employments/me");
        const employmentData = response.data;

        setEmployments(employmentData);

        if (employmentData.length > 0) {
        setEmploymentId((currentId) => {
            if (
            currentId &&
            employmentData.some(
                (employment) => employment.employmentId === currentId
            )
            ) {
            return currentId;
            }

            return employmentData[0].employmentId;
        });
        }
    } catch (error) {
        console.error(error);
        setMessage("Could not load your jobs.");
    }
  };

  const loadUpcomingShifts = async () => {
    try {
      const response = await api.get("/shifts/upcoming");
      setUpcomingShifts(response.data);
    } catch (error) {
      console.error(error);
      setMessage("Could not load upcoming shifts.");
    }
  };

  const loadHours = async () => {
    try {
        const response = await api.get("/hours");

        setTotalMinutes(response.data.totalMinutes);

        const openEntry = response.data.entries.find(
        (entry) => entry.clockOutAt === null
        );

        if (openEntry) {
        setIsClockedIn(true);
        setIsOnBreak(Boolean(openEntry.onBreak));
        setClockedInAt(openEntry.clockInAt);
        setEmploymentId(openEntry.employmentId);
        } else {
        setIsClockedIn(false);
        setIsOnBreak(false);
        setClockedInAt(null);
        }
    } catch (error) {
        console.error(error);
        setMessage("Could not load your hours.");
    }
   };

  const handleClockIn = async () => {
    if (!employmentId) {
        setMessage("Please select a job before clocking in.");
        return;
    }

    try {
        setMessage("");

        const response = await api.post("/me/time-entries/clock-in", {
            employmentId: employmentId,
            shiftId: null
        });

        setIsClockedIn(true);
        setClockedInAt(response.data.clockInAt);
        setMessage("Clocked in successfully.");
    } catch (error) {
        console.error(error);

        if (error.response?.status === 409) {
        setMessage("You are already clocked in.");
        } else {
        setMessage("Could not clock in.");
        }
    }
  };

    const handleStartBreak = async () => {
    try {
      setMessage("");
      const response = await api.post("/me/time-entries/break/start");
      setIsOnBreak(Boolean(response.data.onBreak));
      setMessage("Break started.");
    } catch (error) {
      console.error(error);
      setMessage(
        error.response?.status === 409
          ? "You cannot start a break in your current clock state."
          : "Could not start break."
      );
    }
  };

  const handleEndBreak = async () => {
    try {
      setMessage("");
      const response = await api.post("/me/time-entries/break/end");
      setIsOnBreak(Boolean(response.data.onBreak));
      setMessage("Break ended.");
    } catch (error) {
      console.error(error);
      setMessage(
        error.response?.status === 409
          ? "You are not currently on a break."
          : "Could not end break."
      );
    }
  };
  const handleClockOut = async () => {
    try {
        setMessage("");

        await api.post("/me/time-entries/clock-out");

        setIsClockedIn(false);
        setClockedInAt(null);
        setMessage("Clocked out successfully.");

        await loadHours();
    } catch (error) {
        console.error(error);

        if (error.response?.status === 409) {
        setMessage("You are not currently clocked in.");
        } else {
        setMessage("Could not clock out.");
        }
        }
  };

  const formatHours = (minutes) => {
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    return `${hours}h ${remainingMinutes}m`;
  };

  const formatShiftDate = (date) => {
    return new Date(date).toLocaleDateString([], {
      weekday: "short",
      month: "short",
      day: "numeric"
    });
  };

  const formatShiftTime = (date) => {
    return new Date(date).toLocaleTimeString([], {
      hour: "numeric",
      minute: "2-digit"
    });
  };

  return (
    <StudentLayout
      title="Dashboard"
      subtitle="Here's your work overview for today."
    >
      {message && <p className="dashboard-message">{message}</p>}

      <div className="dashboard-grid">
        <section className="dashboard-card time-clock-card">
          <div className="card-heading">
            <span>TIME CLOCK</span>
            <span>{new Date().toLocaleDateString()}</span>
          </div>

          <div className="clock-job">
            <label htmlFor="clock-job">Working at:</label>

            <select
              id="clock-job"
              value={employmentId}
              onChange={(event) => setEmploymentId(event.target.value)}
              disabled={isClockedIn}
            >
              {employments.length === 0 && (
                <option value="">No jobs available</option>
              )}

              {employments.map((employment) => (
                <option
                  key={employment.employmentId}
                  value={employment.employmentId}
                >
                  {employment.jobTitle}
                </option>
              ))}
            </select>
          </div>

            <div className="clock-status">
          <h2>
            {isOnBreak
              ? "On Break"
              : isClockedIn
                ? "Currently Clocked In"
                : "Currently Clocked Out"}
          </h2>

          {isClockedIn ? (
            isOnBreak ? (
              <p>Your break is in progress.</p>
            ) : (
              <p>
                Clocked in at{" "}
                {clockedInAt
                  ? new Date(clockedInAt).toLocaleTimeString([], {
                      hour: "numeric",
                      minute: "2-digit"
                    })
                  : ""}
              </p>
            )
          ) : (
            <p>Select your job and start your work session.</p>
          )}
        </div>

        {isClockedIn ? (
          <div className="clock-actions">
            {isOnBreak ? (
              <button className="primary-button" onClick={handleEndBreak}>
                End Break
              </button>
            ) : (
              <button className="primary-button" onClick={handleStartBreak}>
                Start Break
              </button>
            )}

            <button
              className="clock-out-button"
              onClick={handleClockOut}
              disabled={isOnBreak}
            >
              Clock Out
            </button>
          </div>
        ) : (
          <button
            className="primary-button"
            onClick={handleClockIn}
            disabled={!employmentId}
          >
            Clock In
          </button>
        )}
        </section>

        <section className="dashboard-card hours-summary-card">
          <span className="card-label">TOTAL HOURS</span>

          <div className="hours-summary">
            <strong>{formatHours(totalMinutes)}</strong>
            <span>hours worked</span>
          </div>

          <button
            className="text-button"
            onClick={() => navigate("/hours")}
          >
            View hours
          </button>
        </section>

        <section className="dashboard-card upcoming-card">
          <div className="section-heading">
            <div>
              <span className="card-label">UPCOMING SHIFTS</span>
              <h2>Your Schedule</h2>
            </div>

            <button
              className="text-button"
              onClick={() => navigate("/shifts")}
            >
              View all
            </button>
          </div>

          {upcomingShifts.length === 0 ? (
            <div className="empty-dashboard-state">
              No upcoming shifts.
            </div>
          ) : (
            <div className="upcoming-shift-list">
              {upcomingShifts.slice(0, 4).map((shift) => (
                <div className="upcoming-shift" key={shift.id}>
                  <div>
                    <strong>
                      {shift.jobTitle || "Scheduled Shift"}
                    </strong>

                    <span>
                      {formatShiftDate(shift.startAt)}
                    </span>
                  </div>

                  <span>
                    {formatShiftTime(shift.startAt)}
                    {" - "}
                    {formatShiftTime(shift.endAt)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="dashboard-card shift-card-dashboard">
          <span className="card-label">SHIFT MANAGEMENT</span>

          <h2>Manage Your Shifts</h2>

          <p>
            Offer shifts you cannot work or browse shifts that are
            available to pick up.
          </p>

          <button
            className="secondary-button"
            onClick={() => navigate("/shifts")}
          >
            Manage shifts
          </button>
        </section>
      </div>
    </StudentLayout>
  );
}

export default Home;