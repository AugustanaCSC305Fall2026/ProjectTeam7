import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./SupervisorAssignments.css";

function SupervisorAssignments() {
  const [users, setUsers] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [form, setForm] = useState({
    userId: "",
    jobId: "",
    hourlyRate: "",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const loadOptions = async () => {
      try {
        const [usersResponse, jobsResponse] = await Promise.all([
          api.get("/admin/users"),
          api.get("/admin/jobs"),
        ]);
        setUsers(usersResponse.data.filter((user) => !user.roles?.includes("ADMIN")));
        setJobs(jobsResponse.data);
      } catch (err) {
        const status = err.response?.status;
        setError(
          status === 403
            ? "Your account is not authorized to manage job assignments."
            : "Unable to load students and jobs. Please try again."
        );
      } finally {
        setLoading(false);
      }
    };

    loadOptions();
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

    const hourlyRate = Number(form.hourlyRate);
    if (!Number.isFinite(hourlyRate) || hourlyRate <= 0) {
      setError("Enter a valid hourly rate greater than zero.");
      return;
    }

    setSaving(true);
    try {
      await api.post("/admin/employments", {
        userId: form.userId,
        jobId: form.jobId,
        payRateCents: Math.round(hourlyRate * 100),
      });

      const student = users.find((user) => user.id === form.userId);
      const job = jobs.find((item) => item.id === form.jobId);
      setMessage(`${student?.firstName} ${student?.lastName} was assigned to ${job?.title}.`);
      setForm({ userId: "", jobId: "", hourlyRate: "" });
    } catch (err) {
      const status = err.response?.status;
      const code = err.response?.data?.code;
      const errors = {
        EMPLOYMENT_ALREADY_ACTIVE: "This student is already assigned to that job.",
        USER_NOT_FOUND: "That student could not be found.",
        JOB_NOT_FOUND: "That job could not be found.",
        PAY_RATE_MUST_BE_POSITIVE: "Enter a valid hourly rate greater than zero.",
      };

      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to manage job assignments.");
      } else {
        setError(errors[code] || "Unable to create the job assignment.");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="supervisor-assignments">
      <div className="supervisor-assignments__heading">
        <div>
          <h2>Job Assignments</h2>
          <p>Connect a student to a campus job and set their hourly rate.</p>
        </div>
      </div>

      {message && <p className="assignment-message" role="status">{message}</p>}
      {error && <p className="assignment-error" role="alert">{error}</p>}

      {loading ? (
        <p className="assignment-muted">Loading students and jobs...</p>
      ) : (
        <form className="assignment-form" onSubmit={handleSubmit}>
          <label>
            Student
            <select name="userId" value={form.userId} onChange={updateField} required>
              <option value="">Select a student</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.firstName} {user.lastName} · {user.email}
                </option>
              ))}
            </select>
          </label>

          <label>
            Job
            <select name="jobId" value={form.jobId} onChange={updateField} required>
              <option value="">Select a job</option>
              {jobs.map((job) => (
                <option key={job.id} value={job.id}>
                  {job.title}{job.department ? ` · ${job.department}` : ""}
                </option>
              ))}
            </select>
          </label>

          <label>
            Hourly rate ($)
            <input
              type="number"
              name="hourlyRate"
              value={form.hourlyRate}
              onChange={updateField}
              min="0.01"
              step="0.01"
              placeholder="15.00"
              required
            />
          </label>

          <button type="submit" disabled={saving || !users.length || !jobs.length}>
            {saving ? "Assigning..." : "Assign job"}
          </button>
        </form>
      )}

      {!loading && (!users.length || !jobs.length) && (
        <p className="assignment-muted">
          Add at least one student and one job before creating an assignment.
        </p>
      )}
    </section>
  );
}

export default SupervisorAssignments;
