import React, { useEffect } from "react";
import { MainLayout } from "../components/Layout/MainLayout.tsx";
import { KpiCards } from "../components/Dashboard/KpiCards.tsx";
import { PositionsGrid } from "../components/Dashboard/PositionsGrid.tsx";
import { TradingChartStub } from "../components/Dashboard/TradingChart.tsx";
import { Box, Typography } from "@mui/material";
import { useDispatch } from "react-redux";
import { updateBalance, updatePositions } from "../store/slices/portfolioSlice.ts";

export const DashboardPage: React.FC = () => {
  const dispatch = useDispatch();

  // Mock initial dashboard state injection for dev visualizing.
  useEffect(() => {
    dispatch(updateBalance(25000));
    dispatch(updatePositions([
      { id: "1", instrument: "EUR/USD", quantity: 1.5, averagePrice: 1.1005, currentPrice: 1.1045, unrealizedPnL: 60 },
      { id: "2", instrument: "GBP/JPY", quantity: -0.5, averagePrice: 182.40, currentPrice: 181.90, unrealizedPnL: 250 },
      { id: "3", instrument: "USD/JPY", quantity: 2, averagePrice: 145.20, currentPrice: 144.10, unrealizedPnL: -2200 },
    ]));
  }, [dispatch]);

  return (
    <MainLayout>
      <Box sx={{ pb: 5 }}>
        <Typography variant="h4" fontWeight="bold" sx={{ mb: 4, mt: 2 }}>
          Trader Dashboard Overview
        </Typography>

        {/* Phase 3: Top Section */}
        <KpiCards />

        {/* Phase 3: Middle Section */}
        <TradingChartStub />

        {/* Trading execution form connecting to Java Middleware */}
        <QuickTradePanel />

        {/* Phase 3: Bottom Section */}
        <PositionsGrid />
      </Box>
    </MainLayout>
  );
};

