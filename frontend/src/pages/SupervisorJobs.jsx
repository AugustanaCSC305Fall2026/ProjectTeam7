import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./SupervisorJobs.css";

function SupervisorJobs() {
  const [jobs, setJobs] = useState([]);
  const [managers, setManagers] = useState([]);
  const [managerForm, setManagerForm] = useState({
    jobId: "",
    managerId: "",
  });
  const [createManagerForm, setCreateManagerForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });
  const [assigning, setAssigning] = useState(false);
  const [creatingManager, setCreatingManager] = useState(false);
  const [form, setForm] = useState({
    title: "",
    department: "",
    description: "",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
  const loadData = async () => {
    try {
      const [jobsResponse, usersResponse] = await Promise.all([
        api.get("/admin/jobs"),
        api.get("/admin/users"),
      ]);

      setJobs(jobsResponse.data);

      const managerUsers = usersResponse.data.filter((user) =>
        user.roles.includes("MANAGER")
      );

      setManagers(managerUsers);
    } catch (err) {
      const status = err.response?.status;

      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to manage jobs.");
      } else {
        setError("Unable to load jobs and managers. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  loadData();
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
    setSaving(true);

    try {
      const response = await api.post("/admin/jobs", {
        title: form.title,
        department: form.department,
        description: form.description,
      });

      setJobs((current) => [...current, response.data]);

      setMessage(`${response.data.title} was created successfully.`);

      setForm({
        title: "",
        department: "",
        description: "",
      });
    } catch (err) {
      const status = err.response?.status;

      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to create jobs.");
      } else {
        setError("Unable to create the job. Please try again.");
      }
    } finally {
      setSaving(false);
    }
  };
  const handleManagerChange = (event) => {
  setManagerForm((current) => ({
    ...current,
    [event.target.name]: event.target.value,
  }));
};

  const handleCreateManagerChange = (event) => {
    setCreateManagerForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }));
  };

  const handleCreateManager = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");
    setCreatingManager(true);

    try {
      const response = await api.post("/admin/managers", createManagerForm);
      setManagers((current) => [...current, response.data]);
      setCreateManagerForm({ firstName: "", lastName: "", email: "", password: "" });
      setMessage(`${response.data.firstName} ${response.data.lastName} was created as a manager.`);
    } catch (err) {
      const status = err.response?.status;
      const code = err.response?.data?.code;

      if (status === 409 || code === "EMAIL_ALREADY_REGISTERED") {
        setError("That email address is already registered.");
      } else if (status === 400) {
        setError("Enter a first name, last name, valid email, and password of at least 8 characters.");
      } else if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to create managers.");
      } else {
        setError("Unable to create the manager account. Please try again.");
      }
    } finally {
      setCreatingManager(false);
    }
  };

  const handleManagerAssignment = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");
    setAssigning(true);

    try {
      const response = await api.post("/admin/jobs/manager", {
        jobId: managerForm.jobId,
        managerId: managerForm.managerId,
      });

      setJobs((current) =>
        current.map((job) =>
          job.id === response.data.id ? response.data : job
        )
      );

      const manager = managers.find(
        (user) => user.id === managerForm.managerId
      );

      setMessage(
        `${manager?.firstName || "Manager"} was assigned to ${response.data.title}.`
      );

      setManagerForm({
        jobId: "",
        managerId: "",
      });
    } catch (err) {
      const status = err.response?.status;

      if (status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (status === 403) {
        setError("Your account is not authorized to assign managers.");
      } else {
        setError("Unable to assign the manager. Please try again.");
      }
    } finally {
      setAssigning(false);
    }
  };

  return (
  <section className="supervisor-jobs">
    <div className="supervisor-jobs__heading">
      <h2>Jobs</h2>
      <p>Create and manage campus jobs.</p>
    </div>

    {error && <p className="job-error" role="alert">{error}</p>}
    {message && <p className="job-message" role="status">{message}</p>}
    <h3>Create New Job</h3>

    <form className="job-form" onSubmit={handleSubmit}>
      <label>
        Job title
        <input
          type="text"
          name="title"
          value={form.title}
          onChange={updateField}
          placeholder="Chemistry Lab Assistant"
          required
        />
      </label>

      <label>
        Department
        <input
          type="text"
          name="department"
          value={form.department}
          onChange={updateField}
          placeholder="Chemistry"
          required
        />
      </label>

      <label>
        Description
        <textarea
          name="description"
          value={form.description}
          onChange={updateField}
          placeholder="Enter a description of the job"
          required
        />
      </label>

      <button type="submit" disabled={saving}>
        {saving ? "Creating..." : "Create job"}
      </button>
    </form>

    <h3>Assign Manager</h3>

    <form className="job-form" onSubmit={handleManagerAssignment}>
      <label>
        Job
        <select
          name="jobId"
          value={managerForm.jobId}
          onChange={handleManagerChange}
          required
        >
          <option value="">Select a job</option>
          {jobs.map((job) => (
            <option key={job.id} value={job.id}>
              {job.title}
            </option>
          ))}
        </select>
      </label>

      <label>
        Manager
        <select
          name="managerId"
          value={managerForm.managerId}
          onChange={handleManagerChange}
          required
        >
          <option value="">Select a manager</option>
          {managers.map((manager) => (
            <option key={manager.id} value={manager.id}>
              {manager.firstName} {manager.lastName}
            </option>
          ))}
        </select>
      </label>

      <button type="submit" disabled={assigning}>
        {assigning ? "Assigning..." : "Assign Manager"}
      </button>
    </form>

    <h3>Create Manager Account</h3>

    <form className="job-form" onSubmit={handleCreateManager}>
      <label>
        First name
        <input name="firstName" value={createManagerForm.firstName}
          onChange={handleCreateManagerChange} required />
      </label>
      <label>
        Last name
        <input name="lastName" value={createManagerForm.lastName}
          onChange={handleCreateManagerChange} required />
      </label>
      <label>
        Email
        <input type="email" name="email" value={createManagerForm.email}
          onChange={handleCreateManagerChange} required />
      </label>
      <label>
        Temporary password
        <input type="password" name="password" minLength="8"
          value={createManagerForm.password} onChange={handleCreateManagerChange} required />
      </label>
      <button type="submit" disabled={creatingManager}>
        {creatingManager ? "Creating..." : "Create manager"}
      </button>
    </form>

    <h3>Current Jobs</h3>

      {loading ? (
        <p>Loading jobs...</p>
      ) : jobs.length === 0 ? (
        <p>No jobs have been created yet.</p>
      ) : (
        <div className="current-jobs">
          {jobs.map((job) => {
            const manager = managers.find(
              (manager) => manager.id === job.managerId
            );

            return (
              <div className="job-card" key={job.id}>
                <h4>{job.title}</h4>
                <p>{job.department}</p>
                <p>{job.description}</p>
                <p>
                  <strong>Manager: </strong>
                  {manager
                    ? `${manager.firstName} ${manager.lastName}`
                    : "Not assigned"}
                </p>
              </div>
            );
          })}
        </div>
      )}
  </section>
);
}

export default SupervisorJobs;
