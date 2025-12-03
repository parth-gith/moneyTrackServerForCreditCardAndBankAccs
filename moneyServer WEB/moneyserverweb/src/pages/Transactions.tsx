import { useMemo, useState } from "react";

type RawRow = [string, string];
type Row = {
  id: number;
  amount: string;
  desc: string;
  dirty?: boolean;
  locked?: boolean;
};

const UPLOAD_URL_RUPAY_HDFC = "/api-rupay";    
const UPLOAD_URL_REGALIA_GOLD_HDFC = "/api-regaliagoldhdfc";          
const UPLOAD_URL_SWIGGY_HDFC = "/api-swiggyhdfc";  
const PUSH_URL = "/api-push-moneyserver";    

export default function Transactions() {
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
  const months = [
    { v: "01", label: "January" }, { v: "02", label: "February" },
    { v: "03", label: "March" },   { v: "04", label: "April" },
    { v: "05", label: "May" },     { v: "06", label: "June" },
    { v: "07", label: "July" },    { v: "08", label: "August" },
    { v: "09", label: "September"},{ v: "10", label: "October" },
    { v: "11", label: "November" },{ v: "12", label: "December" },
  ];
  const years = useMemo(() => {
    const y = new Date().getFullYear();
    const arr: number[] = [];
    for (let k = y + 1; k >= y - 6; k--) arr.push(k);
    return arr;
  }, []);
  const cardOptions = ["Rupay HDFC","HDFC Regalia Gold MasterCard-WORLD","Swiggy HDFC Visa"];



  async function onUpload(e: React.FormEvent) {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    setError(null);
    try {
      const form = new FormData();
      form.append("file", file, file.name);
      let cardName = ""
      if(card){
        switch(card){
          case cardOptions[0]:
            cardName = UPLOAD_URL_RUPAY_HDFC
            break
          case cardOptions[1]:
            cardName = UPLOAD_URL_REGALIA_GOLD_HDFC
            break
          case cardOptions[2]:
            if(year && month){
              cardName = UPLOAD_URL_SWIGGY_HDFC +"?swiggyCard_year=" + year + "&swiggyCard_month=" + months.find(m => m.v === month)?.label || ""
            }else{
              throw new Error("Year and Month Mandatory for Swiggy HDFC card");
            }
            break
        }
      }else if(card == null){
        throw new Error("Null Card");
      }
      if(cardName == ""){
        throw new Error("Invalid Card");
      }
      const res = await fetch(cardName, { method: "POST", body: form });
      if (!res.ok) throw new Error(await res.text());
      const data: RawRow[] = await res.json();
      const mapped: Row[] = data.map(([amt, desc], i) => ({
        id: i,
        amount: amt,
        desc,
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
    } catch (e: any) {
      setPushErr(e?.message || "Push failed");
    } finally {
      setPushing(false);
    }
  }

  return (
    <div>
      <h2 style={{ fontWeight: 700, marginBottom: 12, paddingLeft: "14px" }}>Transactions</h2>

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
      </form>

      {error   && <div style={{ color: "crimson", margin: "0 14px 10px 14px" }}>Error: {error}</div>}
      {pushErr && <div style={{ color: "crimson", margin: "0 14px 10px 14px" }}>Push error: {pushErr}</div>}
      {pushMsg && <div style={{ color: "green",  margin: "0 14px 10px 14px" }}>{pushMsg}</div>}

      <div
        style={{
          maxHeight: "450px",
          overflowY: "auto",
          border: "1px solid #e5e5e5",
          marginLeft: "14px",
          marginRight: "14px",
        }}
      >
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
      </div>
    </div>
  );
}
