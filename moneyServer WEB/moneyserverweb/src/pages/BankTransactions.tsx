import { useState, useEffect } from "react";

type RawRow = [number, string, number, number, string, string, string];
type Row = {
  id: number;
  day: number;
  month: string;
  year: number;
  seqno: number;
  amount: string;
  desc: string;
  saveStateRowId: string;
  dirty?: boolean;
  locked?: boolean;
};

type LiveStateRecord = {
  id: string;
  time: string;
};

const PUSH_URL_BANK = "/api-push-moneyserverbank";
const UPLOAD_URL_HDFC_SAVINGS = "/api-hdfcsavings";
const SAVE_STATE_BASE_URL = "/saveStateBase"

export default function BankTransactions() {
  const [rows, setRows] = useState<Row[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pushing, setPushing] = useState(false);
  const [pushMsg, setPushMsg] = useState<string | null>(null);
  const [pushErr, setPushErr] = useState<string | null>(null);
  const [savingState, setsavingState] = useState(false);
  const [liveStateRows, setLiveStateRows] = useState<LiveStateRecord[]>([]);
  useEffect(() => {
      if (!rows.length ) return;
      const interval = setInterval(() => {
        if (!savingState) {
          moneyserverSaveStateRefresh(true);
        }
      }, 30000); // 30 sec
  
      return () => clearInterval(interval);
  }, [rows.length, savingState]);

  useEffect(() => {
    moneyserverSaveStateLiveGet();
    const interval = setInterval(() => {
      moneyserverSaveStateLiveGet();
    }, 30000); // 30 sec

    return () => clearInterval(interval);
  }, []);

  async function onUpload(e: React.FormEvent) {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    setError(null);
    try {
      const form = new FormData();
      form.append("file", file, file.name);
      const res = await fetch(UPLOAD_URL_HDFC_SAVINGS, { method: "POST", body: form });
      if (!res.ok) throw new Error(await res.text());
      const data: RawRow[] = await res.json();
      const mapped: Row[] = data.map(([day, month, year, seq , amt, desc,saveStateRowId], i) => ({
        id: i,
        day: day,
        month: month,
        year: year,
        seqno: seq,
        amount: amt,
        desc: desc,
        saveStateRowId,
        dirty: false,
        locked: false,
      }));
      setRows(mapped);
    } catch (err: any) {
      setError(err?.message || "Upload failed");
    } finally {
      setUploading(false);
    }
  }

  async function onClickSaveState() {
    try {
      let saveStateGetById = SAVE_STATE_BASE_URL + "/hdfcsavings-bank";
      const res = await fetch(saveStateGetById, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });
      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`saveState get using id failed: ${t}`);
      }
      const jsonData = await res.json();
      const saveStateRows_data = jsonData.data.saveStateRows;
      const mapped: Row[] = saveStateRows_data.map((row: any, i: number) => ({
        id: i,
        day: row.bankTxnDay,
        month: row.bankTxnMonth,
        year: row.bankTxnYear,
        seqno: row.bankTxnSeqNo,
        amount: String(row.txnAmount),
        desc: row.txnDetail || "",
        saveStateRowId: row.saveStateRowId,
        dirty: false,
        locked: false,
      }));
      setRows(mapped);
    } catch (e: any) {
      setPushErr(e?.message || "saveState get using id failed - exception");
    }
  }

  async function moneyserverSaveStateLiveGet() {
    try{
      let saveStateLiveGetURL = SAVE_STATE_BASE_URL + "/live?type=BANK"
      const res = await fetch(saveStateLiveGetURL, {
          method: "GET",
          headers: { "Content-Type": "application/json" },
      });
      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`saveState live get failed during get request : ${t}`);
      }
      const jsonData = await res.json();
      const data = jsonData as { saveStateId: string; lastUpdated: string }[];
      const mapped: LiveStateRecord[] = data.map((item, i) => ({
        id: item.saveStateId,
        time: item.lastUpdated,
      }));
      setLiveStateRows(mapped);
    }catch(e: any){
      setPushErr(e?.message || "saveState live get failed - exception");
    }
  }


  function onEditDesc(id: number, value: string) {
    setRows(prev => prev.map(r => (r.id === id ? (r.locked ? r : { ...r, desc: value, dirty: true }) : r)));
  }


  function onLock(id: number) {
    setRows(prev => prev.map(r => (r.id === id ? { ...r, locked: true, dirty: false } : r)));
  }


  function onUnlock(id: number) {
    setRows(prev => prev.map(r => (r.id === id ? { ...r, locked: false } : r)));
  }


  function onDelete(id: number) {
    setRows(prev => prev.filter(r => r.id !== id).map((r, i) => ({ ...r, id: i })));
  }

  const canPush = rows.length > 0 && !pushing;

  async function onPushToMoneyServer() {
    setPushMsg(null);
    setPushErr(null);
    if (!canPush) return;
    try {
      setPushing(true);
      for (const r of rows) {
        const payload = {
          banktxnBillingMonth: r.month,
          banktxnBillingYear: r.year,
          banktxnBillingDate: r.day,
          bankAccName: "HDFC Bank Salary Account",
          banktxnDetails: r.desc,
          banktxnAmount: parseFloat(r.amount),
          bankTxnSeqNumOrder: r.seqno,
        };
        const res = await fetch(PUSH_URL_BANK, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });
        if (!res.ok) {
          const t = await res.text().catch(() => "");
          throw new Error(`Failed for ₹${r.amount}: ${r.desc} :${t}`);
        }
      }
      setPushMsg("All transactions pushed to MoneyServer successfully.");
      moneyserverSaveStateMarkAUDIT()
    } catch (e: any) {
      setPushErr(e?.message || "Push failed");
    } finally {
      setPushing(false);
    }
  }

  async function moneyserverSaveStateRefresh(autosaveJob: boolean){
    try{
      setsavingState(true)
      const dateTime = new Date().toLocaleString([], {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
      const payload = {
        saveStateId: "hdfcsavings-bank",
        saveStateType: "BANK",
        lastUpdated: String(dateTime),
        saveStateRows: rows.map((r) => ({
          saveStateRowId: r.saveStateRowId,
          txnAmount: parseFloat(r.amount),
          txnDetail: r.desc,
          bankTxnMonth: r.month,
          bankTxnYear: r.year,
          bankTxnDay: r.day,
          bankTxnSeqNo: r.seqno,
        })),
      };
      const res = await fetch(SAVE_STATE_BASE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`saveState refresh failed during post request : ${t}`);
      }
      if(autosaveJob == false){
        setPushMsg(`Manual saveState refresh was successful at ${dateTime}.`);
      }else{
        setPushMsg(`Auto saveState refresh was successful at ${dateTime}.`);
      }
    }catch(e: any){
      setPushErr(e?.message || "saveState refresh failed - exception");
    }finally{
      setsavingState(false)
    }
  }

  async function moneyserverSaveStateMarkAUDIT(){
    setPushMsg(null);
    setPushErr(null);
    try{
      
      const res = await fetch(SAVE_STATE_BASE_URL + "/hdfcsavings-bank/markAsAudit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });
      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`saveState markAudit failed during post request : ${t}`);
      }
      setPushMsg("All transactions pushed to MoneyServer successfully. saveState record marked as audit.");
    }catch(e: any){
      setPushErr(e?.message || "saveState mark audit failed - exception");
    }
  }

  const blinkStyle = {
    animation: "blink 1s infinite"
  };

  return (
    <div>
      <style>
        {`
          @keyframes blink {
            0% { opacity: 1; }
            50% { opacity: 0.5; }
            100% { opacity: 1; }
          }
        `}
      </style>
      <h2 style={{ fontWeight: 700, marginBottom: 12, paddingLeft: "14px" }}>Bank Transactions</h2>

      <form
        onSubmit={onUpload}
        style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12, paddingLeft: "14px", flexWrap: "wrap" }}
      >
        <input
          type="file"
          accept=".xls"
          onChange={(e) => setFile(e.target.files?.[0] || null)}
        />

        <button
          type="submit"
          disabled={!file || uploading}
          style={{
            padding: "8px 12px",
            borderRadius: 6,
            border: "1px solid #d0d0d0",
            background: uploading ? "#f5f5f5" : "white",
            cursor: !file || uploading ? "not-allowed" : "pointer",
          }}
        >
          {uploading ? "Uploading…" : "Upload XLS"}
        </button>

        <button
          type="button"
          onClick={() => setRows([])}
          style={{
            padding: "8px 12px",
            borderRadius: 6,
            border: "1px solid #eee",
            background: "white",
            cursor: "pointer",
          }}
        >
          Clear
        </button>


        <button
          type="button"
          onClick={onPushToMoneyServer}
          disabled={!canPush}
          style={{
            padding: "8px 12px",
            borderRadius: 6,
            border: "1px solid #bbb",
            background: canPush ? "white" : "#f5f5f5",
            cursor: canPush ? "pointer" : "not-allowed",
            whiteSpace: "nowrap",
          }}
        >
          {pushing ? "Pushing…" : "Push to MoneyServer"}
        </button>

        <button
          type="button"
          onClick={() => moneyserverSaveStateRefresh(false)}
          style={{
            padding: "8px 12px",
            borderRadius: 6,
            border: "1px solid #bbb",
            background: "white",
            whiteSpace: "nowrap",
          }}
        >
          {savingState ? "Saving State..." : "Save State to MoneyServer"}
        </button>
      </form>

      {error && <div style={{ color: "crimson", margin: "0 14px 10px 14px" }}>Error: {error}</div>}
      {pushErr && <div style={{ color: "crimson", margin: "0 14px 10px 14px" }}>Push error: {pushErr}</div>}
      {pushMsg && <div style={{ color: "green", margin: "0 14px 10px 14px" }}>{pushMsg}</div>}

      <div
        style={{
          maxHeight: "450px",
          overflowY: "auto",
          border: "1px solid #e5e5e5",
          marginLeft: "14px",
          marginRight: "14px",
        }}
      >
        <table style={{
            width: "100%",
            borderCollapse: "collapse",
            fontSize: "12px",
            color: "#30b424"
          }}>
          <tbody>
            {liveStateRows.map((r) => (
              <tr key={r.id}>
                <td style={{ padding: "10px 12px", borderBottom: "1px solid #e5e5e5", whiteSpace: "nowrap" }}>
                  <span style={blinkStyle}>
                    ● LIVE
                  </span>
                  <span style={{
                      marginLeft: "3px" 
                    }}>
                    | saveStateId :
                  </span> 
                  <span
                    onClick={() => onClickSaveState()}
                    style={{
                      cursor: "pointer",
                      textDecoration: "underline",
                      transition: "0.2s",
                      marginLeft: "6px" 
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.opacity = "0.7"}
                    onMouseLeave={(e) => e.currentTarget.style.opacity = "1"}
                  >    
                  {r.id}             
                  </span>
                </td>
                <td style={{ padding: "10px 12px", borderBottom: "1px solid #e5e5e5", whiteSpace: "nowrap" }}>
                  {r.time}
                </td>
              </tr>
            ))}

            {liveStateRows.length === 0 && (
              <tr>
                <td colSpan={3} style={{ padding: 12, color: "#666", textAlign: "center" }}>
                  Currently no live saveStates available.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr style={{ background: "#f8f8f8" }}>
              <th style={{ textAlign: "left", padding: "10px 12px", borderBottom: "1px solid #e5e5e5", width: 160 }}>
                Date
              </th>
              <th style={{ textAlign: "left", padding: "10px 12px", borderBottom: "1px solid #e5e5e5", width: 160 }}>
                SeqNo
              </th>
              <th style={{ textAlign: "left", padding: "10px 12px", borderBottom: "1px solid #e5e5e5", width: 160 }}>
                Amount
              </th>
              <th style={{ textAlign: "left", padding: "10px 12px", borderBottom: "1px solid #e5e5e5" }}>
                Txn details
              </th>

              <th style={{ width: 56, padding: "10px 12px", borderBottom: "1px solid #e5e5e5" }} />
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>

                <td style={{ padding: "10px 12px", borderBottom: "1px solid #f0f0f0", whiteSpace: "nowrap" }}>
                  {r.day + "-" + r.month + "-" + r.year}
                </td>

                <td style={{ padding: "10px 12px", borderBottom: "1px solid #f0f0f0", whiteSpace: "nowrap" }}>
                  {r.seqno}
                </td>

                <td style={{ padding: "10px 12px", borderBottom: "1px solid #f0f0f0", whiteSpace: "nowrap", color: parseFloat(r.amount) < 0 ? "red" : "green", }}>
                  ₹ {r.amount}
                </td>

                <td style={{ padding: "10px 12px", borderBottom: "1px solid #f0f0f0" }}>
                  <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                    <input
                      value={r.desc}
                      onChange={(e) => onEditDesc(r.id, e.target.value)}
                      disabled={r.locked}
                      style={{
                        flex: 1,
                        padding: "8px 10px",
                        border: "1px solid #d9d9d9",
                        borderRadius: 6,
                        outline: "none",
                        background: r.locked ? "#f7f7f7" : "white",
                      }}
                    />
                    {!r.locked && r.dirty && (
                      <button
                        type="button"
                        onClick={() => onLock(r.id)}
                        style={{
                          padding: "6px 10px",
                          borderRadius: 6,
                          border: "1px solid #bbb",
                          background: "white",
                          cursor: "pointer",
                        }}
                      >
                        Lock
                      </button>
                    )}
                    {r.locked && (
                      <button
                        type="button"
                        onClick={() => onUnlock(r.id)}
                        style={{
                          padding: "6px 10px",
                          borderRadius: 6,
                          border: "1px solid #bbb",
                          background: "#fff8e1",
                          cursor: "pointer",
                        }}
                      >
                        Unlock
                      </button>
                    )}
                  </div>
                  <div style={{ marginTop: 6, fontSize: 12, minHeight: 16 }}>
                    {r.locked && <span style={{ color: "green" }}>Saved</span>}
                    {!r.locked && r.dirty && <span style={{ color: "#8a6d3b" }}>Edited (not saved)</span>}
                  </div>
                </td>
                
                {/* Delete cell */}
                <td style={{ padding: "10px 12px", borderBottom: "1px solid #f0f0f0", textAlign: "right" }}>
                  <button
                    type="button"
                    aria-label="Delete row"
                    title="Delete row"
                    onClick={() => onDelete(r.id)}
                    style={{
                      width: 28,
                      height: 28,
                      borderRadius: 6,
                      border: "1px solid #d0d0d0",
                      background: "white",
                      cursor: "pointer",
                      lineHeight: 1,
                    }}
                  >
                    ×
                  </button>
                </td>

              </tr>
            ))}

            {rows.length === 0 && (
              <tr>
                <td colSpan={3} style={{ padding: 12, color: "#666", textAlign: "center" }}>
                  Upload a XLS to load transactions.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
