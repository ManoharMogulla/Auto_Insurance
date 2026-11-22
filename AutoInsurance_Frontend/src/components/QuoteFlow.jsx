import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/authContext.js";



import {
  createPolicy,
  getCustomerSession,
  getPolicy,
  getPolicyDetails,
  getPolicyPayments,
  makePayment,
} from "../api/insuranceServices.js";

import "../styles.css/Registration.css";
import "../styles.css/QuoteFlow.css";
import PolicyDetails from "./PolicyDetails.jsx";

const steps = [
  "Driver details",
  "Vehicle details",
  "Coverage & discounts",
  "Customer summary",
  "Payment",
  "Policy details",
];

const vehicleTypes = ["HATCHBACK", "SEDAN", "SUV", "TRUCK", "SPORTS"];
const usageTypes = ["Personal", "Commute", "Business","Storage"];
const genderOptions = ["Female", "Male", "Prefer not to say"];
const maritalStatuses = ["Single", "Married", "Divorced", "Widowed"];
const creditScores = ["Excellent", "Good", "Fair", "Poor"];

const states = [
  "Andhra Pradesh",
  "Arunachal Pradesh",
  "Assam",
  "Bihar",
  "Chhattisgarh",
  "Goa",
  "Gujarat",
  "Haryana",
  "Himachal Pradesh",
  "Jharkhand",
  "Karnataka",
  "Kerala",
  "Madhya Pradesh",
  "Maharashtra",
  "Manipur",
  "Meghalaya",
  "Mizoram",
  "Nagaland",
  "Odisha",
  "Punjab",
  "Rajasthan",
  "Sikkim",
  "Tamil Nadu",
  "Telangana",
  "Tripura",
  "Uttar Pradesh",
  "Uttarakhand",
  "West Bengal",

  "Andaman and Nicobar Islands",
  "Chandigarh",
  "Dadra and Nagar Haveli and Daman and Diu",
  "Delhi",
  "Jammu and Kashmir",
  "Ladakh",
  "Lakshadweep",
  "Puducherry",
];
const paymentMethods = [
  "CARD",
  "UPI",
  "NET_BANKING",
  "WALLET",
  "BANK_TRANSFER",
  "CHEQUE",
  "CASH",
];

const discountFields = [
  ["safePilotDiscount", "Safe pilot"],
  ["goodStudentDiscount", "Good student"],
  ["antiTheftDiscount", "Anti-theft"],
  ["lowMileageDiscount", "Low mileage"],
];

const coverageOptions = [
  { type: "COLLISION", label: "Collision" },
  { type: "COMPREHENSIVE", label: "Comprehensive" },
  { type: "PERSONAL_INJURY_PROTECTION", label: "Personal injury protection" },
  { type: "ROADSIDE_ASSISTANCE", label: "Roadside assistance" },
];

function emptyDiscount() {
  return Object.fromEntries(discountFields.map(([name]) => [name, false]));
}

function emptyVehicle() {
  return {
    make: "",
    model: "",
    year: String(new Date().getFullYear()),
    vin: "",
    licensePlate: "",
    registrationState: "",
    registrationExpirationDate: "",
    vehicleType: "SEDAN",
    usageType: "Personal",
    discounts: [emptyDiscount()],
    coverages: [],
  };
}

function coverageMeta(type) {
  return coverageOptions.find((item) => item.type === type) || null;
}

function coverageLabel(type) {
  return coverageMeta(type)?.label || type;
}

function normalizeCoverages(coverages) {
  if (!Array.isArray(coverages)) return [];
  const seen = new Set();
  return coverages
    .filter((coverage) => coverage?.coverageType)
    .filter((coverage) => {
      const key = String(coverage.coverageType).trim().toUpperCase();
      if (!key || seen.has(key)) return false;
      seen.add(key);
      return true;
    })
    .map((coverage) => ({
      coverageType: String(coverage.coverageType).trim().toUpperCase(),
      coverageLimit: coverage.coverageLimit ?? "",
    }));
}

function buildCoverageSummary(drivers) {
  const labels = drivers.flatMap((driver) =>
    (driver.vehicles || []).flatMap((vehicle) =>
      (vehicle.coverages || []).map((coverage) => coverageLabel(coverage.coverageType))
    )
  );
  return [...new Set(labels)].join(", ");
}

function dateInputValue(value) {
  return value ? String(value).slice(0, 10) : "";
}

function emptyDriver(profile = {}, primary = false) {
  return {
    firstName: primary ? profile.firstName || "" : "",
    lastName: primary ? profile.lastName || "" : "",
    dateOfBirth: primary ? dateInputValue(profile.dateOfBirth) : "",
    licenseNumber: primary ? profile.licenseNumber || "" : "",
    licenseState: primary ? profile.licenseState || "" : "",
    licenseExpirationDate: primary ? profile.licenseExpirationDate || "" : "",
    address: primary ? profile.address || "" : "",
    city: primary ? profile.city || "" : "",
    state: primary ? profile.state || "" : "",
    zipCode: primary ? profile.zipCode || "" : "",
    phoneNumber: primary ? profile.phoneNumber || "" : "",
    email: primary ? profile.email || "" : "",
    gender: primary ? profile.gender || "" : "",
    maritalStatus: primary ? profile.maritalStatus || "" : "",
    drivingHistory: primary ? profile.drivingHistory || "" : "",
    insuranceHistory: primary ? profile.insuranceHistory || "" : "",
    creditScore: primary ? profile.creditScore || "" : "",
    vehicles: primary ? [emptyVehicle()] : [],
  };
}

function currency(value) {
  const amount = Number(value);

  return Number.isFinite(amount)
    ? `INR ${new Intl.NumberFormat("en-IN", {
        maximumFractionDigits: 2,
      }).format(amount)}`
    : "Not available";
}

function displayDate(value) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "Not available"
    : date.toLocaleDateString("en-IN");
}

function readProfile() {
  return getCustomerSession() || {};
}

function readFinalPolicy() {
  try {
    return JSON.parse(sessionStorage.getItem("ltm.finalPolicy") || "null");
  } catch {
    return null;
  }
}

