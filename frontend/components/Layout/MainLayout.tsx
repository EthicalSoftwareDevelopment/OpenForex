import React, { useState } from "react";
import { ThemeProvider, createTheme, CssBaseline, Box, Toolbar, Typography } from "@mui/material";
import { TopNavigation } from "./TopNavigation.tsx";
import { LeftSidebar } from "./LeftSidebar.tsx";
import { RightSidebar } from "./RightSidebar.tsx";

// MUI Theme Config to act as baseline
const theme = createTheme({
  palette: {
    mode: "dark", // standard for trading terminals
    primary: {
      main: "#1976d2",
    },
    secondary: {
      main: "#f50057",
    },
    background: {
      default: "#121212",
      paper: "#1e1e1e"
    }
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});

export const MainLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [rightSidebarOpen, setRightSidebarOpen] = useState(false);

  // Use simple boolean check for mobile drawer logic in this scaffolding
  const isMobile = globalThis.window ? window.innerWidth < 600 : false;

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };
  const toggleRightSidebar = () => {
    setRightSidebarOpen(!rightSidebarOpen);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: "flex", minHeight: "100vh" }}>

        {/* Main Top App Bar */}
        <TopNavigation onMenuClick={toggleSidebar} onNotificationsClick={toggleRightSidebar} />

        {/* Left Navigation Menu */}
        <LeftSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} isMobile={isMobile} />

        {/* Right Intelligence Feed */}
        <RightSidebar open={rightSidebarOpen} onClose={() => setRightSidebarOpen(false)} />

        {/* Main Content Area Container */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            bgcolor: "background.default",
            p: 3,
            transition: theme.transitions.create("margin", {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.leavingScreen,
            }),
            marginLeft: isMobile || !sidebarOpen ? 0 : 0,
          }}
        >
          {/* Toolbar filler required to push content down properly below the fixed App Bar */}
          <Toolbar />

          {/* Application Page Routing Content Gets Injected Here */}
          {children}

          {/* Phase 5: Legal Footer Stub */}
          <Box mt={8} py={3} textAlign="center" sx={{ borderTop: "1px solid", borderColor: "divider", color: "#777" }}>
            <Typography variant="body2">© 2026 OpenForex Edge Layer.</Typography>
            <Typography variant="caption">BaFin compliant architecture. Data synchronized via JVM core.</Typography>
          </Box>
        </Box>
      </Box>
    </ThemeProvider>
  );
};



