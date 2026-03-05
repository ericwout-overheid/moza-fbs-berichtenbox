import { useState, useEffect } from "react";
import {
  maakBericht,
  uploadBijlage,
  lijstBerichten,
  type Bericht,
  type OntvangerIdType,
} from "../api";

const ORGANISATIES = [
  { naam: "Belastingdienst", oin: "00000001234567890000" },
  { naam: "RDW", oin: "00000009876543210000" },
  { naam: "SVB", oin: "00000001111111110000" },
  { naam: "DUO", oin: "00000002222222220000" },
  { naam: "Gemeente Amsterdam", oin: "00000003333333330000" },
];

export default function MedewerkerView() {
  const [selectedOrg, setSelectedOrg] = useState(ORGANISATIES[0]);
  const [ontvangerIdType, setOntvangerIdType] = useState<OntvangerIdType>("BSN");
  const [ontvangerId, setOntvangerId] = useState("999999999");
  const [onderwerp, setOnderwerp] = useState("");
  const [inhoud, setInhoud] = useState("");
  const [bijlage, setBijlage] = useState<File | null>(null);
  const [verzonden, setVerzonden] = useState<Bericht[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    laadVerzonden();
  }, []);

  async function verstuur() {
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const bericht = await maakBericht({
        afzenderOin: selectedOrg.oin,
        ontvangerIdType,
        ontvangerId,
        onderwerp,
        inhoud,
      });
      if (bijlage) {
        await uploadBijlage(bericht.id, bijlage);
      }
      setVerzonden((prev) => [bericht, ...prev]);
      setSuccess(`Bericht "${bericht.onderwerp}" verstuurd (${bericht.id})`);
      setOnderwerp("");
      setInhoud("");
      setBijlage(null);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  async function laadVerzonden() {
    setLoading(true);
    try {
      const page = await lijstBerichten();
      setVerzonden(page.results);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold text-ro-blue mb-4">
        Medewerker — Berichten versturen
      </h2>

      {/* Org selector */}
      <div className="mb-6">
        <label className="block text-sm font-medium mb-1">Organisatie</label>
        <div className="flex gap-2 flex-wrap">
          {ORGANISATIES.map((org) => (
            <button
              key={org.oin}
              onClick={() => setSelectedOrg(org)}
              className={`px-3 py-1.5 rounded text-sm border transition-colors ${
                selectedOrg.oin === org.oin
                  ? "bg-ro-blue text-white border-ro-blue"
                  : "bg-white text-ro-gray-700 border-ro-gray-200 hover:border-ro-blue"
              }`}
            >
              {org.naam}
            </button>
          ))}
        </div>
        <p className="text-xs text-ro-gray-500 mt-1">
          OIN: {selectedOrg.oin}
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Compose form */}
        <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-3">
            Nieuw bericht
          </h3>

          <div className="space-y-3">
            <div className="flex gap-3">
              <div>
                <label className="block text-sm font-medium mb-1">Type</label>
                <select
                  value={ontvangerIdType}
                  onChange={(e) =>
                    setOntvangerIdType(e.target.value as OntvangerIdType)
                  }
                  className="border border-ro-gray-200 rounded px-3 py-2 text-sm"
                >
                  <option value="BSN">BSN</option>
                  <option value="KVK">KVK</option>
                  <option value="RSIN">RSIN</option>
                </select>
              </div>
              <div className="flex-1">
                <label className="block text-sm font-medium mb-1">
                  Ontvanger ID
                </label>
                <input
                  type="text"
                  value={ontvangerId}
                  onChange={(e) => setOntvangerId(e.target.value)}
                  className="w-full border border-ro-gray-200 rounded px-3 py-2 text-sm"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Onderwerp</label>
              <input
                type="text"
                value={onderwerp}
                onChange={(e) => setOnderwerp(e.target.value)}
                className="w-full border border-ro-gray-200 rounded px-3 py-2 text-sm"
                placeholder="Onderwerp van het bericht"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Inhoud</label>
              <textarea
                value={inhoud}
                onChange={(e) => setInhoud(e.target.value)}
                rows={6}
                className="w-full border border-ro-gray-200 rounded px-3 py-2 text-sm"
                placeholder="Inhoud van het bericht..."
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">
                Bijlage (optioneel)
              </label>
              <input
                type="file"
                onChange={(e) => setBijlage(e.target.files?.[0] ?? null)}
                className="text-sm"
              />
            </div>

            <button
              onClick={verstuur}
              disabled={loading || !onderwerp.trim() || !inhoud.trim()}
              className="w-full bg-ro-blue text-white px-4 py-2 rounded text-sm font-medium hover:bg-ro-blue-light disabled:opacity-50"
            >
              {loading ? "Versturen..." : "Verstuur bericht"}
            </button>
          </div>

          {error && (
            <div className="mt-3 bg-red-50 border border-ro-red text-ro-red p-3 rounded text-sm">
              {error}
            </div>
          )}
          {success && (
            <div className="mt-3 bg-green-50 border border-ro-green text-ro-green p-3 rounded text-sm">
              {success}
            </div>
          )}
        </div>

        {/* Sent messages */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-semibold text-ro-gray-500 uppercase">
              Verzonden berichten
            </h3>
            <button
              onClick={laadVerzonden}
              className="text-xs text-ro-blue hover:underline"
            >
              Ververs
            </button>
          </div>
          {verzonden.length === 0 ? (
            <p className="text-sm text-ro-gray-500">
              Nog geen berichten verstuurd in deze sessie.
            </p>
          ) : (
            <div className="space-y-2">
              {verzonden.map((b) => (
                <div
                  key={b.id}
                  className="bg-white border border-ro-gray-200 rounded-lg p-3"
                >
                  <div className="font-medium text-sm">{b.onderwerp}</div>
                  <div className="text-xs text-ro-gray-500 mt-1">
                    Aan: {b.ontvangerId} ({b.ontvangerIdType}) &middot;{" "}
                    {new Date(b.aangemaaktOp).toLocaleTimeString("nl-NL")}
                    &middot; {b.status}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
