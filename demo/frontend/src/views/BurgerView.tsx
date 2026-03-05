import { useState } from "react";
import {
  haalBerichtenlijst,
  haalBericht,
  werkBerichtBij,
  zoekBerichten,
  type BerichtRecord,
  type Bericht,
} from "../api";

export default function BurgerView() {
  const [bsn, setBsn] = useState("999999999");
  const [zoekterm, setZoekterm] = useState("");
  const [records, setRecords] = useState<BerichtRecord[]>([]);
  const [selectedBericht, setSelectedBericht] = useState<Bericht | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function fetchInbox() {
    setLoading(true);
    setError(null);
    setSelectedBericht(null);
    try {
      const page = await haalBerichtenlijst("BSN", bsn);
      setRecords(page.results);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  async function search() {
    if (!zoekterm.trim()) return;
    setLoading(true);
    setError(null);
    setSelectedBericht(null);
    try {
      const page = await zoekBerichten("BSN", bsn, zoekterm);
      setRecords(page.results);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  async function openBericht(record: BerichtRecord) {
    setLoading(true);
    setError(null);
    try {
      const bericht = await haalBericht(record.berichtId);
      setSelectedBericht(bericht);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  async function markeerAlsGelezen() {
    if (!selectedBericht) return;
    setLoading(true);
    setError(null);
    try {
      const updated = await werkBerichtBij(selectedBericht.id, "GELEZEN");
      setSelectedBericht(updated);
      setRecords((prev) =>
        prev.map((r) =>
          r.berichtId === updated.id ? { ...r, status: updated.status } : r,
        ),
      );
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold text-ro-blue mb-4">Burger — Inbox</h2>

      {/* BSN input + fetch */}
      <div className="flex gap-3 mb-4">
        <div className="flex-1">
          <label className="block text-sm font-medium mb-1">BSN</label>
          <input
            type="text"
            value={bsn}
            onChange={(e) => setBsn(e.target.value)}
            className="w-full border border-ro-gray-200 rounded px-3 py-2 text-sm"
            placeholder="123456789"
          />
          <p className="text-xs text-ro-gray-500 mt-1">
            Demo BSNs: 999999999, 888888888, 777777777, 666666666, 555555555
          </p>
        </div>
        <div className="flex items-end">
          <button
            onClick={fetchInbox}
            disabled={loading || !bsn.trim()}
            className="bg-ro-blue text-white px-4 py-2 rounded text-sm font-medium hover:bg-ro-blue-light disabled:opacity-50"
          >
            Ophalen
          </button>
        </div>
      </div>

      {/* Search */}
      <div className="flex gap-3 mb-6">
        <div className="flex-1">
          <label className="block text-sm font-medium mb-1">Zoeken</label>
          <input
            type="text"
            value={zoekterm}
            onChange={(e) => setZoekterm(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && search()}
            className="w-full border border-ro-gray-200 rounded px-3 py-2 text-sm"
            placeholder="Zoekterm..."
          />
        </div>
        <div className="flex items-end">
          <button
            onClick={search}
            disabled={loading || !zoekterm.trim()}
            className="bg-ro-blue text-white px-4 py-2 rounded text-sm font-medium hover:bg-ro-blue-light disabled:opacity-50"
          >
            Zoeken
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-ro-red text-ro-red p-3 rounded mb-4 text-sm">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Inbox list */}
        <div>
          <h3 className="text-sm font-semibold text-ro-gray-500 uppercase mb-2">
            Berichten ({records.length})
          </h3>
          {records.length === 0 && !loading && (
            <p className="text-sm text-ro-gray-500">
              Geen berichten gevonden.
            </p>
          )}
          <div className="space-y-2">
            {records.map((r) => (
              <button
                key={r.berichtId}
                onClick={() => openBericht(r)}
                className={`w-full text-left bg-white border rounded-lg p-3 hover:border-ro-blue transition-colors ${
                  selectedBericht?.id === r.berichtId
                    ? "border-ro-blue ring-1 ring-ro-blue"
                    : "border-ro-gray-200"
                }`}
              >
                <div className="flex items-center gap-2 mb-1">
                  <span
                    className={`w-2 h-2 rounded-full ${
                      r.status === "NIEUW"
                        ? "bg-ro-blue"
                        : r.status === "GELEZEN"
                          ? "bg-ro-green"
                          : "bg-ro-gray-200"
                    }`}
                  />
                  <span className="text-xs text-ro-gray-500">{r.afzenderNaam}</span>
                  <span className="text-xs text-ro-gray-200 ml-auto">
                    {new Date(r.aangemaaktOp).toLocaleDateString("nl-NL")}
                  </span>
                </div>
                <div className="font-medium text-sm truncate">{r.onderwerp}</div>
                <div className="text-xs text-ro-gray-500 mt-1">
                  Status: {r.status}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Detail panel */}
        <div>
          {selectedBericht ? (
            <div className="bg-white border border-ro-gray-200 rounded-lg p-4">
              <h3 className="text-lg font-bold mb-2">
                {selectedBericht.onderwerp}
              </h3>
              <div className="text-xs text-ro-gray-500 space-y-1 mb-4">
                <div>Van: {selectedBericht.afzenderOin}</div>
                <div>Aan: {selectedBericht.ontvangerId} ({selectedBericht.ontvangerIdType})</div>
                <div>
                  Datum:{" "}
                  {new Date(selectedBericht.aangemaaktOp).toLocaleString("nl-NL")}
                </div>
                <div>Status: {selectedBericht.status}</div>
              </div>
              <div className="bg-ro-gray-50 rounded p-3 text-sm whitespace-pre-wrap mb-4">
                {selectedBericht.inhoud}
              </div>
              {selectedBericht.bijlagen.length > 0 && (
                <div className="mb-4">
                  <h4 className="text-sm font-semibold mb-1">Bijlagen</h4>
                  <ul className="text-sm space-y-1">
                    {selectedBericht.bijlagen.map((b) => (
                      <li key={b.id} className="text-ro-blue-light">
                        {b.bestandsnaam} ({Math.round(b.grootte / 1024)} KB)
                      </li>
                    ))}
                  </ul>
                </div>
              )}
              {selectedBericht.status === "NIEUW" && (
                <button
                  onClick={markeerAlsGelezen}
                  disabled={loading}
                  className="bg-ro-green text-white px-4 py-2 rounded text-sm font-medium hover:bg-ro-green-light disabled:opacity-50"
                >
                  Markeer als gelezen
                </button>
              )}
            </div>
          ) : (
            <div className="bg-white border border-ro-gray-200 rounded-lg p-8 text-center text-sm text-ro-gray-500">
              Selecteer een bericht om de details te bekijken
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
