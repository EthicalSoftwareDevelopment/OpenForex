import React from "react";
import { useSelector } from "react-redux";
import { RootState } from "../../store/store.ts";
import { Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from "@mui/material";

export const PositionsGrid: React.FC = () => {
  const positions = useSelector((state: RootState) => state.portfolio.positions);

  return (
    <Box sx={{ width: "100%", bgcolor: "background.paper", borderRadius: 2, mt: 3, p: 2, boxShadow: 3 }}>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>
        Open Positions
      </Typography>

      <TableContainer component={Paper} elevation={0} sx={{ bgcolor: "transparent" }}>
        <Table size="small" aria-label="open positions table">
          <TableHead>
            <TableRow>
              <TableCell>Instrument</TableCell>
              <TableCell align="right">Quantity</TableCell>
              <TableCell align="right">Entry Price</TableCell>
              <TableCell align="right">Current Price</TableCell>
              <TableCell align="right">P&amp;L ($)</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {positions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5}>
                  <Typography color="text.secondary">No open positions currently loaded from the core portfolio service.</Typography>
                </TableCell>
              </TableRow>
            ) : (
              positions.map((position) => (
                <TableRow key={position.id} hover>
                  <TableCell component="th" scope="row">{position.instrument}</TableCell>
                  <TableCell align="right">{position.quantity.toFixed(2)}</TableCell>
                  <TableCell align="right">{position.averagePrice.toFixed(4)}</TableCell>
                  <TableCell align="right">{position.currentPrice.toFixed(4)}</TableCell>
                  <TableCell align="right">
                    <Typography color={position.unrealizedPnL >= 0 ? "success.main" : "error.main"} fontWeight="bold">
                      {position.unrealizedPnL > 0 ? "+" : ""}{position.unrealizedPnL.toFixed(2)}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

