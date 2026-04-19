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

## Technologies
- **Framework**: React
  - Chosen for its component-based architecture, virtual DOM for performance, and large ecosystem.
- **UI Libraries**: Material-UI, React-Financial-Charts
  - Material-UI provides pre-built, customizable components for rapid development.
  - React-Financial-Charts offers specialized charting tools for financial data visualization.
- **Runtime**: Deno 2
  - Selected for its secure runtime, modern TypeScript support, and built-in tooling.

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
