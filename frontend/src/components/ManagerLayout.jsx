import { Outlet } from "react-router-dom";
import StudentLayout from "./StudentLayout";

const managerNavItems = [
  { to: "/manager", label: "Dashboard", end: true },
  { to: "/manager/employees", label: "Employees" },
  { to: "/manager/hours", label: "Hours" },
  { to: "/manager/shifts", label: "Shifts" },
  { to: "/manager/applications", label: "Applications" },
  { to: "/manager/messages", label: "Messages" },
];

function ManagerLayout() {
  return (
    <StudentLayout
      title="Manager"
      subtitle="Manage your assigned campus job."
      sectionLabel="MANAGE"
      navItems={managerNavItems}
      showProfileLink={false}
    >
      <Outlet />
    </StudentLayout>
  );
}

export default ManagerLayout;
