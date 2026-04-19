import { createSlice, PayloadAction } from "npm:@reduxjs/toolkit";

export interface Position {
  id: string;
  instrument: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  unrealizedPnL: number;
}

interface PortfolioState {
  positions: Position[];
  balance: number;
  equity: number;
  marginUsed: number;
  freeMargin: number;
}

const initialState: PortfolioState = {
  positions: [],
  balance: 0,
  equity: 0,
  marginUsed: 0,
  freeMargin: 0,
};

const portfolioSlice = createSlice({
  name: "portfolio",
  initialState,
  reducers: {
    updateBalance(state, action: PayloadAction<number>) {
      state.balance = action.payload;
    },
    updatePositions(state, action: PayloadAction<Position[]>) {
      state.positions = action.payload;
      // Calculate equity based on PnL
      const totalPnL = state.positions.reduce((acc, pos) => acc + pos.unrealizedPnL, 0);
      state.equity = state.balance + totalPnL;
    },
    updateMargin(state, action: PayloadAction<number>) {
      state.marginUsed = action.payload;
      state.freeMargin = state.equity - state.marginUsed;
    }
  },
});

export const { updateBalance, updatePositions, updateMargin } = portfolioSlice.actions;
export default portfolioSlice.reducer;

