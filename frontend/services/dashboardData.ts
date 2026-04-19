import type { Position, PortfolioState } from "../store/slices/portfolioSlice.ts";
import type { TradingState } from "../store/slices/tradingSlice.ts";
import type { ComplianceState } from "../store/slices/complianceSlice.ts";

export interface MarketPriceSnapshot {
  instrument: string;
  currentPrice: number;
}

export interface PortfolioSnapshot {
  accountId: string;
  cashBalance: number;
  positions: Record<string, number>;
  grossExposure: number;
  netExposure: number;
  message: string;
}

export interface PortfolioUpdateRequest {
  accountId: string;
  symbol: string;
  side: "BUY" | "SELL";
  quantity: number;
  price: number;
  cashAdjustment?: number;
}

export interface TradingOrderPayload {
  instrument: string;
  type: "MARKET" | "LIMIT" | "STOP";
  direction: "BUY" | "SELL";
  quantity: number;
  price?: number;
}

export interface TradingOrderResponse {
  orderId: string;
  status: "PENDING" | "EXECUTED" | "REJECTED";
}

export interface DashboardState {
  portfolio: PortfolioState;
  trading: TradingState;
  compliance: ComplianceState;
}

type SeedPosition = Pick<Position, "id" | "instrument" | "quantity" | "averagePrice"> & {
  fallbackCurrentPrice: number;
};

const DEFAULT_PORTFOLIO: SeedPosition[] = [
  { id: "seed-eur-usd", instrument: "EUR/USD", quantity: 1.5, averagePrice: 1.1005, fallbackCurrentPrice: 1.1045 },
  { id: "seed-gbp-jpy", instrument: "GBP/JPY", quantity: -0.5, averagePrice: 182.4, fallbackCurrentPrice: 181.9 },
  { id: "seed-usd-jpy", instrument: "USD/JPY", quantity: 2, averagePrice: 145.2, fallbackCurrentPrice: 144.1 },
];

const round = (value: number, precision = 2) => {
  const factor = 10 ** precision;
  return Math.round(value * factor) / factor;
};

export const estimatePositionPnl = (quantity: number, averagePrice: number, currentPrice: number) => {
  const multiplier = currentPrice >= 20 ? 100 : 10_000;
  return round((currentPrice - averagePrice) * quantity * multiplier, 2);
};

const buildPositions = (
  portfolioSnapshot: PortfolioSnapshot | null,
  priceMap: Map<string, number>,
): Position[] => {
  if (portfolioSnapshot && Object.keys(portfolioSnapshot.positions || {}).length > 0) {
    return Object.entries(portfolioSnapshot.positions).map(([instrument, quantity], index) => {
      const fallback = DEFAULT_PORTFOLIO.find((position) => position.instrument === instrument);
      const averagePrice = fallback?.averagePrice ?? priceMap.get(instrument) ?? 1;
      const currentPrice = priceMap.get(instrument) ?? fallback?.fallbackCurrentPrice ?? averagePrice;

      return {
        id: `core-${instrument.replace(/[^A-Za-z0-9]+/g, "-").toLowerCase()}-${index}`,
        instrument,
        quantity,
        averagePrice,
        currentPrice,
        unrealizedPnL: estimatePositionPnl(quantity, averagePrice, currentPrice),
      };
    });
  }

  return DEFAULT_PORTFOLIO.map((position) => {
    const currentPrice = priceMap.get(position.instrument) ?? position.fallbackCurrentPrice;
    return {
      id: position.id,
      instrument: position.instrument,
      quantity: position.quantity,
      averagePrice: position.averagePrice,
      currentPrice,
      unrealizedPnL: estimatePositionPnl(position.quantity, position.averagePrice, currentPrice),
    };
  });
};

const fetchJson = async <T,>(url: string): Promise<T | null> => {
  try {
    const response = await fetch(url, {
      headers: { Accept: "application/json" },
    });

    if (!response.ok) {
      return null;
    }

    return await response.json() as T;
  } catch {
    return null;
  }
};

