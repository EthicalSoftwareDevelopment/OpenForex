# frontend/services

Purpose: Client-side services for talking to backend APIs and data sources (WebSocket price feed handlers, REST clients).
Suggested initial files:
- apiClient.ts
- priceFeed.ts

Notes: Maintain clear DTOs matching `shared/schemas` and handle reconnection/backoff logic for real-time feeds.
