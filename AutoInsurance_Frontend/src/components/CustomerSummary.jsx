import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaHome } from "react-icons/fa";
import { useAuth } from "../auth/authContext.js";
import {
  getAllPolicies,
  getPolicyDetails,
  getPolicyPayments
} from "../api/insuranceServices.js";
import "../styles.css/SummaryDashboard.css";


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

function formatOptionalCurrency(value) {
  return value === null || value === undefined || value === ""
    ? "Not available"
    : formatCurrency(value);
}

function formatDate(value) {
  if (!value) return "Date unavailable";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "Date unavailable" : date.toLocaleDateString("en-IN");
}

export default function CustomerSummary() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
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
        setPolicies(result);
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || "Unable to load your policies.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  const completedPolicies = policies.filter(isCompleted);
  const incompletePolicies = policies.filter((policy) => !isCompleted(policy));

  const createQuote = () => {
    sessionStorage.removeItem("ltm.resumePolicy");
    sessionStorage.removeItem("ltm.finalPolicy");
    navigate("/quote/new");
  };

  const continueQuote = (policy) => {
    sessionStorage.removeItem("ltm.finalPolicy");
    sessionStorage.setItem("ltm.resumePolicy", JSON.stringify(policy));
    navigate(`/quote/${encodeURIComponent(policy.policyId)}?resume=${encodeURIComponent(policy.policyId)}`);
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
      sessionStorage.setItem("ltm.finalPolicy", JSON.stringify({
        policy: details,
        customer: user,
        drivers: details?.driver || policy.driver || [],
        payment,
        paymentMethod: payment?.paymentMethod || details?.paymentType,
        paymentResult: payment?.paymentStatus || "Policy details",
      }));
      navigate("/policy-details");
    } catch (requestError) {
      setError(requestError.message || "Unable to load policy details.");
    } finally {
      setOpeningPolicyId("");
    }
  };

  const signOut = async () => {
    await logout().catch(() => undefined);
    navigate("/login", { replace: true });
  };

  return (
    <main className="customer-dashboard">
      <header className="dashboard-topbar">
        <button
          type="button"
          className="dashboard-brand"
          onClick={() => navigate("/customer-summary")}
          aria-label="LTM customer summary"
          style={{ border: 0, background: "transparent", padding: 0, cursor: "pointer" }}
        >
          <span aria-hidden="true"><FaHome size={16} /></span>
          <span><strong>LTM INSURANCE PORTAL</strong></span>
        </button>
        <div className="dashboard-account">
          <span>{user?.email || `${user?.firstName || "Customer"} ${user?.lastName || ""}`}</span>
          <button type="button" onClick={signOut}>Sign out</button>
        </div>
      </header>
      <section className="dashboard-content" aria-labelledby="dashboard-title">
        <div className="dashboard-heading">
          <div>
            <p className="dashboard-eyebrow">CUSTOMER SUMMARY</p>
            <h1 id="dashboard-title">Your insurance, {user?.firstName || "there"}.</h1>
            <p>Review your coverage or pick up where you left off.</p>
          </div>
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            <button type="button" className="dashboard-policy-action" onClick={() => navigate("/claims")}>File Claim</button>
            <button type="button" className="dashboard-policy-action" onClick={() => navigate("/claims")}>My Claims</button>
            <button type="button" className="dashboard-create" onClick={createQuote}>
              <span aria-hidden="true">+</span> Create New Quote
            </button>
          </div>
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
            <button type="button" className="dashboard-create" onClick={createQuote}>Create New Quote</button>
          </section>
        ) : (
          <div className="dashboard-policy-sections">
            <PolicySection title="Incomplete policies" description="Continue a saved quote without creating another policy."
              policies={incompletePolicies} actionLabel="Continue Quote" onAction={continueQuote} loadingPolicyId={openingPolicyId} />
            <PolicySection title="Completed policies" description="View policy and payment details."
              policies={completedPolicies} actionLabel="View Details" onAction={viewDetails} loadingPolicyId={openingPolicyId} />
          </div>
        )}
      </section>
    </main>
  );
}

function PolicySection({ title, description, policies, actionLabel, onAction, loadingPolicyId }) {
  return (
    <section className="dashboard-policy-section" aria-labelledby={`${actionLabel.replaceAll(" ", "-")}-heading`}>
      <header className="dashboard-section-heading">
        <div><h2 id={`${actionLabel.replaceAll(" ", "-")}-heading`}>{title}</h2><p>{description}</p></div>
        <span className="dashboard-count">{policies.length}</span>
      </header>
      {policies.length ? (
        <div className="dashboard-policy-list">
          {policies.map((policy) => {
            const primary = policy.driver?.find((driver) => driver.namedInsured) || policy.driver?.[0] || {};
            const busy = loadingPolicyId === policy.policyId;
            return (
              <article className="dashboard-policy-row" key={policy.policyId}>
                <div className="dashboard-policy-main">
                  <p className="dashboard-policy-id">{policy.policyId || "Policy"}</p>
                  <h3>{`${primary.firstName || "Auto"} ${primary.lastName || "policy"}`.trim()}</h3>
                  <p>{policy.policyType || "Auto insurance"} <span aria-hidden="true">·</span> {policy.policyStatus || "In progress"}</p>
                </div>
                <div className="dashboard-policy-meta">
                  <span>{formatOptionalCurrency(policy.premiumAmount)}<small>Final premium</small></span>
                  <small>Effective {formatDate(policy.effectiveDate)}</small>
                  <div className="dashboard-premium-breakdown" aria-label="Premium breakdown">
                    <small>Base: {formatOptionalCurrency(policy.basePremium)}</small>
                    <small>Driver adjustment: {formatOptionalCurrency(policy.driverAdjustment)}</small>
                    <small>Vehicle adjustment: {formatOptionalCurrency(policy.vehicleAdjustment)}</small>
                    <small>Discount: {formatOptionalCurrency(policy.totalDiscount)}</small>
                  </div>
                </div>
                <button type="button" className="dashboard-policy-action" onClick={() => onAction(policy)} disabled={Boolean(loadingPolicyId)}>
                  {busy ? "Loading..." : actionLabel}
                </button>
              </article>
            );
          })}
        </div>
      ) : <p className="dashboard-section-empty">Nothing to show here.</p>}
    </section>
  );
}
