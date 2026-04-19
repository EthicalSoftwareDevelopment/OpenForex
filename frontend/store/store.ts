import { configureStore } from "@reduxjs/toolkit";
import portfolioReducer from "./slices/portfolioSlice.ts";
import tradingReducer from "./slices/tradingSlice.ts";
import complianceReducer from "./slices/complianceSlice.ts";
import { bafinAuditLogger } from "./middleware/auditLogger.ts";

export const store = configureStore({
  reducer: {
    portfolio: portfolioReducer,
    trading: tradingReducer,
    compliance: complianceReducer,
    // Add auth slice or others in the future
  },
  middleware: (getDefaultMiddleware) =>
    // Extend default middleware with BaFin audit beacon middleware connecting to Prometheus/ELK via Deno Gateway
    getDefaultMiddleware().concat(bafinAuditLogger),
  devTools: process.env.NODE_ENV !== "production", // Consider disabling completely for trading interfaces in Prod
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

