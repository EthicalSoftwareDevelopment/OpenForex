import React, { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { triggerComplianceAlert, ComplianceAlert } from "../store/slices/complianceSlice.ts";

export interface MarketTick {
  instrument: string;
  price: number;
  timestamp: string;
}

export const useMarketFeed = () => {
  const [ticks, setTicks] = useState<Record<string, MarketTick[]>>({});
  const dispatch = useDispatch();

  useEffect(() => {
    // Connect to the local Deno edge proxy WebSocket
    const protocol = globalThis.location?.protocol === "https:" ? "wss:" : "ws:";
    const host = globalThis.location?.host || "localhost:8000";
    const wsUrl = `${protocol}//${host}/ws/feed`;

    let ws: WebSocket;

    try {
      ws = new WebSocket(wsUrl);

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);

          if (data.type === "MARKET_TICK") {
            setTicks(prev => {
              const instrumentHistory = prev[data.instrument] || [];
              // Keep the last 100 ticks per instrument
              const updatedHistory = [...instrumentHistory, data].slice(-100);
              return { ...prev, [data.instrument]: updatedHistory };
            });
          }

          if (data.type === "COMPLIANCE_ALERT") {
            dispatch(triggerComplianceAlert({
              id: crypto.randomUUID(),
              timestamp: data.timestamp,
              severity: data.severity,
              message: data.message,
              resolved: false
            } as ComplianceAlert));
          }

        } catch (e) {
          console.error("Failed to parse websocket message", e);
        }
      };

      ws.onerror = (err) => {
        console.error("WebSocket feed encountered an error:", err);
      };

    } catch (err) {
      console.warn("WebSocket environment not available/failed to connect:", err);
    }

    return () => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, [dispatch]);

  return { ticks };
};

