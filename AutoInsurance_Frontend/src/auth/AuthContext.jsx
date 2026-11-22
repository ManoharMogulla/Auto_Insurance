import { useState } from "react";
import { AuthContext } from "./authContext.js";
import {
  clearCustomerSession,
  getCustomerSession,
  getCurrentCustomer,
  loginCustomer,
  logoutCustomer,
  registerCustomer,
  saveCustomerSession,
} from "../api/insuranceServices.js";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getCustomerSession());

  const establishSession = (response, fallbackEmail = "") => {
    const session = saveCustomerSession(response, fallbackEmail);
    if (!session.token) {
      clearCustomerSession();
      throw new Error("Authentication service did not return a JWT.");
    }
    setUser(session);
    return session;
  };

  const login = async (credentials) => establishSession(await loginCustomer(credentials), credentials.email);
  const register = async (details) => establishSession(await registerCustomer(details), details.email);

  const refreshMe = async () => {
    const identity = await getCurrentCustomer();
    const session = { ...getCustomerSession(), ...identity, authenticated: true };
    sessionStorage.setItem("ltm.customer", JSON.stringify(session));
    setUser(session);
    return session;
  };

  const logout = async () => {
    try {
      await logoutCustomer();
    } finally {
      clearCustomerSession();
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{
      user,
      authenticated: Boolean(user?.token && user?.authenticated),
      login,
      register,
      logout,
      refreshMe,
    }}>
      {children}
    </AuthContext.Provider>
  );
}
