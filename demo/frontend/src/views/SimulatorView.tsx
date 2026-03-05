import { useEffect, useState, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  startSimulatie,
  stopSimulatie,
  simulatieStatus,
  simulatieEvents,
  type SimulatieStatus,
  type DemoEvent,
} from "../api";

interface FlowDot {
  id: string;
  from: { x: number; y: number };
  to: { x: number; y: number };
  color: string;
}

const NODES = [
  { id: "belastingdienst", label: "Belastingdienst", x: 50, y: 50 },
  { id: "rdw", label: "RDW", x: 50, y: 150 },
  { id: "svb", label: "SVB", x: 50, y: 250 },
  { id: "burger", label: "Burgers", x: 450, y: 150 },
  { id: "duo", label: "DUO", x: 50, y: 350 },
  { id: "amsterdam", label: "Gem. Amsterdam", x: 50, y: 450 },
];

const NODE_MAP: Record<string, (typeof NODES)[number]> = {};
for (const n of NODES) NODE_MAP[n.id] = n;

function findNode(name: string): (typeof NODES)[number] {
  const lower = name.toLowerCase();
  if (lower.includes("bsn") || lower.includes("burger")) return NODE_MAP["burger"];
  return (
    NODES.find((n) => lower.includes(n.id) || lower.includes(n.label.toLowerCase())) ??
    NODES[0]
  );
}