function readResumePolicy() {
  try {
    const savedPolicy = JSON.parse(sessionStorage.getItem("ltm.resumePolicy") || "null");
    const requestedId = new URLSearchParams(window.location.search).get("resume");
    return !requestedId || savedPolicy?.policyId === requestedId ? savedPolicy : null;
  } catch {
    return null;
  }
}

function restoreDriver(driver, index) {
  return {
    ...emptyDriver(driver, index === 0),
    ...driver,
    dateOfBirth: dateInputValue(driver.dateOfBirth),
    licenseExpirationDate: dateInputValue(driver.licenseExpirationDate),
    vehicles: (driver.vehicles || []).map((vehicle) => ({
      ...emptyVehicle(),
      ...vehicle,
      year: String(vehicle.year || ""),
      registrationExpirationDate: dateInputValue(vehicle.registrationExpirationDate),
      discounts: Array.isArray(vehicle.discounts) && vehicle.discounts.length
        ? vehicle.discounts
        : [emptyDiscount()],
      coverages: normalizeCoverages(vehicle.coverages),
    })),
  };
}

function toApiDriver(driver) {
  return {
    firstName: driver.firstName,
    lastName: driver.lastName,
    dateOfBirth: driver.dateOfBirth
      ? new Date(`${driver.dateOfBirth}T00:00:00.000Z`).toISOString()
      : "",
    licenseNumber: driver.licenseNumber,
    licenseState: driver.licenseState,
    licenseExpirationDate: driver.licenseExpirationDate,
    address: driver.address,
    city: driver.city,
    state: driver.state,
    zipCode: driver.zipCode,
    phoneNumber: driver.phoneNumber,
    email: driver.email,
    gender: driver.gender,
    maritalStatus: driver.maritalStatus,
    vehicles: driver.vehicles.map((vehicle) => ({
      make: vehicle.make,
      model: vehicle.model,
      year: vehicle.year,
      vin: vehicle.vin,
      licensePlate: vehicle.licensePlate,
      registrationState: vehicle.registrationState,
      registrationExpirationDate: vehicle.registrationExpirationDate,
      vehicleType: vehicle.vehicleType,
      usageType: vehicle.usageType,
      discounts: vehicle.discounts?.length ? vehicle.discounts : [{}],
      coverages: normalizeCoverages(vehicle.coverages).map((coverage) => ({
        coverageType: coverage.coverageType,
        coverageLimit: coverage.coverageLimit === "" ? null : Number(coverage.coverageLimit),
      })),
    })),
    drivingHistory: driver.drivingHistory,
    insuranceHistory: driver.insuranceHistory,
    creditScore: driver.creditScore,
  };
}

function validateDriver(value) {
  const errors = {};
  const requiredFields = [
    ["firstName", "First name"],
    ["lastName", "Last name"],
    ["email", "Email address"],
    ["dateOfBirth", "Date of birth"],
    ["licenseNumber", "License number"],
    ["licenseState", "License state"],
    ["licenseExpirationDate", "License expiration date"],
    ["address", "Street address"],
    ["city", "City"],
    ["state", "State"],
    ["zipCode", "ZIP code"],
    ["phoneNumber", "Phone number"],
  ];

  for (const [name, label] of requiredFields) {
    if (!String(value[name] || "").trim()) {
      errors[name] = `${label} is required.`;
    }
  }

  if (
    value.email?.trim() &&
    !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.email.trim())
  ) {
    errors.email = "Enter a valid email address.";
  }

  if (value.dateOfBirth && value.dateOfBirth > new Date().toISOString().slice(0, 10)) {
    errors.dateOfBirth = "Date of birth cannot be in the future.";
  }

  return errors;
}

function validateVehicle(value) {
  const errors = {};
  const requiredFields = [
    ["make", "Make"],
    ["model", "Model"],
    ["year", "Model year"],
    ["vin", "VIN"],
    ["licensePlate", "License plate"],
    ["registrationState", "Registration state"],
    ["registrationExpirationDate", "Registration expiration date"],
    ["vehicleType", "Vehicle type"],
    ["usageType", "Usage type"],
  ];

  for (const [name, label] of requiredFields) {
    if (!String(value[name] || "").trim()) {
      errors[name] = `${label} is required.`;
    }
  }

  const year = Number(value.year);
  if (
    value.year &&
    (!Number.isInteger(year) || year < 1980 || year > new Date().getFullYear() + 1)
  ) {
    errors.year = "Enter a valid model year.";
  }

  return errors;
}

