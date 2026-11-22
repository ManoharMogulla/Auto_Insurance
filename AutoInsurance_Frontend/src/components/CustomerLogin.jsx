import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/authContext.js";
import "../styles.css/Registration.css";

export default function CustomerLogin() {
  const { authenticated, login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  if (authenticated) return <Navigate to="/customer-summary" replace />;

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    try {
      await login(form);
      navigate("/customer-summary", { replace: true });
    } catch (error) {
      setMessage(error.message || "Unable to sign in.");
    } finally {
      setLoading(false);
    }
  };

  const change = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }));

  return (
    <main className="registration-page">
      <div className="registration-shell">
        <aside className="registration-aside">
          <div>
            <a className="registration-brand" href="/" aria-label="LTM home">
              <span aria-hidden="true">+</span>
              <span className="registration-brand-copy"><strong>LTM INSURANCE</strong><small>INSURANCE PORTAL</small></span>
            </a>
            <p className="registration-kicker">INSURANCE, MADE PERSONAL</p>
            <h1>Your cover, all in one place.</h1>
            <p className="registration-intro">Sign in to manage your details and policies.</p>
          </div>
          <p className="registration-trust"><span aria-hidden="true" /> Your details stay protected</p>
        </aside>
        <section className="registration-content" aria-labelledby="login-title">
          <div className="registration-heading">
            <p className="registration-label">WELCOME BACK</p>
            <h2 id="login-title">Customer login</h2>
            <p>Enter your account credentials to continue.</p>
          </div>
          {message && <div className="registration-message" role="alert">{message}</div>}
          <form onSubmit={handleSubmit} className="registration-form">
            <Field label="Email address" name="email" type="email" value={form.email} onChange={change} autoComplete="email" />
            <Field label="Password" name="password" type="password" value={form.password} onChange={change} autoComplete="current-password" />
            <button type="submit" disabled={loading} className="registration-submit">
              {loading ? "Signing in..." : "Sign in"}
            </button>
          </form>
          <p className="registration-login">New to LTM Insurance? <Link to="/register">Create an account</Link></p>
        </section>
      </div>
    </main>
  );
}

function Field({ label, name, type, value, onChange, autoComplete }) {
  return (
    <label className="registration-field" htmlFor={`customer-${name}`}>
      <span className="registration-label-row"><span>{label}</span><span className="registration-required">Required</span></span>
      <input className="registration-input" id={`customer-${name}`} name={name} type={type} value={value}
        onChange={onChange} autoComplete={autoComplete} required />
    </label>
  );
}
