import { Outlet } from "react-router-dom";
import StudentLayout from "./StudentLayout";

const supervisorNavItems = [
  { to: "/supervisor", label: "Dashboard", end: true },
  { to: "/supervisor/schedule", label: "Schedule" },
  { to: "/supervisor/corrections", label: "Time Corrections" },
];

function SupervisorLayout() {
  return (
    <StudentLayout
      title="Supervisor"
      subtitle="Manage campus employment."
      sectionLabel="MANAGE"
      navItems={supervisorNavItems}
      userLabel="Supervisor"
      showComingSoon={false}
      showProfileLink={false}
    >
      <Outlet />
    </StudentLayout>
  );
}

export default SupervisorLayout;