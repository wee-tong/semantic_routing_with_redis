Semantic Router with Redis and Java

A dynamic, data-driven semantic routing system built with Java and Redis. This project demonstrates how to route user queries based on meaning (semantic similarity) instead of hardcoded rules, enabling flexible and scalable intelligent applications.

🧠 What is Semantic Routing?

Semantic routing is a technique that classifies incoming requests based on their intent and meaning, then directs them to the appropriate processing pipeline.

Instead of relying on keyword matching, this system uses embeddings and similarity matching to determine which category a query belongs to (e.g., sports, technology, politics).

✨ Features
🔀 Semantic-based routing (not keyword-based)
⚡ Fast classification using embeddings
🧩 Dynamic configuration via Redis
🔄 Hot updates without redeploying
🏗️ Decoupled architecture (data vs logic)
📡 Extensible via service/API layer
🏗️ Architecture Overview
            User Query
                 ↓
        ┌───────────────────┐
        │ Semantic Router   │
        └───────────────────┘
           ↓       ↓       ↓
        Sports   Tech   Blocked
🔁 System Flow
Initialize
Load default routing configuration
Store in Redis
Update
Users modify categories via service/API
Redis is updated dynamically
Runtime
Router loads config from Redis
Builds semantic routes
