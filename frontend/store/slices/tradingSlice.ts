import { createSlice, PayloadAction } from "npm:@reduxjs/toolkit";

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

interface TradingState {
  activeOrders: Order[];
  executions: Execution[];
  lastTradeError: string | null;
}

const initialState: TradingState = {
  activeOrders: [],
  executions: [],
  lastTradeError: null,
};

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

