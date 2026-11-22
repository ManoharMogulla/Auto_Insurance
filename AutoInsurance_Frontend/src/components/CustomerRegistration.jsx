import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/authContext.js";
import "../styles.css/Registration.css";

const initialForm = {
  firstName: "",
  lastName: "",
  email: "",
  phoneNumber: "",
  password: "",
  confirmPassword: "",
};

export default function CustomerRegistration() {
  const { authenticated, register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  if (authenticated) return <Navigate to="/customer-summary" replace />;

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    if (form.password !== form.confirmPassword) {
      setMessage("Passwords do not match.");
      return;
    }

    setLoading(true);
    try {
      await register(form);
      navigate("/customer-summary", { replace: true });
    } catch (error) {
      setMessage(error.message || "Unable to create your account.");
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
              <span className="registration-brand-copy"><strong>LTM</strong><small>INSURANCE PORTAL</small></span>
            </a>
            <p className="registration-kicker">YOUR COVER STARTS HERE</p>
            <h1>Make room for peace of mind.</h1>
            <p className="registration-intro">Create your customer account to start and manage your policies.</p>
          </div>
          <p className="registration-trust"><span aria-hidden="true" /> Your details stay protected</p>
        </aside>
        <section className="registration-content" aria-labelledby="registration-title">
          <div className="registration-heading">
            <p className="registration-label">NEW CUSTOMER</p>
            <h2 id="registration-title">Create your account</h2>
            <p>Your profile is stored by Customer Service; credentials stay with Authentication Service.</p>
          </div>
          {message && <div className="registration-message" role="alert">{message}</div>}
          <form onSubmit={handleSubmit} className="registration-form">
            <div className="registration-grid">
              <Field label="First name" name="firstName" value={form.firstName} onChange={handleChange} autoComplete="given-name" />
              <Field label="Last name" name="lastName" value={form.lastName} onChange={handleChange} autoComplete="family-name" />
            </div>
            <Field label="Email address" name="email" type="email" value={form.email} onChange={handleChange} autoComplete="email" />
            <Field label="Phone number" name="phoneNumber" type="tel" value={form.phoneNumber} onChange={handleChange} autoComplete="tel" pattern="\+?[0-9]{10,15}" />
            <div className="registration-grid">
              <Field label="Password" name="password" type="password" value={form.password} onChange={handleChange} autoComplete="new-password" minLength={8} />
              <Field label="Confirm password" name="confirmPassword" type="password" value={form.confirmPassword} onChange={handleChange} autoComplete="new-password" minLength={8} />
            </div>
            <button type="submit" disabled={loading} className="registration-submit">
              {loading ? "Creating account..." : "Create account"}
            </button>
          </form>
          <p className="registration-login">Already registered? <Link to="/login">Sign in</Link></p>
        </section>
      </div>
    </main>
  );
}

function Field({ label, name, type = "text", value, onChange, autoComplete, minLength, pattern }) {
  return (
    <label className="registration-field" htmlFor={`register-${name}`}>
      <span className="registration-label-row"><span>{label}</span><span className="registration-required">Required</span></span>
      <input className="registration-input" id={`register-${name}`} name={name} type={type} value={value}
        onChange={onChange} autoComplete={autoComplete} minLength={minLength} pattern={pattern} required />
    </label>
  );
}