export default function QuoteFlow() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [customer] = useState(readProfile);
  const [resumePolicy] = useState(readResumePolicy);
  const [drivers, setDrivers] = useState(() =>
    resumePolicy?.driver?.length
      ? resumePolicy.driver.map(restoreDriver)
      : [emptyDriver(readProfile(), true)]
  );
  const [driverErrors, setDriverErrors] = useState([]);
  const [vehicleErrors, setVehicleErrors] = useState([]);
  const [step, setStep] = useState(() =>
    window.location.pathname === "/policy-details" && readFinalPolicy()
      ? 5
      : resumePolicy
        ? 4
        : 0
  );
  const [policy, setPolicy] = useState(() => readFinalPolicy()?.policy || resumePolicy || null);
  const [premium, setPremium] = useState(() => {
    const amount = Number(resumePolicy?.premiumAmount);
    return Number.isFinite(amount) ? { totalPremium: amount } : null;
  });
  const [finalPolicyDetails, setFinalPolicyDetails] = useState(readFinalPolicy);
  const [paymentMethod, setPaymentMethod] = useState(() => resumePolicy?.paymentType || "CARD");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const submissionLock = useRef(false);

  const policyDrivers = Array.isArray(policy?.driver) ? policy.driver : [];
  const returnedVehicles = policyDrivers.flatMap((person) =>
    Array.isArray(person.vehicles) ? person.vehicles : []
  );
  const activeDiscounts = returnedVehicles.flatMap((item) =>
    (item.discounts || []).flatMap((discount) =>
      Object.entries(discount)
        .filter(([name, value]) => name !== "discountId" && value === true)
        .map(([name]) => name.replace(/([A-Z])/g, " $1").replace(/^./, (letter) => letter.toUpperCase()))
    )
  );
  const displayedDrivers = (drivers.length ? drivers : policyDrivers).map(
    (person, index) => ({
      ...person,
      ...(policyDrivers[index] || {}),
      vehicles: policyDrivers[index]?.vehicles || person.vehicles || [],
      role: `Driver ${index + 1}`,
    })
  );
  const selectedCoverageSummary = buildCoverageSummary(displayedDrivers);
  const coverageDetails = policy?.coverageDetails || policy?.coverageType || policy?.coverage || selectedCoverageSummary;
  const customerSummaryDriver = displayedDrivers.find((person) => person.namedInsured) || displayedDrivers[0] || {};

  const changeDriverPart = (index) => (event) => {
    const { name, value } = event.target;

    setDrivers((current) => current.map((person, itemIndex) =>
      itemIndex === index ? { ...person, [name]: value } : person
    ));
    setDriverErrors((current) => current.map((errors, itemIndex) => {
      if (itemIndex !== index || !errors?.[name]) return errors || {};
      const next = { ...errors };
      delete next[name];
      return next;
    }));
  };

  const addDriver = () => {
    if (drivers.length >= 6) return;
    setDrivers((current) => [...current, emptyDriver()]);
    setDriverErrors((current) => [...current, {}]);
    setVehicleErrors((current) => [...current, []]);
  };

  const removeDriver = (index) => {
    if (index === 0) return;
    setDrivers((current) => current.filter((_, itemIndex) => itemIndex !== index));
    setDriverErrors((current) => current.filter((_, itemIndex) => itemIndex !== index));
    setVehicleErrors((current) => current.filter((_, itemIndex) => itemIndex !== index));
  };

  const addVehicle = (driverIndex) => {
    if (drivers[driverIndex]?.vehicles.length >= 6) return;
    setDrivers((current) => current.map((person, index) => index === driverIndex
      ? { ...person, vehicles: [...person.vehicles, emptyVehicle()] }
      : person
    ));
    setVehicleErrors((current) => current.map((items, index) => index === driverIndex
      ? [...(items || []), {}]
      : items || []
    ));
  };

  const removeVehicle = (driverIndex, vehicleIndex) => {
    if (driverIndex === 0 && vehicleIndex === 0) return;
    setDrivers((current) => current.map((person, index) => index === driverIndex
      ? { ...person, vehicles: person.vehicles.filter((_, itemIndex) => itemIndex !== vehicleIndex) }
      : person
    ));
    setVehicleErrors((current) => current.map((items, index) => index === driverIndex
      ? (items || []).filter((_, itemIndex) => itemIndex !== vehicleIndex)
      : items || []
    ));
  };

  const changeVehiclePart = (driverIndex, vehicleIndex) => (event) => {
    const { name, value } = event.target;
    setDrivers((current) => current.map((person, index) => index === driverIndex
      ? { ...person, vehicles: person.vehicles.map((item, itemIndex) =>
        itemIndex === vehicleIndex ? { ...item, [name]: value } : item
      ) }
      : person
    ));
    setVehicleErrors((current) => current.map((items, index) => index === driverIndex
      ? (items || []).map((errors, itemIndex) => {
        if (itemIndex !== vehicleIndex || !errors?.[name]) return errors || {};
        const next = { ...errors };
        delete next[name];
        return next;
      })
      : items || []
    ));
  };

  const changeDiscount = (driverIndex, vehicleIndex, name, checked) => {
    setDrivers((current) => current.map((person, index) => index === driverIndex
      ? { ...person, vehicles: person.vehicles.map((vehicle, itemIndex) => itemIndex === vehicleIndex
        ? { ...vehicle, discounts: [{ ...(vehicle.discounts?.[0] || emptyDiscount()), [name]: checked }] }
        : vehicle
      ) }
      : person
    ));
  };

  const changeCoverageSelection = (driverIndex, vehicleIndex, coverageType, checked) => {
    setDrivers((current) => current.map((person, index) => {
      if (index !== driverIndex) return person;
      return {
        ...person,
        vehicles: person.vehicles.map((vehicle, itemIndex) => {
          if (itemIndex !== vehicleIndex) return vehicle;
          const coverages = normalizeCoverages(vehicle.coverages);
          if (!checked) {
            return {
              ...vehicle,
              coverages: coverages.filter((item) => item.coverageType !== coverageType),
            };
          }
          if (coverages.some((item) => item.coverageType === coverageType)) {
            return vehicle;
          }
          return {
            ...vehicle,
            coverages: [
              ...coverages,
              {
                coverageType,
                coverageLimit: "",
              },
            ],
          };
        }),
      };
    }));
  };

  const changeCoverageValue = (driverIndex, vehicleIndex, coverageType, field, value) => {
    setDrivers((current) => current.map((person, index) => {
      if (index !== driverIndex) return person;
      return {
        ...person,
        vehicles: person.vehicles.map((vehicle, itemIndex) => {
          if (itemIndex !== vehicleIndex) return vehicle;
          return {
            ...vehicle,
            coverages: normalizeCoverages(vehicle.coverages).map((coverage) =>
              coverage.coverageType === coverageType
                ? { ...coverage, [field]: value }
                : coverage
            ),
          };
        }),
      };
    }));
    setVehicleErrors((current) => current.map((items, index) => index === driverIndex
      ? (items || []).map((vehicleError, itemIndex) => {
        if (itemIndex !== vehicleIndex) return vehicleError || {};
        const next = { ...(vehicleError || {}) };
        delete next[`coverage-${coverageType}-${field}`];
        if (Object.keys(next).length === 0) return {};
        return next;
      })
      : items || []
    ));
  };

  const validateCoverages = (vehicle) => {
    const errors = {};
    const coverages = normalizeCoverages(vehicle.coverages);
    if (!coverages.length) {
      errors.coverages = "Select at least one coverage for this vehicle.";
      return errors;
    }

    const seen = new Set();
    for (const coverage of coverages) {
      const type = coverage.coverageType;
      if (!type) {
        errors.coverages = "Coverage type is required.";
        continue;
      }
      if (seen.has(type)) {
        errors[`coverage-${type}-coverageType`] = "Duplicate coverage type is not allowed for the same vehicle.";
      }
      seen.add(type);

      const coverageLimit = Number(coverage.coverageLimit);
      if (coverage.coverageLimit === "" || !Number.isFinite(coverageLimit) || coverageLimit <= 0) {
        errors[`coverage-${type}-coverageLimit`] = "Coverage limit must be greater than 0.";
      }
    }
    return errors;
  };

  // STEP 1
  const handleDriverSubmit = (event) => {
    event.preventDefault();

    const errors = drivers.map(validateDriver);
    setDriverErrors(errors);

    if (errors.some((item) => Object.keys(item).length)) {
      return;
    }

    setError("");
    setStep(1);
  };

  // STEP 2
  const handleVehicleSubmit = async (event) => {
    event.preventDefault();

    const errors = drivers.map((person) => person.vehicles.map((vehicle) => ({
      ...validateVehicle(vehicle),
      ...validateCoverages(vehicle),
    })));
    setVehicleErrors(errors);
    if (errors.some((items) => items.some((item) => Object.keys(item).length))) return;
    if (!drivers[0]?.vehicles.length) {
      setError("Add a Primary Vehicle for the primary driver to continue.");
      return;
    }
    if (submissionLock.current) return;
    submissionLock.current = true;

    setLoading(true);
    setError("");

    try {
      const policyRequest = drivers.map(toApiDriver);

      console.log("Policy request:", { drivers: policyRequest });

      const coverageDetailsText = buildCoverageSummary(drivers);

      const savedPolicy = await createPolicy(policyRequest, paymentMethod, {
        coverageDetails: coverageDetailsText || "Vehicle-level coverages selected",
      });

      console.log("Created policy:", savedPolicy);

      if (!savedPolicy) {
        throw new Error(
          "Policy service did not return a policy."
        );
      }

      if (!savedPolicy.policyId) {
        throw new Error(
          "Policy service did not return a policy ID."
        );
      }

      setPolicy(savedPolicy);

      const premiumAmount = Number(savedPolicy.premiumAmount);
      if (!Number.isFinite(premiumAmount) || premiumAmount <= 0) {
        throw new Error(
          "Policy service did not return a valid premium amount."
        );
      }
      setPremium({
        totalPremium: premiumAmount,
      });

      setStep(2);
    } catch (requestError) {
      console.error(
        "Policy creation failed:",
        requestError
      );

      setError(
        requestError.message ||
          "Could not issue the policy."
      );
    } finally {
      submissionLock.current = false;
      setLoading(false);
    }
  };

  const handlePaymentSubmit = async (event) => {
    event.preventDefault();

    if (submissionLock.current) return;
    submissionLock.current = true;

    setLoading(true);
    setError("");

    try {
      const result = await makePayment(
        policy.policyId,
        premium.totalPremium,
        paymentMethod
      );

      if (
        typeof result !== "string" ||
        !/payment successful/i.test(result)
      ) {
        throw new Error(
          typeof result === "string"
            ? result
            : "Payment service did not confirm the payment."
        );
      }

      // Fetch the complete policy details (policy info, premium, payment
      // method, drivers, vehicles, discounts) using the returned policyId.
      const updatedPolicy = await getPolicyDetails(policy.policyId)
        .catch(() => getPolicy(policy.policyId))
        .catch(() => policy);
      setPolicy(updatedPolicy);

      const returnedDrivers = Array.isArray(updatedPolicy?.driver)
        ? updatedPolicy.driver
        : [];

      const details = {
        policy: updatedPolicy,
        customer,
        drivers: returnedDrivers.length ? returnedDrivers : displayedDrivers,
        paymentMethod,
        paymentResult: result,
        payment: null,
      };

      try {
        const payments = await getPolicyPayments(policy.policyId);
        details.payment = Array.isArray(payments) ? payments[0] || null : null;
      } catch {
        details.payment = null;
      }

      sessionStorage.removeItem("ltm.resumePolicy");
      sessionStorage.setItem("ltm.finalPolicy", JSON.stringify(details));
      setFinalPolicyDetails(details);
      window.history.pushState({ policyDetails: true }, "", "/policy-details");
      setStep(5);
    } catch (requestError) {
      setError(
        requestError.message || "Could not process the payment."
      );
    } finally {
      submissionLock.current = false;
      setLoading(false);
    }
  };

  return (
    <main className="quote-page">
      {/* HEADER */}

      <header className="quote-topbar">
        <a
          className="quote-brand"
          href="/"
          aria-label="LTM home"
        >
          <span aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="m3 10 9-7 9 7" />
              <path d="M5 9v12h14V9" />
              <path d="M9 21v-7h6v7" />
            </svg>
          </span>
          <span className="quote-brand-copy">
            <strong>LTM</strong>
            <small>INSURANCE PORTAL</small>
          </span>
        </a>

        <div className="quote-user">
          <span>
            {customer.firstName
              ? `Hello, ${customer.firstName}`
              : "Your insurance"}
          </span>

          <button
            className="quote-logout"
            type="button"
            onClick={() => {
              void logout().catch(() => undefined).finally(() => navigate("/login", { replace: true }));
            }}
          >
            Sign out
          </button>
        </div>
      </header>

      {/* MAIN CONTENT */}

      <section
        className="quote-workspace"
        aria-labelledby="quote-title"
      >
        <div className="quote-heading">
          <p className="quote-eyebrow">
            AUTO INSURANCE
          </p>

          <h1 id="quote-title">
            Build your policy
          </h1>

          <p>
            Share a few details to create your
            insurance policy.
          </p>
        </div>

        {/* STEPS */}

        <nav
          className="quote-steps"
          aria-label="Policy creation steps"
        >
          {steps.map((label, index) => (
            <div
              key={label}
              aria-current={step === index ? "step" : undefined}
              className={`quote-step${
                step === index
                  ? " is-current"
                  : ""
              }${
                step > index
                  ? " is-complete"
                  : ""
              }`}
            >
              <span>
                {step > index
                  ? "✓"
                  : index + 1}
              </span>

              <p>{label}</p>
            </div>
          ))}
        </nav>

        <div className="quote-panel">
          {/* ERROR */}

          {error && (
            <div
              className="quote-alert"
              role="alert"
            >
              {error}
            </div>
          )}

          {/* STEP 1: DRIVER */}

          {step === 0 && (
            <form noValidate onSubmit={handleDriverSubmit}>
              <div className="quote-panel-heading">
                <p className="quote-eyebrow">01 / DRIVER DETAILS</p>
                <h2>Drivers and named insured</h2>
                <p>Customer details already present in your profile are prefilled. The first driver is the Named Insured.</p>
              </div>

              {drivers.map((person, index) => (
                <section className="quote-driver-card" key={`driver-${index}`} aria-labelledby={`driver-title-${index}`}>
                  <header className="quote-driver-card-heading">
                    <div>
                      <h3 id={`driver-title-${index}`}>Driver {index + 1}</h3>
                      <p>{index === 0 ? "Primary Driver / Named Insured" : "Additional Driver"}</p>
                    </div>
                    <div className="quote-driver-heading-actions">
                      {index === 0 ? (
                        <><span className="quote-driver-badge is-primary">Primary Driver</span><span className="quote-driver-badge">Named Insured</span></>
                      ) : (
                        <>
                          <span className="quote-driver-badge">Additional Driver</span>
                          <button type="button" className="quote-remove-driver" onClick={() => removeDriver(index)} aria-label={`Remove Driver ${index + 1}`}>Remove</button>
                        </>
                      )}
                    </div>
                  </header>
                  <h4 className="quote-form-section-title">Personal information</h4>
                  <div className="quote-fields">
                    <FormField label="First name" name="firstName" value={person.firstName} onChange={changeDriverPart(index)} autoComplete="given-name" required error={driverErrors[index]?.firstName} id={`driver-${index}-firstName`} readOnly={index === 0 && Boolean(customer.firstName)} />
                    <FormField label="Last name" name="lastName" value={person.lastName} onChange={changeDriverPart(index)} autoComplete="family-name" required error={driverErrors[index]?.lastName} id={`driver-${index}-lastName`} readOnly={index === 0 && Boolean(customer.lastName)} />
                    <FormField label="Date of birth" name="dateOfBirth" type="date" value={person.dateOfBirth} onChange={changeDriverPart(index)} required error={driverErrors[index]?.dateOfBirth} id={`driver-${index}-dateOfBirth`} readOnly={index === 0 && Boolean(customer.dateOfBirth)} />
                    <FormField label="Email address" name="email" type="email" value={person.email} onChange={changeDriverPart(index)} autoComplete="email" required error={driverErrors[index]?.email} id={`driver-${index}-email`} readOnly={index === 0 && Boolean(customer.email)} />
                    <FormField label="Phone number" name="phoneNumber" type="tel" value={person.phoneNumber} onChange={changeDriverPart(index)} autoComplete="tel" required error={driverErrors[index]?.phoneNumber} id={`driver-${index}-phoneNumber`} readOnly={index === 0 && Boolean(customer.phoneNumber)} />
                    <FormField label="Street address" name="address" value={person.address} onChange={changeDriverPart(index)} autoComplete="street-address" required error={driverErrors[index]?.address} id={`driver-${index}-address`} readOnly={index === 0 && Boolean(customer.address)} />
                    <FormField label="City" name="city" value={person.city} onChange={changeDriverPart(index)} autoComplete="address-level2" required error={driverErrors[index]?.city} id={`driver-${index}-city`} readOnly={index === 0 && Boolean(customer.city)} />
                    <SelectField label="State" name="state" value={person.state} onChange={changeDriverPart(index)} options={states} required error={driverErrors[index]?.state} id={`driver-${index}-state`} disabled={index === 0 && Boolean(customer.state)} />
                    <FormField label="ZIP code" name="zipCode" value={person.zipCode} onChange={changeDriverPart(index)} autoComplete="postal-code" required error={driverErrors[index]?.zipCode} id={`driver-${index}-zipCode`} readOnly={index === 0 && Boolean(customer.zipCode)} />
                    <SelectField label="Gender" name="gender" value={person.gender} onChange={changeDriverPart(index)} options={genderOptions} id={`driver-${index}-gender`} />
                    <SelectField label="Marital status" name="maritalStatus" value={person.maritalStatus} onChange={changeDriverPart(index)} options={maritalStatuses} id={`driver-${index}-maritalStatus`} />
                  </div>
                  <h4 className="quote-form-section-title">License information</h4>
                  <div className="quote-fields">
                    <FormField label="Driving license number" name="licenseNumber" value={person.licenseNumber} onChange={changeDriverPart(index)} required error={driverErrors[index]?.licenseNumber} id={`driver-${index}-licenseNumber`} readOnly={index === 0 && Boolean(customer.licenseNumber)} />
                    <SelectField label="License state" name="licenseState" value={person.licenseState} onChange={changeDriverPart(index)} options={states} required error={driverErrors[index]?.licenseState} id={`driver-${index}-licenseState`} />
                    <FormField label="License expiration date" name="licenseExpirationDate" type="date" value={person.licenseExpirationDate} onChange={changeDriverPart(index)} required error={driverErrors[index]?.licenseExpirationDate} id={`driver-${index}-licenseExpirationDate`} />
                  </div>
                  <h4 className="quote-form-section-title">Driving information</h4>
                  <div className="quote-fields">
                    <FormField label="Driving history" name="drivingHistory" value={person.drivingHistory} onChange={changeDriverPart(index)} id={`driver-${index}-drivingHistory`} />
                    <FormField label="Insurance history" name="insuranceHistory" value={person.insuranceHistory} onChange={changeDriverPart(index)} id={`driver-${index}-insuranceHistory`} />
                    <SelectField label="Credit score" name="creditScore" value={person.creditScore} onChange={changeDriverPart(index)} options={creditScores} id={`driver-${index}-creditScore`} />
                  </div>
                </section>
              ))}

              <div className="quote-add-control">
                <button type="button" className="quote-add-button" onClick={addDriver} disabled={drivers.length >= 6}>
                  <span aria-hidden="true">+</span> Add Driver
                </button>
                {drivers.length >= 6 && <p className="quote-limit-note">Maximum 6 drivers allowed</p>}
              </div>

              <FormActions loading={loading} onBack={() => navigate("/customer-summary")} nextLabel="Continue to vehicles" />
            </form>
          )}

          {/* STEP 2: VEHICLE */}

          {step === 1 && (
            <form noValidate onSubmit={handleVehicleSubmit}>
              <div className="quote-panel-heading">
                <p className="quote-eyebrow">02 / VEHICLE DETAILS</p>
                <h2>Vehicle details</h2>
                <p>Vehicles are saved under the driver they belong to. Add up to six vehicles per driver.</p>
              </div>

              {drivers.map((person, driverIndex) => (
                <section className="quote-driver-card" key={`vehicles-driver-${driverIndex}`} aria-labelledby={`vehicles-driver-title-${driverIndex}`}>
                  <header className="quote-driver-card-heading">
                    <div>
                      <h3 id={`vehicles-driver-title-${driverIndex}`}>Driver {driverIndex + 1}: {`${person.firstName} ${person.lastName}`.trim() || "Driver details"}</h3>
                      <p>{driverIndex === 0 ? "Primary Driver / Named Insured" : "Additional Driver"}</p>
                    </div>
                    {driverIndex === 0 && <span className="quote-driver-badge is-primary">Primary Driver</span>}
                  </header>
                  {person.vehicles.map((vehicle, vehicleIndex) => (
                    <section className="quote-vehicle-card" key={`vehicle-${driverIndex}-${vehicleIndex}`} aria-labelledby={`vehicle-title-${driverIndex}-${vehicleIndex}`}>
                      <header className="quote-driver-card-heading">
                        <div className="quote-driver-heading-actions">
                          <h4 id={`vehicle-title-${driverIndex}-${vehicleIndex}`}>Vehicle {vehicleIndex + 1}</h4>
                          {driverIndex === 0 && vehicleIndex === 0 && <span className="quote-driver-badge is-primary">Primary Vehicle</span>}
                        </div>
                        {!(driverIndex === 0 && vehicleIndex === 0) && (
                          <button type="button" className="quote-remove-driver" onClick={() => removeVehicle(driverIndex, vehicleIndex)} aria-label={`Remove Vehicle ${vehicleIndex + 1} for Driver ${driverIndex + 1}`}>Remove</button>
                        )}
                      </header>
                      <div className="quote-fields">
                        <FormField label="Make" name="make" value={vehicle.make} onChange={changeVehiclePart(driverIndex, vehicleIndex)} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.make} id={`vehicle-${driverIndex}-${vehicleIndex}-make`} />
                        <FormField label="Model" name="model" value={vehicle.model} onChange={changeVehiclePart(driverIndex, vehicleIndex)} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.model} id={`vehicle-${driverIndex}-${vehicleIndex}-model`} />
                        <FormField label="Model year" name="year" type="number" min="1980" max={new Date().getFullYear() + 1} value={vehicle.year} onChange={changeVehiclePart(driverIndex, vehicleIndex)} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.year} id={`vehicle-${driverIndex}-${vehicleIndex}-year`} />
                        <FormField label="VIN" name="vin" value={vehicle.vin} onChange={changeVehiclePart(driverIndex, vehicleIndex)} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.vin} id={`vehicle-${driverIndex}-${vehicleIndex}-vin`} />
                        <FormField label="License plate" name="licensePlate" value={vehicle.licensePlate} onChange={changeVehiclePart(driverIndex, vehicleIndex)} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.licensePlate} id={`vehicle-${driverIndex}-${vehicleIndex}-plate`} />
                        <SelectField label="Registration state" name="registrationState" value={vehicle.registrationState} onChange={changeVehiclePart(driverIndex, vehicleIndex)} options={states} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.registrationState} id={`vehicle-${driverIndex}-${vehicleIndex}-registrationState`} />
                        <FormField label="Registration expiration date" name="registrationExpirationDate" type="date" value={vehicle.registrationExpirationDate} onChange={changeVehiclePart(driverIndex, vehicleIndex)} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.registrationExpirationDate} id={`vehicle-${driverIndex}-${vehicleIndex}-registrationExpirationDate`} />
                        <SelectField label="Vehicle type" name="vehicleType" value={vehicle.vehicleType} onChange={changeVehiclePart(driverIndex, vehicleIndex)} options={vehicleTypes} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.vehicleType} id={`vehicle-${driverIndex}-${vehicleIndex}-type`} />
                        <SelectField label="Usage type" name="usageType" value={vehicle.usageType} onChange={changeVehiclePart(driverIndex, vehicleIndex)} options={usageTypes} required error={vehicleErrors[driverIndex]?.[vehicleIndex]?.usageType} id={`vehicle-${driverIndex}-${vehicleIndex}-usage`} />
                      </div>
                      <fieldset className="quote-discounts">
                        <legend>Discount eligibility</legend>
                        {discountFields.map(([name, label]) => (
                          <label key={name} className="quote-discount-option">
                            <input type="checkbox" checked={Boolean(vehicle.discounts?.[0]?.[name])} onChange={(event) => changeDiscount(driverIndex, vehicleIndex, name, event.target.checked)} />
                            <span>{label}</span>
                          </label>
                        ))}
                      </fieldset>
                      <fieldset className="quote-discounts quote-coverages">
                        <legend>Coverages</legend>
                        {vehicleErrors[driverIndex]?.[vehicleIndex]?.coverages && (
                          <p className="quote-field-error">{vehicleErrors[driverIndex][vehicleIndex].coverages}</p>
                        )}
                        <div className="quote-coverage-grid">
                          {coverageOptions.map((option) => {
                            const selectedCoverage = normalizeCoverages(vehicle.coverages).find((coverage) => coverage.coverageType === option.type);
                            return (
                              <section className="quote-coverage-card" key={option.type}>
                                <label className="quote-discount-option quote-coverage-toggle">
                                  <input
                                    type="checkbox"
                                    checked={Boolean(selectedCoverage)}
                                    onChange={(event) => changeCoverageSelection(driverIndex, vehicleIndex, option.type, event.target.checked)}
                                  />
                                  <span>{option.label}</span>
                                </label>
                                {selectedCoverage && (
                                  <div className="quote-coverage-fields">
                                    <FormField
                                      label="Coverage limit"
                                      name={`coverage-${option.type}-limit`}
                                      type="number"
                                      min="0.01"
                                      step="0.01"
                                      value={selectedCoverage.coverageLimit}
                                      onChange={(event) => changeCoverageValue(driverIndex, vehicleIndex, option.type, "coverageLimit", event.target.value)}
                                      required
                                      error={vehicleErrors[driverIndex]?.[vehicleIndex]?.[`coverage-${option.type}-coverageLimit`]}
                                      id={`coverage-${driverIndex}-${vehicleIndex}-${option.type}-limit`}
                                    />
                                  </div>
                                )}
                              </section>
                            );
                          })}
                        </div>
                      </fieldset>
                    </section>
                  ))}
                  <div className="quote-add-control">
                    <button type="button" className="quote-add-button" onClick={() => addVehicle(driverIndex)} disabled={person.vehicles.length >= 6}>
                      <span aria-hidden="true">+</span> Add Vehicle
                    </button>
                    {person.vehicles.length >= 6 && <p className="quote-limit-note">Maximum 6 vehicles for this driver</p>}
                  </div>
                </section>
              ))}
              <FormActions
                loading={loading}
                onBack={() => setStep(0)}
                nextLabel="Create quote"
              />
            </form>
          )}

          {/* STEP 3: PREMIUM */}

          {step === 2 && policy && (
            <div>
              <div className="quote-panel-heading">
                <p className="quote-eyebrow">03 / COVERAGE &amp; DISCOUNTS</p>
                <h2>Coverage and discounts</h2>
                <p>Review the coverage and discount information returned by the existing policy service.</p>
              </div>
              <div className="quote-summary-groups">
                <section className="quote-summary-group">
                  <h3>Coverage</h3>
                  <dl className="quote-summary">
                    <SummaryItem label="Coverage details" value={coverageDetails || "No coverage selection returned by the policy service"} />
                    <SummaryItem label="Policy type" value={policy.policyType || "Not available"} />
                  </dl>
                </section>
                <section className="quote-summary-group">
                  <h3>Discounts</h3>
                  <dl className="quote-summary">
                    <SummaryItem label="Active discounts" value={activeDiscounts.length ? activeDiscounts.join(", ") : "No active discounts returned"} />
                  </dl>
                </section>
              </div>
              <FormActions loading={false} onBack={() => setStep(1)} nextLabel="Review customer summary" onNext={() => setStep(3)} />
            </div>
          )}

          {step === 3 && policy && (
            <div>
              <div className="quote-panel-heading">
                <p className="quote-eyebrow">04 / CUSTOMER SUMMARY</p>
                <h2>Customer Summary</h2>
                <p>Review the customer, drivers, vehicles and quote information before payment.</p>
              </div>
              <div className="quote-summary-groups">
                <section className="quote-summary-group">
                  <h3>Customer</h3>
                  <dl className="quote-summary">
                    <SummaryItem label="Customer ID" value={customer.id || "Not available"} />
                    <SummaryItem label="Name" value={`${customer.firstName || ""} ${customer.lastName || ""}`.trim() || "Not available"} />
                    <SummaryItem label="Email" value={customer.email || "Not available"} />
                    <SummaryItem label="Date of birth" value={displayDate(customer.dateOfBirth || customerSummaryDriver.dateOfBirth)} />
                    <SummaryItem label="License number" value={customer.licenseNumber || customerSummaryDriver.licenseNumber || "Not available"} />
                  </dl>
                  <p className="quote-summary-role"><span className="quote-driver-badge">Named Insured</span></p>
                </section>
                <section className="quote-summary-group">
                  <h3>Primary and additional drivers</h3>
                  {displayedDrivers.length ? <DriverSummaryCards drivers={displayedDrivers} /> : <p className="quote-inline-note">No drivers added.</p>}
                </section>
                <section className="quote-summary-group">
                  <h3>Coverage and discounts</h3>
                  <dl className="quote-summary">
                    <SummaryItem label="Coverage" value={coverageDetails || "Not returned by the policy service"} />
                    <SummaryItem label="Discounts" value={activeDiscounts.length ? activeDiscounts.join(", ") : "No active discounts returned"} />
                  </dl>
                </section>
                <section className="quote-summary-group">
                  <h3>Quote and policy</h3>
                  <dl className="quote-summary">
                    <SummaryItem label="Policy number" value={policy.policyId} />
                    <SummaryItem label="Status" value={policy.policyStatus || "Not available"} />
                    <SummaryItem label="Effective date" value={displayDate(policy.effectiveDate)} />
                    <SummaryItem label="Expiration date" value={displayDate(policy.expirationDate)} />
                    <SummaryItem label="Created date" value={displayDate(policy.createdDate)} />
                    <SummaryItem label="Base premium" value={currency(policy.basePremium)} />
                    <SummaryItem label="Driver adjustment" value={currency(policy.driverAdjustment)} />
                    <SummaryItem label="Vehicle adjustment" value={currency(policy.vehicleAdjustment)} />
                    <SummaryItem label="Discount amount" value={currency(policy.totalDiscount)} />
                    <SummaryItem label="Premium" value={currency(policy.premiumAmount)} />
                    <SummaryItem label="Final premium" value={currency(policy.finalPremium ?? policy.premiumAmount)} prominent />
                  </dl>
                </section>
              </div>
              <FormActions loading={false} onBack={() => setStep(2)} nextLabel="Continue to payment" onNext={() => setStep(4)} />
            </div>
          )}

          {step === 4 && policy && (
            <form onSubmit={handlePaymentSubmit}>
              <div className="quote-panel-heading">
                <p className="quote-eyebrow">05 / PAYMENT</p>
                <h2>Select payment method</h2>
              </div>
              <div className="quote-payment-overview">
                <div><span>Policy number</span><strong>{policy.policyId}</strong></div>
                <div><span>Amount due</span><strong>{currency(premium?.totalPremium)}</strong></div>
              </div>
              <section className="quote-driver-card" aria-labelledby="payment-method-title">
                <header className="quote-driver-card-heading">
                  <div>
                    <h3 id="payment-method-title">Payment method</h3>
                    <p>Choose how you want to pay the premium. Defaults to card.</p>
                  </div>
                </header>
                <div className="quote-fields">
                  <label className="quote-field">
                    <span className="quote-label-row"><span>Payment method</span><span className="quote-required">Required</span></span>
                    <select className="quote-input" value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}>
                      {paymentMethods.map((method) => <option key={method} value={method}>{method.replaceAll("_", " ")}</option>)}
                    </select>
                  </label>
                </div>
              </section>
              <FormActions loading={loading} onBack={() => setStep(3)} nextLabel="Make payment" />
            </form>
          )}

          {step === 5 && finalPolicyDetails && (
            <PolicyDetails
              details={finalPolicyDetails}
              onBack={() => navigate("/customer-summary")}
            />
          )}
        </div>

        <p className="quote-footnote">Your primary driver is submitted first. Additional drivers and their vehicles remain separately associated in the policy request.</p>
      </section>
    </main>
  );
}

