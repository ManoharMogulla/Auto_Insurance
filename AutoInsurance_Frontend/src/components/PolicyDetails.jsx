import { useEffect, useRef, useState } from "react";
import {
  fetchPolicyDocumentPdf,
  generateAndEmailPolicyDocument,
  generatePolicyDocument,
  getCustomerSession,
  getPolicyDocuments,
  sendPolicyDocumentEmail,
} from "../api/insuranceServices.js";

function formatValue(value) {
  if (value === null || value === undefined || value === "") return "Not available";
  if (Array.isArray(value)) return value.length ? value.map(formatValue).join(", ") : "Not available";
  if (typeof value === "object") {
    return Object.entries(value)
      .map(([name, item]) => `${name.replace(/([A-Z])/g, " $1")}: ${formatValue(item)}`)
      .join(" · ");
  }
  return String(value);
}

function displayDate(value) {
  if (!value) return "Not available";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "Not available" : date.toLocaleDateString("en-IN");
}

function currency(value) {
  const amount = Number(value);
  return Number.isFinite(amount)
    ? `INR ${new Intl.NumberFormat("en-IN", { maximumFractionDigits: 2 }).format(amount)}`
    : "Not available";
}

function coverageLabel(type) {
  const labels = {
    LIABILITY: "Liability",
    COLLISION: "Collision",
    COMPREHENSIVE: "Comprehensive",
    UNINSURED_MOTORIST: "Uninsured motorist",
    UNDERINSURED_MOTORIST: "Underinsured motorist",
    PERSONAL_INJURY_PROTECTION: "Personal injury protection",
    MEDICAL_PAYMENTS: "Medical payments",
    ROADSIDE_ASSISTANCE: "Roadside assistance",
    RENTAL_CAR_REIMBURSEMENT: "Rental car reimbursement",
  };
  return labels[type] || type;
}

function SummaryItem({ label, value, prominent = false }) {
  return (
    <div className={`quote-summary-item${prominent ? " is-prominent" : ""}`}>
      <dt>{label}</dt>
      <dd>{formatValue(value)}</dd>
    </div>
  );
}

function InfoCards({ entries }) {
  if (!entries.length) return <p className="quote-inline-note">No drivers returned for this policy.</p>;

  return (
    <div className="quote-driver-list">
      {entries.map((entry, index) => {
        const title = `${entry.firstName || ""} ${entry.lastName || ""}`.trim() || `Driver ${index + 1}`;
        const vehicles = Array.isArray(entry.vehicles) ? entry.vehicles : [];

        return (
          <article className="quote-driver-summary-card" key={`driver-${entry.memberId || entry.email || index}`}>
            <header className="quote-driver-summary-heading">
              <span className={`quote-driver-badge${index === 0 ? " is-primary" : ""}`}>{index === 0 ? "Primary Driver" : `Additional Driver ${index}`}</span>
              {index === 0 && <span className="quote-driver-badge">Named Insured</span>}
              <strong>{title}</strong>
            </header>
            <dl className="quote-driver-summary-details">
              <SummaryItem label="Member ID" value={entry.memberId} />
              <SummaryItem label="Email" value={entry.email} />
              <SummaryItem label="Date of birth" value={displayDate(entry.dateOfBirth)} />
              <SummaryItem label="Phone" value={entry.phoneNumber} />
              <SummaryItem label="License number" value={entry.licenseNumber} />
              <SummaryItem label="License state" value={entry.licenseState} />
              <SummaryItem label="License expiration" value={displayDate(entry.licenseExpirationDate)} />
              <SummaryItem label="Address" value={[entry.address, entry.city, entry.state, entry.zipCode].filter(Boolean).join(", ")} />
              <SummaryItem label="Gender" value={entry.gender} />
              <SummaryItem label="Marital status" value={entry.maritalStatus} />
              <SummaryItem label="Driving history" value={entry.drivingHistory} />
              <SummaryItem label="Insurance history" value={entry.insuranceHistory} />
              <SummaryItem label="Credit score" value={entry.creditScore} />
            </dl>
            <section className="quote-summary-vehicles">
              <h4>Vehicles</h4>
              {vehicles.length ? vehicles.map((vehicle, vehicleIndex) => {
                const discounts = (vehicle.discounts || []).flatMap((discount) =>
                  Object.entries(discount)
                    .filter(([name, enabled]) => name !== "discountId" && enabled === true)
                    .map(([name]) => name.replace(/([A-Z])/g, " $1").replace(/^./, (letter) => letter.toUpperCase()))
                );
                return (
                  <div className="quote-summary-vehicle" key={`vehicle-${vehicle.vehicleId || vehicle.vin || vehicleIndex}`}>
                    <header className="quote-driver-summary-heading">
                      <span className="quote-driver-badge">Vehicle {vehicleIndex + 1}</span>
                      {index === 0 && vehicleIndex === 0 && <span className="quote-driver-badge is-primary">Primary Vehicle</span>}
                      <strong>{`${vehicle.year || ""} ${vehicle.make || ""} ${vehicle.model || ""}`.trim() || "Vehicle details"}</strong>
                    </header>
                    <dl className="quote-driver-summary-details">
                      <SummaryItem label="Vehicle type" value={vehicle.vehicleType} />
                      <SummaryItem label="Usage type" value={vehicle.usageType} />
                      <SummaryItem label="VIN" value={vehicle.vin} />
                      <SummaryItem label="License plate" value={vehicle.licensePlate} />
                      <SummaryItem label="Registration state" value={vehicle.registrationState} />
                      <SummaryItem label="Registration expiration" value={displayDate(vehicle.registrationExpirationDate)} />
                      <SummaryItem
                        label="Coverages"
                        value={(vehicle.coverages || []).length
                          ? (vehicle.coverages || []).map((coverage) =>
                              `${coverageLabel(coverage.coverageType)} (Limit: ${formatValue(coverage.coverageLimit)})`
                            ).join(", ")
                          : "Not available"}
                      />
                      <SummaryItem label="Applied discounts" value={discounts.length ? discounts.join(", ") : "None returned"} />
                    </dl>
                  </div>
                );
              }) : <p className="quote-inline-note">No vehicles associated with this driver.</p>}
            </section>
          </article>
        );
      })}
    </div>
  );
}

