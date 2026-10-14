import { createBrowserRouter, RouterProvider, Link } from "react-router-dom";

import GettingStarted from "./pages/GettingStarted";
import Login from "./pages/Login";
import NotFound from "./pages/NotFound";
import Shifts from "./pages/Shifts";
import Hours from "./pages/Hours";
import Home from "./pages/Home";
import Calendar from "./pages/Calendar";
import ProtectedRoute from "./components/ProtectedRoute";
import RoleProtectedRoute from "./components/RoleProtectedRoute";
import SupervisorLayout from "./components/SupervisorLayout";
import SupervisorDashboard from "./pages/SupervisorDashboard";
import SupervisorCorrections from "./pages/SupervisorCorrections";
import SupervisorSchedule from "./pages/SupervisorSchedule";
import SupervisorAvailability from "./pages/SupervisorAvailability";

/*
 * We use the react-router-dom library to load pages based
 * on path. This object configures which paths map to which
 * page.
 *
 * Matching happens from top to bottom; the last path is "*",
 * which is the fall-through case when no other path matches.
 */
const router = createBrowserRouter([
  {
    path: "/",
    element:(
    <ProtectedRoute>
      <Home />
    </ProtectedRoute>
    ),
  },
  {
    path: "/login",
    element: <Login />,
  },
  {
  path: "/shifts",
  element: (
    <ProtectedRoute>
      <Shifts />
    </ProtectedRoute>
    ),
  },
  {
  path: "/hours",
  element: (
    <ProtectedRoute>
      <Hours />
    </ProtectedRoute>
    ),
  },
  {
  path: "/calendar",
  element: (
    <ProtectedRoute>
      <Calendar />
    </ProtectedRoute>
    ),
  },
  {
  path: "/supervisor",
  element: (
    <RoleProtectedRoute requiredRole="ADMIN">
      <SupervisorLayout />
    </RoleProtectedRoute>
  ),
  children: [
    { index: true, element: <SupervisorDashboard /> },
    {
      path: "schedule",
      element: <SupervisorSchedule />,
    },
    {
      path: "corrections",
      element: <SupervisorCorrections />,
    },
    {
      path: "availability",
      element: <SupervisorAvailability />,
    },
  ],
},
  {
    path: "*",
    element: <NotFound />,
  },

]);

function App() {
  return (
    <>
      <RouterProvider router={router} />
    </>
  );
}

export default App;
