import { useMemo, useState, useEffect  } from "react";

type RawRow = [string, string, string];
type Row = {
  id: number;
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

const UPLOAD_URL_RUPAY_HDFC = "/api-rupay";
const UPLOAD_URL_REGALIA_GOLD_HDFC = "/api-regaliagoldhdfc";
const UPLOAD_URL_SWIGGY_HDFC = "/api-swiggyhdfc";
const UPLOAD_URL_YES_RESERV = "/api-yesreserv";
const PUSH_URL = "/api-push-moneyserver";
const SAVE_STATE_BASE_URL = "/saveStateBase"
const UPDATE_SWIGGY_COOKIE = "/update-swiggy-cookie"

export default function CreditCardTransactions() {
  const [rows, setRows] = useState<Row[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [card, setCard] = useState<string>("");
  const [month, setMonth] = useState<string>("");
  const [year, setYear] = useState<string>("");
  const [pushing, setPushing] = useState(false);
  const [pushMsg, setPushMsg] = useState<string | null>(null);
  const [pushErr, setPushErr] = useState<string | null>(null);
  const [savingState, setsavingState] = useState(false);
  const [showCookieModal, setShowCookieModal] = useState(false);
  const [cookieInput, setCookieInput] = useState("");
  const [onCookieSubmit, setOnCookieSubmit] = useState<(cookie: string) => void>(() => {});
  const [liveStateRows, setLiveStateRows] = useState<LiveStateRecord[]>([]);
  const months = [
    { v: "01", label: "January" }, { v: "02", label: "February" },
    { v: "03", label: "March" }, { v: "04", label: "April" },
    { v: "05", label: "May" }, { v: "06", label: "June" },
    { v: "07", label: "July" }, { v: "08", label: "August" },
    { v: "09", label: "September" }, { v: "10", label: "October" },
    { v: "11", label: "November" }, { v: "12", label: "December" },
  ];
  const years = useMemo(() => {
    const y = new Date().getFullYear();
    const arr: number[] = [];
    for (let k = y + 1; k >= y - 6; k--) arr.push(k);
    return arr;
  }, []);
  const cardOptions = ["Rupay HDFC", "HDFC Regalia Gold MasterCard-WORLD", "Swiggy HDFC Visa", "YesBank Reserv MasterCard-WORLD"];

  useEffect(() => {
    if (!rows.length || !card || !month || !year) return;
    const interval = setInterval(() => {
      if (!savingState) {
        moneyserverSaveStateRefresh(true);
      }
    }, 30000); // 30 sec

    return () => clearInterval(interval);
  }, [rows.length, card, month, year, savingState]);

  useEffect(() => {
    moneyserverSaveStateLiveGet();
    const interval = setInterval(() => {
      moneyserverSaveStateLiveGet();
    }, 30000); // 30 sec

    return () => clearInterval(interval);
  }, []);

  function openCookieModal(callback: (cookie: string) => void) {
    setOnCookieSubmit(() => callback);
    setShowCookieModal(true);
  }

  async function onUpload(e: React.FormEvent) {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    setError(null);
    try {
      const form = new FormData();
      form.append("file", file, file.name);
      let cardURLName = ""
      if (card) {
        if (year && month) {
          switch (card) {
            case cardOptions[0]:
              cardURLName = UPLOAD_URL_RUPAY_HDFC + "?card_year=" + year + "&card_month=" + months.find(m => m.v === month)?.label || ""
              break
            case cardOptions[1]:
              cardURLName = UPLOAD_URL_REGALIA_GOLD_HDFC + "?card_year=" + year + "&card_month=" + months.find(m => m.v === month)?.label || ""
              break
            case cardOptions[2]:
              cardURLName = UPLOAD_URL_SWIGGY_HDFC + "?card_year=" + year + "&card_month=" + months.find(m => m.v === month)?.label || ""
              break
            case cardOptions[3]:
              cardURLName = UPLOAD_URL_YES_RESERV + "?card_year=" + year + "&card_month=" + months.find(m => m.v === month)?.label || ""
          }
        } else {
          throw new Error("Year and Month Mandatory for saveState fetch");
        }
      } else if (card == null) {
        throw new Error("Null Card");
      }
      if (cardURLName == "") {
        throw new Error("Invalid Card");
      }
      const res = await fetch(cardURLName, { method: "POST", body: form });
      if (!res.ok) throw new Error(await res.text());
      const jsonData = await res.json();
      if(jsonData.creditCardName == "swiggyhdfc" && jsonData.swiggyCookieAlive == false){
        openCookieModal(async (cookieData) => {
          const cookiePayload = {
            cookie: cookieData,
          };
          const cookieUpdateRes = await fetch(UPDATE_SWIGGY_COOKIE, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cookiePayload),
          });
          const jsonData = await cookieUpdateRes.json();
          if(cookieUpdateRes.ok && jsonData.message == "cookie-received"){
            setPushMsg("Swiggy cookie updated successfully, please retrigger the txn fetch.");
            return;
          }else{
            throw new Error(await cookieUpdateRes.text());
          }
        });
        return;
      }
      const data: RawRow[] = jsonData.rows;
      const mapped: Row[] = data.map(([amt, desc, saveStateRowId], i) => ({
        id: i,
        amount: amt,
        desc,
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


  async function moneyserverSaveStateLiveGet() {
    try{
      let saveStateLiveGetURL = SAVE_STATE_BASE_URL + "/live?type=CREDIT_CARD"
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

  async function onClickSaveState(saveStateId: string) {
    try {
      let saveStateGetById = SAVE_STATE_BASE_URL + "/" + saveStateId
      const res = await fetch(saveStateGetById, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });
      if (!res.ok) {
        const t = await res.text().catch(() => "");
        throw new Error(`saveState get using id failed: ${t}`);
      }
      const jsonData = await res.json();
      setMonth(
        months.find(m => m.label === jsonData.data.creditCardBillMonth)?.v || ""
      );
      setYear(jsonData.data.creditCardBillYear);
      const cardId = jsonData.data.saveStateId.split("-")[0];
      switch (cardId) {
        case "rupayhdfc":
          setCard(cardOptions[0]);
          break;
        case "regaliagoldhdfc":
          setCard(cardOptions[1]);
          break;
        case "swiggyhdfc":
          setCard(cardOptions[2]);
          break;
        case "yesreserv":
          setCard(cardOptions[3]);
          break;
      }
      const saveStateRows_data = jsonData.data.saveStateRows;
      const mapped: Row[] = saveStateRows_data.map((row: any, i: number) => ({
        id: i,
        amount: String(row.txnAmount),
        desc: row.txnDetail,
        saveStateRowId: row.saveStateRowId,
        dirty: false,
        locked: false,
      }));
      setRows(mapped);
    }
    catch(e: any){
      setPushErr(e?.message || "saveState get using id failed - exception");
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

  const canPush = rows.length > 0 && card && month && year && !pushing;

  async function onPushToMoneyServer() {
    setPushMsg(null);
    setPushErr(null);
    if (!canPush) return;
    try {
      setPushing(true);
      for (const r of rows) {
        const payload = {
          txnBillingMonth: months.find(m => m.v === month)?.label || "",
          txnBillingYear: year,
          txnCCused: card,
          txnDetails: r.desc,
          txnAmount: parseFloat(r.amount),
          txnIsEmi: false,
        };
        const res = await fetch(PUSH_URL, {
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
      const billMonthLabel = months.find(m => m.v === month)?.label || "";
      let cardId = ""
      switch (card) {
        case cardOptions[0]:
          cardId = "rupayhdfc"
          break
        case cardOptions[1]:
          cardId = "regaliagoldhdfc"
          break
        case cardOptions[2]:
          cardId = "swiggyhdfc"
          break
        case cardOptions[3]:
          cardId = "yesreserv"
      }
      const dateTime = new Date().toLocaleString([], {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
      const payload = {
        saveStateId: `${cardId}-${billMonthLabel}-${year}`.toLowerCase(),
        saveStateType: "CREDIT_CARD",
        creditCardBillMonth: billMonthLabel,
        creditCardBillYear: String(year),
        lastUpdated: String(dateTime),
        saveStateRows: rows.map((r) => ({
          saveStateRowId: r.saveStateRowId,
          txnAmount: parseFloat(r.amount),
          txnDetail: r.desc,
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
      const billMonthLabel = months.find(m => m.v === month)?.label || "";
      let cardId = ""
      switch (card) {
        case cardOptions[0]:
          cardId = "rupayhdfc"
          break
        case cardOptions[1]:
          cardId = "regaliagoldhdfc"
          break
        case cardOptions[2]:
          cardId = "swiggyhdfc"
          break
        case cardOptions[3]:
          cardId = "yesreserv"
      }
      let saveStateIdforAuditMark = `${cardId}-${billMonthLabel}-${year}`.toLowerCase()
      const res = await fetch(SAVE_STATE_BASE_URL + "/" + saveStateIdforAuditMark + "/markAsAudit", {
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

      <h2 style={{ fontWeight: 700, marginBottom: 12, paddingLeft: "14px" }}>Credit Card Transactions</h2>

      <form
        onSubmit={onUpload}
        style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12, paddingLeft: "14px", flexWrap: "wrap" }}
      >
        <input
          type="file"
          accept="application/pdf"
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
          {uploading ? "Uploading…" : "Upload PDF"}
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

        <select
          value={card}
          onChange={(e) => setCard(e.target.value)}
          style={{ padding: "6px 8px", borderRadius: 6, border: "1px solid #d0d0d0" }}
        >
          <option value="">Card</option>
          {cardOptions.map(c => <option key={c} value={c}>{c}</option>)}
        </select>

        <select
          value={month}
          onChange={(e) => setMonth(e.target.value)}
          style={{ padding: "6px 8px", borderRadius: 6, border: "1px solid #d0d0d0" }}
        >
          <option value="">Month</option>
          {months.map(m => <option key={m.v} value={m.v}>{m.label}</option>)}
        </select>

        <select
          value={year}
          onChange={(e) => setYear(e.target.value)}
          style={{ padding: "6px 8px", borderRadius: 6, border: "1px solid #d0d0d0" }}
        >
          <option value="">Year</option>
          {years.map(y => <option key={y} value={String(y)}>{y}</option>)}
        </select>

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
                    onClick={() => onClickSaveState(r.id)}
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
                  Upload a PDF to load transactions.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {showCookieModal && (
              <div style={{
                position: "fixed",
                top: 0,
                left: 0,
                width: "100vw",
                height: "100vh",
                background: "rgba(0,0,0,0.4)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                zIndex: 1000
              }}>
                <div style={{
                  background: "white",
                  padding: 20,
                  borderRadius: 8,
                  width: 350
                }}>
                  <h3>Session Expired</h3>

                  <p style={{ fontSize: 12 }}>
                    Login to Swiggy and paste cookie here
                  </p>

                  <textarea
                    value={cookieInput}
                    onChange={(e) => setCookieInput(e.target.value)}
                    placeholder="Paste cookie here..."
                    style={{
                      width: "100%",
                      height: 80,
                      marginBottom: 10
                    }}
                  />

                  <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
                    <button onClick={() => setShowCookieModal(false)}>
                      Cancel
                    </button>

                    <button
                      onClick={() => {
                        onCookieSubmit(cookieInput);
                        setCookieInput("");
                        setShowCookieModal(false);
                      }}
                    >
                      Submit
                    </button>
                  </div>
                </div>
              </div>
            )}
      </div>
    </div>
  );
}
