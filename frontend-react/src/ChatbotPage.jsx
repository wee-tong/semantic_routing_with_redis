import React, { useState, useEffect } from "react";

function ChatbotPage() {
  const [message, setMessage] = useState("");
  const [chat, setChat] = useState([]);
  const [suggestions, setSuggestions] = useState({});
  const [bulkInput, setBulkInput] = useState("");

  const buttonStyle = {
    padding: "8px 16px",
    cursor: "pointer",
    marginRight: "10px"
  };

  useEffect(() => {
    const fetchSuggestions = async () => {
      try {
        const response = await fetch(
          "http://localhost:8080/api/getrouter"
        );

        const data = await response.json();
        setSuggestions(data);
      } catch (err) {
        console.error("Failed to load suggestions");
      }
    };

    fetchSuggestions();
  }, []);


  const sendMessage = async () => {
    if (!message.trim()) return;

    const userMessage = { sender: "user", text: message };
    setChat([...chat, userMessage]);

    try {
      const response = await fetch(
        `http://localhost:8080/api/routequestion?userquestion=${encodeURIComponent(message)}`
      );

      const data = await response.text(); // or .json()

      const botMessage = { sender: "bot", text: data };

      setChat(prev => [...prev, botMessage]);

    } catch (err) {
      setChat(prev => [...prev, { sender: "bot", text: "Error contacting server" }]);
    }

    setMessage("");
  };

  const loadDefaultRouteConfig = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/loaddefaultrouteconfig",
        {
          method: "POST"   // or "GET" if your backend uses @GetMapping
        }
      );

      const data = await response.text(); // or .json() if returning JSON

      setChat(prev => [
        ...prev,
        { sender: "bot", text: "Default route config loaded."},
        { sender: "bot", text: data  }
      ]);

    } catch (err) {
      setChat(prev => [
        ...prev,
        { sender: "bot", text: "Error loading default route config" }
      ]);
    }
  };

  const sendBulkQuestions = async () => {
    if (!bulkInput.trim()) return;

    const lines = bulkInput
      .split("\n")
      .map(line => line.trim())
      .filter(line => line.length > 0);

    let parsedData = {};
    let currentClass = null;

    for (let line of lines) {
      // Detect class header
      if (line.toLowerCase().startsWith("class:")) {
        currentClass = line.split(":")[1].trim();
        parsedData[currentClass] = [];
        continue;
      }

      if (currentClass) {
        parsedData[currentClass].push(line);
      }
    }

    try {
      const response = await fetch(
        "http://localhost:8080/api/updateroutequestion",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(parsedData)
        }
      );

      const data = await response.json();

      setChat(prev => [
        ...prev,
        { sender: "user", text: "Submitted class-based question list" },
        { sender: "bot", text: JSON.stringify(data, null, 2) }
      ]);

    } catch (err) {
      setChat(prev => [
        ...prev,
        { sender: "bot", text: "Error submitting bulk questions" }
      ]);
    }

    setBulkInput("");
  };

  return (


    <div style={{ padding: "30px" }}>
      <h2>Router for questions</h2>

      <div
        style={{
          border: "1px solid #ccc",
          height: "400px",
          overflowY: "auto",
          padding: "10px",
          marginBottom: "10px",
          fontSize: "18px"
        }}
      >

        {/* 🔥 Show suggestions only if no chat yet */}
        {chat.length === 0 && Object.keys(suggestions).length > 0 && (
          <div>
            <h3>Semantic Routing Settings</h3>

            {Object.entries(suggestions).map(([category, questions]) => (
              <div key={category} style={{ marginBottom: "15px" }}>
                <strong style={{ textTransform: "capitalize" }}>
                  {category}
                </strong>

                <ul>
                  {questions.map((q, index) => (
                    <li
                      key={index}
                      style={{ cursor: "pointer", color: "blue" }}
                      onClick={() => sendMessage(q)}  // 🔥 auto-send
                    >
                      {q}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
        {/* 🔥 Chat messages */}
        {chat.map((msg, index) => (
          <div key={index} style={{ marginBottom: "8px" }}>
            <strong>{msg.sender === "user" ? "You" : "Chatbot"}:</strong> {msg.text}
          </div>
        ))}
      </div>

      <input
        type="text"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        placeholder="Type your message..."
        style={{ height: "28px", width: "70%", marginRight: "10px" }}
      />

      <button onClick={sendMessage} style={buttonStyle}>
        Send
      </button>

      <hr style={{ margin: "20px 0" }} />

      <h3>Submit Question List</h3>

      <textarea
        rows={10}
        value={bulkInput}
        onChange={(e) => setBulkInput(e.target.value)}
        placeholder="Paste multiple questions here using format below...
        Format:
          class:business
          question 1
          question 2"
        style={{
          width: "100%",
          marginBottom: "10px",
          fontSize: "16px"
        }}
      />

      <button onClick={sendBulkQuestions} style={buttonStyle}>
        Submit List
      </button>
      <button onClick={loadDefaultRouteConfig} style={buttonStyle}>
        Load Default Config
      </button>
    </div>


  );


}

export default ChatbotPage;
