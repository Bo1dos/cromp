import { Outlet } from 'react-router-dom';

export default function DashboardLayout() {
  return (
    <div style={{ padding: 24 }}>
      <h1>Cron as a Service</h1>
      <Outlet />
    </div>
  );
}
