import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/backendApi.js";
import logo from "../assets/augustana-logo.png";
import "./Login.css";

function CreateAccount() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (form.password.length < 8) {
      setError("Your password must be at least 8 characters.");
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError("The passwords do not match.");
      return;
    }

    setSaving(true);
    try {
      await api.post("auth/register", {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      });
      navigate("/login", {
        replace: true,
        state: { message: "Your account was created. Sign in to continue." },
      });
    } catch (err) {
      if (err.response?.status === 409) {
        setError("That email is already registered.");
      } else if (err.response?.status === 400) {
        setError("Please check your information and try again.");
      } else {
        setError("We could not create your account. Please try again.");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="auth-page">
      <main className="auth-card" aria-labelledby="create-account-heading">
        <div className="auth-brand">
          <img src={logo} alt="Augustana College" />
          <div>
            <strong>cLockedIn</strong>
            <span>Campus work</span>
          </div>
        </div>

        <div className="auth-heading">
          <p className="auth-eyebrow">STUDENT WORK PORTAL</p>
          <h1 id="create-account-heading">Create your account</h1>
          <p>Set up your student account to get started.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="auth-form-row">
            <div className="input">
              <label htmlFor="firstName">First name</label>
              <input className="credentialField" id="firstName" name="firstName"
                value={form.firstName} onChange={handleChange} autoComplete="given-name" required />
            </div>
            <div className="input">
              <label htmlFor="lastName">Last name</label>
              <input className="credentialField" id="lastName" name="lastName"
                value={form.lastName} onChange={handleChange} autoComplete="family-name" required />
            </div>
          </div>
          <div className="input">
            <label htmlFor="email">Email</label>
            <input className="credentialField" type="email" id="email" name="email"
              value={form.email} onChange={handleChange} autoComplete="email" required />
          </div>
          <div className="input">
            <label htmlFor="password">Password</label>
            <input className="credentialField" type="password" id="password" name="password"
              value={form.password} onChange={handleChange} autoComplete="new-password" required />
          </div>
          <div className="input">
            <label htmlFor="confirmPassword">Confirm password</label>
            <input className="credentialField" type="password" id="confirmPassword" name="confirmPassword"
              value={form.confirmPassword} onChange={handleChange} autoComplete="new-password" required />
          </div>
          {error && <p className="error">{error}</p>}
          <button type="submit" className="submit" disabled={saving}>
            {saving ? "Creating account..." : "Create account"}
          </button>
        </form>
        <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
      </main>
    </div>
  );
}

export default CreateAccount;
