import { useEffect, useState, useRef } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
} from "recharts";
import {
  SERVICES,
  checkHealth,
  mockEvents,
  simulatieEvents,
  type ServiceHealth,
  type DemoEvent,
} from "../api";

const STATUS_COLORS: Record<string, string> = {
  NIEUW: "#154273",
  GELEZEN: "#39870c",
  GEARCHIVEERD: "#696969",
};

export default function BeheerderView() {
  const [services, setServices] = useState<ServiceHealth[]>(
    SERVICES.map((s) => ({ ...s, status: "UNKNOWN" as const })),
  );
  const [events, setEvents] = useState<DemoEvent[]>([]);
  const [stats, setStats] = useState({
    statusCounts: [
      { name: "NIEUW", value: 0 },
      { name: "GELEZEN", value: 0 },
      { name: "GEARCHIVEERD", value: 0 },
    ],
    afzenderCounts: [] as { name: string; count: number }[],
  });
  const eventLogRef = useRef<HTMLDivElement>(null);

  // Health polling
  useEffect(() => {
    let active = true;
    async function poll() {
      const results = await Promise.all(
        SERVICES.map(async (svc) => {
          const { up, ms } = await checkHealth(svc.url);
          return {
            ...svc,
            status: up ? ("UP" as const) : ("DOWN" as const),
            responseTimeMs: ms,
          };
        }),
      );
      if (active) setServices(results);
    }
    poll();
    const interval = setInterval(poll, 5000);
    return () => {
      active = false;
      clearInterval(interval);
    };
  }, []);

  // SSE event streams
  useEffect(() => {
    const sources: EventSource[] = [];

    function addEvent(event: DemoEvent) {
      setEvents((prev) => [event, ...prev].slice(0, 200));
      // Update stats based on event type
      if (event.type === "BERICHT_VERSTUURD" || event.type === "BERICHT_ONTVANGEN") {
        setStats((prev) => {
          const statusCounts = prev.statusCounts.map((s) =>
            s.name === "NIEUW" ? { ...s, value: s.value + 1 } : s,
          );
          const afzender = event.afzender ?? "Onbekend";
          const existing = prev.afzenderCounts.find((a) => a.name === afzender);
          const afzenderCounts = existing
            ? prev.afzenderCounts.map((a) =>
                a.name === afzender ? { ...a, count: a.count + 1 } : a,
              )
            : [...prev.afzenderCounts, { name: afzender, count: 1 }];
          return { statusCounts, afzenderCounts };
        });
      } else if (event.type === "BERICHT_GELEZEN") {
        setStats((prev) => ({
          ...prev,
          statusCounts: prev.statusCounts.map((s) =>
            s.name === "NIEUW"
              ? { ...s, value: Math.max(0, s.value - 1) }
              : s.name === "GELEZEN"
                ? { ...s, value: s.value + 1 }
                : s,
          ),
        }));
      } else if (event.type === "BERICHT_GEARCHIVEERD") {
        setStats((prev) => ({
          ...prev,
          statusCounts: prev.statusCounts.map((s) =>
            s.name === "GELEZEN"
              ? { ...s, value: Math.max(0, s.value - 1) }
              : s.name === "GEARCHIVEERD"
                ? { ...s, value: s.value + 1 }
                : s,
          ),
        }));
      }
    }

    try {
      const mock = mockEvents();
      mock.onmessage = (e) => {
        try {
          addEvent(JSON.parse(e.data));
        } catch {
          /* ignore parse errors */
        }
      };
      sources.push(mock);
    } catch {
      /* mock-services not available */
    }

    try {
      const sim = simulatieEvents();
      sim.onmessage = (e) => {
        try {
          addEvent(JSON.parse(e.data));
        } catch {
          /* ignore parse errors */
        }
      };
      sources.push(sim);
    } catch {
      /* simulator not available */
    }

    return () => sources.forEach((s) => s.close());
  }, []);

  // Auto-scroll event log
  useEffect(() => {
    eventLogRef.current?.scrollTo({ top: 0, behavior: "smooth" });
  }, [events.length]);

  return (
    <div>
      <h2 className="text-2xl font-bold text-ro-blue mb-4">Beheerder</h2>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Stats */}
        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Berichten per status
          </h3>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={stats.statusCounts}
                dataKey="value"
                nameKey="name"
                cx="50%"
                cy="50%"
                outerRadius={70}
                label={({ name, value }) => `${name}: ${value}`}
              >
                {stats.statusCounts.map((entry) => (
                  <Cell
                    key={entry.name}
                    fill={STATUS_COLORS[entry.name] ?? "#ccc"}
                  />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Berichten per afzender
          </h3>
          {stats.afzenderCounts.length === 0 ? (
            <p className="text-sm text-ro-gray-500 text-center py-8">
              Nog geen data — start de simulator of verstuur berichten
            </p>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={stats.afzenderCounts}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="#154273" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Service health inline */}
        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Service Health
          </h3>
          <div className="space-y-2">
            {services.map((svc) => (
              <div key={svc.name} className="flex items-center gap-2">
                <div
                  className={`w-2 h-2 rounded-full ${
                    svc.status === "UP" ? "bg-ro-green" : "bg-ro-red"
                  }`}
                />
                <span className="text-sm flex-1">{svc.name}</span>
                <span className="text-xs text-ro-gray-500">:{svc.port}</span>
                {svc.responseTimeMs !== undefined && (
                  <span className="text-xs text-ro-gray-500">
                    {svc.responseTimeMs}ms
                  </span>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Event log */}
        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Live Event Log
          </h3>
          <div
            ref={eventLogRef}
            className="h-64 overflow-y-auto font-mono text-xs space-y-1"
          >
            {events.length === 0 ? (
              <p className="text-ro-gray-500 text-center py-8">
                Wacht op events...
              </p>
            ) : (
              events.map((evt, i) => (
                <div
                  key={`${evt.timestamp}-${i}`}
                  className="flex gap-2 py-0.5 border-b border-ro-gray-50"
                >
                  <span className="text-ro-gray-500 shrink-0">
                    {new Date(evt.timestamp).toLocaleTimeString("nl-NL")}
                  </span>
                  <span
                    className={`font-semibold shrink-0 ${
                      evt.type.includes("ERROR")
                        ? "text-ro-red"
                        : evt.type.includes("NOTIFICATIE")
                          ? "text-ro-orange"
                          : "text-ro-blue"
                    }`}
                  >
                    {evt.type}
                  </span>
                  <span className="text-ro-gray-700 truncate">
                    {evt.detail ??
                      [evt.afzender, evt.ontvanger, evt.onderwerp]
                        .filter(Boolean)
                        .join(" → ")}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
