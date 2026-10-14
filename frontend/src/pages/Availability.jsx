import { useEffect, useState } from "react";
import api from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./Availability.css";

const days = [
  ["MONDAY", "Monday"],
  ["TUESDAY", "Tuesday"],
  ["WEDNESDAY", "Wednesday"],
  ["THURSDAY", "Thursday"],
  ["FRIDAY", "Friday"],
  ["SATURDAY", "Saturday"],
  ["SUNDAY", "Sunday"],
];

const blankWindow = () => ({
  dayOfWeek: "MONDAY",
  startTime: "09:00",
  endTime: "17:00",
});

function Availability() {
  const [windows, setWindows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const loadAvailability = async () => {
      try {
        const response = await api.get("/availability/me");
        setWindows(response.data.windows || []);
      } catch (requestError) {
        console.error(requestError);
        setError("Unable to load your availability. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    loadAvailability();
  }, []);

  const updateWindow = (index, field, value) => {
    setWindows((currentWindows) => currentWindows.map((window, windowIndex) =>
      windowIndex === index ? { ...window, [field]: value } : window
    ));
    setMessage("");
    setError("");
  };

  const addWindow = () => {
    setWindows((currentWindows) => [...currentWindows, blankWindow()]);
    setMessage("");
    setError("");
  };

  const removeWindow = (index) => {
    setWindows((currentWindows) => currentWindows.filter((_, windowIndex) => windowIndex !== index));
    setMessage("");
    setError("");
  };

  const saveAvailability = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");

    const invalidWindow = windows.find((window) => !window.dayOfWeek ||
      !window.startTime || !window.endTime || window.endTime <= window.startTime);

    if (invalidWindow) {
      setError("Each availability window needs a day and an end time after its start time.");
      return;
    }

    setSaving(true);
    try {
      const response = await api.post("/availability", { windows });
      setWindows(response.data.windows || windows);
      setMessage("Your availability was saved successfully.");
    } catch (requestError) {
      console.error(requestError);
      setError(requestError.response?.data?.message || "Unable to save your availability. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <StudentLayout
      title="Availability"
      subtitle="Tell your supervisor when you are available to work."
    >
      <section className="availability-form-card">
        <div className="availability-heading">
          <div>
            <span className="availability-label">WEEKLY AVAILABILITY</span>
            <h2>Your available times</h2>
            <p>Add each day and time range when you can work.</p>
          </div>
          <span className="availability-count">
            {windows.length} {windows.length === 1 ? "window" : "windows"}
          </span>
        </div>

        {loading ? (
          <div className="availability-empty">Loading your availability…</div>
        ) : (
          <form onSubmit={saveAvailability}>
            {windows.length === 0 ? (
              <div className="availability-empty">
                No availability added yet. Add a time range to get started.
              </div>
            ) : (
              <div className="availability-window-list">
                {windows.map((window, index) => (
                  <div className="availability-window" key={`${window.dayOfWeek}-${index}`}>
                    <label>
                      Day
                      <select
                        value={window.dayOfWeek}
                        onChange={(event) => updateWindow(index, "dayOfWeek", event.target.value)}
                      >
                        {days.map(([value, label]) => (
                          <option value={value} key={value}>{label}</option>
                        ))}
                      </select>
                    </label>

                    <label>
                      Start time
                      <input
                        type="time"
                        value={window.startTime}
                        onChange={(event) => updateWindow(index, "startTime", event.target.value)}
                      />
                    </label>

                    <label>
                      End time
                      <input
                        type="time"
                        value={window.endTime}
                        onChange={(event) => updateWindow(index, "endTime", event.target.value)}
                      />
                    </label>

                    <button
                      type="button"
                      className="availability-remove"
                      onClick={() => removeWindow(index)}
                      aria-label={`Remove ${window.dayOfWeek.toLowerCase()} availability`}
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            )}

            {error && <div className="availability-message availability-error">{error}</div>}
            {message && <div className="availability-message availability-success">{message}</div>}

            <div className="availability-actions">
              <button type="button" className="availability-add" onClick={addWindow}>
                + Add availability
              </button>
              <button type="submit" className="availability-save" disabled={saving}>
                {saving ? "Saving…" : "Save availability"}
              </button>
            </div>
          </form>
        )}
      </section>
    </StudentLayout>
  );
}

export default Availability;
