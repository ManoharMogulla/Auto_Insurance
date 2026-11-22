import { useEffect, useState } from "react";
import {
  clearCustomerSession,
  getAllPolicies,
  getCustomerSession,
  getPolicyDetails,
  getPolicyPayments,
} from "../api/insuranceServices.js";
import "./SummaryDashboard.css";

function belongsToCustomer(policy, customer) {
  const drivers = Array.isArray(policy?.driver) ? policy.driver : [];
  const email = String(customer?.email || "").trim().toLowerCase();
  const customerId = String(customer?.id || customer?.memberId || "");

  return drivers.some((driver) =>
    (email && String(driver?.email || "").trim().toLowerCase() === email) ||
    (customerId && String(driver?.memberId || "") === customerId)
  );
}

function isCompleted(policy) {
  return ["active", "issued", "renewed", "completed"].includes(
    String(policy?.policyStatus || "").trim().toLowerCase()
  );
}

function formatCurrency(value) {
  const amount = Number(value);
  return Number.isFinite(amount)
    ? `INR ${new Intl.NumberFormat("en-IN", { maximumFractionDigits: 2 }).format(amount)}`
    : "Premium unavailable";
}

function formatDate(value) {
  if (!value) return "Date unavailable";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "Date unavailable" : date.toLocaleDateString("en-IN");
}

export default function SummaryDashboard() {
  const [customer] = useState(() => getCustomerSession() || {});
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [openingPolicyId, setOpeningPolicyId] = useState("");

  useEffect(() => {
    let active = true;

    getAllPolicies()
      .then((result) => {
        if (!active) return;
        if (!Array.isArray(result)) throw new Error("Policy service returned an unexpected response.");
        setPolicies(result.filter((policy) => belongsToCustomer(policy, customer)));
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || "Unable to load your policies.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => { active = false; };
  }, [customer]);

  const completedPolicies = policies.filter(isCompleted);
  const incompletePolicies = policies.filter((policy) => !isCompleted(policy));

  const startNewQuote = () => {
    sessionStorage.removeItem("ltm.resumePolicy");
    sessionStorage.removeItem("ltm.finalPolicy");
    window.location.assign("/quote");
  };

  const continuePolicy = (policy) => {
    sessionStorage.removeItem("ltm.finalPolicy");
    sessionStorage.setItem("ltm.resumePolicy", JSON.stringify(policy));
    window.location.assign(`/quote?resume=${encodeURIComponent(policy.policyId)}`);
  };

  const viewDetails = async (policy) => {
    setOpeningPolicyId(policy.policyId);
    setError("");

    try {
      const details = await getPolicyDetails(policy.policyId);
      let payment = null;
      try {
        const payments = await getPolicyPayments(policy.policyId);
        payment = Array.isArray(payments) ? payments[0] || null : null;
      } catch {
        payment = null;
      }

      const finalDetails = {
        policy: details,
        customer,
        drivers: details?.driver || policy.driver || [],
        payment,
        paymentMethod: payment?.paymentMethod || details?.paymentType,
        paymentResult: payment?.paymentStatus || "Policy details",
      };
      sessionStorage.setItem("ltm.finalPolicy", JSON.stringify(finalDetails));
      sessionStorage.removeItem("ltm.resumePolicy");
      window.location.assign("/policy-details");
    } catch (requestError) {
      setError(requestError.message || "Unable to load policy details.");
      setOpeningPolicyId("");
    }
  };

  const logout = () => {
    clearCustomerSession();
    window.location.assign("/login");
  };

  const firstName = customer.firstName || "there";

  return (
    <main className="customer-dashboard">
      <header className="dashboard-topbar">
        <a className="dashboard-brand" href="/summary" aria-label="LTM customer summary">
          <span aria-hidden="true">+</span>
          <span><strong>LTM</strong><small>INSURANCE PORTAL</small></span>
        </a>
        <div className="dashboard-account">
          <span>{customer.email || `${customer.firstName || "Customer"} ${customer.lastName || ""}`}</span>
          <button type="button" onClick={logout}>Sign out</button>
        </div>
      </header>

      <section className="dashboard-content" aria-labelledby="dashboard-title">
        <div className="dashboard-heading">
          <div>
            <p className="dashboard-eyebrow">CUSTOMER SUMMARY</p>
            <h1 id="dashboard-title">Your insurance, {firstName}.</h1>
            <p>Review your coverage or pick up where you left off.</p>
          </div>
          <button type="button" className="dashboard-create" onClick={startNewQuote}>
            <span aria-hidden="true">+</span> Create New Quote
          </button>
        </div>

        {error && <div className="dashboard-error" role="alert">{error}</div>}

        {loading ? (
          <div className="dashboard-loading" role="status" aria-live="polite">
            <span className="dashboard-spinner" aria-hidden="true" />
            <span>Loading your policies...</span>
          </div>
        ) : policies.length === 0 && !error ? (
          <section className="dashboard-empty" aria-labelledby="empty-title">
            <span className="dashboard-empty-mark" aria-hidden="true">+</span>
            <p className="dashboard-eyebrow">A FRESH START</p>
            <h2 id="empty-title">No policies yet</h2>
            <p>Your policy and saved quotes will appear here.</p>
            <button type="button" className="dashboard-create" onClick={startNewQuote}>Create New Quote</button>
          </section>
        ) : (
          <div className="dashboard-policy-sections">
            <PolicySection
              title="Incomplete policies & saved quotes"
              description="Continue a quote without creating another policy."
              policies={incompletePolicies}
              actionLabel="Continue"
              onAction={continuePolicy}
              loadingPolicyId={openingPolicyId}
            />
            <PolicySection
              title="Completed policies"
              description="View policy and payment details."
              policies={completedPolicies}
              actionLabel="View Details"
              onAction={viewDetails}
              loadingPolicyId={openingPolicyId}
            />
          </div>
        )}
      </section>
    </main>
  );
}

function PolicySection({ title, description, policies, actionLabel, onAction, loadingPolicyId }) {
  return (
    <section className="dashboard-policy-section" aria-labelledby={`${actionLabel}-heading`}>
      <header className="dashboard-section-heading">
        <div>
          <h2 id={`${actionLabel}-heading`}>{title}</h2>
          <p>{description}</p>
        </div>
        <span className="dashboard-count">{policies.length}</span>
      </header>
      {policies.length ? (
        <div className="dashboard-policy-list">
          {policies.map((policy) => {
            const primaryDriver = policy.driver?.[0] || {};
            const busy = loadingPolicyId === policy.policyId;
            return (
              <article className="dashboard-policy-row" key={policy.policyId}>
                <div className="dashboard-policy-main">
                  <p className="dashboard-policy-id">{policy.policyId || "Policy"}</p>
                  <h3>{`${primaryDriver.firstName || "Auto"} ${primaryDriver.lastName || "policy"}`.trim()}</h3>
                  <p>{policy.policyType || "Auto insurance"} <span aria-hidden="true">·</span> {policy.policyStatus || "In progress"}</p>
                </div>
                <div className="dashboard-policy-meta">
                  <span>{formatCurrency(policy.premiumAmount)}</span>
                  <small>Updated {formatDate(policy.effectiveDate)}</small>
                </div>
                <button
                  type="button"
                  className="dashboard-policy-action"
                  onClick={() => onAction(policy)}
                  disabled={Boolean(loadingPolicyId)}
                >
                  {busy ? "Loading..." : actionLabel}
                </button>
              </article>
            );
          })}
        </div>
      ) : (
        <p className="dashboard-section-empty">Nothing to show here.</p>
      )}
    </section>
  );
}