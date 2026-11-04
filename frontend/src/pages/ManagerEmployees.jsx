import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./ManagerEmployees.css";

function ManagerEmployees() {
  const [employees, setEmployees] = useState([]);
  const [form, setForm] = useState({ userId: "", hourlyRate: "" });
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [removingId, setRemovingId] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function loadEmployees() {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/manager/employees");
      setEmployees(response.data);
    } catch (err) {
      const status = err.response?.status;
      setError(
        status === 401
          ? "Your session has expired. Please log in again."
          : status === 403
            ? "Your account is not authorized to manage employees."
            : "Unable to load employees."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadEmployees();
  }, []);

  function updateField(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }));
  }

  async function handleAdd(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    const hourlyRate = Number(form.hourlyRate);
    if (!Number.isFinite(hourlyRate) || hourlyRate <= 0) {
      setError("Enter a valid hourly rate greater than zero.");
      return;
    }

    setAdding(true);
    try {
      await api.post("/manager/employees", {
        userId: form.userId.trim(),
        payRateCents: Math.round(hourlyRate * 100),
      });

      setMessage("Employee added successfully.");
      setForm({ userId: "", hourlyRate: "" });
      await loadEmployees();
    } catch (err) {
      const status = err.response?.status;
      const code = err.response?.data?.code || err.response?.data?.error;
      const errors = {
        EMPLOYMENT_ALREADY_ACTIVE: "This employee is already on your roster.",
        PAY_RATE_MUST_BE_POSITIVE: "Enter a valid hourly rate greater than zero.",
        USER_NOT_FOUND: "That User ID was not found.",
        USER_IS_NOT_AN_EMPLOYEE: "That account is not an employee account.",
      };

      setError(
        status === 401
          ? "Your session has expired. Please log in again."
          : status === 403
            ? "Your account is not authorized to add employees."
            : errors[code] || "Unable to add this employee."
      );
    } finally {
      setAdding(false);
    }
  }

  async function handleRemove(employee) {
    if (!window.confirm(`Deactivate ${employee.name} from your roster?`)) {
      return;
    }

    setError("");
    setMessage("");
    setRemovingId(employee.employmentId);

    try {
      await api.delete(`/manager/employees/${employee.employmentId}`);
      setMessage(`${employee.name} was removed from your active roster.`);
      await loadEmployees();
    } catch {
      setError("Unable to remove this employee.");
    } finally {
      setRemovingId("");
    }
  }

  return (
    <section className="manager-employees">
      <h2>Employees</h2>
      <p className="manager-employees__intro">
        Manage employees assigned to your job.
      </p>

      {message && <p className="manager-employees__message" role="status">{message}</p>}
      {error && <p className="manager-employees__error" role="alert">{error}</p>}

      <form className="manager-employee-form" onSubmit={handleAdd}>
        <label>
          Employee User ID
          <input
            name="userId"
            value={form.userId}
            onChange={updateField}
            required
          />
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

        <button type="submit" disabled={adding}>
          {adding ? "Adding..." : "Add Employee"}
        </button>
      </form>

      <section className="manager-employee-list">
        <h3>Active roster</h3>

        {loading ? (
          <p>Loading employees...</p>
        ) : employees.length === 0 ? (
          <p>No active employees are assigned to your job.</p>
        ) : (
          employees.map((employee) => (
            <article className="manager-employee-card" key={employee.employmentId}>
              <div>
                <h4>{employee.name}</h4>
                <p>{employee.email}</p>
              </div>
              <button
                type="button"
                onClick={() => handleRemove(employee)}
                disabled={removingId !== ""}
              >
                {removingId === employee.employmentId
                  ? "Removing..."
                  : "Deactivate"}
              </button>
            </article>
          ))
        )}
      </section>
    </section>
  );
}

export default ManagerEmployees;