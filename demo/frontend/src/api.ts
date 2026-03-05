const BERICHTENMAGAZIJN_URL =
  import.meta.env.VITE_BERICHTENMAGAZIJN_URL ?? "http://localhost:8080";
const BERICHTENLIJST_URL =
  import.meta.env.VITE_BERICHTENLIJST_URL ?? "http://localhost:8081";
const MOCK_SERVICES_URL =
  import.meta.env.VITE_MOCK_SERVICES_URL ?? "http://localhost:8095";
const SIMULATOR_URL =
  import.meta.env.VITE_SIMULATOR_URL ?? "http://localhost:8092";
const PROFIEL_URL =
  import.meta.env.VITE_PROFIEL_URL ?? "/profiel-api";

const headers: Record<string, string> = {
  "Content-Type": "application/json",
  "API-Version": "1.0.0",
};

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, { headers, ...init });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status}: ${text}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

// --- Types matching fbs-common DTOs ---

export type OntvangerIdType = "BSN" | "RSIN" | "KVK";
export type BerichtStatus = "NIEUW" | "GELEZEN" | "GEARCHIVEERD";

export interface Bericht {
  id: string;
  afzenderOin: string;
  ontvangerIdType: OntvangerIdType;
  ontvangerId: string;
  onderwerp: string;
  inhoud: string;
  status: BerichtStatus;
  aangemaaktOp: string;
  gelezenOp: string | null;
  bijlagen: BijlageMetadata[];
}

export interface BerichtRecord {
  berichtId: string;
  afzenderOin: string;
  afzenderNaam: string;
  onderwerp: string;
  status: BerichtStatus;
  aangemaaktOp: string;
  gelezenOp: string | null;
  magazijnUrl: string;
}

export interface BijlageMetadata {
  id: string;
  bestandsnaam: string;
  mediaType: string;
  grootte: number;
  aangemaaktOp: string;
}

export interface Page<T> {
  results: T[];
  page: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
}

export interface SimulatieConfig {
  aantalGebruikers: number;
  berichtenPerSeconde: number;
  duurSeconden: number;
}

export interface SimulatieStatus {
  actief: boolean;
  verstuurd: number;
  gelezen: number;
  gearchiveerd: number;
  gestart: string | null;
}

export interface DemoEvent {
  type: string;
  afzender?: string;
  ontvanger?: string;
  onderwerp?: string;
  detail?: string;
  timestamp: string;
}

// --- Service health ---

export interface ServiceHealth {
  name: string;
  port: number;
  url: string;
  status: "UP" | "DOWN" | "UNKNOWN";
  responseTimeMs?: number;
}

export const SERVICES: Omit<ServiceHealth, "status" | "responseTimeMs">[] = [
  { name: "Berichtenmagazijn", port: 8080, url: `${BERICHTENMAGAZIJN_URL}/q/health` },
  { name: "Berichtenlijst", port: 8081, url: `${BERICHTENLIJST_URL}/q/health` },
  { name: "Mock Services", port: 8095, url: `${MOCK_SERVICES_URL}/q/health` },
  { name: "Simulator", port: 8092, url: `${SIMULATOR_URL}/q/health` },
  { name: "Profiel Service", port: 8088, url: `${PROFIEL_URL}/q/openapi` },
];

export async function checkHealth(
  url: string,
): Promise<{ up: boolean; ms: number }> {
  const start = performance.now();
  try {
    const res = await fetch(url, { signal: AbortSignal.timeout(5000) });
    const ms = Math.round(performance.now() - start);
    return { up: res.ok, ms };
  } catch {
    return { up: false, ms: Math.round(performance.now() - start) };
  }
}

// --- Berichtenlijst API ---

export async function haalBerichtenlijst(
  ontvangerIdType: OntvangerIdType,
  ontvangerId: string,
  page = 1,
  pageSize = 20,
): Promise<Page<BerichtRecord>> {
  const params = new URLSearchParams({
    ontvangerIdType,
    ontvangerId,
    page: String(page),
    pageSize: String(pageSize),
  });
  return fetchJson(`${BERICHTENLIJST_URL}/api/v1/berichtenlijst?${params}`);
}

export async function zoekBerichten(
  ontvangerIdType: OntvangerIdType,
  ontvangerId: string,
  zoekterm: string,
  page = 1,
  pageSize = 20,
): Promise<Page<BerichtRecord>> {
  const params = new URLSearchParams({
    ontvangerIdType,
    ontvangerId,
    zoekterm,
    page: String(page),
    pageSize: String(pageSize),
  });
  return fetchJson(`${BERICHTENLIJST_URL}/api/v1/berichtenlijst/zoek?${params}`);
}

// --- Berichtenmagazijn API ---

export async function haalBericht(berichtId: string): Promise<Bericht> {
  return fetchJson(`${BERICHTENMAGAZIJN_URL}/api/v1/berichten/${berichtId}`);
}

export async function maakBericht(verzoek: {
  afzenderOin: string;
  ontvangerIdType: OntvangerIdType;
  ontvangerId: string;
  onderwerp: string;
  inhoud: string;
}): Promise<Bericht> {
  const { afzenderOin, ...body } = verzoek;
  return fetchJson(`${BERICHTENMAGAZIJN_URL}/api/v1/berichten`, {
    method: "POST",
    headers: { ...headers, "X-Afzender-OIN": afzenderOin },
    body: JSON.stringify(body),
  });
}

export async function werkBerichtBij(
  berichtId: string,
  status: BerichtStatus,
): Promise<Bericht> {
  return fetchJson(`${BERICHTENMAGAZIJN_URL}/api/v1/berichten/${berichtId}`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });
}

export async function uploadBijlage(
  berichtId: string,
  file: File,
): Promise<BijlageMetadata> {
  const formData = new FormData();
  formData.append("bestand", file);
  const res = await fetch(
    `${BERICHTENMAGAZIJN_URL}/api/v1/berichten/${berichtId}/bijlagen`,
    {
      method: "POST",
      headers: { "API-Version": "1.0.0" },
      body: formData,
    },
  );
  if (!res.ok) throw new Error(`${res.status}: ${await res.text()}`);
  return res.json();
}

export async function lijstBerichten(
  page = 1,
  pageSize = 20,
): Promise<Page<Bericht>> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  });
  return fetchJson(`${BERICHTENMAGAZIJN_URL}/api/v1/berichten?${params}`);
}

// --- Simulator API ---

export async function startSimulatie(
  config: SimulatieConfig,
): Promise<SimulatieStatus> {
  return fetchJson(`${SIMULATOR_URL}/api/demo/simulatie/start`, {
    method: "POST",
    body: JSON.stringify(config),
  });
}

export async function stopSimulatie(): Promise<SimulatieStatus> {
  return fetchJson(`${SIMULATOR_URL}/api/demo/simulatie/stop`, {
    method: "POST",
  });
}

export async function simulatieStatus(): Promise<SimulatieStatus> {
  return fetchJson(`${SIMULATOR_URL}/api/demo/simulatie/status`);
}

// --- SSE streams ---

export function simulatieEvents(): EventSource {
  return new EventSource(`${SIMULATOR_URL}/api/demo/simulatie/events`);
}

export function mockEvents(): EventSource {
  return new EventSource(`${MOCK_SERVICES_URL}/api/demo/events/stream`);
}

// --- Mock Services API ---

export async function haalNotificaties(): Promise<DemoEvent[]> {
  return fetchJson(`${MOCK_SERVICES_URL}/api/demo/notificaties`);
}
