import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./authContext.js";

export default function ProtectedRoute({ children }) {
  const { authenticated } = useAuth();
  const location = useLocation();

  if (!authenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return children;
}
