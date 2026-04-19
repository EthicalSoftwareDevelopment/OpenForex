# frontend/services

## Purpose
Client-side services for interacting with backend APIs and data sources (WebSocket price feed handlers, REST clients).

## Suggested Initial Files
- **apiClient.ts**: For REST API interactions.
- **priceFeed.ts**: For handling WebSocket price feeds and real-time updates.

## Notes
- Maintain clear DTOs matching `shared/schemas`.
- Handle reconnection and backoff logic for real-time feeds.
- Ensure services are modular and reusable across components and pages.
- Integrate seamlessly with the data grid and chart components in the main content area.
