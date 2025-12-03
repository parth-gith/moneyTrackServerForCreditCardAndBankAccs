import { Link, Routes, Route } from "react-router-dom";
import Transactions from "./pages/Transactions";
import logo from "./assets/logo.png";

export default function App() {
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

        {/* Right side */}
        <div style={{ display: "flex", gap: "16px" }}>
          <Link to="/transactions" style={{ textDecoration: "none", fontWeight: 500, color: "black" }}>Transactions</Link>
        </div>
      </nav>

      
      {/* Main content */}
      <main style={{ paddingTop: "16px" }}>
        <Routes>
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/" element={<Transactions />} />
        </Routes>
      </main>
    </div>
  );
}
