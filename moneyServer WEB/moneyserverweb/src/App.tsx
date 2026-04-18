import { Link, Routes, Route } from "react-router-dom";
import CreditCardTransactions from "./pages/CreditCardTransactions";
import BankTransactions from "./pages/BankTransactions";
import logo from "./assets/logo.png";
import { useState } from "react";

export default function App() {
  const [open, setOpen] = useState(false);
  return (
    <div>
      {/* Header */}
      <header style={{ padding: "13px",
        borderBottom: "1px solid #ddd"
       }}>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <Link to="/">
            <img src={logo} alt="logo" style={{ height: "34px" }} />
          </Link>

          <span
            style={{
              fontFamily: "Merriweather Sans , sans-serif",
              fontWeight: 400,
              fontSize: "18px",
              letterSpacing: "0.2px",
              lineHeight: "1",
              paddingBottom: "4px"
            }}
          >
            Financial Software
          </span>
        </div>
      </header>

      <nav style={{
        padding: "10px 14px",
        borderBottom: "1px solid rgba(0,0,0,0.1)",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        background: "#a5a3a37b" 
      }}>
        {/* Left side */}
        <div style={{ fontWeight: 600 }}>
          MoneyServer Private Wealth Management
        </div>

        <details 
        open={open}
        onToggle={(e) => setOpen(e.currentTarget.open)}
        style={{ position: "relative" }}
        >
        <summary
          style={{
            cursor: "pointer",
            fontWeight: 500,
            listStyle: "none",
          }}
        >
          Transactions ▾
        </summary>

        <div
          style={{
            position: "absolute",
            right: 0,
            top: "120%",
            background: "#fff",
            border: "1px solid rgba(0,0,0,0.15)",
            borderRadius: "6px",
            minWidth: "210px",
            boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
            zIndex: 100,
          }}
        >
          <Link to="/creditcardtransactions" style={menuItemStyle} onClick={() => setOpen(false)}>Credit Card Transactions</Link>
          <Link to="/banktransactions" style={menuItemStyle} onClick={() => setOpen(false)} >Bank Transactions</Link>
        </div>
      </details>

      </nav>

      
      {/* Main content */}
      <main style={{ paddingTop: "16px" }}>
        <Routes>
          <Route path="/creditcardtransactions" element={<CreditCardTransactions />} />
          <Route path="/banktransactions" element={<BankTransactions />} />
          <Route path="/" element={<CreditCardTransactions />} />
        </Routes>
      </main>
    </div>
  );
}

const menuItemStyle: React.CSSProperties = {
  display: "block",
  padding: "10px 12px",
  textDecoration: "none",
  color: "black",
  fontWeight: 500,
};