function FormField({
  label,
  name,
  value,
  onChange,
  type = "text",
  min,
  max,
  step,
  autoComplete,
  required = false,
  readOnly = false,
  error,
  id,
}) {
  return (
    <label className="quote-field">
      <span className="quote-label-row">
        <span>{label}</span>
        {required && <span className="quote-required">Required</span>}
      </span>

      <input
        className="quote-input"
        id={id}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        min={min}
        max={max}
        step={step}
        autoComplete={autoComplete}
        required={required}
        readOnly={readOnly}
        aria-invalid={error ? "true" : undefined}
        aria-describedby={error && id ? `${id}-error` : undefined}
      />
      {error && <span className="quote-field-error" id={id ? `${id}-error` : undefined}>{error}</span>}
    </label>
  );
}

function SelectField({
  label,
  name,
  value,
  onChange,
  options,
  required = false,
  error,
  id,
  disabled = false,
}) {
  return (
    <label className="quote-field" htmlFor={id}>
      <span className="quote-label-row">
        <span>{label}</span>
        {required && <span className="quote-required">Required</span>}
      </span>
      <select
        className="quote-input"
        id={id}
        name={name}
        value={value}
        onChange={onChange}
        required={required}
        disabled={disabled}
        aria-invalid={error ? "true" : undefined}
        aria-describedby={error && id ? `${id}-error` : undefined}
      >
        <option value="">Select {label.toLowerCase()}</option>
        {options.map((option) => {
          const optionValue = typeof option === "string" ? option : option.value;
          const optionLabel = typeof option === "string" ? option : option.label;
          return <option key={optionValue} value={optionValue}>{optionLabel}</option>;
        })}
      </select>
      {error && <span className="quote-field-error" id={id ? `${id}-error` : undefined}>{error}</span>}
    </label>
  );
}

