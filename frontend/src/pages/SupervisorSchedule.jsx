import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./SupervisorSchedule.css";

function SupervisorSchedule() {
  const [jobs, setJobs] = useState([]);
  const [jobsLoading, setJobsLoading] = useState(true);
  const [jobsError, setJobsError] = useState("");
  const [form, setForm] = useState({
    jobId: "",
    employmentId: "",
    date: "",
    startTime: "",
    endTime: "",
    note: "",
  });
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

    useEffect(() => {
    const loadJobs = async () => {
      try {
        const response = await api.get("/admin/jobs");
        setJobs(response.data);
      } catch {
        setJobsError("Unable to load jobs. Refresh the page and try again.");
      } finally {
        setJobsLoading(false);
      }
    };

    loadJobs();
  }, []);

  const updateField = (event) => {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");

    const startAt = new Date(`${form.date}T${form.startTime}:00`);
    const endAt = new Date(`${form.date}T${form.endTime}:00`);

    if (Number.isNaN(startAt.getTime()) || Number.isNaN(endAt.getTime())) {
      setError("Enter a valid date and start/end time.");
      return;
    }

    if (endAt <= startAt) {
      setError("The end time must be after the start time.");
      return;
    }

    setSaving(true);

    try {
      await api.post("/shifts", {
        jobId: form.jobId.trim(),
        employmentId: form.employmentId.trim() || null,
        startAt: startAt.toISOString(),
        endAt: endAt.toISOString(),
        note: form.note.trim() || null,
      });

      setMessage("Shift created successfully.");
      setForm((current) => ({ ...current, note: "" }));
    } catch (err) {
      const status = err.response?.status;
      const code = err.response?.data?.error;

      const errorMessages = {
        OVERLAPPING_SHIFT: "This employment already has a shift at that time.",
        INVALID_RANGE: "The end time must be after the start time.",
        INVALID_JOB_ID: "Enter a valid Job ID.",
        JOB_NOT_FOUND: "That Job ID was not found.",
        INVALID_EMPLOYMENT_ID: "Enter a valid Employment ID.",
        EMPLOYMENT_NOT_FOUND: "That Employment ID was not found.",
        INACTIVE_EMPLOYMENT: "That employment is inactive.",
        EMPLOYMENT_JOB_MISMATCH:
          "The selected employment does not match the Job ID.",
      };

      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to create shifts.");
      } else {
        setError(errorMessages[code] || code || "Unable to create the shift.");
      }

      
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="supervisor-schedule">
      <h2>Create Shift</h2>
      <p>Select a job for this shift. Employment ID is optional.</p>

      {message && <p role="status">{message}</p>}
      {error && <p role="alert">{error}</p>}
      {jobsError && <p role="alert">{jobsError}</p>}
      {!jobsLoading && !jobsError && jobs.length === 0 && (
        <p role="status">No jobs are available. Create a job first.</p>
      )}
      <form onSubmit={handleSubmit}>
                <label>
          Job
          <select
            name="jobId"
            value={form.jobId}
            onChange={updateField}
            required
            disabled={jobsLoading || Boolean(jobsError) || jobs.length === 0}
          >
            <option value="">
              {jobsLoading ? "Loading jobs..." : "Select a job"}
            </option>
            {jobs.map((job) => (
              <option key={job.id} value={job.id}>
                {job.title}{job.department ? ` — ${job.department}` : ""}
              </option>
            ))}
          </select>
        </label>

        <label>
          Employment ID (optional)
          <input
            name="employmentId"
            value={form.employmentId}
            onChange={updateField}
          />
        </label>

        <label>
          Date
          <input
            type="date"
            name="date"
            value={form.date}
            onChange={updateField}
            required
          />
        </label>

        <label>
          Start time
          <input
            type="time"
            name="startTime"
            value={form.startTime}
            onChange={updateField}
            required
          />
        </label>

        <label>
          End time
          <input
            type="time"
            name="endTime"
            value={form.endTime}
            onChange={updateField}
            required
          />
        </label>

        <label className="schedule-note-field">
          Note (optional)
          <textarea
            name="note"
            value={form.note}
            onChange={updateField}
          />
        </label>

        <button type="submit" disabled={saving || jobsLoading || Boolean(jobsError) || jobs.length === 0}>
          {saving ? "Creating..." : "Create Shift"}
        </button>
      </form>
    </section>
  );
}

export default SupervisorSchedule;