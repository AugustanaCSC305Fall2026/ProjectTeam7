import { Navigate } from "react-router-dom";

function RoleProtectedRoute({ children, requiredRole }) {
  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  let user = null;

  try {
    user = JSON.parse(localStorage.getItem("user") || "null");
  } catch {
    user = null;
  }

  if (!user?.roles?.includes(requiredRole)) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default RoleProtectedRoute;