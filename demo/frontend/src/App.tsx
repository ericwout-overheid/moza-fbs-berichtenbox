import { Routes, Route, NavLink, Navigate } from "react-router-dom";
import StatusView from "./views/StatusView";
import BurgerView from "./views/BurgerView";
import MedewerkerView from "./views/MedewerkerView";
import BeheerderView from "./views/BeheerderView";
import SimulatorView from "./views/SimulatorView";

const navItems = [
  { to: "/status", label: "Status" },
  { to: "/burger", label: "Burger" },
  { to: "/medewerker", label: "Medewerker" },
  { to: "/beheerder", label: "Beheerder" },
  { to: "/simulator", label: "Simulator" },
];

export default function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-ro-blue text-white px-6 py-3 flex items-center gap-6">
        <h1 className="text-lg font-bold tracking-wide">
          FBS Demo
        </h1>
        <nav className="flex gap-1">
          {navItems.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `px-3 py-1.5 rounded text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-white/20 text-white"
                    : "text-white/70 hover:text-white hover:bg-white/10"
                }`
              }
            >
              {label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main className="flex-1 p-6 max-w-7xl mx-auto w-full">
        <Routes>
          <Route path="/status" element={<StatusView />} />
          <Route path="/burger/*" element={<BurgerView />} />
          <Route path="/medewerker" element={<MedewerkerView />} />
          <Route path="/beheerder" element={<BeheerderView />} />
          <Route path="/simulator" element={<SimulatorView />} />
          <Route path="*" element={<Navigate to="/status" replace />} />
        </Routes>
      </main>

      <footer className="bg-ro-gray-100 text-ro-gray-500 text-xs text-center py-2">
        Federatief Berichtenstelsel — Demo Applicatie — Rijksoverheid / Moza
      </footer>
    </div>
  );
}
