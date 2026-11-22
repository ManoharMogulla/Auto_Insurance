import axios from "axios";

for (const key of ["customer", "finalPolicy", "resumePolicy"]) {
  const oldKey = `coverwell.${key}`;
  const newKey = `ltm.${key}`;
  if (sessionStorage.getItem(newKey) === null) {
    const oldValue = sessionStorage.getItem(oldKey);
    if (oldValue !== null) sessionStorage.setItem(newKey, oldValue);
  }
  sessionStorage.removeItem(oldKey);
}

const apiClient = axios.create({
  headers: { "Content-Type": "application/json" },
});

apiClient.interceptors.request.use((config) => {
  try {
    const session = JSON.parse(sessionStorage.getItem("ltm.customer") || "null");
    if (session?.token) {
      config.headers.Authorization = `Bearer ${session.token}`;
    }
  } catch {
    sessionStorage.removeItem("ltm.customer");
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url || "";
    const isPublicAuthRequest = /\/api\/auth\/(login|register)\/?$/.test(url);

    if (error.response?.status === 401 && !isPublicAuthRequest) {
      sessionStorage.removeItem("ltm.customer");
      sessionStorage.removeItem("ltm.finalPolicy");
      sessionStorage.removeItem("ltm.resumePolicy");
      if (window.location.pathname !== "/login") {
        window.location.assign("/login");
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
