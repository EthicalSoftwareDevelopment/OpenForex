import React, { useState } from "react";
import { Box, Button, TextField, Typography, Select, MenuItem, FormControl, InputLabel, Paper, Alert } from "@mui/material";
import { useDispatch, useSelector } from "react-redux";
import { submitTradeOrder } from "../../store/slices/tradingSlice.ts";
import { RootState, AppDispatch } from "../../store/store.ts";

export const QuickTradePanel: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const lastError = useSelector((state: RootState) => state.trading.lastTradeError);

  const [instrument, setInstrument] = useState("EUR/USD");
  const [direction, setDirection] = useState<"BUY" | "SELL">("BUY");
  const [quantity, setQuantity] = useState(1);
  const [type, setType] = useState<"MARKET" | "LIMIT" | "STOP">("MARKET");

  const handleTrade = () => {
    dispatch(submitTradeOrder({
      instrument,
      direction,
      quantity,
      type
    }));
  };

  return (
    <Box sx={{ mt: 3, p: 2, bgcolor: "background.paper", borderRadius: 2, boxShadow: 3 }}>
      <Typography variant="h6" fontWeight="bold" mb={2}>
        Quick Trade Entry
      </Typography>

      {lastError && (
        <Alert severity="error" sx={{ mb: 2 }}>{lastError}</Alert>
      )}

      <Box sx={{ display: "flex", gap: 2, flexWrap: "wrap" }}>
        <FormControl size="small" sx={{ minWidth: 120 }}>
          <InputLabel>Instrument</InputLabel>
          <Select value={instrument} label="Instrument" onChange={(e) => setInstrument(e.target.value)}>
            <MenuItem value="EUR/USD">EUR/USD</MenuItem>
            <MenuItem value="GBP/JPY">GBP/JPY</MenuItem>
            <MenuItem value="USD/JPY">USD/JPY</MenuItem>
            <MenuItem value="BTC/USD">BTC/USD</MenuItem>
          </Select>
        </FormControl>

        <FormControl size="small" sx={{ minWidth: 100 }}>
          <InputLabel>Type</InputLabel>
          <Select value={type} label="Type" onChange={(e) => setType(e.target.value as any)}>
            <MenuItem value="MARKET">Market</MenuItem>
            <MenuItem value="LIMIT">Limit</MenuItem>
          </Select>
        </FormControl>

        <TextField
          label="Quantity (Lots)"
          type="number"
          size="small"
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
          sx={{ width: 120 }}
          inputProps={{ min: 0.1, step: 0.1 }}
        />

        <Button
          variant="contained"
          onClick={() => setDirection("BUY")}
          color={direction === "BUY" ? "success" : "inherit"}
          sx={{ fontWeight: "bold" }}
        >
          BUY
        </Button>
        <Button
          variant="contained"
          onClick={() => setDirection("SELL")}
          color={direction === "SELL" ? "error" : "inherit"}
          sx={{ fontWeight: "bold" }}
        >
          SELL
        </Button>
      </Box>

      <Box sx={{ mt: 2, textAlign: "right" }}>
        <Button
          variant="contained"
          size="large"
          color="primary"
          fullWidth
          onClick={handleTrade}
        >
          Submit {direction} {type} Order
        </Button>
      </Box>
    </Box>
  );
};

