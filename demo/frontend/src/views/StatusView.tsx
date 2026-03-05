import { useEffect, useState } from "react";
import { SERVICES, checkHealth, type ServiceHealth } from "../api";

export default function StatusView() {
  const [services, setServices] = useState<ServiceHealth[]>(
    SERVICES.map((s) => ({ ...s, status: "UNKNOWN" as const })),
  );

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
    const interval = setInterval(poll, 2000);
    return () => {
      active = false;
      clearInterval(interval);
    };
  }, []);

  const allUp = services.every((s) => s.status === "UP");
  const upCount = services.filter((s) => s.status === "UP").length;

  return (
    <div>
      <h2 className="text-2xl font-bold text-ro-blue mb-4">Systeem Status</h2>

      <div
        className={`mb-6 p-4 rounded-lg border-2 text-center font-semibold ${
          allUp
            ? "bg-green-50 border-ro-green text-ro-green"
            : "bg-orange-50 border-ro-orange text-ro-orange"
        }`}
      >
        {allUp
          ? "Alle systemen operationeel"
          : `${upCount} van ${services.length} services beschikbaar`}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {services.map((svc) => (
          <div
            key={svc.name}
            className="bg-white rounded-lg border border-ro-gray-200 p-4 flex items-center gap-3"
          >
            <div
              className={`w-3 h-3 rounded-full shrink-0 ${
                svc.status === "UP"
                  ? "bg-ro-green"
                  : svc.status === "DOWN"
                    ? "bg-ro-red"
                    : "bg-ro-gray-200"
              }`}
            />
            <div className="flex-1 min-w-0">
              <div className="font-medium text-sm">{svc.name}</div>
              <div className="text-xs text-ro-gray-500">
                :{svc.port}
                {svc.responseTimeMs !== undefined && (
                  <span className="ml-2">{svc.responseTimeMs}ms</span>
                )}
              </div>
            </div>
            <span
              className={`text-xs font-mono px-2 py-0.5 rounded ${
                svc.status === "UP"
                  ? "bg-green-100 text-ro-green"
                  : svc.status === "DOWN"
                    ? "bg-red-100 text-ro-red"
                    : "bg-gray-100 text-ro-gray-500"
              }`}
            >
              {svc.status}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
