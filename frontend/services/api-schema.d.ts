// Fallback type definition to satisfy Deno/IDE checks during development
// before `generate-client` is executed against a live Java backend instance.

export interface components {
  schemas: {
    MarketPriceSnapshot: {
      instrument: string;
      currentPrice: number;
    };
    TradingOrderRequest: {
      type: "MARKET" | "LIMIT" | "STOP";
      direction: "BUY" | "SELL";
      quantity: number;
      instrument: string;
      price?: number;
    };
    TradingOrderResponse: {
      orderId: string;
      status: "PENDING" | "EXECUTED" | "REJECTED";
    };
    PortfolioSnapshot: {
      accountId: string;
      cashBalance: number;
      positions: Record<string, number>;
      grossExposure: number;
      netExposure: number;
      message: string;
    };
    PortfolioUpdateRequest: {
      accountId: string;
      symbol: string;
      side: "BUY" | "SELL";
      quantity: number;
      price: number;
      cashAdjustment?: number;
    };
    DashboardState: {
      portfolio: {
        positions: Array<{
          id: string;
          instrument: string;
          quantity: number;
          averagePrice: number;
          currentPrice: number;
          unrealizedPnL: number;
        }>;
        balance: number;
        equity: number;
        marginUsed: number;
        freeMargin: number;
      };
      trading: {
        activeOrders: components["schemas"]["TradingOrderResponse"][];
        executions: Array<{
          orderId: string;
          executionPrice: number;
          slippage: number;
          timestamp: string;
        }>;
        lastTradeError: string | null;
      };
      compliance: {
        kycStatus: "UNVERIFIED" | "PENDING" | "VERIFIED" | "SUSPENDED";
        amlRiskLevel: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
        auditLogs: Array<{
          id: string;
          timestamp: string;
          action: string;
          ipAddress: string;
          details: string;
        }>;
        complianceAlerts: Array<{
          id: string;
          timestamp: string;
          severity: "INFO" | "WARNING" | "CRITICAL";
          message: string;
          resolved: boolean;
        }>;
      };
    };
  };
}

export interface paths {
  "/v1/market/prices": {
    get: {
      parameters: {
        query?: {
          instrument?: string;
        };
      };
      responses: {
        200: {
          content: {
            "application/json": components["schemas"]["MarketPriceSnapshot"][];
          };
        };
      };
    };
  };
  "/v1/trading/orders": {
    post: {
      requestBody: {
        content: {
          "application/json": components["schemas"]["TradingOrderRequest"];
        };
      };
      responses: {
        201: {
          content: {
            "application/json": components["schemas"]["TradingOrderResponse"];
          };
        };
      };
    };
  };
  "/portfolio": {
    get: {
      parameters: {
        query?: {
          accountId?: string;
        };
      };
      responses: {
        200: {
          content: {
            "application/json": components["schemas"]["PortfolioSnapshot"];
          };
        };
      };
    };
  };
  "/portfolio/update": {
    post: {
      requestBody: {
        content: {
          "application/json": components["schemas"]["PortfolioUpdateRequest"];
        };
      };
      responses: {
        200: {
          content: {
            "application/json": components["schemas"]["PortfolioSnapshot"];
          };
        };
      };
    };
  };
  "/v1/dashboard": {
    get: {
      responses: {
        200: {
          content: {
            "application/json": components["schemas"]["DashboardState"];
          };
        };
      };
    };
  };
}

