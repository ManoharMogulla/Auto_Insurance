import { useState } from "react";
import { saveCustomerSession } from "../api/insuranceServices.js";
import "./Registration.css";

const initialForm = {
  firstName: "",
  lastName: "",
  email: "",
  dateOfBirth: "",
  licenseNumber: "",
};

export default function InsuranceRegistration() {
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    saveCustomerSession(form, form.email, false);
    window.location.assign("/login");
  };

  return (
    <main className="registration-page">
      <div className="registration-shell">
        <aside className="registration-aside">
          <div>
            <div className="registration-brand">
              <span aria-hidden="true">+</span>
              <span className="registration-brand-copy">
                <strong>LTM</strong>
                <small>INSURANCE PORTAL</small>
              </span>
            </div>
            <p className="registration-kicker">DRIVER PROFILE</p>
            <h1>Start with your details.</h1>
            <p className="registration-intro">
              Add the driver details used by the policy service.
            </p>
          </div>
          <p className="registration-trust"><span aria-hidden="true" /> Your details stay protected</p>
        </aside>

        <section className="registration-content" aria-labelledby="registration-title">
          <div className="registration-heading">
            <p className="registration-label">DRIVER</p>
            <h2 id="registration-title">Your driver profile</h2>
            <p>The customer service assigns a member ID when a policy is issued.</p>
          </div>

          <form onSubmit={handleSubmit} className="registration-form">
            <div className="registration-grid">
              <Field label="First name" name="firstName" value={form.firstName} onChange={handleChange} autoComplete="given-name" />
              <Field label="Last name" name="lastName" value={form.lastName} onChange={handleChange} autoComplete="family-name" />
            </div>

            <Field label="Email address" name="email" type="email" value={form.email} onChange={handleChange} autoComplete="email" />
            <div className="registration-grid">
              <Field label="Date of birth" name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={handleChange} />
              <Field label="License number" name="licenseNumber" value={form.licenseNumber} onChange={handleChange} />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="registration-submit"
            >
              {loading ? "Continuing..." : "Continue to policy"}
            </button>
          </form>
        </section>
      </div>
    </main>
  );
}

function Field({ label, name, type = "text", value, onChange, minLength, autoComplete }) {
  return (
    <label className="registration-field">
      <span className="registration-label-row">
        <span>{label}</span>
        <span className="registration-required">Required</span>
      </span>
      <input
        className="registration-input"
        id={name}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        minLength={minLength}
        autoComplete={autoComplete}
        required
      />
    </label>
  );
}