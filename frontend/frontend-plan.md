# Frontend Plan - Phases

## Table of Contents
- [Technologies](#technologies)
- [Phase 1: Top Navigation Bar](#phase-1-top-navigation-bar)
- [Phase 2: Left Sidebar (Collapsible)](#phase-2-left-sidebar-collapsible)
- [Phase 3: Main Content Area](#phase-3-main-content-area)
  - [Top Section (KPIs)](#top-section-kpis)
  - [Middle Section (Charts)](#middle-section-charts)
  - [Bottom Section (Data Grid)](#bottom-section-data-grid)
- [Phase 4: Right Sidebar (Optional)](#phase-4-right-sidebar-optional)
- [Phase 5: Footer](#phase-5-footer)
- [Architecture & Data Flow](#architecture--data-flow)
- [State Management](#state-management)

## Technologies
- **Framework**: React (served as CSR bundles via Aleph.js on Deno)
  - Chosen for its component-based architecture, virtual DOM for performance, and large ecosystem.
- **UI Libraries**: Material-UI, React-Financial-Charts
  - Material-UI provides pre-built, customizable components for rapid development.
  - React-Financial-Charts offers specialized charting tools for financial data visualization.
- **Runtime**: Deno 2
  - Selected for its secure runtime, modern TypeScript support, and built-in tooling.
  - Acts as a high-performance edge proxy and frontend delivery layer.

## Architecture & Data Flow

### 1. Frontend Runtime / Edge Layer (Deno 2 + Aleph.js)
- **Asset Delivery**: Operates as the edge layer serving React CSR bundles and static assets (charts, UI libraries) directly without hitting backend Java servers.
- **WebSocket Gateway**: Terminates low-latency WebSocket connections for live trading feeds directly at the edge to ensure maximum performance. 
- **Proxy/Bridge**: Forwards API calls from the React UI to the Java backend. Generates type-safe client stubs using MicroProfile OpenAPI definitions from the Java layer.

### 2. Backend Services (Java)
- **Core Logic**: Jakarta EE / Spring Boot / Quarkus manages core trading business rules.
- **APIs**: Exposed via REST (JAX-RS) or GraphQL endpoints.
- **Security & Compliance**: Responsible for PCI-DSS tokenization, intensive AML/KYC checks, and persisting data for BaFin audit logging.

### 3. Data Lifecycle Example
1. User requests market data → React CSR app calls Aleph.js (Deno) API endpoint.
2. Deno proxies the request to the Java backend (JAX-RS/Quarkus).
3. Java backend returns compliance-verified data → Deno edge → React.
4. WebSockets for high-speed trading updates are established and managed in Deno, but critical events are relayed to Java for persistence and comprehensive BaFin compliance.

## State Management

- **Library**: Redux Toolkit for predictable state transitions across complex trading flows.
- **Core Slices**:
  - `portfolioSlice.ts`: Manages user positions, active balances, and margin.
  - `tradingSlice.ts`: Tracks order lifecycle, execution statuses, and P&L.
  - `complianceSlice.ts`: Stores real-time AML/KYC flags, restrictions, and audit states.
- **BaFin Audit Logging**: A custom Redux middleware intercepts every critical state transition (e.g., order execution, compliance flag changes) and dispatches non-blocking metric beacons. These are exported to ELK/Prometheus for strict regulatory auditing.

## Phase 1: Top Navigation Bar
- **Logo / Brand (left)**
- **Global navigation**: Home, Markets, Portfolio, Reports
- **User menu (right)**: Profile, Settings, Logout
- **Notifications**: Compliance alerts, system messages

## Phase 2: Left Sidebar (Collapsible)
- **Quick links**: Dashboard, Trading, Risk, Compliance
- **Filters**: Market selection, asset classes
- **Icons only mode**: Minimal footprint when collapsed

## Phase 3: Main Content Area
### Top Section (KPIs):
- Balance, P&L, Risk exposure, Compliance status
- Use Material-UI cards for clarity

### Middle Section (Charts):
- React-Financial-Charts for candlestick, OHLC, EMA overlays
- Interactive zoom/pan, tooltips, volume bars
- Grid layout for multiple instruments side by side

### Bottom Section (Data Grid):
- Material-UI DataGrid for positions, transactions, audit logs
- Features: sorting, filtering, export to CSV/PDF
- Compliance flags highlighted with color coding

## Phase 4: Right Sidebar (Optional)
- **Live feed**: Market news, compliance updates
- **Chat/Support**: Secure messaging with compliance team
- **Widgets**: Risk alerts, AML/KYC verification status

## Phase 5: Footer
- Legal disclaimers (GDPR, desiring BaFin compliance)
