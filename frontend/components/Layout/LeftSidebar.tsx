import React from "react";
import {
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Toolbar,
  Typography,
  Box
} from "@mui/material";
import DashboardIcon from "@mui/icons-material/Dashboard";
import TrendingUpIcon from "@mui/icons-material/TrendingUp";
import PolicyIcon from "@mui/icons-material/Policy";
import TimelineIcon from "@mui/icons-material/Timeline";

const drawerWidth = 240;

interface LeftSidebarProps {
  open: boolean;
  onClose: () => void;
  isMobile: boolean;
}

export const LeftSidebar: React.FC<LeftSidebarProps> = ({ open, onClose, isMobile }) => {
  const drawerVariant = isMobile ? "temporary" : "persistent";

  const menuItems = [
    { label: "Dashboard", icon: <DashboardIcon /> },
    { label: "Trading Terminal", icon: <TrendingUpIcon /> },
    { label: "Risk Exposure", icon: <TimelineIcon /> },
    { label: "BaFin Compliance", icon: <PolicyIcon />, highlight: true },
  ];

  return (
    <Drawer
      variant={drawerVariant}
      open={open}
      onClose={onClose}
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: drawerWidth,
          boxSizing: "border-box",
        },
      }}
    >
      {/* Invisible toolbar to push items below Header */}
      <Toolbar />
      <Box sx={{ overflow: "auto" }}>
        <Typography variant="overline" sx={{ px: 3, pt: 2, color: 'text.secondary' }}>
          OpenForex Nav
        </Typography>
        <List>
          {menuItems.map((item) => (
            <ListItem button key={item.label} sx={item.highlight ? { bgcolor: 'action.hover' } : {}}>
              <ListItemIcon sx={item.highlight ? { color: 'primary.main' } : {}}>
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItem>
          ))}
        </List>
        <Divider />
        <List>
          <ListItem button>
            <ListItemText primary="Settings" />
          </ListItem>
        </List>
      </Box>
    </Drawer>
  );
};



