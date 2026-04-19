import React from "react";
import { Grid, Card, CardContent, Typography, Box } from "@mui/material";
import { useSelector } from "react-redux";
import { RootState } from "../../store/store.ts";

export const KpiCards: React.FC = () => {
  const portfolio = useSelector((state: RootState) => state.portfolio);
  const compliance = useSelector((state: RootState) => state.compliance);

  const kpis = [
    { title: "Balance ($)", value: portfolio.balance.toFixed(2), color: "text.primary" },
    { title: "Equity ($)", value: portfolio.equity.toFixed(2), color: "text.primary" },
    { title: "Free Margin ($)", value: portfolio.freeMargin.toFixed(2), color: portfolio.freeMargin < 1000 ? "error.main" : "text.primary" },
    { title: "Risk Level", value: compliance.amlRiskLevel, color: compliance.amlRiskLevel === "HIGH" ? "error.main" : "success.main" },
  ];

  return (
    <Grid container spacing={3} mb={3}>
      {kpis.map((kpi, idx) => (
        <Grid item xs={12} sm={6} md={3} key={idx}>
          <Card elevation={3} sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography color="text.secondary" gutterBottom variant="subtitle2">
                {kpi.title}
              </Typography>
              <Typography variant="h4" component="div" sx={{ color: kpi.color, fontWeight: "bold" }}>
                {kpi.value}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

