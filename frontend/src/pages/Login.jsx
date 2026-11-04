import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import api from '../api/backendApi.js';
import './Login.css';
import logo from '../assets/augustana-logo.png';


/*
 * A simple login page to help you get started with authenticating
 * users.
 */
function Login() {
    const navigate = useNavigate();
    const location = useLocation();
  // This component needs to track state, namely the user's credentials
  // and a possible error message.
  //
  // To declare UI state in react, call the `useState` function. This
  // function takes as input the initial state, and outputs both an
  // object with that state and setter. For example, saying:
  //
  //   [isCold, setCold] = useState(true)
  //
  // gives us a state `isCold` and a function `setCold`. The value of
  // `isCold` is initially `true`, but will change if `setCold` is
  // called. Crucially, whenever `setCold` is called, any UI
  // componenent that depends on the value of `isCold` will
  // automatically be re-rendered.
  //
  // Here we set up state for user credentials and an error message:
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [error, setError] = useState('');

  // This is a function that gets called whenever the email or password
  // text field is updated by the user.
  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  // This is a function that gets called when the login button is clicked.
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post("auth/login", credentials);
      const { token, user } = response.data;

      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(user));

      let destination = "/";

      if (user?.roles?.includes("ADMIN")) {
        destination = "/supervisor";
      } else if (user?.roles?.includes("MANAGER")) {
        destination = "/manager";
      }

      navigate(destination, { replace: true });
    } catch {
      // On failure, set an error message.
      setError('Invalid email or password.');
    }
  };

  // We are now ready to construct and return the login page HTML.
  return (
    <div className="auth-page">
      <main className="auth-card" id="loginPage">
        <div className="auth-brand">
          <img src={logo} alt="Augustana College" />
          <div>
            <strong>cLockedIn</strong>
            <span>Campus work</span>
          </div>
        </div>

        <div className="auth-heading">
          <p className="auth-eyebrow">STUDENT WORK PORTAL</p>
          <h1>Welcome back</h1>
          <p>Sign in to manage your campus work.</p>
        </div>

        {location.state?.message && <p className="success">{location.state.message}</p>}
        <form onSubmit={handleSubmit}>
          <div className="input">
            <label htmlFor="email">Email</label>
            <input type="email" id="email" name="email" value={credentials.email}
              onChange={handleChange} className="credentialField" autoComplete="email" required />
          </div>
          <div className="input">
            <label htmlFor="password">Password</label>
            <input type="password" id="password" name="password" value={credentials.password}
              onChange={handleChange} className="credentialField" autoComplete="current-password" required />
          </div>
          {error && <p className="error">{error}</p>}
          <button type="submit" className="submit">Sign in</button>
        </form>
        <p className="auth-switch">New to cLockedIn? <Link to="/create-account">Create an account</Link></p>
      </main>
    </div>
  );
};

export default Login;
