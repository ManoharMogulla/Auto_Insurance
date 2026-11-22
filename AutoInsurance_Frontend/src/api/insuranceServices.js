import apiClient from "./apiClient.js";

const serviceUrls = {
  auth: "/api/auth",
  customer: "/customer",
  policy: "/api/policy",
  payment: "/api/payment",
  claims: "/api/claims",
  documents: "/api/documents",
};

async function request(baseUrl, path = "", options = {}) {
  try {
    const response = await apiClient.request({
      url: `${baseUrl.replace(/\/$/, "")}${path}`,
      method: options.method || "GET",
      data: options.body ? JSON.parse(options.body) : undefined,
      headers: options.headers,
    });
    return response.data;
  } catch (error) {
    const data = error.response?.data;
    throw new Error(data?.message || data?.detail || data?.error || error.message || "Request failed", { cause: error });
  }
}

const json = (method, body) => ({
  method,
  body: JSON.stringify(body),
});

export async function createPolicy(driverOrDrivers, paymentType = "CARD", policyTerms = {}) {
  const drivers = Array.isArray(driverOrDrivers)
    ? driverOrDrivers
    : [driverOrDrivers];
  const response = await request(
    serviceUrls.customer,
    "/addPolicy",
    json("POST", {
      drivers: drivers.map((driver, index) => ({
        ...driver,
        namedInsured: index === 0,
      })),
      paymentType,
      coverageDetails: policyTerms.coverageDetails,
    })
  );
  const match = typeof response === "string"
    ? response.match(/Policy Created Successfully with ID:\s*([\w-]+)/i)
    : null;

  if (!match) {
    throw new Error(typeof response === "string" ? response : "Customer service did not return a policy ID.");
  }

  const policyId = match[1];
  const policy = await getPolicyDetails(policyId);

  if (!policy) {
    throw new Error(`Policy service did not return a saved policy record for ${policyId}.`);
  }

  return policy;
}

export function loginCustomer(credentials) {
  return request(serviceUrls.auth, "/login", json("POST", credentials));
}

export function registerCustomer(details) {
  return request(serviceUrls.auth, "/register", json("POST", details));
}

export function getCurrentCustomer() {
  return request(serviceUrls.auth, "/me");
}

export function logoutCustomer() {
  return request(serviceUrls.auth, "/logout", json("POST", {}));
}

export function getAllPolicies() {
  return request(serviceUrls.customer, "/policies");
}

export function getPolicy(policyId) {
  return getPolicyDetails(policyId);
}

export function getPolicyDetails(policyId) {
  return request(
    serviceUrls.policy,
    `/policies/${encodeURIComponent(policyId)}`
  );
}

export function getPolicyPayments(policyId) {
  return request(
    serviceUrls.payment,
    `/${encodeURIComponent(policyId)}`
  );
}

export function makePayment(policyId, amount, paymentMethod) {
  return request(
    serviceUrls.policy,
    `/payments/${encodeURIComponent(policyId)}`,
    json("POST", {
      amount: Number(amount),
      paymentMethod,
    })
  );
}

export function createClaim(payload) {
  return request(
    serviceUrls.claims,
    "",
    json("POST", payload)
  );
}

export function getClaimsByCustomer(customerId) {
  return request(
    serviceUrls.claims,
    `/customer/${encodeURIComponent(customerId)}`
  );
}

export function getClaimsByPolicy(policyId) {
  return request(
    serviceUrls.claims,
    `/policy/${encodeURIComponent(policyId)}`
  );
}

export function getClaimById(claimId) {
  return request(
    serviceUrls.claims,
    `/${encodeURIComponent(claimId)}`
  );
}

export function updateClaimStatus(claimId, status) {
  return request(
    serviceUrls.claims,
    `/${encodeURIComponent(claimId)}/status`,
    json("PATCH", { status })
  );
}

export function saveCustomerSession(response, fallbackEmail = "", authenticated = true) {
  const result =
    response?.customer ||
    response?.user ||
    response?.data?.customer ||
    response?.data?.user ||
    response?.data ||
    response;

  const session = {
    authenticated,
    address: result?.address || "",
    city: result?.city || "",
    state: result?.state || "",
    zipCode: result?.zipCode || "",
    phoneNumber: result?.phoneNumber || "",
    id:
      result?.id ??
      result?.customerId ??
      result?.memberId ??
      result?.customerID ??
      "",
    memberId: result?.memberId ?? result?.id ?? "",
    customerId: result?.customerId ?? result?.memberId ?? result?.id ?? "",
    firstName: result?.firstName || "",
    lastName: result?.lastName || "",
    email: result?.email || fallbackEmail,
    dateOfBirth: result?.dateOfBirth || "",
    licenseNumber: result?.licenseNumber || "",
    licenseState: result?.licenseState || "",
    licenseExpirationDate: result?.licenseExpirationDate || "",
    gender: result?.gender || "",
    maritalStatus: result?.maritalStatus || "",
    drivingHistory: result?.drivingHistory || "",
    insuranceHistory: result?.insuranceHistory || "",
    creditScore: result?.creditScore || "",
    role: result?.role || "CUSTOMER",
    token:
      response?.token ||
      response?.accessToken ||
      response?.data?.token ||
      response?.data?.accessToken ||
      result?.token ||
      "",
  };

  sessionStorage.setItem(
    "ltm.customer",
    JSON.stringify(session)
  );

  return session;
}

export function getCustomerSession() {
  try {
    return JSON.parse(
      sessionStorage.getItem("ltm.customer") || "null"
    );
  } catch {
    return null;
  }
}

export function clearCustomerSession() {
  sessionStorage.removeItem("ltm.customer");
  sessionStorage.removeItem("ltm.finalPolicy");
  sessionStorage.removeItem("ltm.resumePolicy");
}

export function getPolicyDocuments(policyId) {
  return request(
    serviceUrls.documents,
    `/policies/${encodeURIComponent(policyId)}`
  );
}

export function generatePolicyDocument(policyId) {
  return request(
    serviceUrls.documents,
    `/policies/${encodeURIComponent(policyId)}/generate`,
    json("POST", {})
  );
}

export function generateAndEmailPolicyDocument(policyId) {
  return request(
    serviceUrls.documents,
    `/policies/${encodeURIComponent(policyId)}/generate-and-email`,
    json("POST", {})
  );
}

export function sendPolicyDocumentEmail(documentId, recipientEmail) {
  return request(
    serviceUrls.documents,
    `/${encodeURIComponent(documentId)}/email`,
    json("POST", { recipientEmail })
  );
}

export async function fetchPolicyDocumentPdf(documentId, disposition = "inline") {
  try {
    const response = await apiClient.request({
      url: `${serviceUrls.documents.replace(/\/$/, "")}/${encodeURIComponent(documentId)}/download`,
      method: "GET",
      params: { disposition },
      responseType: "blob",
    });
    return URL.createObjectURL(response.data);
  } catch (error) {
    const data = error.response?.data;
    throw new Error(data?.message || data?.detail || data?.error || error.message || "Unable to open policy document", { cause: error });
  }
}