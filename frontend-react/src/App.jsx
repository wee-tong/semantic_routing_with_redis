import { Routes, Route, Link } from "react-router-dom";
import SearchPage from "./SearchPage";
import ChatbotPage from "./ChatbotPage";
import SemanticCache from "./SemanticCache";
import GeoMapPage from "./GeoMapPage";

function App() {
  return (
    <div>
      {/* Navigation */}
      <nav style={{fontSize:"18px", padding: "20px", background: "#beef9f"}}>
        <Link to="/" style={{ marginRight: "20px" }}>
          Fulltext and Hybrid Search
        </Link>

        <Link to="/map" style={{ marginRight: "20px" }}>
          Tweet Hybrid Search
        </Link>

        <Link to="/chat" style={{ marginRight: "15px" }}>
          Chatbot for Routing
        </Link>

        <Link to="/semantic-cache">
          Semantic Cache Chatbot
        </Link>
      </nav>

      {/* Routes */}
      <Routes>
        <Route path="/" element={<SearchPage />} />
        <Route path="/map" element={<GeoMapPage />} />
        <Route path="/chat" element={<ChatbotPage />} />
        <Route path="/semantic-cache" element={<SemanticCache />} /> 

      </Routes>
    </div>
  );
}

export default App;
