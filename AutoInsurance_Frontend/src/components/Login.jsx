import { useState } from "react";
import { loginCustomer, saveCustomerSession } from "../api/insuranceServices.js";
import "./Registration.css";

export default function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const customer = await loginCustomer(form);
      if (customer?.success === false || customer?.authenticated === false) {
        throw new Error(customer.message || "The email or password was not accepted.");
      }
      saveCustomerSession(customer, form.email);
      window.location.assign("/summary");
    } catch (error) {
      setMessage(error.message || "Unable to reach the login service.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="registration-page">
      <div className="registration-shell">
        <aside className="registration-aside">
          <div>
            <a className="registration-brand" href="/" aria-label="LTM home">
              <span aria-hidden="true">+</span>
              <span className="registration-brand-copy">
                <strong>LTM</strong>
                <small>INSURANCE PORTAL</small>
              </span>
            </a>
            <p className="registration-kicker">INSURANCE, MADE PERSONAL</p>
            <h1>Your cover, all in one place.</h1>
            <p className="registration-intro">
              Sign in to manage your customer details and stay up to date with your coverage.
            </p>
          </div>
          <p className="registration-trust"><span aria-hidden="true" /> Your details stay protected</p>
        </aside>

        <section className="registration-content" aria-labelledby="login-title">
          <div className="registration-heading">
            <p className="registration-label">WELCOME BACK</p>
            <h2 id="login-title">Sign in to LTM </h2>
            <p>Enter your account details to continue.</p>
          </div>

          {message && (
            <div className="registration-message" role="status">{message}</div>
          )}

          <form onSubmit={handleSubmit} className="registration-form">
            <label className="registration-field" htmlFor="login-email">
              <span className="registration-label-row">
                <span>Email address</span>
                <span className="registration-required">Required</span>
              </span>
              <input
                className="registration-input"
                id="login-email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                autoComplete="email"
                required
              />
            </label>

            <label className="registration-field" htmlFor="login-password">
              <span className="registration-label-row">
                <span>Password</span>
                <span className="registration-required">Required</span>
              </span>
              <input
                className="registration-input"
                id="login-password"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
                required
              />
            </label>

            <button type="submit" disabled={loading} className="registration-submit">
              {loading ? "Signing in..." : "Sign in"}
            </button>
          </form>

          <p className="registration-login">
            New to LTM? <a href="/">Create an account</a>
          </p>
        </section>
      </div>
    </main>
  );
}
