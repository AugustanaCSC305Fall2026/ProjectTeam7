import { createBrowserRouter, RouterProvider } from "react-router-dom";

import Login from "./pages/Login";
import CreateAccount from "./pages/CreateAccount";
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
import SupervisorAssignments from "./pages/SupervisorAssignments";
import SupervisorJobs from "./pages/SupervisorJobs";
import Availability from "./pages/Availability";
import ManagerLayout from "./components/ManagerLayout";
import ManagerDashboard from "./pages/ManagerDashboard";
import ManagerEmployees from "./pages/ManagerEmployees";
import ManagerHours from "./pages/ManagerHours";
import ManagerShifts from "./pages/ManagerShifts";
import StudentJobs from "./pages/StudentJobs";

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
    path: "/create-account",
    element: <CreateAccount />,
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
    path: "/availability",
    element: (
      <ProtectedRoute>
        <Availability />
      </ProtectedRoute>
    ),
  },
  {
    path: "/jobs",
    element: (
      <ProtectedRoute>
        <StudentJobs />
      </ProtectedRoute>
    ),
  },
  {
  path: "/manager",
    element: (
      <RoleProtectedRoute requiredRole="MANAGER">
        <ManagerLayout />
      </RoleProtectedRoute>
    ),
    children: [
      { index: true, element: <ManagerDashboard /> },
      {
        path: "employees",
        element: <ManagerEmployees />,
      },
      {
        path: "hours",
        element: <ManagerHours />,
      },
      {
        path: "shifts",
        element: <ManagerShifts />,
      },
    ],
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
      path: "jobs",
      element: <SupervisorJobs />,
    },
    {
      path: "schedule",
      element: <SupervisorSchedule />,
    },
    {
      path: "assignments",
      element: <SupervisorAssignments />,
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
