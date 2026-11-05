import { Outlet } from "react-router-dom";
import StudentLayout from "./StudentLayout";

const supervisorNavItems = [
  { to: "/supervisor", label: "Dashboard", end: true },
  { to: "/supervisor/messages", label: "Messages" },
  { to: "/supervisor/availability", label: "Availability" },
  { to: "/supervisor/jobs", label: "Jobs & Management" },
  { to: "/supervisor/assignments", label: "Job Assignments" },
  { to: "/supervisor/schedule", label: "Schedule" },
  { to: "/supervisor/corrections", label: "Time Corrections" },
];

function SupervisorLayout({ children }) {
  return (
    <StudentLayout
      title="Supervisor"
      subtitle="Manage campus employment."
      sectionLabel="MANAGE"
      navItems={supervisorNavItems}
      showProfileLink={false}
    >
      {children || <Outlet />}
    </StudentLayout>
  );
}

export default SupervisorLayout;
