import { createSlice, PayloadAction, createAsyncThunk } from "@reduxjs/toolkit";
import { apiClient } from "../../services/apiClient.ts";

export interface Order {
  id: string;
  instrument: string;
  type: "MARKET" | "LIMIT" | "STOP";
  direction: "BUY" | "SELL";
  quantity: number;
  price: number;
  status: "PENDING" | "EXECUTED" | "REJECTED";
  timestamp: string;
}

export interface Execution {
  orderId: string;
  executionPrice: number;
  slippage: number;
  timestamp: string;
}

export interface TradingState {
  activeOrders: Order[];
  executions: Execution[];
  lastTradeError: string | null;
}

const initialState: TradingState = {
  activeOrders: [],
  executions: [],
  lastTradeError: null,
};

export const submitTradeOrder = createAsyncThunk(
  "trading/submitTradeOrder",
  async (
    payload: { instrument: string; type: "MARKET" | "LIMIT" | "STOP"; direction: "BUY" | "SELL"; quantity: number; price?: number },
    { dispatch, rejectWithValue }
  ) => {
    try {
      const { data, error } = await apiClient.POST("/v1/trading/orders", {
        body: payload
      });

      if (error) {
        dispatch(setTradeError(typeof error === "string" ? error : "API Rejected Order"));
        return rejectWithValue(error);
      }

      // Optimistically record the order locally immediately after the gateway acknowledges it
      const newOrder: Order = {
        id: data.orderId,
        instrument: payload.instrument,
        type: payload.type,
        direction: payload.direction,
        quantity: payload.quantity,
        price: payload.price || 0,
        status: data.status,
        timestamp: new Date().toISOString(),
      };

      dispatch(placeOrder(newOrder));
      return data;
    } catch (err: any) {
      dispatch(setTradeError(err.message || "Network Fault"));
      return rejectWithValue(err.message);
    }
  }
);

const tradingSlice = createSlice({
  name: "trading",
  initialState,
  reducers: {
    placeOrder(state, action: PayloadAction<Order>) {
      state.activeOrders.push(action.payload);
      state.lastTradeError = null;
    },
    updateOrderStatus(state, action: PayloadAction<{orderId: string, status: Order["status"]}>) {
      const order = state.activeOrders.find(o => o.id === action.payload.orderId);
      if (order) {
        order.status = action.payload.status;
      }
    },
    recordExecution(state, action: PayloadAction<Execution>) {
      state.executions.push(action.payload);
    },
    setTradeError(state, action: PayloadAction<string>) {
      state.lastTradeError = action.payload;
    }
  },
});

export const { placeOrder, updateOrderStatus, recordExecution, setTradeError } = tradingSlice.actions;
export default tradingSlice.reducer;
