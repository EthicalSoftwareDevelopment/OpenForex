import React from "react";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import { useSelector } from "react-redux";
import { RootState } from "../../store/store.ts";
import { Box, Typography } from "@mui/material";

export const PositionsGrid: React.FC = () => {
  const positions = useSelector((state: RootState) => state.portfolio.positions);

  const columns: GridColDef[] = [
    { field: "instrument", headerName: "Instrument", width: 150 },
    { field: "quantity", headerName: "Quantity", type: "number", width: 110 },
    { field: "averagePrice", headerName: "Entry Price", type: "number", width: 130 },
    { field: "currentPrice", headerName: "Current Price", type: "number", width: 130 },
    {
      field: "unrealizedPnL",
      headerName: "P&L ($)",
      type: "number",
      width: 130,
      renderCell: (params) => {
        const pnl = Number(params.value);
        return (
          <Typography color={pnl >= 0 ? "success.main" : "error.main"} fontWeight="bold">
            {pnl > 0 ? "+" : ""}{pnl.toFixed(2)}
          </Typography>
        );
      }
    },
  ];

  return (
    <Box sx={{ width: "100%", height: 400, bgcolor: "background.paper", borderRadius: 2, mt: 3, p: 2, boxShadow: 3 }}>
      <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>
        Open Positions
      </Typography>
      <DataGrid
        rows={positions}
        columns={columns}
        autoHeight={false}
        disableRowSelectionOnClick
        initialState={{
          pagination: {
            paginationModel: { pageSize: 5 }
          },
        }}
        pageSizeOptions={[5, 10, 25]}
        sx={{ border: 0 }}
      />
    </Box>
  );
};

