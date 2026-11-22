import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/authContext.js";
import { FaHome } from "react-icons/fa";
import {
  createClaim,
  getAllPolicies,
  getClaimsByCustomer,
} from "../api/insuranceServices.js";
import "../styles.css/SummaryDashboard.css";

function formatDate(value) {
  if (!value) return "-";

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "-"
    : date.toLocaleDateString("en-IN");
}

function formatAmount(value) {
  const amount = Number(value);

  return Number.isFinite(amount)
    ? `INR ${new Intl.NumberFormat("en-IN", {
        maximumFractionDigits: 2,
      }).format(amount)}`
    : "-";
}

function localDateValue(date = new Date()) {
  const localDate = new Date(
    date.getTime() - date.getTimezoneOffset() * 60000
  );

  return localDate.toISOString().slice(0, 10);
}

function isClaimablePolicy(policy) {
  const status = String(policy?.policyStatus || "")
    .trim()
    .toLowerCase();

  return status === "active";
}

export default function ClaimsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [policies, setPolicies] = useState([]);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(null);

  const [form, setForm] = useState({
    policyId: "",
    claimType: "ACCIDENT",
    incidentDate: "",
    incidentLocation: "",
    description: "",
    estimatedDamageAmount: "",
  });

  const customerId = useMemo(
    () => user?.customerId || user?.id || user?.memberId,
    [user]
  );

  const claimablePolicies = useMemo(
    () => policies.filter(isClaimablePolicy),
    [policies]
  );

  const loadData = useCallback(async () => {
    if (!customerId) {
      setError("Unable to identify logged-in customer.");
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const [policiesResult, claimsResult] = await Promise.all([
        getAllPolicies(),
        getClaimsByCustomer(customerId),
      ]);

      const loadedPolicies = Array.isArray(policiesResult)
        ? policiesResult
        : [];

      setPolicies(loadedPolicies);

      setClaims(
        Array.isArray(claimsResult) ? claimsResult : []
      );

      setForm((current) => {
        if (current.policyId) {
          return current;
        }

        const firstClaimable =
          loadedPolicies.find(isClaimablePolicy);

        return {
          ...current,
          policyId: firstClaimable?.policyId || "",
        };
      });
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to load claims data."
      );
    } finally {
      setLoading(false);
    }
  }, [customerId]);

  useEffect(() => {
    const timer = setTimeout(() => {
      void loadData();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadData]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const submitClaim = async (event) => {
    event.preventDefault();

    setSubmitting(true);
    setError("");
    setSuccess(null);

    try {
      const payload = {
        customerId: Number(customerId),
        policyId: form.policyId,
        claimType: "ACCIDENT",
        incidentDate: form.incidentDate,
        incidentLocation: form.incidentLocation,
        description: form.description,
        estimatedDamageAmount: Number(
          form.estimatedDamageAmount
        ),
      };

      console.log("Submitting claim:", payload);

      const created = await createClaim(payload);

      console.log("Claim created:", created);

      setSuccess({
        claimId: created.claimId,
        status: created.status,
      });

      setForm((current) => ({
        ...current,
        incidentDate: "",
        incidentLocation: "",
        description: "",
        estimatedDamageAmount: "",
      }));

      await loadData();
    } catch (requestError) {
      setError(
        requestError.message ||
          "Unable to submit claim."
      );
    } finally {
      setSubmitting(false);
    }
  };

  const signOut = async () => {
    await logout().catch(() => undefined);

    navigate("/login", {
      replace: true,
    });
  };

  return (
    <main className="customer-dashboard">

      {/* HEADER */}
      <header className="dashboard-topbar">

        {/* Existing + is used as Home */}
        <button
          type="button"
          className="dashboard-brand"
          onClick={() => navigate("/customer-summary")}
          aria-label="LTM customer summary"
          title="Home"
          style={{
            border: 0,
            background: "transparent",
            padding: 0,
            cursor: "pointer",
          }}
        >
         <FaHome size={18} aria-hidden="true" />

          <span>
            <strong>LTM INSURANCE PORTAL</strong>
          </span>
        </button>

        <div className="dashboard-account">
          <span>
            {user?.email ||
              `${user?.firstName || "Customer"} ${
                user?.lastName || ""
              }`}
          </span>

          <button
            type="button"
            onClick={signOut}
          >
            Sign out
          </button>
        </div>

      </header>

      <section
        className="dashboard-content"
        aria-labelledby="claims-title"
      >

        <div className="dashboard-heading">
          <div>

            <p className="dashboard-eyebrow">
              CLAIMS
            </p>

            <h1 id="claims-title">
              File and track your claims.
            </h1>

            <p>
              Only active policies are eligible for
              new accident claims.
            </p>

          </div>
        </div>

        {/* ERROR MESSAGE */}
        {error && (
          <div
            className="dashboard-error"
            role="alert"
          >
            {error}
          </div>
        )}

        {/* SUCCESS MESSAGE */}
        {success && (
          <div
            className="dashboard-error"
            role="status"
            style={{
              borderColor: "#b9e2d9",
              color: "#146b68",
              background: "#effaf7",
            }}
          >
            <strong>
              Claim submitted successfully
            </strong>

            <br />

            Claim ID: {success.claimId}

            <br />

            Status: {success.status}
          </div>
        )}

        {/* FILE CLAIM */}
        <section
          className="dashboard-policy-section"
          aria-labelledby="file-claim-heading"
          style={{
            marginBottom: "32px",
          }}
        >

          <header className="dashboard-section-heading">
            <div>
              <h2 id="file-claim-heading">
                File Claim
              </h2>

              <p>
                Submit a new claim for your policy.
              </p>
            </div>
          </header>

          <form
            onSubmit={submitClaim}
            className="dashboard-policy-list"
            style={{
              paddingTop: "16px",
              borderTop: "1px solid #dce5ed",
            }}
          >

            <div
              style={{
                display: "grid",
                gap: "14px",
                gridTemplateColumns:
                  "repeat(auto-fit, minmax(220px, 1fr))",
              }}
            >

              {/* POLICY */}
              <label>
                <div
                  className="dashboard-section-empty"
                  style={{
                    borderTop: "0",
                    padding: "0 0 8px",
                    textAlign: "left",
                  }}
                >
                  Policy
                </div>

                <select
                  name="policyId"
                  value={form.policyId}
                  onChange={handleChange}
                  className="quote-input"
                  required
                >
                  <option value="">
                    Select policy
                  </option>

                  {claimablePolicies.map((policy) => (
                    <option
                      key={policy.policyId}
                      value={policy.policyId}
                    >
                      {policy.policyId}
                    </option>
                  ))}
                </select>
              </label>

              {/* CLAIM TYPE */}
              <label>
                <div
                  className="dashboard-section-empty"
                  style={{
                    borderTop: "0",
                    padding: "0 0 8px",
                    textAlign: "left",
                  }}
                >
                  Claim Type
                </div>

                <select
                  name="claimType"
                  value={form.claimType}
                  onChange={handleChange}
          className="quote-input"
                  required
                >
                  <option value="ACCIDENT">
                    Accident
                  </option>
                </select>
              </label>

              {/* INCIDENT DATE */}
              <label>
                <div
                  className="dashboard-section-empty"
                  style={{
                    borderTop: "0",
                    padding: "0 0 8px",
                    textAlign: "left",
                  }}
                >
                  Incident Date
                </div>

                <input
                  name="incidentDate"
                  type="date"
                  max={localDateValue()}
                  value={form.incidentDate}
                  onChange={handleChange}
                  className="quote-input"
                  required
                />
              </label>

              {/* LOCATION */}
              <label>
                <div
                  className="dashboard-section-empty"
                  style={{
                    borderTop: "0",
                    padding: "0 0 8px",
                    textAlign: "left",
                  }}
                >
                  Location
                </div>

                <input
                  name="incidentLocation"
                  value={form.incidentLocation}
                  onChange={handleChange}
                  className="quote-input"
                  required
                />
              </label>

              {/* DESCRIPTION */}
              <label
                style={{
                  gridColumn: "1 / -1",
                }}
              >
                <div
                  className="dashboard-section-empty"
                  style={{
                    borderTop: "0",
                    padding: "0 0 8px",
                    textAlign: "left",
                  }}
                >
                  Description
                </div>

                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  className="quote-input"
                  rows={3}
                  required
                />
              </label>

              {/* ESTIMATED DAMAGE */}
              <label>
                <div
                  className="dashboard-section-empty"
                  style={{
                    borderTop: "0",
                    padding: "0 0 8px",
                    textAlign: "left",
                  }}
                >
                  Estimated Damage Amount
                </div>

                <input
                  name="estimatedDamageAmount"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={
                    form.estimatedDamageAmount
                  }
                  onChange={handleChange}
                  className="quote-input"
                  required
                />
              </label>

            </div>

            <div
              style={{
                marginTop: "14px",
              }}
            >
              <button
                type="submit"
                className="dashboard-create"
                disabled={
                  submitting ||
                  loading ||
                  !claimablePolicies.length
                }
              >
                {submitting
                  ? "Submitting..."
                  : "Submit Claim"}
              </button>
            </div>

          </form>

        </section>

        {/* MY CLAIMS */}
        <section
          className="dashboard-policy-section"
          aria-labelledby="my-claims-heading"
        >

          <header className="dashboard-section-heading">

            <div>
              <h2 id="my-claims-heading">
                My Claims
              </h2>

              <p>
                Track all submitted claims.
              </p>
            </div>

            <button
              type="button"
              className="dashboard-policy-action"
              onClick={() => void loadData()}
              disabled={loading}
            >
              Refresh
            </button>

          </header>

          {loading ? (

            <div
              className="dashboard-loading"
              role="status"
              aria-live="polite"
            >
              <span
                className="dashboard-spinner"
                aria-hidden="true"
              />

              <span>
                Loading claims...
              </span>
            </div>

          ) : claims.length ? (

            <div className="dashboard-policy-list">

              {claims.map((claim) => (

                <article
                  className="dashboard-policy-row"
                  key={claim.claimId}
                >

                  <div className="dashboard-policy-main">

                    <p className="dashboard-policy-id">
                      {claim.claimId}
                    </p>

                    <h3>
                      {claim.claimType}
                    </h3>

                    <p>
                      {claim.policyId}
                      {" "}
                      <span aria-hidden="true">
                        ·
                      </span>
                      {" "}
                      {claim.status}
                    </p>

                  </div>

                  <div className="dashboard-policy-meta">

                    <span>
                      {formatAmount(
                        claim.estimatedDamageAmount
                      )}
                    </span>

                    <small>
                      Incident{" "}
                      {formatDate(
                        claim.incidentDate
                      )}
                    </small>

                  </div>

                  <button
                    type="button"
                    className="dashboard-policy-action"
                    onClick={() =>
                      navigate(
                        `/quote/${encodeURIComponent(
                          claim.policyId
                        )}?resume=${encodeURIComponent(
                          claim.policyId
                        )}`
                      )
                    }
                  >
                    Open Policy
                  </button>

                </article>

              ))}

            </div>

          ) : (

            <p className="dashboard-section-empty">
              No claims submitted yet.
            </p>

          )}

        </section>

      </section>
    </main>
  );
}