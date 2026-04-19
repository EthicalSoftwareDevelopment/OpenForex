import { configureStore } from "@reduxjs/toolkit";
import portfolioReducer from "./slices/portfolioSlice.ts";
import tradingReducer from "./slices/tradingSlice.ts";
import complianceReducer from "./slices/complianceSlice.ts";
import { bafinAuditLogger } from "./middleware/auditLogger.ts";
import type { PortfolioState } from "./slices/portfolioSlice.ts";
import type { TradingState } from "./slices/tradingSlice.ts";
import type { ComplianceState } from "./slices/complianceSlice.ts";

export interface AppState {
  portfolio: PortfolioState;
  trading: TradingState;
  compliance: ComplianceState;
}

const reducer = {
  portfolio: portfolioReducer,
  trading: tradingReducer,
  compliance: complianceReducer,
  // Add auth slice or others in the future
};

const isProductionRuntime =
  typeof Deno !== "undefined" && Deno.env.get("NODE_ENV") === "production";

export const createAppStore = (preloadedState?: Partial<AppState>) =>
  configureStore({
    reducer,
    preloadedState,
    middleware: (getDefaultMiddleware) =>
      // Extend default middleware with BaFin audit beacon middleware connecting to Prometheus/ELK via Deno Gateway
      getDefaultMiddleware().concat(bafinAuditLogger),
    devTools: !isProductionRuntime,
  });

export const store = createAppStore();

export type AppStore = ReturnType<typeof createAppStore>;

export type RootState = ReturnType<AppStore["getState"]>;
export type AppDispatch = AppStore["dispatch"];

