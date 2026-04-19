import React, { useState } from "react";
import { Drawer, Box, Typography, List, ListItem, ListItemText, Divider, IconButton, Paper } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import AnnouncementIcon from "@mui/icons-material/Announcement";
import SupportAgentIcon from "@mui/icons-material/SupportAgent";
import { useMarketFeed } from "../../hooks/useMarketFeed.ts";
import { useSelector } from "react-redux";
import { RootState } from "../../store/store.ts";

interface RightSidebarProps {
  open: boolean;
  onClose: () => void;
}

export const RightSidebar: React.FC<RightSidebarProps> = ({ open, onClose }) => {
  const drawerWidth = 320;

  // Custom Hook to consume Deno WebSocket feed
  const { ticks } = useMarketFeed();
  const complianceAlerts = useSelector((state: RootState) => state.compliance.complianceAlerts);

  const getLatestPrice = (instrument: string) => {
    const history = ticks[instrument] || [];
    return history.length ? history[history.length - 1].price.toFixed(4) : "Loading...";
  };

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        "& .MuiDrawer-paper": {
          width: drawerWidth,
          boxSizing: "border-box",
          bgcolor: "background.paper",
        },
      }}
    >
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", p: 2 }}>
        <Typography variant="h6" fontWeight="bold">
          Live Intelligence
        </Typography>
        <IconButton onClick={onClose} aria-label="close drawer">
          <CloseIcon />
        </IconButton>
      </Box>
      <Divider />

      <Box sx={{ p: 2, height: "100%", display: "flex", flexDirection: "column" }}>

        {/* Market Feed Section */}
        <Typography variant="subtitle1" color="text.secondary" fontWeight="bold" sx={{ display: "flex", alignItems: "center", mb: 1 }}>
          <AnnouncementIcon fontSize="small" sx={{ mr: 1 }} />
          Market Ticker (Deno WS)
        </Typography>

        <Paper elevation={0} sx={{ p: 1, mb: 3, bgcolor: "background.default", border: 1, borderColor: "divider" }}>
          <List dense disablePadding>
            {Object.keys(ticks).length === 0 ? (
              <ListItem dense>
                <ListItemText primary="Awaiting stream..." secondary="Ensure proxy is active" />
              </ListItem>
            ) : (
                Object.keys(ticks).map(instrument => (
                  <ListItem key={instrument} dense>
                    <ListItemText primary={instrument} primaryTypographyProps={{ fontWeight: "bold" }} />
                    <Typography variant="body2" fontWeight="medium" color="success.main">
                      {getLatestPrice(instrument)}
                    </Typography>
                  </ListItem>
                ))
            )}
          </List>
        </Paper>

        {/* Compliance Alerts Feed Section */}
        <Typography variant="subtitle1" color="text.secondary" fontWeight="bold" sx={{ display: "flex", alignItems: "center", mb: 1 }}>
          <SupportAgentIcon fontSize="small" sx={{ mr: 1 }} />
          Compliance Broadcasts
        </Typography>

        <Paper elevation={0} sx={{ p: 1, flexGrow: 1, overflow: "auto", bgcolor: "background.default", border: 1, borderColor: "divider" }}>
          <List dense>
            {complianceAlerts.length === 0 ? (
              <ListItem dense>
                <ListItemText secondary="No active alerts." />
              </ListItem>
            ) : (
              complianceAlerts.slice(0, 10).map(alert => (
                <ListItem key={alert.id} alignItems="flex-start" sx={{ mb: 1, bgcolor: alert.severity === "WARNING" ? "warning.main" : "info.main", borderRadius: 1, p: 1 }}>
                  <ListItemText
                    primary={alert.message}
                    secondary={new Date(alert.timestamp).toLocaleTimeString()}
                    primaryTypographyProps={{ variant: "body2", fontWeight: "bold", color: "white" }}
                    secondaryTypographyProps={{ variant: "caption", color: "rgba(255,255,255,0.7)" }}
                  />
                </ListItem>
              ))
            )}
          </List>
        </Paper>

      </Box>

    </Drawer>
  );
};

