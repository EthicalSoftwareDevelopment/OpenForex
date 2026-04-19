import React, { useState } from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Button,
  Menu,
  MenuItem,
  Badge,
  Box,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu.js";
import NotificationsIcon from "@mui/icons-material/Notifications.js";
import AccountCircle from "@mui/icons-material/AccountCircle.js";
import SecurityIcon from "@mui/icons-material/Security.js";
import { useSelector } from "react-redux";
import { RootState } from "../../store/store.ts";

interface TopNavigationProps {
  onMenuClick: () => void;
  onNotificationsClick: () => void;
}

export const TopNavigation: React.FC<TopNavigationProps> = ({ onMenuClick, onNotificationsClick }) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  // Connect to Redux to display active compliance state on the AppBar
  const { kycStatus, amlRiskLevel, complianceAlerts } = useSelector((state: RootState) => state.compliance);

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        <IconButton
          edge="start"
          color="inherit"
          aria-label="open drawer"
          onClick={onMenuClick}
          sx={{ mr: 2 }}
        >
          <MenuIcon />
        </IconButton>

        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
          OpenForex Trading Edge
        </Typography>

        <Box sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', mr: 2 }}>
          <Button color="inherit">Home</Button>
          <Button color="inherit">Markets</Button>
          <Button color="inherit">Portfolio</Button>
          <Button color="inherit">Reports</Button>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', ml: 'auto' }}>
          {/* Compliance Status Chip */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              mr: 3,
              px: 1.5,
              py: 0.5,
              borderRadius: 1,
              bgcolor: kycStatus === 'VERIFIED' ? 'success.dark' : 'warning.dark',
              color: 'white'
            }}
          >
            <SecurityIcon fontSize="small" sx={{ mr: 1 }} />
            <Typography variant="caption">
              KYC: {kycStatus} | AML: {amlRiskLevel}
            </Typography>
          </Box>

          <IconButton size="large" aria-label="show pending alerts" color="inherit" onClick={onNotificationsClick}>
            <Badge badgeContent={complianceAlerts?.length || 0} color="error">
              <NotificationsIcon />
            </Badge>
          </IconButton>

          <IconButton
            size="large"
            edge="end"
            aria-label="account of current user"
            aria-haspopup="true"
            onClick={handleProfileMenuOpen}
            color="inherit"
          >
            <AccountCircle />
          </IconButton>
        </Box>
      </Toolbar>

      {/* User Dropdown Menu */}
      <Menu
        anchorEl={anchorEl}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        keepMounted
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleMenuClose}>Profile</MenuItem>
        <MenuItem onClick={handleMenuClose}>Settings</MenuItem>
        <MenuItem onClick={handleMenuClose}>Logout</MenuItem>
      </Menu>
    </AppBar>
  );
};

