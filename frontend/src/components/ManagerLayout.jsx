import { Outlet } from "react-router-dom";
import StudentLayout from "./StudentLayout";

const managerNavItems = [
  { to: "/manager", label: "Dashboard", end: true },
  { to: "/manager/employees", label: "Employees" },
  { to: "/manager/hours", label: "Hours" },
  { to: "/manager/shifts", label: "Shifts" },
  { to: "/manager/messages", label: "Messages" },
];

function ManagerLayout() {
  return (
    <StudentLayout
      title="Manager"
      subtitle="Manage your assigned campus job."
      sectionLabel="MANAGE"
      navItems={managerNavItems}
      showComingSoon={false}
      showProfileLink={false}
    >
      <Outlet />
    </StudentLayout>
  );
}

export default ManagerLayout;
