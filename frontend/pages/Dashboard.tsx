import React from "react";
import { MainLayout } from "../components/Layout/MainLayout.tsx";
import { KpiCards } from "../components/Dashboard/KpiCards.tsx";
import { PositionsGrid } from "../components/Dashboard/PositionsGrid.tsx";
import { TradingChartStub } from "../components/Dashboard/TradingChart.tsx";
import { QuickTradePanel } from "../components/Dashboard/QuickTradePanel.tsx";
import { Box, Typography } from "@mui/material";

export const DashboardPage: React.FC = () => {

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

