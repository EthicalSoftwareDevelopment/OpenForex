import { createSlice, PayloadAction } from "@reduxjs/toolkit";

export interface Position {
  id: string;
  instrument: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  unrealizedPnL: number;
}

export interface PortfolioState {
  positions: Position[];
  balance: number;
  equity: number;
  marginUsed: number;
  freeMargin: number;
}

const estimatePnl = (quantity: number, averagePrice: number, currentPrice: number) => {
  const multiplier = currentPrice >= 20 ? 100 : 10_000;
  return Math.round((currentPrice - averagePrice) * quantity * multiplier * 100) / 100;
};

const recalculatePortfolio = (state: PortfolioState) => {
  const totalPnL = state.positions.reduce((acc, pos) => acc + pos.unrealizedPnL, 0);
  state.equity = state.balance + totalPnL;
  state.freeMargin = state.equity - state.marginUsed;
};

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
    hydratePortfolio(state, action: PayloadAction<PortfolioState>) {
      return action.payload;
    },
    updateBalance(state, action: PayloadAction<number>) {
      state.balance = action.payload;
      recalculatePortfolio(state);
    },
    updatePositions(state, action: PayloadAction<Position[]>) {
      state.positions = action.payload;
      recalculatePortfolio(state);
    },
    updateMargin(state, action: PayloadAction<number>) {
      state.marginUsed = action.payload;
      recalculatePortfolio(state);
    },
    applyMarketPrice(state, action: PayloadAction<{ instrument: string; price: number }>) {
      let hasUpdate = false;
      state.positions = state.positions.map((position) => {
        if (position.instrument !== action.payload.instrument) {
          return position;
        }

        hasUpdate = true;
        return {
          ...position,
          currentPrice: action.payload.price,
          unrealizedPnL: estimatePnl(position.quantity, position.averagePrice, action.payload.price),
        };
      });

      if (hasUpdate) {
        recalculatePortfolio(state);
      }
    },
  },
});

export const { hydratePortfolio, updateBalance, updatePositions, updateMargin, applyMarketPrice } = portfolioSlice.actions;
export default portfolioSlice.reducer;