export default function SimulatorView() {
  const [aantalGebruikers, setAantalGebruikers] = useState(10);
  const [berichtenPerSeconde, setBerichtenPerSeconde] = useState(5);
  const [duurSeconden, setDuurSeconden] = useState(60);
  const [status, setStatus] = useState<SimulatieStatus | null>(null);
  const [events, setEvents] = useState<DemoEvent[]>([]);
  const [dots, setDots] = useState<FlowDot[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const dotCounter = useRef(0);
  const eventLogRef = useRef<HTMLDivElement>(null);

  // Poll status
  useEffect(() => {
    let active = true;
    async function poll() {
      try {
        const s = await simulatieStatus();
        if (active) setStatus(s);
      } catch {
        /* simulator not available */
      }
    }
    poll();
    const interval = setInterval(poll, 2000);
    return () => {
      active = false;
      clearInterval(interval);
    };
  }, []);

  // SSE events
  useEffect(() => {
    let es: EventSource | null = null;
    try {
      es = simulatieEvents();
      es.onmessage = (e) => {
        try {
          const event: DemoEvent = JSON.parse(e.data);
          setEvents((prev) => [event, ...prev].slice(0, 100));

          // Create flow dot
          if (event.afzender && event.ontvanger) {
            const from = findNode(event.afzender);
            const to = findNode(event.ontvanger);
            const id = String(++dotCounter.current);
            const color =
              event.type === "BERICHT_ONTVANGEN"
                ? "#154273"
                : event.type === "BERICHT_GELEZEN"
                  ? "#39870c"
                  : "#e17000";
            setDots((prev) => [
              ...prev.slice(-30),
              {
                id,
                from: { x: from.x + 60, y: from.y + 15 },
                to: { x: to.x, y: to.y + 15 },
                color,
              },
            ]);
            setTimeout(() => {
              setDots((prev) => prev.filter((d) => d.id !== id));
            }, 1500);
          }
        } catch {
          /* ignore */
        }
      };
    } catch {
      /* simulator not available */
    }
    return () => es?.close();
  }, []);

  async function handleStart() {
    setLoading(true);
    setError(null);
    try {
      const s = await startSimulatie({
        aantalGebruikers,
        berichtenPerSeconde,
        duurSeconden,
      });
      setStatus(s);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  async function handleStop() {
    setLoading(true);
    try {
      const s = await stopSimulatie();
      setStatus(s);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  const actief = status?.actief ?? false;

  return (
    <div>
      <h2 className="text-2xl font-bold text-ro-blue mb-4">Simulator</h2>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Controls */}
        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Configuratie
          </h3>
          <div className="space-y-4">
            <div>
              <label className="flex justify-between text-sm font-medium mb-1">
                <span>Gebruikers</span>
                <span className="text-ro-blue">{aantalGebruikers}</span>
              </label>
              <input
                type="range"
                min={1}
                max={100}
                value={aantalGebruikers}
                onChange={(e) => setAantalGebruikers(Number(e.target.value))}
                disabled={actief}
                className="w-full"
              />
            </div>
            <div>
              <label className="flex justify-between text-sm font-medium mb-1">
                <span>Berichten/s</span>
                <span className="text-ro-blue">{berichtenPerSeconde}</span>
              </label>
              <input
                type="range"
                min={1}
                max={20}
                value={berichtenPerSeconde}
                onChange={(e) => setBerichtenPerSeconde(Number(e.target.value))}
                disabled={actief}
                className="w-full"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">
                Duur (seconden)
              </label>
              <input
                type="number"
                min={10}
                max={600}
                value={duurSeconden}
                onChange={(e) => setDuurSeconden(Number(e.target.value))}
                disabled={actief}
                className="w-full border border-ro-gray-200 rounded px-3 py-2 text-sm"
              />
            </div>

            {actief ? (
              <button
                onClick={handleStop}
                disabled={loading}
                className="w-full bg-ro-red text-white px-4 py-2 rounded text-sm font-medium hover:opacity-90 disabled:opacity-50"
              >
                Stop simulatie
              </button>
            ) : (
              <button
                onClick={handleStart}
                disabled={loading}
                className="w-full bg-ro-green text-white px-4 py-2 rounded text-sm font-medium hover:bg-ro-green-light disabled:opacity-50"
              >
                Start simulatie
              </button>
            )}

            {error && (
              <div className="bg-red-50 border border-ro-red text-ro-red p-2 rounded text-xs">
                {error}
              </div>
            )}
          </div>

          {/* Counters */}
          {status && (
            <div className="mt-4 grid grid-cols-3 gap-2">
              <div className="text-center bg-ro-gray-50 rounded p-2">
                <div className="text-lg font-bold text-ro-blue">
                  {status.verstuurd}
                </div>
                <div className="text-xs text-ro-gray-500">Verstuurd</div>
              </div>
              <div className="text-center bg-ro-gray-50 rounded p-2">
                <div className="text-lg font-bold text-ro-green">
                  {status.gelezen}
                </div>
                <div className="text-xs text-ro-gray-500">Gelezen</div>
              </div>
              <div className="text-center bg-ro-gray-50 rounded p-2">
                <div className="text-lg font-bold text-ro-gray-500">
                  {status.gearchiveerd}
                </div>
                <div className="text-xs text-ro-gray-500">Gearchiveerd</div>
              </div>
            </div>
          )}
        </div>

        {/* Flow visualization */}
        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Berichtstroom
          </h3>
          <svg viewBox="0 0 550 500" className="w-full h-auto">
            {/* Node labels */}
            {NODES.map((node) => (
              <g key={node.id}>
                <rect
                  x={node.x}
                  y={node.y}
                  width={node.id === "burger" ? 80 : 120}
                  height={30}
                  rx={4}
                  fill={node.id === "burger" ? "#39870c" : "#154273"}
                />
                <text
                  x={node.x + (node.id === "burger" ? 40 : 60)}
                  y={node.y + 19}
                  textAnchor="middle"
                  fill="white"
                  fontSize={10}
                  fontWeight="bold"
                >
                  {node.label}
                </text>
              </g>
            ))}

            {/* Animated dots */}
            <AnimatePresence>
              {dots.map((dot) => (
                <motion.circle
                  key={dot.id}
                  r={4}
                  fill={dot.color}
                  initial={{ cx: dot.from.x, cy: dot.from.y, opacity: 1 }}
                  animate={{ cx: dot.to.x, cy: dot.to.y, opacity: 0.3 }}
                  exit={{ opacity: 0 }}
                  transition={{ duration: 1.2, ease: "easeInOut" }}
                />
              ))}
            </AnimatePresence>
          </svg>
        </div>

        {/* Event stream */}
        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Event Stream
          </h3>
          <div
            ref={eventLogRef}
            className="h-96 overflow-y-auto font-mono text-xs space-y-1"
          >
            {events.length === 0 ? (
              <p className="text-ro-gray-500 text-center py-8">
                Start de simulator om events te zien
              </p>
            ) : (
              events.map((evt, i) => (
                <div
                  key={`${evt.timestamp}-${i}`}
                  className="py-0.5 border-b border-ro-gray-50"
                >
                  <span className="text-ro-gray-500">
                    {new Date(evt.timestamp).toLocaleTimeString("nl-NL")}
                  </span>{" "}
                  <span
                    className={`font-semibold ${
                      evt.type === "BERICHT_ONTVANGEN"
                        ? "text-ro-blue"
                        : evt.type === "BERICHT_GELEZEN"
                          ? "text-ro-green"
                          : "text-ro-orange"
                    }`}
                  >
                    {evt.type}
                  </span>{" "}
                  <span className="text-ro-gray-700">
                    {evt.afzender} → {evt.ontvanger}
                  </span>
                  {evt.onderwerp && (
                    <span className="text-ro-gray-500 block pl-4 truncate">
                      {evt.onderwerp}
                    </span>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
