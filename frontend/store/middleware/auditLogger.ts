import { Middleware } from "npm:@reduxjs/toolkit";

const AUDIT_ENDPOINT = "/api/v1/bafin-audit"; // Point to the Deno Edge Proxy

export const bafinAuditLogger: Middleware = store => next => action => {
  const result = next(action);

  // We want to log critical state transitions (e.g., trade executions, compliance changes)
  const state = store.getState();
  const criticalActions = [
    "trading/placeOrder",
    "trading/updateOrderStatus",
    "trading/recordExecution",
    "compliance/updateKycStatus",
    "compliance/updateAmlRisk",
    "portfolio/updatePositions", // significant balance or position updates
  ];

  if (action.type && typeof action.type === 'string' && criticalActions.includes(action.type)) {
    const payload = {
      timestamp: new Date().toISOString(),
      actionType: action.type,
      payload: (action as any).payload,
      userId: state.auth?.userId || "UNKNOWN", // Mock auth slice
      kycState: state.compliance?.kycStatus || "UNKNOWN",
      amlRisk: state.compliance?.amlRiskLevel || "UNKNOWN",
      sessionId: globalThis.crypto ? crypto.randomUUID() : "legacy-session-id",
    };

    // Non-blocking async fetch to ELK / Prometheus gateway
    // In Deno/Browser environment, this dispatches a beacon or simple POST
    if (globalThis.fetch) {
      globalThis.fetch(AUDIT_ENDPOINT, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ auditLog: payload }),
        keepalive: true, // Helps ensure delivery even if user navigates away
      }).catch(err => {
        console.error("Failed to send BaFin audit log beacon:", err);
        // Fallback for edge runtime (Deno proxy) could write to a local persistent queue mechanism here
      });
    }
  }

  return result;
};