export const buildDashboardStateFromSnapshots = (
  marketPrices: MarketPriceSnapshot[] | null,
  portfolioSnapshot: PortfolioSnapshot | null,
): DashboardState => {
  const priceMap: Map<string, number> = new Map(
    (marketPrices ?? []).map((price) => [price.instrument, price.currentPrice] as [string, number]),
  );
  const positions = buildPositions(portfolioSnapshot, priceMap);
  const totalPnl = positions.reduce((sum, position) => sum + position.unrealizedPnL, 0);
  const balance = portfolioSnapshot?.cashBalance && portfolioSnapshot.cashBalance !== 0
    ? round(portfolioSnapshot.cashBalance, 2)
    : 25_000;
  const equity = round(balance + totalPnl, 2);
  const grossExposure = portfolioSnapshot?.grossExposure
    ? Math.abs(portfolioSnapshot.grossExposure)
    : positions.reduce((sum, position) => sum + Math.abs(position.quantity * position.currentPrice), 0);
  const marginUsed = round(grossExposure * 0.05, 2);
  const backendConnected = Boolean(marketPrices || portfolioSnapshot);

  return {
    portfolio: {
      positions,
      balance,
      equity,
      marginUsed,
      freeMargin: round(equity - marginUsed, 2),
    },
    trading: {
      activeOrders: [],
      executions: [],
      lastTradeError: backendConnected
        ? null
        : "Java core is not reachable yet. The dashboard is rendering fallback data until backend services boot.",
    },
    compliance: {
      kycStatus: backendConnected ? "VERIFIED" : "PENDING",
      amlRiskLevel: backendConnected ? "LOW" : "MEDIUM",
      auditLogs: [],
      complianceAlerts: backendConnected
        ? []
        : [{
          id: "edge-backend-warning",
          timestamp: new Date().toISOString(),
          severity: "WARNING",
          message: "Deno edge is live, but JVM core data endpoints are still warming up.",
          resolved: false,
        }],
    },
  };
};

const trimTrailingSlash = (value: string) => value.endsWith("/") ? value.slice(0, -1) : value;

export const fetchDashboardStateFromBackend = async (apiBaseUrl: string): Promise<DashboardState> => {
  const base = trimTrailingSlash(apiBaseUrl);
  const [marketPrices, portfolioSnapshot] = await Promise.all([
    fetchJson<MarketPriceSnapshot[]>(`${base}/v1/market/prices`),
    fetchJson<PortfolioSnapshot>(`${base}/portfolio?accountId=default`),
  ]);

  return buildDashboardStateFromSnapshots(marketPrices, portfolioSnapshot);
};

export const fetchDashboardStateFromEdge = async (endpoint = "/api/v1/dashboard"): Promise<DashboardState | null> =>
  fetchJson<DashboardState>(endpoint);

export const syncPortfolioTrade = async (
  apiBaseUrl: string,
  payload: TradingOrderPayload,
  marketPrices?: MarketPriceSnapshot[] | null,
) => {
  const base = trimTrailingSlash(apiBaseUrl);
  const fallbackPrice = marketPrices?.find((snapshot) => snapshot.instrument === payload.instrument)?.currentPrice
    ?? DEFAULT_PORTFOLIO.find((position) => position.instrument === payload.instrument)?.fallbackCurrentPrice
    ?? 1;
  const price = payload.price ?? fallbackPrice;

  const request: PortfolioUpdateRequest = {
    accountId: "default",
    symbol: payload.instrument,
    side: payload.direction,
    quantity: payload.quantity,
    price,
    cashAdjustment: 0,
  };

  return fetch(`${base}/portfolio/update`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(request),
  }).then(async (response) => response.ok ? await response.json() as PortfolioSnapshot : null).catch(() => null);
};