export default function PolicyDetails({ details, onBack }) {
  const policy = details?.policy || {};
  const customer = details?.customer || {};
  const drivers = details?.drivers || policy.driver || [];
  const primaryDriver = drivers.find((driver) => driver?.namedInsured) || drivers[0] || {};
  const customerView = {
    id: customer.id || customer.memberId || primaryDriver.customerId || primaryDriver.memberId,
    firstName: customer.firstName || primaryDriver.firstName,
    lastName: customer.lastName || primaryDriver.lastName,
    email: customer.email || primaryDriver.email,
    dateOfBirth: customer.dateOfBirth || primaryDriver.dateOfBirth,
    licenseNumber: customer.licenseNumber || primaryDriver.licenseNumber,
  };
  const payment = details?.payment || {};
  const policyNumber = policy.policyId || policy.id;
  const premiumAmount = payment.premiumAmount ?? policy.premiumAmount;
  const activeDiscounts = drivers.flatMap((driver) => (driver.vehicles || []).flatMap((vehicle) =>
    (vehicle.discounts || []).flatMap((discount) =>
      Object.entries(discount)
        .filter(([name, value]) => name !== "discountId" && value === true)
        .map(([name]) => name.replace(/([A-Z])/g, " $1").replace(/^./, (letter) => letter.toUpperCase()))
    )
  ));
  const coverage = policy.coverageDetails || policy.coverageType || policy.coverage;
  const policyId = policy.policyId || policy.id;
  const [documents, setDocuments] = useState([]);
  const [recipientEmail, setRecipientEmail] = useState(customerView.email || getCustomerSession()?.email || "");
  const [busy, setBusy] = useState(policyId ? "automatic" : "");
  const [documentMessage, setDocumentMessage] = useState(policyId ? "Generating your policy document..." : "");
  const [documentError, setDocumentError] = useState("");
  const busyRef = useRef(false);
  const automaticRequest = useRef(null);

  const refreshDocuments = async () => {
    const result = await getPolicyDocuments(policyId);
    setDocuments(Array.isArray(result) ? result : []);
  };

  useEffect(() => {
    if (!policyId) return undefined;
    let active = true;
    if (automaticRequest.current?.policyId !== policyId) {
      automaticRequest.current = { policyId, promise: generateAndEmailPolicyDocument(policyId) };
    }
    const request = automaticRequest.current.promise;
    busyRef.current = true;
    getPolicyDocuments(policyId)
      .then((result) => {
        if (active && Array.isArray(result)) {
          setDocuments((current) => {
            const byId = new Map(current.map((document) => [document.documentId, document]));
            result.forEach((document) => byId.set(document.documentId, document));
            return [...byId.values()];
          });
        }
      })
      .catch(() => {});
    request
      .then((document) => {
        if (!active) return;
        setDocuments((current) => [document, ...current.filter((item) => item.documentId !== document.documentId)]);
        setDocumentMessage(document.emailStatus === "SENT"
          ? "Policy document generated and emailed successfully."
          : "Policy document generated. Email delivery needs attention.");
      })
      .catch((error) => {
        if (active) setDocumentError(error.message || "Unable to generate the policy document.");
      })
      .finally(() => {
        if (active) {
          busyRef.current = false;
          setBusy("");
        }
      });
    return () => { active = false; };
  }, [policyId]);

  const runDocumentAction = async (name, action, successMessage) => {
    if (busyRef.current) return;
    busyRef.current = true;
    setBusy(name);
    setDocumentMessage("");
    setDocumentError("");
    try {
      const document = await action();
      if (document) {
        setDocuments((current) => [document, ...current.filter((item) => item.documentId !== document.documentId)]);
      } else {
        await refreshDocuments();
      }
      setDocumentMessage(successMessage);
    } catch (error) {
      setDocumentError(error.message || "Document request failed.");
    } finally {
      busyRef.current = false;
      setBusy("");
    }
  };

  const openPdf = async (document, disposition) => {
    if (busyRef.current) return;
    busyRef.current = true;
    setBusy(disposition);
    setDocumentError("");
    const viewWindow = disposition === "inline" ? window.open("about:blank", "_blank") : null;
    try {
      const url = await fetchPolicyDocumentPdf(document.documentId, disposition);
      if (viewWindow) {
        viewWindow.location.assign(url);
      } else {
        const link = window.document.createElement("a");
        link.href = url;
        link.download = document.fileName || "policy-document.pdf";
        link.click();
      }
      window.setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (error) {
      viewWindow?.close();
      setDocumentError(error.message || "Unable to open the policy document.");
    } finally {
      busyRef.current = false;
      setBusy("");
    }
  };

  const submitEmail = (document) => {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(recipientEmail.trim())) {
      setDocumentError("Enter a valid recipient email address.");
      setDocumentMessage("");
      return;
    }
    runDocumentAction("email", () => sendPolicyDocumentEmail(document.documentId, recipientEmail.trim()),
      "Policy document email sent successfully.");
  };

  return (
    <div className="policy-details-page">
      <header className="policy-details-success">
        <span aria-hidden="true">✓</span>
        <div>
          <p className="quote-eyebrow">PAYMENT CONFIRMED</p>
          <h2>Policy Created Successfully</h2>
          <p>{details?.paymentResult}</p>
        </div>
      </header>

      <section className="policy-details-hero" aria-label="Policy identifiers">
        <div>
          <span>POLICY NUMBER</span>
          <strong>{formatValue(policyNumber)}</strong>
        </div>
        <div>
          <span>PREMIUM</span>
          <strong>{currency(premiumAmount)}</strong>
        </div>
      </section>

      <div className="quote-summary-groups">
        <section className="quote-summary-group">
          <h3>Customer</h3>
          <dl className="quote-summary">
            <SummaryItem label="Customer ID" value={customerView.id} />
            <SummaryItem label="Name" value={`${customerView.firstName || ""} ${customerView.lastName || ""}`.trim()} />
            <SummaryItem label="Email" value={customerView.email} />
            <SummaryItem label="Date of birth" value={displayDate(customerView.dateOfBirth)} />
            <SummaryItem label="License number" value={customerView.licenseNumber} />
          </dl>
          <p className="quote-summary-role"><span className="quote-driver-badge">Named Insured</span></p>
        </section>

        <section className="quote-summary-group">
          <h3>Primary and additional drivers</h3>
          <InfoCards entries={drivers} />
        </section>

        <section className="quote-summary-group">
          <h3>Coverage and discounts</h3>
          <dl className="quote-summary">
            <SummaryItem label="Coverage" value={coverage} />
            <SummaryItem label="Active discounts" value={activeDiscounts.length ? activeDiscounts.join(", ") : "No active discounts returned"} />
          </dl>
        </section>

        <section className="quote-summary-group">
          <h3>Policy and quote</h3>
          <dl className="quote-summary">
            <SummaryItem label="Policy ID" value={policyNumber} />
            <SummaryItem label="Policy type" value={policy.policyType} />
            <SummaryItem label="Status" value={policy.policyStatus} />
            <SummaryItem label="Effective date" value={displayDate(policy.effectiveDate)} />
            <SummaryItem label="Expiration date" value={displayDate(policy.expirationDate)} />
            <SummaryItem label="Base premium" value={currency(policy.basePremium)} />
            <SummaryItem label="Driver adjustment" value={currency(policy.driverAdjustment)} />
            <SummaryItem label="Vehicle adjustment" value={currency(policy.vehicleAdjustment)} />
            <SummaryItem label="Discount amount" value={currency(policy.totalDiscount)} />
            <SummaryItem label="Premium" value={currency(policy.premiumAmount)} prominent />
            <SummaryItem label="Applied discounts" value={activeDiscounts.length ? activeDiscounts.join(", ") : "None returned"} />
            <SummaryItem label="Final premium" value={currency(policy.finalPremium ?? policy.premiumAmount)} prominent />
          </dl>
        </section>

        <section className="quote-summary-group">
          <h3>Payment</h3>
          <dl className="quote-summary">
            <SummaryItem label="Payment status" value={payment.paymentStatus || details?.paymentResult} />
            <SummaryItem label="Payment method" value={payment.paymentMethod || details?.paymentMethod} />
            <SummaryItem label="Amount paid" value={currency(payment.premiumAmount ?? policy.premiumAmount)} />
            <SummaryItem label="Payment ID" value={payment.paymentId || payment.id} />
            <SummaryItem label="Payment date" value={displayDate(payment.paymentDate || payment.createdAt)} />
          </dl>
        </section>

        <section className="quote-summary-group policy-documents" aria-labelledby="policy-documents-title">
          <div className="policy-documents-heading">
            <h3 id="policy-documents-title">Documents</h3>
            <button type="button" className="quote-back" disabled={!policyId || Boolean(busy)}
              onClick={() => runDocumentAction("generate", () => generatePolicyDocument(policyId), "Policy document generated.")}>
              {busy === "generate" ? "Generating..." : "Generate Policy Document"}
            </button>
          </div>
          <label className="policy-document-email">
            Recipient email
            <input type="email" value={recipientEmail} onChange={(event) => setRecipientEmail(event.target.value)}
              autoComplete="email" placeholder="customer@example.com" />
          </label>
          {!documents.length && <p className="quote-inline-note">No policy document available yet.</p>}
          {documents.map((document) => (
            <article className="policy-document-row" key={document.documentId}>
              <dl className="quote-summary">
                <SummaryItem label="Document name" value={document.fileName} />
                <SummaryItem label="Document type" value={document.documentType} />
                <SummaryItem label="Generated" value={displayDate(document.generatedAt)} />
                {/* <SummaryItem label="Email status" value={document.emailStatus} /> */}
                <SummaryItem label="Recipient email" value={document.recipientEmail} />
              </dl>
              <div className="policy-document-actions">
                <button type="button" className="quote-back" disabled={Boolean(busy)} onClick={() => openPdf(document, "inline")}>
                  {busy === "inline" ? "Opening..." : "View Document"}
                </button>
                <button type="button" className="quote-back" disabled={Boolean(busy)} onClick={() => openPdf(document, "attachment")}>
                  {busy === "attachment" ? "Downloading..." : "Download PDF"}
                </button>
                {document.emailStatus === "FAILED" ? (
                  <button type="button" className="quote-back" disabled={Boolean(busy)} onClick={() => submitEmail(document)}>
                    {busy === "email" ? "Sending..." : "Resend Email"}
                  </button>
                ) : (
                  <button type="button" className="quote-back" disabled={Boolean(busy)} onClick={() => submitEmail(document)}>
                    {busy === "email" ? "Sending..." : "Send to Email"}
                  </button>
                )}
              </div>
            </article>
          ))}
          {documentMessage && <p className="policy-document-message" role="status">{documentMessage}</p>}
          {documentError && <p className="policy-document-error" role="alert">{documentError}</p>}
        </section>
      </div>

      <div className="quote-actions">
        <button type="button" className="quote-back" onClick={onBack}>Home</button>
      </div>
    </div>
  );
}