function FormActions({
  loading,
  onBack,
  nextLabel,
  onNext,
}) {
  return (
    <div className="quote-actions">
      <button
        type="button"
        className="quote-back"
        onClick={onBack}
        disabled={loading}
      >
        Back
      </button>

      {onNext ? (
        <button
          type="button"
          className="quote-continue"
          onClick={onNext}
          disabled={loading}
        >
          {nextLabel}
        </button>
      ) : (
        <button
          type="submit"
          className="quote-continue"
          disabled={loading}
        >
          {loading
            ? "Saving..."
            : nextLabel}
        </button>
      )}
    </div>
  );
}

function SummaryItem({
  label,
  value,
  prominent = false,
}) {
  return (
    <div
      className={
        prominent
          ? "quote-summary-item is-prominent"
          : "quote-summary-item"
      }
    >
      <dt>{label}</dt>
      <dd>{formatValue(value)}</dd>
    </div>
  );
}

function DriverSummaryCards({ drivers }) {
  return (
    <div className="quote-driver-list">
      {drivers.map((person, index) => {
        const fullName = `${person.firstName || ""} ${person.lastName || ""}`.trim();
        const isPrimary = index === 0;
        const vehicles = Array.isArray(person.vehicles) ? person.vehicles : [];

        return (
          <article
            className="quote-driver-summary-card"
            key={`${person.memberId || person.email || person.role}-${index}`}
          >
            <header className="quote-driver-summary-heading">
              <span className={`quote-driver-badge${isPrimary ? " is-primary" : ""}`}>{person.role || `Driver ${index + 1}`}</span>
              {isPrimary && <span className="quote-driver-badge">Named Insured</span>}
              <strong>{fullName || "Driver details pending"}</strong>
            </header>

            <div className="quote-driver-summary-meta">
              {person.email && <span>{person.email}</span>}
              {person.memberId && <span>Member ID {person.memberId}</span>}
            </div>

            <dl className="quote-driver-summary-details">
              <SummaryItem label="Date of birth" value={displayDate(person.dateOfBirth)} />
              <SummaryItem label="License number" value={person.licenseNumber || "Not available"} />
            </dl>
            <div className="quote-summary-vehicles">
              <h4>Vehicles</h4>
              {vehicles.length ? vehicles.map((vehicle, vehicleIndex) => {
                const discounts = (vehicle.discounts || []).flatMap((discount) =>
                  Object.entries(discount)
                    .filter(([name, enabled]) => name !== "discountId" && enabled === true)
                    .map(([name]) => name.replace(/([A-Z])/g, " $1").replace(/^./, (letter) => letter.toUpperCase()))
                );
                return (
                  <div className="quote-summary-vehicle" key={`${vehicle.vin || vehicle.licensePlate || vehicleIndex}`}>
                    <div className="quote-driver-summary-heading">
                      <span className={`quote-driver-badge${isPrimary && vehicleIndex === 0 ? " is-primary" : ""}`}>Vehicle {vehicleIndex + 1}</span>
                      {isPrimary && vehicleIndex === 0 && <span className="quote-driver-badge">Primary Vehicle</span>}
                      <strong>{`${vehicle.year || ""} ${vehicle.make || ""} ${vehicle.model || ""}`.trim() || "Vehicle details"}</strong>
                    </div>
                    <dl className="quote-driver-summary-details">
                      <SummaryItem label="Vehicle type" value={vehicle.vehicleType} />
                      <SummaryItem label="VIN" value={vehicle.vin} />
                      <SummaryItem label="License plate" value={vehicle.licensePlate} />
                      <SummaryItem label="Applied discounts" value={discounts.length ? discounts.join(", ") : "None returned"} />
                      <SummaryItem
                        label="Coverages"
                        value={(vehicle.coverages || []).length
                          ? (vehicle.coverages || []).map((coverage) =>
                              `${coverageLabel(coverage.coverageType)} (Limit: ${formatValue(coverage.coverageLimit)})`
                            ).join(", ")
                          : "Not selected"}
                      />
                    </dl>
                  </div>
                );
              }) : <p className="quote-inline-note">No vehicles added for this driver.</p>}
            </div>
          </article>
        );
      })}
    </div>
  );
}

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
